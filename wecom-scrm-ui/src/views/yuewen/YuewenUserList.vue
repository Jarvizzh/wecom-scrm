<template>
  <div class="user-container mp-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><User /></el-icon>
            <span class="title">阅文用户列表</span>
          </div>
          <div class="right">
            <el-button 
              type="primary" 
              :icon="CollectionTag" 
              :disabled="multipleSelection.length === 0"
              @click="handleBatchTag"
            >
              批量打标签 {{ multipleSelection.length > 0 ? `(${multipleSelection.length})` : '' }}
            </el-button>
            <el-button type="primary" plain :icon="Refresh" @click="handleSync">同步用户</el-button>
          </div>
        </div>
      </template>

      <!-- Filter Section -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="产品">
            <el-select 
              v-model="queryForm.appFlag" 
              placeholder="选择产品" 
              clearable 
              style="width: 200px"
              @change="handleSearch"
            >
              <el-option
                v-for="item in activeProducts"
                :key="item.appFlag"
                :label="item.productName"
                :value="item.appFlag"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="OpenID">
            <el-input
              v-model="queryForm.openid"
              placeholder="搜索微信 OpenID"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="queryForm.nickname"
              placeholder="搜索用户名"
              style="width: 180px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="累计充值">
            <div class="amount-range-input">
              <el-input-number 
                v-model="queryForm.minAmount" 
                :precision="2" 
                :step="10" 
                :min="0"
                placeholder="最小" 
                controls-position="right"
                style="width: 110px"
              />
              <span class="range-separator">-</span>
              <el-input-number 
                v-model="queryForm.maxAmount" 
                :precision="2" 
                :step="10" 
                :min="queryForm.minAmount || 0"
                placeholder="最大" 
                controls-position="right"
                style="width: 110px"
              />
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Selection Info Banner -->
      <div v-if="multipleSelection.length > 0" class="selection-info-banner">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            <div class="selection-banner-content">
              <span>已选择本页 {{ multipleSelection.length }} 位阅文用户</span>
              <template v-if="!isAllSelected">
                <el-button type="primary" link @click="selectAllMatching" class="banner-link">
                  选择满足当前条件的全部 {{ total }} 位用户
                </el-button>
              </template>
              <template v-else>
                <span class="all-selected-text">已选择满足当前条件的全部 {{ total }} 位用户</span>
                <el-button type="primary" link @click="clearAllSelected" class="banner-link">
                  清除全选，仅保留本页选择
                </el-button>
              </template>
            </div>
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
        <el-table-column label="用户名" width="180">
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
            <span class="monospace-id">{{ scope.row.openid }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属产品" width="150">
          <template #default="scope">
            <el-tag 
              size="small" 
              :style="getProductTagStyle(scope.row.appFlag)"
            >
              {{ scope.row.productName || scope.row.appFlag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chargeAmount" label="累计充值(元)" width="150" sortable="custom">
          <template #default="scope">
            <span style="font-weight: 600; color: #f56c6c">¥ {{ (scope.row.chargeAmount / 100).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="chargeNum" label="充值次数" width="110" sortable="custom" />
        <el-table-column prop="registTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.registTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="channelName" label="渠道" width="150" show-overflow-tooltip />
        <el-table-column prop="bookName" label="书籍" width="150" show-overflow-tooltip />
        <el-table-column prop="vipEndTime" label="会员到期" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.vipEndTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="yuewenUpdateTime" label="更新时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.yuewenUpdateTime) }}
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

    <!-- Tagging Dialog -->
    <el-dialog 
      v-model="tagDialogVisible" 
      width="600px"
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
            <h3 class="dialog-title">批量打标签</h3>
            <p class="dialog-subtitle">为选中的阅文用户同步微信标签</p>
          </div>
        </div>
      </template>

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
          <el-button @click="tagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchTag" :loading="marking">
            确认应用
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Sync Dialog -->
    <el-dialog
      v-model="syncDialogVisible"
      title="同步用户"
      width="550px"
      @closed="resetSyncForm"
    >
      <el-form :model="syncForm" :rules="syncRules" ref="syncFormRef" label-width="100px">
        <el-form-item label="选择产品" prop="appFlag">
          <el-select v-model="syncForm.appFlag" placeholder="请选择要同步的产品" style="width: 100%">
            <el-option
              v-for="item in activeProducts"
              :key="item.appFlag"
              :label="item.productName"
              :value="item.appFlag"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getUsers, getProducts, syncUsersFromList, type YuewenUser, type YuewenProduct } from '@/api/yuewen'
import { getTagGroups, getTagsByGroup, batchMarkCustomerTags } from '@/api/tag'
import { User, UserFilled, Search, CollectionTag, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref<YuewenUser[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortField = ref('')
const sortOrder = ref('')
const activeProducts = ref<YuewenProduct[]>([])
const multipleSelection = ref<YuewenUser[]>([])
const isAllSelected = ref(false)
const tagDialogVisible = ref(false)
const tagsLoading = ref(false)
const marking = ref(false)
const allTagGroups = ref<any[]>([])
const selectedTagIds = ref<string[]>([])

const syncDialogVisible = ref(false)
const syncing = ref(false)
const syncFormRef = ref()
const syncForm = reactive({
  appFlag: '',
  timeRange: [] as string[]
})

const syncRules = {
  appFlag: [{ required: true, message: '请选择产品', trigger: 'change' }]
}

const dateShortcuts = [
  { text: '最近1小时', value: () => [new Date(Date.now() - 3600 * 1000), new Date()] },
  { text: '最近1天', value: () => [new Date(Date.now() - 3600 * 1000 * 24), new Date()] },
  { text: '最近7天', value: () => [new Date(Date.now() - 3600 * 1000 * 24 * 7), new Date()] },
  { text: '最近30天', value: () => [new Date(Date.now() - 3600 * 1000 * 24 * 30), new Date()] }
]

const queryForm = reactive({
  appFlag: '',
  openid: '',
  nickname: '',
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined
})

const fetchData = async () => {
  loading.value = true
  isAllSelected.value = false // Reset global selection on data fetch
  try {
    const params = {
      ...queryForm,
      minAmount: queryForm.minAmount != null ? Math.round(queryForm.minAmount * 100) : undefined,
      maxAmount: queryForm.maxAmount != null ? Math.round(queryForm.maxAmount * 100) : queryForm.minAmount != null ? 99999 : undefined,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      page: page.value,
      size: size.value
    }
    const res: any = await getUsers(params)
    users.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('Failed to fetch users', error)
  } finally {
    loading.value = false
  }
}

const fetchActiveProducts = async () => {
  try {
    const res: any = await getProducts({ page: 1, size: 100 })
    activeProducts.value = res.content
  } catch (error) {
    console.error('Failed to fetch products', error)
  }
}

const getProductTagStyle = (id: any) => {
  const colors = [
    { color: '#409eff', border: '#d9ecff', bg: '#ecf5ff' }, // blue
    { color: '#67c23a', border: '#e1f3d8', bg: '#f0f9eb' }, // green
    { color: '#e6a23c', border: '#faecd8', bg: '#fdf6ec' }, // orange
    { color: '#f56c6c', border: '#fde2e2', bg: '#fef0f0' }, // red
    { color: '#909399', border: '#e9e9eb', bg: '#f4f4f5' }, // gray
    { color: '#8e44ad', border: '#ebdcf5', bg: '#f5f0fa' }, // purple
    { color: '#e91e63', border: '#fcd2e1', bg: '#fff0f5' }, // pink
    { color: '#11a1ad', border: '#d2f1f3', bg: '#e6f9fa' }, // cyan
    { color: '#ff9800', border: '#ffe8cc', bg: '#fff8e1' }, // gold
    { color: '#009688', border: '#d2e9e7', bg: '#e0f2f1' }, // teal
  ];
  const hash = typeof id === 'number' ? id : String(id).split('').reduce((a, b) => a + b.charCodeAt(0), 0);
  const index = Math.abs(hash) % 10;
  const color = colors[index];
  return {
    color: color.color,
    borderColor: color.border,
    backgroundColor: color.bg
  };
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const resetQuery = () => {
  queryForm.appFlag = ''
  queryForm.openid = ''
  queryForm.nickname = ''
  queryForm.minAmount = undefined
  queryForm.maxAmount = undefined
  isAllSelected.value = false
  handleSearch()
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
        const data: any = {
          appFlag: syncForm.appFlag
        }
        if (syncForm.timeRange && syncForm.timeRange.length === 2) {
          data.startTime = syncForm.timeRange[0].replace(' ', 'T')
          data.endTime = syncForm.timeRange[1].replace(' ', 'T')
        }
        
        await syncUsersFromList(data)
        ElMessage.success('同步任务已启动')
        syncDialogVisible.value = false
        setTimeout(fetchData, 1500)
      } catch (error) {
        console.error('Sync failed', error)
      } finally {
        syncing.value = false
      }
    }
  })
}

const resetSyncForm = () => {
  syncForm.appFlag = ''
  syncForm.timeRange = []
  if (syncFormRef.value) syncFormRef.value.resetFields()
}

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortField.value = prop
  sortOrder.value = order
  fetchData()
}

const handleSelectionChange = (val: YuewenUser[]) => {
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
  const validTargets = multipleSelection.value.filter(u => u.externalUserid)
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
      targets: isAllSelected.value ? [] : multipleSelection.value.map(u => ({
        externalUserid: u.externalUserid,
        userid: ''
      })),
      addTagIds: selectedTagIds.value,
      selectAll: isAllSelected.value,
      targetType: 'yuewen'
    }

    if (isAllSelected.value) {
      if (queryForm.appFlag) params.appFlag = queryForm.appFlag
      if (queryForm.openid) params.openid = queryForm.openid
      if (queryForm.nickname) params.nickname = queryForm.nickname
      if (queryForm.minAmount != null) {
        params.minAmount = Math.round(queryForm.minAmount * 100)
      }
      if (queryForm.maxAmount != null) {
        params.maxAmount = Math.round(queryForm.maxAmount * 100)
      }
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


const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}

onMounted(() => {
  fetchActiveProducts()
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
.amount-range-input {
  display: flex;
  align-items: center;
  gap: 4px;
}

.range-separator {
  color: #909399;
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
