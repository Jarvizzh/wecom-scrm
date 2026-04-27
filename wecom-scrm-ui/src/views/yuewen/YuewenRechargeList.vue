<template>
  <div class="recharge-container mp-container">
    <el-card class="recharge-card account-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><Wallet /></el-icon>
            <span class="title">阅文充值记录</span>
          </div>
          <div class="right">
            <el-button type="primary" plain :icon="Refresh" @click="handleSync">同步充值记录</el-button>
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
                v-for="item in productOptions"
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
              style="width: 240px"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="订单ID">
            <el-input
              v-model="queryForm.orderId"
              placeholder="搜索订单ID"
              style="width: 180px"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="queryForm.nickname"
              placeholder="模糊搜索用户名"
              style="width: 200px"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="records" style="width: 100%" v-loading="loading" class="modern-table">
         <el-table-column label="所属产品" width="120">
          <template #default="scope">
            <el-tag 
              size="small" 
              :style="getProductTagStyle(scope.row.appFlag)"
            >
              {{ scope.row.appName || getProductName(scope.row.appFlag) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="阅文订单ID" min-width="220">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.ywOrderId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" min-width="180">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-avatar :size="32" :src="scope.row.avatar">{{ scope.row.nickname?.charAt(0) || 'U' }}</el-avatar>
              <div style="display: flex; flex-direction: column; line-height: 1.2">
                <div style="display: flex; align-items: center; gap: 4px">
                  <span style="font-weight: 500">{{ scope.row.nickname || '-' }}</span>
                  <el-tag v-if="scope.row.sex === 1" size="small" type="primary" effect="plain" style="padding: 0 4px; height: 16px; line-height: 14px">男</el-tag>
                  <el-tag v-else-if="scope.row.sex === 2" size="small" type="danger" effect="plain" style="padding: 0 4px; height: 16px; line-height: 14px">女</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="微信OpenID" min-width="250">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.openid || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="scope">
            <span style="font-weight: 600; color: #f56c6c">¥ {{ scope.row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.orderStatus === 2" type="success" size="small">已支付</el-tag>
            <el-tag v-else-if="scope.row.orderStatus === 1" type="warning" size="small">待支付</el-tag>
            <el-tag v-else type="info" size="small">未知({{ scope.row.orderStatus }})</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderType" label="类型" width="120">
          <template #default="scope">
            <span v-if="scope.row.orderType === 1">充值</span>
            <span v-else-if="scope.row.orderType === 2">年费</span>
            <span v-else-if="scope.row.orderType === 3">道具</span>
            <span v-else>未知({{ scope.row.orderType }})</span>
          </template>
        </el-table-column>
        <el-table-column label="来源信息" min-width="180">
          <template #default="scope">
            <div class="sub-text" v-if="scope.row.channelName">渠道: {{ scope.row.channelName }}</div>
            <div class="sub-text" v-if="scope.row.bookName">书籍: {{ scope.row.bookName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.payTime) }}
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
    <el-dialog
      v-model="syncDialogVisible"
      title="同步充值记录"
      width="550px"
      @closed="resetSyncForm"
    >
      <el-form :model="syncForm" :rules="syncRules" ref="syncFormRef" label-width="100px">
        <el-form-item label="选择产品" prop="appFlag">
          <el-select v-model="syncForm.appFlag" placeholder="请选择要同步的产品" style="width: 100%">
            <el-option
              v-for="item in productOptions"
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
          <div class="tip">若不指定时间，默认同步最近1个月的充值记录</div>
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
import { getRechargeRecords, syncRechargeRecords, getProducts } from '@/api/yuewen'
import { getProductTagStyle } from '@/utils/color'
import { Wallet, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const productOptions = ref<any[]>([])

const queryForm = reactive({
  appFlag: '',
  openid: '',
  nickname: '',
  orderId: ''
})

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

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: page.value,
      size: size.value,
      appFlag: queryForm.appFlag || undefined,
      orderId: queryForm.orderId || undefined
    }
    if (queryForm.openid && queryForm.openid.trim()) {
      params.openid = queryForm.openid.trim()
    }
    if (queryForm.nickname && queryForm.nickname.trim()) {
      params.nickname = queryForm.nickname.trim()
    }
    const res: any = await getRechargeRecords(params)
    records.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('Failed to fetch records', error)
  } finally {
    loading.value = false
  }
}

const fetchProducts = async () => {
  try {
    const res: any = await getProducts({ page: 1, size: 1000 })
    productOptions.value = res.content
  } catch (error) {
    console.error('Failed to fetch products', error)
  }
}

const getProductName = (appFlag: string) => {
  const product = productOptions.value.find(p => p.appFlag === appFlag)
  return product ? product.productName : appFlag
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const resetQuery = () => {
  queryForm.appFlag = ''
  queryForm.openid = ''
  queryForm.nickname = ''
  queryForm.orderId = ''
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
        
        await syncRechargeRecords(data)
        ElMessage.success('同步任务已启动')
        syncDialogVisible.value = false
        setTimeout(fetchData, 1000)
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

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}

onMounted(() => {
  fetchProducts()
  fetchData()
})
</script>

<style scoped>
.recharge-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.recharge-card {
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

.monospace-id {
  font-family: monospace;
  font-size: 13px;
  color: #606266;
}

.sub-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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

.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
