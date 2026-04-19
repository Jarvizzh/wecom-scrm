<template>
  <div class="consume-container mp-container">
    <el-card class="consume-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><Tickets /></el-icon>
            <span class="title">阅文消费记录</span>
          </div>
          <div class="right">
            <el-button 
              type="primary" 
              :icon="Refresh" 
              @click="syncDialogVisible = true"
            >
              同步消费记录
            </el-button>
          </div>
        </div>
      </template>

      <!-- Filter Section -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="产品">
            <el-select v-model="queryForm.appFlag" placeholder="选择产品" clearable style="width: 180px">
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
              style="width: 220px"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="书籍名称">
            <el-input
              v-model="queryForm.bookName"
              placeholder="搜索书籍名称"
              style="width: 180px"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table 
        :data="records" 
        style="width: 100%" 
        v-loading="loading" 
        class="modern-table" 
        @sort-change="handleSortChange"
      >
        <el-table-column label="用户OpenID" min-width="220">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.openid }}</span>
          </template>
        </el-table-column>
        <el-table-column label="所属产品" width="140">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.productName || scope.row.appFlag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消费内容" min-width="250">
          <template #default="scope">
            <div class="book-info">
              <div class="book-name">{{ scope.row.bookName || '未知书籍' }}</div>
              <div class="chapter-name" v-if="scope.row.chapterName">
                {{ scope.row.chapterName }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="worthAmount" label="有价币" width="100" sortable="custom" />
        <el-table-column prop="freeAmount" label="免费币" width="100" sortable="custom" />
        <el-table-column prop="consumeTime" label="消费时间" width="180" sortable="custom">
          <template #default="scope">
            {{ formatTime(scope.row.consumeTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="订单号" width="180" show-overflow-tooltip>
          <template #default="scope">
             <span class="monospace-id">{{ scope.row.orderId }}</span>
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
    <el-dialog v-model="syncDialogVisible" title="同步消费记录" width="500px">
      <el-form :model="syncForm" label-width="100px">
        <el-form-item label="产品" required>
          <el-select v-model="syncForm.appFlag" placeholder="选择产品" style="width: 100%">
            <el-option
              v-for="item in activeProducts"
              :key="item.appFlag"
              :label="item.productName"
              :value="item.appFlag"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围" required>
          <el-date-picker
            v-model="syncForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
            :shortcuts="shortcuts"
            style="width: 100%"
          />
          <div class="sync-tip">注：仅支持最近半年内的消费记录查询，系统将自动拆分时间区间进行同步。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="syncDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSync" :loading="syncing">
            开始同步
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getConsumeRecords, syncConsumeRecords, getProducts, type YuewenConsumeRecord, type YuewenProduct } from '../../api/yuewen'
import { Search, Refresh, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const syncing = ref(false)
const records = ref<YuewenConsumeRecord[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortField = ref('')
const sortOrder = ref('')
const activeProducts = ref<YuewenProduct[]>([])
const syncDialogVisible = ref(false)
const shortcuts = [
  {
    text: '最近1天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24)
      return [start, end]
    },
  },
  {
    text: '最近3天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 3)
      return [start, end]
    },
  },
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近1个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近3个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [start, end]
    },
  },
  {
    text: '最近半年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
]

const queryForm = reactive({
  appFlag: '',
  openid: '',
  bookName: ''
})

const syncForm = reactive({
  appFlag: '',
  timeRange: [] as string[]
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      page: page.value,
      size: size.value
    }
    const res: any = await getConsumeRecords(params)
    records.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('Failed to fetch consume records', error)
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

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const resetQuery = () => {
  queryForm.appFlag = ''
  queryForm.openid = ''
  queryForm.bookName = ''
  handleSearch()
}

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortField.value = prop
  sortOrder.value = order
  fetchData()
}

const handleSync = async () => {
  if (!syncForm.appFlag) {
    ElMessage.warning('请选择产品')
    return
  }
  if (!syncForm.timeRange || syncForm.timeRange.length === 0) {
    ElMessage.warning('请选择时间范围')
    return
  }

  syncing.value = true
  try {
    await syncConsumeRecords({
      appFlag: syncForm.appFlag,
      startTime: syncForm.timeRange[0],
      endTime: syncForm.timeRange[1]
    })
    ElMessage.success('同步任务已提交，后台处理中')
    syncDialogVisible.value = false
  } catch (error) {
    console.error('Sync failed', error)
  } finally {
    syncing.value = false
  }
}

const disabledDate = (time: Date) => {
  return time.getTime() > Date.now() || time.getTime() < Date.now() - 180 * 24 * 60 * 60 * 1000
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
.consume-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.consume-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
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

.search-bar {
  margin-bottom: 24px;
}

.filter-form :deep(.el-form-item) {
  margin-right: 24px;
}

.monospace-id {
  font-family: monospace;
  font-size: 13px;
  color: #606266;
}

.book-info .book-name {
  font-weight: 600;
  color: #303133;
}

.book-info .chapter-name {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
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

.sync-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 8px;
  line-height: 1.4;
}
</style>
