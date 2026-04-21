<template>
  <div class="user-container mp-container">
    <el-card class="user-card account-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><User /></el-icon>
            <span class="title">常读用户列表</span>
          </div>
          <div class="right">
            <el-button type="primary" :icon="PriceTag" :disabled="multipleSelection.length === 0 && !isAllSelected" @click="handleBatchTag">批量打标签</el-button>
            <el-button type="primary" :icon="Refresh" @click="handleSync">同步用户</el-button>
          </div>
        </div>
      </template>

      <!-- Filter Section -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="产品">
            <el-select v-model="queryForm.distributorId" placeholder="选择产品" clearable style="width: 200px">
              <el-option
                v-for="item in productOptions"
                :key="item.distributorId"
                :label="item.productName"
                :value="item.distributorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="OpenID">
            <el-input
              v-model="queryForm.openId"
              placeholder="搜索微信 OpenID"
              style="width: 260px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="queryForm.nickname"
              placeholder="模糊搜索用户名"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Select All Alert (Moved here: below search bar, above table) -->
      <div v-if="multipleSelection.length > 0" style="margin-bottom: 16px">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            <span v-if="!isAllSelected">
              已选择本页 {{ multipleSelection.length }} 条数据。
              <el-button link type="primary" @click="selectAllMatching">勾选满足当前筛选条件的所有 {{ total }} 条数据</el-button>
            </span>
            <span v-else>
              已勾选满足当前筛选条件的全部 {{ total }} 条数据。
              <el-button link type="primary" @click="clearAllSelected">取消全选</el-button>
            </span>
          </template>
        </el-alert>
      </div>

      <el-table 
        :data="users" 
        style="width: 100%" 
        v-loading="loading" 
        class="modern-table"
        @sort-change="handleSortChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="所属产品" width="120">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ getProductName(scope.row.distributorId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="150">
          <template #default="scope">
            <div class="user-info-cell">
              <el-avatar :size="40" :src="scope.row.avatar" :icon="UserFilled" />
              <div class="user-text">
                <div class="name">{{ scope.row.nickname || '未知' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="微信OpenID" min-width="250">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.openId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180" />
        <el-table-column prop="rechargeAmount" label="累计充值" width="120" sortable="custom">
          <template #default="scope">
            <span style="font-weight: 600; color: #f56c6c">¥ {{ ((scope.row.rechargeAmount || 0) / 100).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rechargeTimes" label="充值次数" width="110" sortable="custom" />
        <el-table-column prop="balanceAmount" label="余额" width="120">
          <template #default="scope">
            {{ ((scope.row.balanceAmount || 0) / 100).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="来源信息" min-width="180">
          <template #default="scope">
            <div class="sub-text">推广: {{ scope.row.promotionName || '-' }}</div>
            <div class="sub-text">书籍: {{ scope.row.bookName || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="设备信息" min-width="200">
          <template #default="scope">
            <div class="sub-text">品牌: {{ scope.row.deviceBrand || '-' }}</div>
            <div class="sub-text monospace-id" style="font-size: 11px">设备ID: {{ scope.row.encryptedDeviceId }}</div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container pagination-block">
        <el-pagination
          background
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- Sync Dialog -->
    <el-dialog
      v-model="syncDialogVisible"
      title="同步用户"
      width="550px"
      @closed="resetSyncForm"
    >
      <el-form :model="syncForm" :rules="syncRules" ref="syncFormRef" label-width="100px">
        <el-form-item label="选择产品" prop="distributorId">
          <el-select v-model="syncForm.distributorId" placeholder="请选择要同步的产品" style="width: 100%">
            <el-option
              v-for="item in productOptions"
              :key="item.distributorId"
              :label="item.productName"
              :value="item.distributorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" prop="timeRange">
          <el-date-picker
            v-model="syncForm.timeRange"
            type="datetimerange"
            :shortcuts="dateShortcuts"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            clearable
          />
          <div class="tip">若不指定时间，默认同步最近1年的用户</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="syncDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSyncSubmit" :loading="syncing">开始同步</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Batch Tag Dialog -->
    <el-dialog
      v-model="tagDialogVisible"
      width="650px"
      :show-close="false"
      class="modern-dialog"
    >
      <template #header>
        <div class="dialog-custom-header">
          <div class="header-icon-box">
            <el-icon><PriceTag /></el-icon>
          </div>
          <div class="header-text">
            <h3 class="dialog-title">批量打标签</h3>
            <p class="dialog-subtitle">为选中的常读用户对应的企微客户添加标签</p>
          </div>
        </div>
      </template>

      <div class="tags-scroll-area" v-loading="tagsLoading">
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
                :class="{ 'is-checked': selectedTagIds.includes(tag.tagId) }"
              >
                {{ tag.name }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        <el-empty v-if="allTagGroups.length === 0 && !tagsLoading" description="暂无可用标签" />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="tagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchTag" :loading="marking">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getChangduUsers, syncChangduUsers, getChangduProducts } from '@/api/changdu'
import { getTagGroups, getTagsByGroup, batchMarkCustomerTags } from '@/api/tag'
import { User, Search, Refresh, UserFilled, PriceTag } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortField = ref('')
const sortOrder = ref('')
const productOptions = ref<any[]>([])

const multipleSelection = ref<any[]>([])
const isAllSelected = ref(false)

const tagDialogVisible = ref(false)
const tagsLoading = ref(false)
const marking = ref(false)
const allTagGroups = ref<any[]>([])
const selectedTagIds = ref<string[]>([])

const queryForm = reactive({
  distributorId: undefined as number | undefined,
  openId: '',
  nickname: ''
})

const syncDialogVisible = ref(false)
const syncing = ref(false)
const syncFormRef = ref()
const syncForm = reactive({
  distributorId: undefined as number | undefined,
  timeRange: [] as string[]
})

const syncRules = {
  distributorId: [{ required: true, message: '请选择产品', trigger: 'change' }]
}

const dateShortcuts = [
  { text: '最近1小时', value: () => [new Date(Date.now() - 3600 * 1000), new Date()] },
  { text: '最近1天', value: () => [new Date(Date.now() - 3600 * 1000 * 24), new Date()] },
  { text: '最近7天', value: () => [new Date(Date.now() - 3600 * 1000 * 24 * 7), new Date()] },
  { text: '最近30天', value: () => [new Date(Date.now() - 3600 * 1000 * 24 * 30), new Date()] }
]

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: size.value,
      distributorId: queryForm.distributorId,
      sortField: sortField.value,
      sortOrder: sortOrder.value
    }
    if (queryForm.openId && queryForm.openId.trim()) {
      params.openId = queryForm.openId.trim()
    }
    if (queryForm.nickname && queryForm.nickname.trim()) {
      params.nickname = queryForm.nickname.trim()
    }
    const res: any = await getChangduUsers(params)
    users.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('Failed to fetch users', error)
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  try {
    const res: any = await getChangduProducts({ page: 1, size: 1000 })
    productOptions.value = res.content
  } catch (error) {
    console.error('Failed to fetch products', error)
  }
}

const getProductName = (distributorId: number) => {
  const product = productOptions.value.find(p => p.distributorId === distributorId)
  return product ? product.productName : distributorId
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortField.value = prop
  sortOrder.value = order
  fetchData()
}

const resetQuery = () => {
  queryForm.distributorId = undefined
  queryForm.openId = ''
  queryForm.nickname = ''
  isAllSelected.value = false
  handleSearch()
}

const handleSelectionChange = (val: any[]) => {
  multipleSelection.value = val
  if (val.length === 0) {
    isAllSelected.value = false
  }
}

const selectAllMatching = () => {
  isAllSelected.value = true
}

const clearAllSelected = () => {
  isAllSelected.value = false
}

const fetchTagGroupsData = async () => {
  tagsLoading.value = true
  try {
    const groups = await getTagGroups() as any
    const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
       const tags = await getTagsByGroup(g.groupId) as any
       return { ...g, tags }
    }))
    allTagGroups.value = groupsWithTags
  } catch (error) {
    ElMessage.error('获取标签库失败')
  } finally {
    tagsLoading.value = false
  }
}

const handleBatchTag = () => {
  const validTargets = multipleSelection.value.filter(u => u.externalId)
  if (validTargets.length === 0 && !isAllSelected.value) {
    ElMessage.warning('选中的用户均未关联外部联系人ID，无法打标签')
    return
  }
  
  if (validTargets.length < multipleSelection.value.length && !isAllSelected.value) {
    ElMessageBox.confirm(
      `选中的 ${multipleSelection.value.length} 个用户中，仅 ${validTargets.length} 个已关联外部联系人。是否继续为这 ${validTargets.length} 个用户打标签？`,
      '提示',
      { confirmButtonText: '继续', cancelButtonText: '取消', type: 'warning' }
    ).then(() => {
      openTagDialog()
    }).catch(() => {})
  } else {
    openTagDialog()
  }
}

const openTagDialog = () => {
  selectedTagIds.value = []
  tagDialogVisible.value = true
  fetchTagGroupsData()
}

const confirmBatchTag = async () => {
  if (selectedTagIds.value.length === 0) {
    ElMessage.warning('请至少选择一个标签')
    return
  }

  marking.value = true
  try {
    const params: any = {
      targets: isAllSelected.value ? [] : multipleSelection.value.filter(u => u.externalId).map(u => ({
        externalUserid: u.externalId,
        userid: ''
      })),
      addTagIds: selectedTagIds.value,
      selectAll: isAllSelected.value,
      targetType: 'changdu'
    }

    if (isAllSelected.value) {
      params.distributorId = queryForm.distributorId
      params.openid = queryForm.openId
      params.nickname = queryForm.nickname
    }

    await batchMarkCustomerTags(params)
    ElMessage.success('批量打标签任务已启动，后台处理中')
    tagDialogVisible.value = false
    isAllSelected.value = false
  } catch (error) {
    console.error('Batch tag error:', error)
  } finally {
    marking.value = false
  }
}

const handleSync = () => {
  syncDialogVisible.value = true
}

const handleSyncSubmit = async () => {
  if (!syncFormRef.value) return
  await syncFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      syncing.value = true
      try {
        const params: any = {
          distributorId: syncForm.distributorId!
        }
        if (syncForm.timeRange && syncForm.timeRange.length === 2) {
          params.startTime = syncForm.timeRange[0].replace(' ', 'T')
          params.endTime = syncForm.timeRange[1].replace(' ', 'T')
        }
        
        await syncChangduUsers(params)
        ElMessage.success('同步任务已启动')
        syncDialogVisible.value = false
        setTimeout(fetchData, 1000)
      } catch (error) {
        console.error('Sync failed', error)
      } finally {
        syncing.value = false
      }
    }
  })
}

