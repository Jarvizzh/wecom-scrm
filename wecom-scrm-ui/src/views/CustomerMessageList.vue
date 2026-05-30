<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><ChatDotRound /></el-icon>
            <span>客户单聊群发 / Broadcast</span>
          </div>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="handleRefresh" :loading="loading || loopLoading">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="handleNewTask">
              新建任务
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="premium-tabs">
        <el-tab-pane label="单次任务" name="normal">
          <el-table :data="taskList" v-loading="loading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" class-name="hidden-sm-and-down" />
            <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="sendType" label="发送类型" width="100" class-name="hidden-sm-and-down">
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
            <el-table-column prop="targetType" label="目标客户" width="120" class-name="hidden-sm-and-down">
              <template #default="{ row }">
                {{ row.targetType === 0 ? '全部客户' : '筛选客户' }}
              </template>
            </el-table-column>
            <el-table-column prop="targetCount" label="目标客户数" width="120">
              <template #default="{ row }">
                {{ row.targetCount || 0 }} 人
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
            <el-table-column prop="createTime" label="创建时间" width="180" class-name="hidden-md-and-down">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="$router.push(`/customer-messages/detail/${row.id}`)">详情</el-button>
                <el-button link type="primary" v-if="row.status !== 0" @click="showSendResult(row)">发送结果</el-button>
                <el-button link type="primary" :icon="CopyDocument" @click="handleCopy(row)">复制</el-button>
                <el-button v-if="row.status === 0" link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
                <el-button v-if="row.status === 0 || row.status === 3" link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
        </el-tab-pane>

        <el-tab-pane label="循环任务" name="recurring">
          <el-table :data="loopTaskList" v-loading="loopLoading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="loopType" label="循环类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.loopType === 1 ? 'success' : 'warning'">
                  {{ row.loopType === 1 ? '每天' : '每周' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="loopDayOfWeek" label="发送周几" width="150" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.loopType === 1">每天</span>
                <span v-else>{{ formatWeeks(row.loopDayOfWeek) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sendTimeOfDay" label="发送时间" width="100">
              <template #default="{ row }">
                {{ row.sendTimeOfDay }}
              </template>
            </el-table-column>
            <el-table-column prop="targetType" label="目标客户" width="120" class-name="hidden-sm-and-down">
              <template #default="{ row }">
                {{ row.targetType === 0 ? '全部客户' : '筛选客户' }}
              </template>
            </el-table-column>
            <el-table-column prop="lastTriggerTime" label="上次运行时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.lastTriggerTime) || '未运行' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="启用状态" width="100">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="CopyDocument" @click="handleCopy(row)">复制</el-button>
                <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-block">
            <el-pagination 
              v-model:current-page="loopCurrentPage"
              v-model:page-size="loopPageSize"
              :page-sizes="[10, 20, 50, 100]"
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="loopTotal"
              @size-change="handleLoopSizeChange"
              @current-change="handleLoopCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Send Result Dialog -->
    <el-dialog v-model="sendResultVisible" title="成员发送情况" width="700px" align-center>
      <el-table :data="sendResultList" v-loading="resultLoading" style="width: 100%">
        <el-table-column label="成员" width="120">
          <template #default="{ row }">
            {{ row.userName || row.userId }}
          </template>
        </el-table-column>
        <el-table-column label="发送详情" width="200">
          <template #default="{ row }">
            <div v-if="row.totalCount > 0" class="send-status-counts">
              <span class="text-success">成功: {{ row.successCount }}</span>
              <span class="text-danger" style="margin: 0 10px">失败: {{ row.failCount }}</span>
              <el-button link type="primary" size="small" @click="showMemberDetails(row)">详情</el-button>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="状态更新时间" min-width="170">
          <template #default="{ row }">
            {{ formatTime(row.sendTime) || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="sendResultVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Member Details Dialog -->
    <el-dialog v-model="memberDetailVisible" :title="'成员发送详情 - ' + (currentMember?.userName || currentMember?.userId)" width="800px" append-to-body>
      <el-table :data="memberDetailList" v-loading="memberLoading" height="400" border>
        <el-table-column label="客户" min-width="150">
          <template #default="{ row }">
            {{ row.customerName || row.externalUserid }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getMemberStatusType(row.status)">
              {{ getMemberStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="情况说明" min-width="200">
          <template #default="{ row }">
            <span v-if="row.status === 2" class="text-danger">{{ row.failReason }}</span>
            <span v-else-if="row.status === 0">未完成发送</span>
            <span v-else>-</span>
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
import { 
  Refresh, Plus, Edit, Delete, CopyDocument, ChatDotRound,
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { 
  getCustomerMessageList, 
  deleteCustomerMessage, 
  getCustomerMessageSendResult, 
  getMemberSendResult,
  getCustomerMessageLoopList,
  updateCustomerMessageLoop,
  deleteCustomerMessageLoop
} from '@/api/customerMessage'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('normal')

// Normal tasks pagination & data
const taskList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// Loop tasks pagination & data
const loopTaskList = ref([])
const loopLoading = ref(false)
const loopCurrentPage = ref(1)
const loopPageSize = ref(10)
const loopTotal = ref(0)

const sendResultVisible = ref(false)
const resultLoading = ref(false)
const sendResultList = ref([])
const memberDetailVisible = ref(false)
const memberLoading = ref(false)
const memberDetailList = ref([])
const currentMember = ref<any>(null)
const currentTask = ref(null)

const handleRefresh = () => {
  if (activeTab.value === 'normal') {
    fetchTasks()
  } else {
    fetchLoopTasks()
  }
}

const handleTabChange = () => {
  handleRefresh()
}

const handleNewTask = () => {
  if (activeTab.value === 'normal') {
    router.push('/customer-messages/create')
  } else {
    router.push('/customer-messages/loop/create')
  }
}

const handleEdit = (row: any) => {
  if (activeTab.value === 'normal') {
    router.push(`/customer-messages/edit/${row.id}`)
  } else {
    router.push(`/customer-messages/loop/edit/${row.id}`)
  }
}

const handleCopy = (row: any) => {
  if (activeTab.value === 'normal') {
    router.push(`/customer-messages/copy/${row.id}`)
  } else {
    router.push(`/customer-messages/loop/copy/${row.id}`)
  }
}

const handleDelete = (row: any) => {
  const isLoop = activeTab.value === 'recurring'
  ElMessageBox.confirm(
    isLoop ? '确定要删除该循环群发任务吗？' : '确定要删除该群发任务吗？', 
    '提示', 
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      if (isLoop) {
        await deleteCustomerMessageLoop(row.id!)
        ElMessage.success('删除成功')
        fetchLoopTasks()
      } else {
        await deleteCustomerMessage(row.id!)
        ElMessage.success('删除成功')
        fetchTasks()
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const handleStatusChange = async (row: any) => {
  try {
    const payload = {
      taskName: row.taskName,
      targetType: row.targetType,
      targetCondition: row.targetCondition ? JSON.parse(row.targetCondition) : null,
      text: row.content,
      attachments: row.attachments ? JSON.parse(row.attachments) : null,
      senderList: row.senderList ? JSON.parse(row.senderList) : null,
      loopType: row.loopType,
      loopDayOfWeek: row.loopDayOfWeek,
      sendTimeOfDay: row.sendTimeOfDay,
      status: row.status
    }
    await updateCustomerMessageLoop(row.id, payload)
    ElMessage.success(row.status === 1 ? '启用成功' : '停用成功')
  } catch (error) {
    ElMessage.error('更新状态失败')
    row.status = row.status === 1 ? 0 : 1 // Revert switch value
  }
}

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getCustomerMessageList({
      page: currentPage.value,
      size: pageSize.value
    }) as any
    if (res && res.content) {
      taskList.value = res.content
      total.value = res.totalElements
    } else {
      taskList.value = Array.isArray(res) ? res : (res.data || [])
      total.value = taskList.value.length
    }
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const fetchLoopTasks = async () => {
  loopLoading.value = true
  try {
    const res = await getCustomerMessageLoopList({
      page: loopCurrentPage.value,
      size: loopPageSize.value
    }) as any
    if (res && res.content) {
      loopTaskList.value = res.content
      loopTotal.value = res.totalElements
    } else {
      loopTaskList.value = Array.isArray(res) ? res : (res.data || [])
      loopTotal.value = loopTaskList.value.length
    }
  } catch (error) {
    ElMessage.error('获取循环任务列表失败')
  } finally {
    loopLoading.value = false
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

const handleLoopSizeChange = (val: number) => {
  loopPageSize.value = val
  fetchLoopTasks()
}

const handleLoopCurrentChange = (val: number) => {
  loopCurrentPage.value = val
  fetchLoopTasks()
}

const getStatusLabel = (status: number) => {
  const labels = ['待发送', '发送中', '已下发', '发送失败']
  return labels[status] || '未知'
}

const getStatusType = (status: number) => {
  const types = ['info', 'warning', 'success', 'danger']
  return types[status] || 'info'
}

const formatWeeks = (daysStr: string) => {
  if (!daysStr) return '-'
  const weeksMap: Record<string, string> = {
    '1': '周一', '2': '周二', '3': '周三', '4': '周四', '5': '周五', '6': '周六', '7': '周日'
  }
  return daysStr.split(',')
    .map(d => weeksMap[d.trim()] || d)
    .join(', ')
}

const formatTime = (time: string | number) => {
  if (!time) return '-'
  let date: Date
  if (typeof time === 'number') {
    date = new Date(time < 10000000000 ? time * 1000 : time)
  } else {
    if (/^\d+$/.test(time)) {
      const num = parseInt(time, 10)
      date = new Date(num < 10000000000 ? num * 1000 : num)
    } else {
      date = new Date(time.replace(' ', 'T'))
    }
  }
  
  if (isNaN(date.getTime())) return time.toString()
  
  const Y = date.getFullYear()
  const M = String(date.getMonth() + 1).padStart(2, '0')
  const D = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

const showSendResult = async (row: any) => {
  currentTask.value = row
  sendResultVisible.value = true
  resultLoading.value = true
  try {
    const res = await getCustomerMessageSendResult(row.id) as any
    sendResultList.value = res.taskList || []
  } catch (error) {
    ElMessage.error('获取发送结果失败')
  } finally {
    resultLoading.value = false
  }
}

const showMemberDetails = async (member: any) => {
  currentMember.value = member
  memberDetailVisible.value = true
  memberLoading.value = true
  try {
    const res = await getMemberSendResult((currentTask.value as any).id, member.userId) as any
    memberDetailList.value = res.customerList || []
  } catch (error) {
    ElMessage.error('获取明细失败')
  } finally {
    memberLoading.value = false
  }
}

const getMemberStatusLabel = (status: number) => {
  const labels: any = {
    0: '未发送',
    1: '已发送',
    2: '客户已拒收',
    3: '客户已达接收上限'
  }
  return labels[status] || '未知'
}

const getMemberStatusType = (status: number) => {
  if (status === 1) return 'success'
  if (status === 0) return 'info'
  return 'danger'
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  font-size: 18px;
  color: #409eff;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.att-detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}
.att-type-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}
.att-title-text {
  font-size: 13px;
  color: #606266;
}

.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
