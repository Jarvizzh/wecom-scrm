<template>
  <div class="customer-container">

    <!-- Filter Bar -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <div class="filter-items">
          <el-form-item label="客户搜索">
            <el-input 
              v-model="searchCustomer" 
              placeholder="搜索客户名" 
              :prefix-icon="Search"
              style="width: 160px" 
              clearable 
              @keyup.enter="fetchCustomers" 
            />
          </el-form-item>
          <el-form-item label="UnionID">
            <el-input 
              v-model="searchUnionid" 
              placeholder="精准过滤" 
              :prefix-icon="Search"
              style="width: 200px" 
              clearable 
              @keyup.enter="fetchCustomers" 
            />
          </el-form-item>
          <el-form-item label="归属员工">
            <el-select 
              v-model="searchEmployee" 
              placeholder="选择成员" 
              style="width: 160px" 
              clearable 
              filterable
              @change="fetchCustomers"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
              <el-option
                v-for="user in employeeList"
                :key="user.userid"
                :label="user.name"
                :value="user.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源公众号">
            <el-select 
              v-model="searchMpAppId" 
              placeholder="所有账号" 
              style="width: 160px" 
              clearable 
              filterable
              @change="fetchCustomers"
            >
              <template #prefix>
                <el-icon><Cellphone /></el-icon>
              </template>
              <el-option
                v-for="acc in mpAccounts"
                :key="acc.appId"
                :label="acc.name"
                :value="acc.appId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="好友状态">
            <el-select 
              v-model="searchStatus" 
              placeholder="所有状态" 
              style="width: 120px" 
              clearable 
              @change="fetchCustomers"
            >
              <template #prefix>
                <el-icon><Connection /></el-icon>
              </template>
              <el-option label="正常" :value="0" />
              <el-option label="已流失" :value="2" />
              <el-option label="已删除" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select
              v-model="searchTags"
              placeholder="按标签筛选"
              style="width: 240px"
              clearable
              multiple
              collapse-tags
              collapse-tags-indicator
              @change="fetchCustomers"
            >
              <template #prefix>
                <el-icon><CollectionTag /></el-icon>
              </template>
              <el-option-group
                v-for="group in allTagGroups"
                :key="group.groupId"
                :label="group.groupName"
              >
                <el-option
                  v-for="tag in group.tags"
                  :key="tag.tagId"
                  :label="tag.name"
                  :value="tag.tagId"
                />
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox
              v-model="onlyDuplicates"
              label="查重模式"
              class="filter-checkbox"
              @change="fetchCustomers"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="search-btn" :icon="Search" @click="fetchCustomers">查询</el-button>
          </el-form-item>
        </div>

        <div class="action-items">
          <el-button 
            type="primary" 
            :disabled="multipleSelection.length === 0"
            class="action-btn batch-tag-btn"
            :icon="CollectionTag"
            @click="handleBatchTag"
          >
            批量打标签 {{ multipleSelection.length > 0 ? `(${multipleSelection.length})` : '' }}
          </el-button>
          <el-button 
            type="primary" 
            plain
            class="action-btn sync-btn"
            :icon="Refresh" 
            :loading="syncing" 
            @click="handleSync"
          >
            同步客户
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- Selection Info Banner -->
    <div v-if="multipleSelection.length > 0" class="selection-banner-container">
      <el-alert :closable="false" class="selection-banner">
        <template #title>
          <div class="banner-content">
            <el-icon><InfoFilled /></el-icon>
            <span v-if="!isAllSelected">
              已选择本页 <strong>{{ multipleSelection.length }}</strong> 名客户。
              <el-link 
                v-if="total > multipleSelection.length" 
                type="primary" 
                class="banner-link" 
                @click="selectAllMatching"
              >
                选择满足条件的全部 {{ total }} 名客户
              </el-link>
            </span>
            <span v-else>
              已选择满足条件的全部 <strong>{{ total }}</strong> 名客户。
              <el-link type="primary" class="banner-link" @click="clearAllSelected">
                清除全选，仅保留本页选择
              </el-link>
            </span>
          </div>
        </template>
      </el-alert>
    </div>

    <!-- Table list -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%" 
        @selection-change="handleSelectionChange"
        class="custom-table"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="客户信息" min-width="240">
          <template #default="scope">
            <div class="user-info-cell">
              <el-avatar :size="48" :src="scope.row.avatar" class="user-avatar" />
              <div class="user-detail">
                <div class="name-row">
                  <span class="user-name">{{ scope.row.customerName }}</span>
                  <span 
                    :class="['type-tag', scope.row.type === 1 ? 'is-wechat' : 'is-corp']"
                  >
                    {{ scope.row.type === 1 ? '@微信' : '@企微' }}
                  </span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

         <el-table-column label="归属员工" min-width="150">
          <template #default="scope">
            <div class="employee-cell">
              <span>{{ scope.row.employeeName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="客户标签" min-width="180" class-name="hidden-sm-and-down">
          <template #default="scope">
            <div class="tags-container">
              <el-tag 
                v-for="tag in (scope.row.tagNames ? scope.row.tagNames.split(',') : [])" 
                :key="tag"
                size="small"
                effect="plain"
                round
                class="soft-tag"
              >
                {{ tag }}
              </el-tag>
              <div v-if="!(scope.row.tagNames)" class="no-tags">-</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="UnionID" width="160" class-name="hidden-md-and-down">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.unionid"
              :content="scope.row.unionid"
              placement="top"
              effect="dark"
            >
              <span class="unionid-text">{{ scope.row.unionid }}</span>
            </el-tooltip>
            <span v-else class="unionid-text">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="关联公众号" width="100" class-name="hidden-lg-and-down">
          <template #default="scope">
            <span v-if="scope.row.mpName" class="mp-info-text">{{ scope.row.mpName }}</span>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column label="公众号OpenID" width="120" class-name="hidden-lg-and-down">
          <template #default="scope">
             <el-tooltip
              v-if="scope.row.mpOpenid"
              :content="scope.row.mpOpenid"
              placement="top"
              effect="dark"
            >
              <span class="openid-text">{{ scope.row.mpOpenid }}</span>
            </el-tooltip>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="关系状态" width="90">
          <template #default="scope">
            <div class="status-cell">
              <span :class="['status-dot', 
                scope.row.status === 0 ? 'is-active' : 
                scope.row.status === 2 ? 'is-lost' : 'is-inactive']"></span>
              <span class="status-text">
                {{ scope.row.status === 0 ? '正常' : 
                   scope.row.status === 2 ? '已流失' : '已删除' }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="relationCreateTime" label="添加时间" width="180" class-name="hidden-sm-and-down">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDateTime(scope.row.relationCreateTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              circle
              plain
              class="action-icon-btn"
              :icon="CollectionTag" 
              @click="openTagDialog(scope.row)" 
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-block">
        <el-pagination 
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Tagging Dialog -->
    <el-dialog 
      v-model="tagDialogVisible" 
      width="680px"
      class="custom-tag-dialog"
      destroy-on-close
      align-center
    >
      <template #header>
        <div class="dialog-custom-header">
          <div class="header-icon-box">
             <el-icon><CollectionTag /></el-icon>
          </div>
          <div class="header-text-box">
            <h3 class="dialog-title">{{ isBatch ? '客户批量标签管理' : '客户标签设置' }}</h3>
            <p class="dialog-subtitle">精准分类客户，提升运营效率</p>
          </div>
        </div>
      </template>

      <!-- Mode Switch for Batch -->
      <div v-if="isBatch" class="batch-mode-card">
        <el-radio-group v-model="batchTagMode" class="batch-segmented">
          <el-radio-button label="add">
            <el-icon><Plus /></el-icon> 批量新增
          </el-radio-button>
          <el-radio-button label="remove">
            <el-icon><Minus /></el-icon> 批量移除
          </el-radio-button>
        </el-radio-group>
        <div class="batch-notice">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ batchTagMode === 'add' ? '选中的标签将同步应用至所有选定客户' : '选中的标签将从所有选定客户中统一移除' }}</span>
        </div>
      </div>

      <div v-loading="tagsLoading" class="tags-scroll-area">
        <div v-if="allTagGroups.length === 0" class="empty-state">
           <el-empty description="暂无标签数据" />
        </div>
        <div v-for="group in allTagGroups" :key="group.groupId" class="tag-group-section">
          <div class="group-title">
            <span class="title-dot"></span>
            {{ group.groupName }}
          </div>
          <div class="tag-grid">
            <el-checkbox-group v-model="selectedTagIds">
              <el-checkbox 
                v-for="tag in group.tags" 
                :key="tag.tagId" 
                :label="tag.tagId" 
                class="modern-tag-checkbox"
              >
                {{ tag.name }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="tagDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleMarkTags" :loading="marking" class="submit-btn">
            确认应用
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCustomers, syncCustomers } from '../api/customer'
import { getTagGroups, getTagsByGroup, markCustomerTags, batchMarkCustomerTags } from '../api/tag'
import { getUsers } from '../api/user'
import { getMpAccounts } from '../api/mp'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Minus, CollectionTag, InfoFilled, User, Calendar, Cellphone, Connection } from '@element-plus/icons-vue'

