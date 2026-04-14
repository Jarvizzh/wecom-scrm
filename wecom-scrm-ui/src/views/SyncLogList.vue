<template>
  <div class="sync-log-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>同步日志 / Sync Logs</span>
          <el-button :icon="Refresh" @click="fetchData">
            刷新 / Refresh
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" style="width: 100%" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="syncType" label="同步类型" width="150">
          <template #default="scope">
            {{ formatSyncType(scope.row.syncType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
        <el-table-column prop="createTime" label="同步开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-block">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getSyncLogs } from '../api/syncLog'

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getSyncLogs({
      page: currentPage.value,
      size: pageSize.value
    })
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch sync logs:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const formatSyncType = (type: string) => {
  const map: Record<string, string> = {
    'USER_SYNC': '员工同步',
    'DEPARTMENT_SYNC': '部门同步',
    'TAG_SYNC': '标签同步',
    'CUSTOMER_SYNC': '客户同步',
    'GROUP_CHAT_SYNC': '客户群同步'
  }
  return map[type] || type
}

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '同步中'
    case 1: return '成功'
    case 2: return '失败'
    default: return '未知'
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.sync-log-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
