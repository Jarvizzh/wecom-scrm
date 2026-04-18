<template>
  <div class="user-container mp-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><User /></el-icon>
            <span class="title">阅文用户列表</span>
          </div>
        </div>
      </template>

      <!-- Filter Section -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="产品">
            <el-select v-model="queryForm.appFlag" placeholder="选择产品" clearable style="width: 200px">
              <el-option
                v-for="item in activeProducts"
                :key="item.appFlag"
                :label="item.productName"
                :value="item.appFlag"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="搜索">
            <el-input
              v-model="queryForm.openid"
              placeholder="搜索微信 OpenID"
              style="width: 260px"
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

      <el-table :data="users" style="width: 100%" v-loading="loading" class="modern-table" @sort-change="handleSortChange">
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
            <el-tag size="small" effect="plain">{{ scope.row.productName || scope.row.appFlag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chargeAmount" label="累计充值(元)" width="150" sortable="custom">
          <template #default="scope">
            {{ (scope.row.chargeAmount / 100).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="chargeNum" label="充值次数" width="110" sortable="custom" />
        <el-table-column prop="isSubscribe" label="是否关注" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isSubscribe === 1 ? 'success' : 'info'">
              {{ scope.row.isSubscribe === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.registTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="yuewenUpdateTime" label="最后同步" width="180">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getUsers, getProducts, type YuewenUser, type YuewenProduct } from '../../api/yuewen'
import { User, UserFilled, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const users = ref<YuewenUser[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortField = ref('')
const sortOrder = ref('')
const activeProducts = ref<YuewenProduct[]>([])

const queryForm = reactive({
  appFlag: '',
  openid: '',
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined
})

const fetchData = async () => {
  loading.value = true
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

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const resetQuery = () => {
  queryForm.appFlag = ''
  queryForm.openid = ''
  queryForm.minAmount = undefined
  queryForm.maxAmount = undefined
  handleSearch()
}

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortField.value = prop
  sortOrder.value = order
  fetchData()
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
</style>