const tableData = ref([])
const loading = ref(false)
const syncing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchCustomer = ref('')
const searchUnionid = ref('')
const searchEmployee = ref('')
const searchMpAppId = ref('')
const searchStatus = ref<number>()
const searchTags = ref<string[]>([])
const onlyDuplicates = ref(false)
const employeeList = ref<any[]>([])
const mpAccounts = ref<any[]>([])
const isAllSelected = ref(false)

const tagDialogVisible = ref(false)
const tagsLoading = ref(false)
const marking = ref(false)
const allTagGroups = ref<any[]>([])
const selectedTagIds = ref<string[]>([])
const initialTagIds = ref<string[]>([])
const currentCustomer = ref<any>(null)
const multipleSelection = ref<any[]>([])
const isBatch = ref(false)
const batchTagMode = ref<'add' | 'remove'>('add')

const handleSelectionChange = (val: any[]) => {
  multipleSelection.value = val
  if (val.length === 0 || (val.length < tableData.value.length && isAllSelected.value)) {
    isAllSelected.value = false
  }
}

const selectAllMatching = () => {
  isAllSelected.value = true
}

const clearAllSelected = () => {
  isAllSelected.value = false
}

const handleBatchTag = () => {
  if (multipleSelection.value.length === 0) return
  isBatch.value = true
  batchTagMode.value = 'add'
  openTagDialog(null)
}

