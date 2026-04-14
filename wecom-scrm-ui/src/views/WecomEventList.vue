<template>
  <div class="wecom-event-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Notification /></el-icon>
            <span class="header-title">回调事件日志 / Callback Event Logs</span>
          </div>
          <div class="header-actions">
            <el-button :icon="Refresh" @click="fetchData" circle />
          </div>
        </div>
      </template>

      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%" 
        stripe 
        border
        class="custom-table"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" class-name="hidden-sm-and-down" />
        <el-table-column prop="msgType" label="消息类型" width="100">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.msgType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="event" label="事件" width="180">
          <template #default="scope">
            <el-tag size="small" type="success" v-if="scope.row.event">{{ scope.row.event }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="changeType" label="变更类型" width="180" class-name="hidden-sm-and-down">
          <template #default="scope">
            <template v-if="scope.row.event === 'change_external_chat' && scope.row.updateDetail">
              <el-tag size="small" type="danger" v-if="scope.row.updateDetail === 'del_member'">退出群聊</el-tag>
              <el-tag size="small" type="success" v-else-if="scope.row.updateDetail === 'add_member'">加入群聊</el-tag>
              <el-tag size="small" type="info" v-else>{{ scope.row.updateDetail }}</el-tag>
            </template>
            <template v-else-if="scope.row.changeType">
              <el-tag size="small" type="danger" v-if="scope.row.changeType === 'del_follow_user'">用户流失</el-tag>
              <el-tag size="small" type="danger" v-else-if="scope.row.changeType === 'del_external_contact'">删除用户</el-tag>
              <el-tag size="small" type="success" v-else-if="scope.row.changeType === 'add_external_contact'">添加用户</el-tag>
              <el-tag size="small" type="warning" v-else>{{ scope.row.changeType }}</el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="关联对象" min-width="220">
          <template #default="scope">
            <div class="relation-container">
              <div v-if="scope.row.userid" class="entity-item">
                <el-avatar :size="24" :src="scope.row.userAvatar" class="avatar-sm">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div class="entity-details">
                  <span class="entity-name">{{ scope.row.userName || '未知员工' }}</span>
                </div>
              </div>
              <div v-if="scope.row.externalUserid" class="entity-item">
                <el-avatar :size="24" :src="scope.row.externalUserAvatar" class="avatar-sm">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div class="entity-details">
                  <span class="entity-name customer-name">{{ scope.row.externalUserName || '未知联系人' }}</span>
                </div>
              </div>
              <div v-if="scope.row.chatId" class="entity-item no-avatar">
                <div class="entity-details">
                  <span class="entity-name">{{ scope.row.groupName || '未知客户群' }}</span>
                </div>
              </div>
              <div v-if="scope.row.memberName" class="entity-item">
                <el-avatar :size="24" :src="scope.row.memberAvatar" class="avatar-sm">
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div class="entity-details">
                  <span class="entity-name customer-name">{{ scope.row.memberName }}</span>
                </div>
              </div>
              <span v-if="!scope.row.userid && !scope.row.externalUserid && !scope.row.chatId" class="no-data">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="dark">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="产生时间" width="180" class-name="hidden-md-and-down">
          <template #default="scope">
            <span class="time-text">{{ formatDateTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="showDetail(scope.row)"
              :icon="View"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-block">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="事件详情"
      width="60%"
      class="detail-dialog responsive-dialog"
    >
      <div v-if="selectedEvent" class="detail-content">
        <el-descriptions border :column="isMobile ? 1 : 2">
          <el-descriptions-item label="ID">{{ selectedEvent.id }}</el-descriptions-item>
          <el-descriptions-item label="消息类型">{{ selectedEvent.msgType }}</el-descriptions-item>
          <el-descriptions-item label="事件">{{ selectedEvent.event || '-' }}</el-descriptions-item>
          <el-descriptions-item label="变更类型">{{ selectedEvent.changeType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusType(selectedEvent.status)">
              {{ getStatusText(selectedEvent.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="重试次数">{{ selectedEvent.retryCount }}</el-descriptions-item>
          <el-descriptions-item label="相关员工">
            <div v-if="selectedEvent.userid" class="detail-entity">
              <el-avatar :size="32" :src="selectedEvent.userAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="entity-meta">
                <div class="name">{{ selectedEvent.userName || '未知员工' }}</div>
              </div>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="外部联系人">
            <div v-if="selectedEvent.externalUserid" class="detail-entity">
              <el-avatar :size="32" :src="selectedEvent.externalUserAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="entity-meta">
                <div class="name customer-name">{{ selectedEvent.externalUserName || '未知联系人' }}</div>
              </div>
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="客户群" v-if="selectedEvent.chatId">
            <div class="detail-entity">
              <div class="entity-meta">
                <div class="name">{{ selectedEvent.groupName || '未知客户群' }}</div>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="变更成员" v-if="selectedEvent.memberName">
            <div class="detail-entity">
              <el-avatar :size="32" :src="selectedEvent.memberAvatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="entity-meta">
                <div class="name customer-name">{{ selectedEvent.memberName }}</div>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="变更详情" v-if="selectedEvent.updateDetail">
             <el-tag size="small" type="info">{{ selectedEvent.updateDetail }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ formatDateTime(selectedEvent.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2" v-if="selectedEvent.errorMsg">
            <div class="error-msg">{{ selectedEvent.errorMsg }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="raw-content-section">
          <div class="section-title">原始内容 (JSON)</div>
          <pre class="raw-json">{{ formatJson(selectedEvent.content) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh, Notification, View, UserFilled } from '@element-plus/icons-vue'
import { getWecomEvents } from '../api/wecomEvent'
import { useResponsive } from '../hooks/useResponsive'

const { isMobile } = useResponsive()

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const selectedEvent = ref<any>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getWecomEvents({
      page: currentPage.value,
      size: pageSize.value
    })
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch callback events:', error)
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
    case 0: return '待处理'
    case 1: return '成功'
    case 2: return '失败'
    default: return '未知'
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const showDetail = (row: any) => {
  selectedEvent.value = row
  detailVisible.value = true
}

const formatJson = (content: string) => {
  try {
    return JSON.stringify(JSON.parse(content), null, 2)
  } catch (e) {
    return content
  }
}
</script>

<style scoped>
.wecom-event-container {
  padding: 20px;
}

.box-card {
  border-radius: 12px;
  border: none;
  background: white;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
  color: #409EFF;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
  margin-top: 10px;
}

.relation-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.entity-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-sm {
  border: 1px solid #ebeef5;
}

.entity-details {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
  justify-content: center;
}

.entity-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.entity-name.customer-name {
  color: #67C23A;
}

.entity-name.group-name {
  color: #303133;
}
.member-name {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.user-icon-detail {
  font-size: 32px;
  color: #909399;
}

.entity-item.no-avatar {
  padding-left: 0;
}

.entity-item.no-avatar .entity-details {
  padding-left: 0;
}

.entity-id {
  font-size: 11px;
  color: #909399;
  font-family: monospace;
}

.detail-entity {
  display: flex;
  align-items: center;
  gap: 12px;
}

.entity-meta .name {
  font-weight: 600;
  font-size: 14px;
}

.entity-meta .name.customer-name {
  color: #67C23A;
}

.entity-meta .name.group-name {
  color: #303133;
}

.group-icon-detail {
  font-size: 24px;
  color: #E6A23C;
}

.entity-item.no-avatar {
  padding-left: 0;
}

.entity-item.no-avatar .entity-details {
  padding-left: 0;
}

.entity-meta .id {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.no-data {
  color: #C0C4CC;
}

.time-text {
  color: #606266;
  font-size: 13px;
}

.pagination-block {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.error-msg {
  color: #F56C6C;
  font-family: monospace;
  background-color: #fef0f0;
  padding: 8px;
  border-left: 3px solid #F56C6C;
  border-radius: 4px;
  font-size: 12px;
}

.raw-content-section {
  margin-top: 10px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 14px;
  background: #409EFF;
  border-radius: 2px;
}

.raw-json {
  background-color: #282c34;
  color: #abb2bf;
  padding: 15px;
  border-radius: 8px;
  font-family: 'Fira Code', 'Roboto Mono', monospace;
  font-size: 12px;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
  line-height: 1.5;
}

:deep(.detail-dialog .el-dialog__header) {
  margin-right: 0;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.detail-dialog .el-dialog__body) {
  padding: 30px;
}
</style>
