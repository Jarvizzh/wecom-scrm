<template>
  <div class="group-container">

    <!-- Filter Bar -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-input
            v-model="searchName"
            placeholder="搜索群聊名称"
            :prefix-icon="Search"
            clearable
            style="width: 240px"
            @input="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-select
            v-model="searchOwner"
            placeholder="筛选群主"
            clearable
            filterable
            style="width: 200px"
            @change="handleSearch"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
            <el-option
              v-for="user in userList"
              :key="user.userid"
              :label="user.name"
              :value="user.userid"
            >
              <div class="option-content">
                <span>{{ user.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="search-btn" :icon="Search" @click="handleSearch">查询</el-button>
        </el-form-item>
        <el-form-item class="action-wrapper">
          <el-button 
            type="primary" 
            plain
            class="action-btn sync-btn"
            :icon="Refresh" 
            :loading="syncing" 
            @click="handleSync"
          >
            同步客户群
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table list -->
    <el-card class="table-card" shadow="never">
      <el-table 
        v-loading="loading" 
        :data="tableData" 
        style="width: 100%" 
        @sort-change="handleSortChange"
        class="custom-table"
      >
        <el-table-column label="群聊信息" min-width="240">
          <template #default="scope">
            <div class="group-info-cell">
              <div class="group-icon-brand">
                <el-icon><ChatLineRound /></el-icon>
              </div>
              <div class="group-detail">
                <div class="group-name">{{ scope.row.name || '未命名群聊' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="ownerName" label="群主" width="180">
          <template #default="scope">
            <div class="owner-cell">
              <span>{{ scope.row.ownerName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="memberCount" label="群人数" width="120" align="center" sortable="custom">
          <template #default="scope">
            <el-tag effect="plain" round class="member-count-tag">
              {{ scope.row.memberCount }} 人
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="180" sortable="custom" class-name="hidden-sm-and-down">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDateTime(scope.row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>


        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link class="detail-link" @click="viewMembers(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-block">
        <el-pagination 
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchGroups"
          @current-change="fetchGroups"
        />
      </div>
    </el-card>

    <!-- Members Dialog -->
    <el-dialog 
      v-model="membersDialogVisible" 
      width="780px"
      class="custom-members-dialog"
      align-center
    >
      <template #header>
        <div class="dialog-custom-header">
          <div class="header-icon-box">
             <el-icon><ChatLineRound /></el-icon>
          </div>
          <div class="header-text-box">
            <h3 class="dialog-title">{{ currentGroupName }} - 成员列表</h3>
            <p class="dialog-subtitle">查看最新的群成员组成及入群信息</p>
          </div>
        </div>
      </template>

      <el-table 
        v-loading="membersLoading" 
        :data="membersData" 
        style="width: 100%" 
        max-height="500"
        class="members-inner-table"
      >
        <el-table-column label="成员身份" min-width="200">
          <template #default="scope">
            <div class="member-identity-cell">
              <el-avatar 
                v-if="scope.row.type !== 1"
                :size="40" 
                :src="scope.row.avatar" 
                class="member-avatar"
              >
                {{ scope.row.memberName?.charAt(0) }}
              </el-avatar>
              <div class="member-detail">
                <span class="member-name">{{ scope.row.memberName }}</span>
                <span :class="['role-tag', scope.row.type === 1 ? 'is-host' : 'is-guest']">
                  {{ scope.row.type === 1 ? '企业员工' : '外部客户' }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="入群时间" min-width="180" class-name="hidden-sm-and-down">
          <template #default="scope">
             <div class="time-cell">
               <el-icon><Calendar /></el-icon>
               <span>{{ formatDateTime(scope.row.joinTime) }}</span>
             </div>
          </template>
        </el-table-column>
        <el-table-column label="来源方式" width="140" align="center">
          <template #default="scope">
            <el-tag effect="light" size="small" type="info" class="scene-tag">
               {{ getJoinSceneLabel(scope.row.joinScene) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="dialog-footer">
        <el-button @click="membersDialogVisible = false" class="close-btn">关闭对话框</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh, Search, User, Calendar, ChatLineRound } from '@element-plus/icons-vue'
import { getGroupChatList, syncGroupChats, getGroupMembers } from '@/api/groupChat'
import { getUsers } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const syncing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const membersDialogVisible = ref(false)
const membersLoading = ref(false)
const membersData = ref([])
const currentGroupName = ref('')
const searchOwner = ref('')
const searchName = ref('')
const sortProp = ref('')
const sortOrder = ref('')
const userList = ref<any[]>([])

const fetchGroups = async () => {
  loading.value = true
  try {
    const res = (await getGroupChatList({
      page: currentPage.value,
      size: pageSize.value,
      owner: searchOwner.value,
      name: searchName.value,
      sortProp: sortProp.value,
      sortOrder: sortOrder.value
    })) as any
    tableData.value = res.content
    total.value = res.totalElements
  } catch (error: any) {
    ElMessage.error('加载群聊数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchGroups()
}

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  sortProp.value = prop
  sortOrder.value = order
  fetchGroups()
}

const fetchUsers = async () => {
  try {
    const res = (await getUsers({ page: 1, size: 200 })) as any
    userList.value = res.content
  } catch (error) {}
}

const handleSync = async () => {
  try {
    await ElMessageBox.confirm(
      '执行数据同步可能需要一些时间，后台处理中，请稍后刷新查看最新状态。',
      {
        confirmButtonText: '确定同步',
        cancelButtonText: '取消',
        type: 'info',
        title: '同步提示'
      }
    )
    syncing.value = true
    await syncGroupChats()
    ElMessage.success('已触发全量同步任务')
    setTimeout(() => {
      fetchGroups()
    }, 1500)
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error('同步请求发送失败')
  } finally {
    syncing.value = false
  }
}

const viewMembers = async (row: any) => {
  currentGroupName.value = row.name || '未命名群聊'
  membersDialogVisible.value = true
  membersLoading.value = true
  membersData.value = []
  try {
    const res = await getGroupMembers(row.chatId)
    membersData.value = res as any
  } catch (error: any) {
    ElMessage.error('加载成员信息失败')
  } finally {
    membersLoading.value = false
  }
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

const getJoinSceneLabel = (scene: number) => {
  const map: Record<number, string> = { 1: '直接邀请', 2: '链接加入', 3: '扫码入群' }
  return map[scene] || '其它方式'
}

onMounted(() => {
  fetchGroups()
  fetchUsers()
})
</script>

<style scoped>
.group-container {
  --primary-color: #409eff;
  --primary-light: #ecf5ff;
  --primary-hover: #66b1ff;
  --bg-main: #f8fafc;
  --slate-50: #f8fafc;
  --slate-100: #f1f5f9;
  --slate-200: #e2e8f0;
  --slate-500: #64748b;
  --slate-700: #334155;
  --slate-900: #0f172a;

  padding: 32px;
  background-color: var(--bg-main);
  min-height: calc(100vh - 64px);
}

@media (max-width: 768px) {
  .group-container {
    padding: 12px;
  }
}

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

.action-btn {
  border-radius: 10px;
  font-weight: 600;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.sync-btn:hover {
  transform: translateY(-1px);
}

.action-wrapper {
  margin-left: auto;
  margin-right: 0 !important;
}

.filter-card {
  margin-bottom: 10px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.filter-form {
  padding: 8px 4px;
  display: flex;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 24px;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

.table-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.custom-table {
  border: none;
}

.custom-table :deep(.el-table__header) th {
  background-color: #f1f5f9;
  color: #475569;
  font-weight: 700;
  font-size: 13px;
  height: 56px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.custom-table :deep(.el-table__row):hover {
  background-color: #f8fafc !important;
}

/* Group Identity Cell */
.group-info-cell {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 0;
}

.group-icon-brand {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: var(--primary-color);
  box-shadow: 0 4px 6px -1px rgba(64, 158, 255, 0.1);
}

.group-name {
  font-weight: 700;
  color: #1e293b;
  font-size: 15px;
}

.group-id {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.owner-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  color: #334155;
}

.owner-badge {
  width: 32px;
  height: 32px;
  background-color: #f1f5f9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.member-count-tag {
  background-color: var(--primary-light);
  border-color: #d9ecff;
  color: var(--primary-color);
  font-weight: 700;
  padding: 0 12px;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 13px;
}


.detail-link {
  font-weight: 700;
  color: var(--primary-color);
}

.detail-link:hover {
  text-decoration: underline;
}

.pagination-block {
  padding: 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

/* Dialog Stylings */
.custom-members-dialog :deep(.el-dialog) {
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

.members-inner-table {
  padding: 0 20px;
}

.member-identity-cell {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 0;
}

.member-avatar {
  border: 2px solid #fff;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
  background-color: #f1f5f9;
  color: var(--primary-color);
  font-weight: 700;
}

.member-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-name {
  font-weight: 700;
  color: #1e293b;
  font-size: 14px;
}

.role-tag {
  font-size: 10px;
  padding: 1px 8px;
  border-radius: 6px;
  font-weight: 700;
  width: fit-content;
}

.role-tag.is-host {
  background-color: #fef2f2;
  color: #ef4444;
}

.role-tag.is-guest {
  background-color: #ecfdf5;
  color: #10b981;
}

.scene-tag {
  border-radius: 6px;
  font-weight: 600;
}

.dialog-footer {
  padding: 24px 32px 32px;
  display: flex;
  justify-content: flex-end;
}

.close-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 600;
}
</style>