const fetchCustomers = async () => {
  loading.value = true
  isAllSelected.value = false
  try {
    const res = (await getCustomers({
      page: currentPage.value,
      size: pageSize.value,
      customerName: searchCustomer.value,
      unionid: searchUnionid.value,
      employeeName: searchEmployee.value,
      mpAppId: searchMpAppId.value,
      tagIds: searchTags.value,
      status: searchStatus.value,
      onlyDuplicates: onlyDuplicates.value
    })) as any
    tableData.value = res.content
    total.value = res.totalElements
  } catch (error) {
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSync = async () => {
  try {
    await ElMessageBox.confirm(
      '执行数据同步可能需要一些时间，后台处理中，请稍后刷新查看最新状态。',
      {
        confirmButtonText: '开始同步',
        cancelButtonText: '取消',
        type: 'info',
        title: '同步提醒'
      }
    )
    syncing.value = true
    await syncCustomers()
    ElMessage.success('同步任务已提交至后台')
    setTimeout(fetchCustomers, 1500)
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('触发同步失败')
  } finally {
    syncing.value = false
  }
}

const openTagDialog = async (row: any) => {
  currentCustomer.value = row
  isBatch.value = !row
  tagDialogVisible.value = true
  tagsLoading.value = true
  selectedTagIds.value = []
  initialTagIds.value = []
  
  try {
    const groups = await getTagGroups() as any
    const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
       const tags = await getTagsByGroup(g.groupId) as any
       return { ...g, tags }
    }))
    allTagGroups.value = groupsWithTags

    if (row) {
      // Ensure IDs are treated as strings
      const currentTags = (row.tagIds || []).map((id: any) => String(id))
      selectedTagIds.value = [...currentTags]
      initialTagIds.value = [...currentTags]
    }
  } catch (error) {
    console.error('Fetch tags error:', error)
    ElMessage.error('获取标签库失败')
  } finally {
    tagsLoading.value = false
  }
}

