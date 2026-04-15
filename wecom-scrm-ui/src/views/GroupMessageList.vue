<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>【客户群】群发任务列表</span>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="fetchTasks" :loading="loading">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="$router.push('/group-messages/create')">
              新建任务
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="taskList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sendType" label="发送类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.sendType === 0 ? 'success' : 'warning'">
              {{ row.sendType === 0 ? '立即发送' : '定时发送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.sendTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetCount" label="目标客户群数" width="120">
          <template #default="{ row }">
            {{ row.targetCount || 0 }} 个
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.status === 3 && row.failMsg"
              effect="dark"
              :content="row.failMsg"
              placement="top"
            >
              <el-tag :type="getStatusType(row.status)" style="cursor: pointer">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </el-tooltip>
            <el-tag v-else :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/group-messages/detail/${row.id}`)">详情</el-button>
            <el-button link type="primary" v-if="row.status !== 0" @click="showSendResult(row)">发送结果</el-button>
            <el-button link type="primary" @click="handleCopy(row)">复制</el-button>
            <el-button v-if="row.status === 0" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0 || row.status === 3" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- Send Result Dialog -->
    <el-dialog v-model="sendResultVisible" title="群发送情况" width="700px">
      <el-table :data="sendResultList" v-loading="resultLoading" style="width: 100%">
        <el-table-column label="客户群" prop="groupName" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : (row.status === 0 ? 'info' : 'danger')">
              {{ row.status === 1 ? '已发送' : (row.status === 0 ? '未发送' : '发送失败') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发送时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.sendTime) || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { getGroupMessageList, deleteGroupMessage, getGroupMessageSendResult } from '../api/groupMessage'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const taskList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sendResultVisible = ref(false)
const resultLoading = ref(false)
const sendResultList = ref([])

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getGroupMessageList({
      page: currentPage.value,
      size: pageSize.value
    }) as any
    if (res && res.content) {
      taskList.value = res.content
      total.value = res.totalElements
    } else {
      taskList.value = res || []
      total.value = taskList.value.length
    }
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTasks()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchTasks()
}

const handleEdit = (row: any) => {
  router.push(`/group-messages/edit/${row.id}`)
}

const handleCopy = (row: any) => {
  router.push(`/group-messages/copy/${row.id}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该群发任务吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteGroupMessage(row.id!)
    ElMessage.success('删除成功')
    fetchTasks()
  })
}

const showSendResult = async (row: any) => {
  sendResultVisible.value = true
  resultLoading.value = true
  try {
    const res = await getGroupMessageSendResult(row.id) as any
    sendResultList.value = res.groupList || []
  } catch (error) {
    ElMessage.error('加载发送结果失败')
  } finally {
    resultLoading.value = false
  }
}

const getStatusLabel = (status: number) => ['待发送', '发送中', '已下发', '发送失败'][status] || '未知'
const getStatusType = (status: number) => ['info', 'warning', 'success', 'danger'][status] || 'info'
const formatTime = (time: any) => {
  if (!time) return '-'
  const date = new Date(typeof time === 'number' ? (time < 10000000000 ? time * 1000 : time) : time.replace(' ', 'T'))
  return date.toLocaleString()
}

onMounted(fetchTasks)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }

.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
