<template>
  <div class="recharge-container mp-container">
    <el-card class="recharge-card account-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><Tickets /></el-icon>
            <span class="title">常读充值记录</span>
          </div>
          <div class="right">
            <el-button type="primary" :icon="Refresh" @click="handleSync">同步充值记录</el-button>
          </div>
        </div>
      </template>

      <!-- Filter Section -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryForm" class="filter-form">
          <el-form-item label="产品">
            <el-select v-model="queryForm.distributorId" placeholder="选择产品" clearable style="width: 200px">
              <el-option
                v-for="item in productOptions"
                :key="item.distributorId"
                :label="item.productName"
                :value="item.distributorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="OpenID">
            <el-input
              v-model="queryForm.openId"
              placeholder="搜索微信 OpenID"
              style="width: 240px"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="订单ID">
            <el-input
              v-model="queryForm.tradeNo"
              placeholder="常读订单ID"
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
            <el-tag size="small" effect="plain">{{ getProductName(scope.row.distributorId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单ID" min-width="180">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.tradeNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" min-width="150">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px" v-if="scope.row.nickname || scope.row.avatar">
              <el-avatar :size="32" :src="scope.row.avatar">{{ scope.row.nickname?.charAt(0) }}</el-avatar>
              <span>{{ scope.row.nickname }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="微信OpenID" min-width="250">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.openId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payFee" label="支付金额" width="120">
          <template #default="scope">
            <span style="font-weight: 600; color: #f56c6c">¥ {{ (scope.row.payFee / 100).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderType" label="订单类型" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.orderType === 1" type="info" size="small">虚拟支付</el-tag>
            <el-tag v-else-if="scope.row.orderType === 2" type="success" size="small">非虚拟支付</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payWay" label="支付方式" width="100">
          <template #default="scope">
            <span v-if="scope.row.payWay === '1'">微信</span>
            <span v-else-if="scope.row.payWay === '2'">支付宝</span>
            <span v-else>{{ scope.row.payWay }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rechargeType" label="充值类型" width="120">
          <template #default="scope">
            <span v-if="scope.row.rechargeType === 0">单次充值</span>
            <span v-else-if="scope.row.rechargeType === 1">会员充值</span>
            <span v-else-if="scope.row.rechargeType === 2">整剧购买</span>
            <span v-else-if="scope.row.rechargeType === 3">连包付费</span>
            <span v-else>{{ scope.row.rechargeType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="书籍/推广" min-width="180">
          <template #default="scope">
            <div class="sub-text" v-if="scope.row.bookName">书籍: {{ scope.row.bookName }}</div>
            <div class="sub-text" v-if="scope.row.promotionId">推广ID: {{ scope.row.promotionId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180" />
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
        <el-form-item label="选择产品" prop="distributorId">
          <el-select v-model="syncForm.distributorId" placeholder="请选择要同步的产品" style="width: 100%">
            <el-option
              v-for="item in productOptions"
              :key="item.distributorId"
              :label="item.productName"
              :value="item.distributorId"
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
          <div class="tip">若不指定时间，默认同步最近1年的充值记录</div>
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
import { getChangduRechargeRecords, syncChangduRechargeRecords, getChangduProducts } from '@/api/changdu'
import { Tickets, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const productOptions = ref<any[]>([])

const queryForm = reactive({
  distributorId: undefined as number | undefined,
  openId: '',
  nickname: '',
  tradeNo: undefined as number | undefined
})

const syncDialogVisible = ref(false)
const syncing = ref(false)
const syncFormRef = ref()
const syncForm = reactive({
  distributorId: undefined as number | undefined,
  timeRange: [] as string[]
})

const syncRules = {
  distributorId: [{ required: true, message: '请选择产品', trigger: 'change' }]
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
      distributorId: queryForm.distributorId,
      tradeNo: queryForm.tradeNo
    }
    if (queryForm.openId && queryForm.openId.trim()) {
      params.openId = queryForm.openId.trim()
    }
    if (queryForm.nickname && queryForm.nickname.trim()) {
      params.nickname = queryForm.nickname.trim()
    }
    const res: any = await getChangduRechargeRecords(params)
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
    const res: any = await getChangduProducts({ page: 1, size: 1000 })
    productOptions.value = res.content
  } catch (error) {
    console.error('Failed to fetch products', error)
  }
}

const getProductName = (distributorId: number) => {
  const product = productOptions.value.find(p => p.distributorId === distributorId)
  return product ? product.productName : distributorId
}

const handleSearch = () => {
  page.value = 1
  fetchData()
}

const resetQuery = () => {
  queryForm.distributorId = undefined
  queryForm.openId = ''
  queryForm.nickname = ''
  queryForm.tradeNo = undefined
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
        const params: any = {
          distributorId: syncForm.distributorId!
        }
        if (syncForm.timeRange && syncForm.timeRange.length === 2) {
          params.startTime = syncForm.timeRange[0].replace(' ', 'T')
          params.endTime = syncForm.timeRange[1].replace(' ', 'T')
        }
        
        await syncChangduRechargeRecords(params)
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
  syncForm.distributorId = undefined
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