const handleMarkTags = async () => {
  // Guard against invalid state
  if (!isBatch.value && !currentCustomer.value) return
  
  if (isBatch.value && multipleSelection.value.length === 0) {
    ElMessage.warning('请先选择需要打标签的客户')
    return
  }
  
  if (isBatch.value && selectedTagIds.value.length === 0) {
    ElMessage.warning('请至少选择一个标签进行操作')
    return
  }

  marking.value = true
  try {
    // Ensure all IDs used for calculation are strings
    const currentSelected = selectedTagIds.value.map(id => String(id))
    const currentInitial = initialTagIds.value.map(id => String(id))

    let addTagIds: string[] = []
    let removeTagIds: string[] = []

    if (isBatch.value) {
      if (batchTagMode.value === 'add') {
        addTagIds = [...currentSelected]
      } else {
        removeTagIds = [...currentSelected]
      }
    } else {
      // Calculate diff for individual tagging
      addTagIds = currentSelected.filter(id => !currentInitial.includes(id))
      removeTagIds = currentInitial.filter(id => !currentSelected.includes(id))
    }

    // Individual mode check: notify user if no changes were made
    if (!isBatch.value && addTagIds.length === 0 && removeTagIds.length === 0) {
      ElMessage.info('未检测到标签变更')
      tagDialogVisible.value = false
      return
    }

    if (isBatch.value) {
      const targets = multipleSelection.value.map(item => ({
        // Defensive field resolution
        userid: item.employeeUserid || item.userid || item.userId,
        externalUserid: item.externalUserid
      }))
      await batchMarkCustomerTags({ 
        targets, 
        addTagIds, 
        removeTagIds,
        selectAll: isAllSelected.value,
        customerName: searchCustomer.value,
        unionid: searchUnionid.value,
        employeeName: searchEmployee.value,
        mpAppId: searchMpAppId.value,
        tagIds: searchTags.value,
        status: searchStatus.value,
        onlyDuplicates: onlyDuplicates.value
      })
      ElMessage.success('批量同步任务处理中...')
    } else {
      await markCustomerTags({
        // Defensive field resolution
        userid: currentCustomer.value.employeeUserid || currentCustomer.value.userid || currentCustomer.value.userId,
        externalUserid: currentCustomer.value.externalUserid,
        addTagIds,
        removeTagIds
      })
      ElMessage.success('客户标签更新成功')
    }
    
    tagDialogVisible.value = false
    setTimeout(fetchCustomers, 1000)
  } catch (error) {
    console.error('Mark tags error:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    marking.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchCustomers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchCustomers()
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchEmployees = async () => {
  try {
    const res: any = await getUsers({ page: 1, size: 1000 })
    employeeList.value = res.content || []
  } catch (error) {
    console.error('Fetch error:', error)
  }
}

const fetchMpAccounts = async () => {
  try {
    const res: any = await getMpAccounts()
    mpAccounts.value = res || []
  } catch (error) {
    console.error('Fetch MP accounts error:', error)
  }
}

const fetchTagGroupsData = async () => {
  try {
    const groups = await getTagGroups() as any
    const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
       const tags = await getTagsByGroup(g.groupId) as any
       return { ...g, tags }
    }))
    allTagGroups.value = groupsWithTags
  } catch (error) {}
}

onMounted(() => {
  fetchEmployees()
  fetchMpAccounts()
  fetchTagGroupsData()
  fetchCustomers()
})
</script>

<style scoped>
/* V3 Color Palette & Tokens */
:root {
  --primary-color: #409eff;
  --primary-light: #ecf5ff;
  --primary-hover: #66b1ff;
  --bg-main: #f8fafc;
  --card-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

.customer-container {
  --primary-color: #409eff;
  --primary-hover: #66b1ff;
  --primary-light: #ecf5ff;
  --slate-50: #f8fafc;
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 64px);
}

@media (max-width: 768px) {
  .customer-container {
    padding: 12px;
  }
}

/* Header Sections */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.025em;
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 15px;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 14px;
}