const resetSyncForm = () => {
  syncForm.distributorId = undefined
  syncForm.timeRange = []
  if (syncFormRef.value) syncFormRef.value.resetFields()
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}

onMounted(() => {
  fetchProducts()
  fetchData()
})
</script>

<style scoped>
.user-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.user-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.search-bar {
  margin-bottom: 24px;
}

.filter-form :deep(.el-form-item) {
  margin-right: 24px;
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.card-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.user-text .name {
  font-weight: 600;
  color: #303133;
}

.monospace-id {
  font-family: monospace;
  font-size: 13px;
  color: #606266;
}

.sub-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8faff;
  color: #606266;
  font-weight: 600;
}

.pagination-block {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* Tag Dialog Styles */
.dialog-custom-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.header-icon-box {
  width: 48px;
  height: 48px;
  background: #ecf5ff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 24px;
}

.dialog-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.dialog-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.tags-scroll-area {
  max-height: 400px;
  overflow-y: auto;
  padding: 16px 0;
}

.tag-group-section {
  margin-bottom: 24px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

.title-dot {
  width: 6px;
  height: 6px;
  background: #409eff;
  border-radius: 50%;
}

.tag-grid :deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
}

.modern-tag-checkbox {
  margin-right: 0 !important;
  border: 1px solid #e4e7ed;
  padding: 4px 10px;
  border-radius: 6px;
  transition: all 0.2s;
  height: auto;
  display: inline-flex;
  align-items: center;
}

.modern-tag-checkbox :deep(.el-checkbox__label) {
  padding-left: 8px;
  font-size: 13px;
  line-height: 1;
}

.modern-tag-checkbox:hover {
  border-color: #c0c4cc;
  background: #f5f7fa;
}

.modern-tag-checkbox.is-checked {
  border-color: #409eff;
  background: #f0f7ff;
}

.modern-tag-checkbox.is-checked :deep(.el-checkbox__label) {
  color: #409eff;
}

.dialog-footer {
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
