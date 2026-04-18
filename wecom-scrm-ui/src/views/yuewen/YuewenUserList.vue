<template>
  <div class="user-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">阅文用户列表</span>
        </div>
      </template>

      <!-- Filter Section -->
      <div class="filter-section">
        <el-form :inline="true" :model="queryForm" class="demo-form-inline">
          <el-form-item label="产品">
            <el-select v-model="queryForm.appFlag" placeholder="请选择产品" clearable style="width: 200px">
              <el-option
                v-for="item in activeProducts"
                :key="item.appFlag"
                :label="item.productName"
                :value="item.appFlag"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="OpenID">
            <el-input v-model="queryForm.openid" placeholder="请输入微信OpenID" clearable style="width: 300px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="users" style="width: 100%" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="guid" label="阅文GUID" width="150" />
        <el-table-column prop="openid" label="微信OpenID" min-width="250" show-overflow-tooltip />
        <el-table-column label="所属产品" width="150">
          <template #default="scope">
            {{ getProductName(scope.row.appFlag) }}
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

      <div class="pagination-container">
        <el-pagination
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
import { getUsers, getProducts } from '../../api/yuewen'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortField = ref('')
const sortOrder = ref('')
const activeProducts = ref([])

const queryForm = reactive({
  appFlag: '',
  openid: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getUsers({
      ...queryForm,
      sortField: sortField.value,
      sortOrder: sortOrder.value,
      page: page.value,
      size: size.value
    })
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
  handleSearch()
}

const handleSortChange = ({ prop, order }: { prop: string, order: string }) => {
  sortField.value = prop
  sortOrder.value = order
  fetchData()
}

const getProductName = (appFlag: string) => {
  const product = activeProducts.value.find((p: any) => p.appFlag === appFlag)
  return product ? product.productName : appFlag
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
  padding: 10px;
}
.filter-section {
  margin-bottom: 20px;
  background-color: #f5f7fa;
  padding: 18px 18px 0;
  border-radius: 4px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