.action-btn {
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.sync-btn:hover {
  transform: translateY(-1px);
}

/* Filter Card */
.filter-card {
  margin-bottom: 10px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
}

.filter-form {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-items {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
  flex: 1;
}

.filter-items :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.filter-items :deep(.el-form-item__label) {
  font-weight: 600;
  color: #64748b;
  font-size: 13px;
  padding-right: 8px;
}

.action-items {
  display: flex;
  gap: 12px;
  align-items: center;
  padding-top: 4px;
}

.selection-banner-container {
  margin-bottom: 12px;
}

.selection-banner {
  background-color: var(--primary-light) !important;
  border: 1px solid #d9ecff;
  border-radius: 12px;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.banner-link {
  margin-left: 12px;
  font-weight: 600;
  vertical-align: baseline;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

/* Table Card */
.table-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  overflow: hidden;
}

.custom-table {
  border: none;
}

.custom-table :deep(.el-table__header) th {
  background-color: #f1f5f9;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  height: 56px;
  border-bottom: 1px solid #e2e8f0;
}

.custom-table :deep(.el-table__row) {
  transition: background-color 0.2s;
}

.custom-table :deep(.el-table__row):hover {
  background-color: #f8fafc !important;
}

/* Cell Styling */
.user-info-cell {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.user-avatar {
  border: 3px solid #fff;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  background-color: #f1f5f9;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 700;
  color: #1e293b;
  font-size: 15px;
}

.type-tag {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 700;
  text-transform: uppercase;
}

.type-tag.is-wechat {
  background-color: #dcfce7;
  color: #166534;
}

.type-tag.is-corp {
  background-color: #dbeafe;
  color: #1e40af;
}

.user-subtext {
  font-size: 12px;
  color: #64748b;
  font-family: monospace;
}

.employee-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #334155;
  font-weight: 500;
}

.employee-badge {
  width: 32px;
  height: 32px;
  background-color: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  position: relative;
}

.status-dot.is-active {
  background-color: #10b981;
}

.status-dot.is-active::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: inherit;
  animation: pulse 2s infinite;
  opacity: 0.4;
}

.status-dot.is-inactive {
  background-color: #94a3b8;
}

.status-dot.is-lost {
  background-color: #f59e0b;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.6; }
  100% { transform: scale(2.5); opacity: 0; }
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
}

.unionid-text, .openid-text {
  font-family: monospace;
  font-size: 12px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.mp-info-text {
  font-weight: 600;
  color: #475569;
  font-size: 13px;
}

.empty-text {
  color: #94a3b8;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.soft-tag {
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  color: #475569;
  font-weight: 600;
  padding: 0 10px;
}

.action-icon-btn {
  border-color: #e2e8f0;
  color: #64748b;
  background-color: #fff;
}

.action-icon-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background-color: var(--primary-light);
}

/* Pagination */
.pagination-block {
  padding: 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

/* Dialog V3 */
.custom-tag-dialog :deep(.el-dialog) {
  border-radius: 20px;
  padding: 0;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgb(0 0 0 / 0.25);
}

.dialog-custom-header {
  padding: 32px 32px 24px;
  background-color: #ffffff;
  display: flex;
  gap: 20px;
  border-bottom: 1px solid #f1f5f9;
}

.header-icon-box {
  width: 56px;
  height: 56px;
  background-color: var(--primary-light);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: var(--primary-color);
}

.dialog-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.dialog-subtitle {
  margin: 4px 0 0;
  font-size: 14px;
  color: #64748b;
}

.batch-mode-card {
  margin: 24px 32px;
  padding: 20px;
  background-color: #f8fafc;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.batch-segmented {
  background: #fff;
  padding: 4px;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
}

.batch-notice {
  font-size: 13px;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
}

.tags-scroll-area {
  max-height: 400px;
  padding: 0 32px 32px;
}

.tag-group-section {
  margin-top: 24px;
}

.group-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.title-dot {
  width: 4px;
  height: 14px;
  background-color: var(--primary-color);
  border-radius: 2px;
  margin-right: 10px;
}

.tag-grid {
  display: flex;
  flex-wrap: wrap;
}

.modern-tag-checkbox {
  margin: 4px !important;
}

.modern-tag-checkbox :deep(.el-checkbox__inner) {
  display: none;
}

.modern-tag-checkbox :deep(.el-checkbox__label) {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #fff;
  color: #475569;
  font-weight: 500;
  font-size: 13px;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.modern-tag-checkbox.is-checked :deep(.el-checkbox__label) {
  background-color: var(--primary-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.modern-tag-checkbox.is-checked :deep(.el-checkbox__label::before) {
  content: '✓';
  margin-right: 4px;
  font-weight: 900;
}

.dialog-footer {
  padding: 20px 32px 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.submit-btn {
  height: 44px;
  padding: 0 32px;
  border-radius: 12px;
  font-weight: 700;
  background-color: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: #ffffff !important;
}

.cancel-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 600;
}
</style>
