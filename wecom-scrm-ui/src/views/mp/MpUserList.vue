<template>
  <div class="mp-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><User /></el-icon>
            <span>公众号粉丝</span>
          </div>
          <div class="right">
            <el-select v-model="selectedAppId" placeholder="选择公众号" style="width: 200px; margin-right: 12px" @change="handleSearch">
              <el-option label="全部公众号" value="" />
              <el-option v-for="acc in accounts" :key="acc.appId" :label="acc.name" :value="acc.appId" />
            </el-select>
            <el-button type="primary" :icon="Refresh" @click="handleSync" :loading="syncing" :disabled="!selectedAppId">
              同步粉丝资料
            </el-button>
          </div>
        </div>
      </template>

      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索昵称 / OpenID / UnionID"
          style="width: 320px"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>

      <el-table v-loading="loading" :data="users" style="width: 100%" class="modern-table" @sort-change="handleSortChange">
        <el-table-column label="粉丝信息" min-width="200">
          <template #default="scope">
            <div class="user-info-cell">
              <el-avatar :size="40" :src="scope.row.avatarUrl" :icon="UserFilled" />
              <div class="user-text">
                <div class="name">
                  {{ scope.row.nickname || '微信用户' }}
                  <el-icon v-if="scope.row.gender === 1" class="male-icon"><Male /></el-icon>
                  <el-icon v-if="scope.row.gender === 2" class="female-icon"><Female /></el-icon>
                </div>
                <div class="openid">OpenId: {{ scope.row.openid }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="unionid" label="UnionID" width="280">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.unionid || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mpName" label="所属公众号" width="180">
          <template #default="scope">
            <el-tag size="small" effect="plain">{{ scope.row.mpName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subscribeTime" label="关注时间" width="200" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.subscribeTime) }}
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
import { ElMessage } from 'element-plus'
import { Search, Refresh, User, UserFilled, Male, Female } from '@element-plus/icons-vue'
import { getMpUsers, syncMpUsers, getMpAccounts } from '../../api/mp'

const loading = ref(false)
const syncing = ref(false)
const users = ref([])
const accounts = ref<any[]>([])
const selectedAppId = ref('')

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const orderBy = ref('subscribeTime')
const order = ref('descending')

const fetchAccounts = async () => {
  try {
    const res: any = await getMpAccounts()
    accounts.value = res || []
  } catch (error) {
    console.error('Failed to fetch accounts:', error)
  }
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await getMpUsers({
      mpAppId: selectedAppId.value || undefined,
      keyword: searchKeyword.value || undefined,
      page: currentPage.value,
      size: pageSize.value,
      orderBy: orderBy.value,
      order: order.value
    })
    users.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch users:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const handleSortChange = ({ prop, order: sortOrder }: any) => {
  orderBy.value = prop || 'subscribeTime'
  order.value = sortOrder || 'descending'
  fetchUsers()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchUsers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUsers()
}

const handleSync = async () => {
  if (!selectedAppId.value) {
    ElMessage.warning('请先选择一个公众号进行同步')
    return
  }
  syncing.value = true
  try {
    await syncMpUsers(selectedAppId.value)
    ElMessage.success('已触发粉丝资料同步任务，请稍后刷新查看最新数据')
    // Auto refresh after some time
    setTimeout(() => {
      fetchUsers()
    }, 2000)
  } catch (error) {
    console.error('Failed to sync users:', error)
  } finally {
    syncing.value = false
  }
}



const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchAccounts()
  fetchUsers()
})
</script>

<style scoped>
.mp-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.user-card {
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

.right {
  display: flex;
  align-items: center;
}

.search-bar {
  margin-bottom: 24px;
  display: flex;
  justify-content: flex-start;
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
  display: flex;
  align-items: center;
  gap: 4px;
}

.male-icon { color: #409eff; }
.female-icon { color: #f56c6c; }

.user-text .openid {
  font-size: 12px;
  color: #909399;
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
</style>
