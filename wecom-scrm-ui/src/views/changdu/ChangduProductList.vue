<template>
  <div class="product-container mp-container">
    <el-card class="box-card account-card">
      <template #header>
        <div class="card-header">
          <div class="header-left left">
            <el-icon><Tickets /></el-icon>
            <span class="title">常读产品管理</span>
          </div>
          <div class="header-actions">
            <el-button type="primary" plain :icon="Refresh" :loading="syncing" @click="handleSync">同步产品</el-button>
            <el-dropdown v-if="selectedIds.length > 0" @command="handleBatchCommand" style="margin-left: 12px">
              <el-button type="primary">
                批量操作 ({{ selectedIds.length }})<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="enable">批量启用</el-dropdown-item>
                  <el-dropdown-item command="disable">批量禁用</el-dropdown-item>
                  <el-dropdown-item command="delete" divided style="color: #F56C6C">批量删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <el-table 
        :data="productList" 
        style="width: 100%" 
        v-loading="loading" 
        class="modern-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="产品信息" min-width="160">
          <template #default="scope">
            <div class="product-info-cell">
              <div class="info-icon">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="product-text">
                <div class="name">{{ scope.row.productName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="distributorId" label="分销ID" min-width="180">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.distributorId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="appId" label="AppID" min-width="130">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.appId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="wxAppId" label="微信AppID" min-width="180">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.wxAppId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="appType" label="应用类型" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.appType === 1" type="info" size="small">快应用</el-tag>
            <el-tag v-else-if="scope.row.appType === 3" type="success" size="small">微信</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="启用状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" round effect="light">
              {{ scope.row.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该产品吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @closed="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" disabled placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="分销ID" prop="distributorId">
          <el-input v-model="form.distributorId" disabled placeholder="请输入分销ID" />
        </el-form-item>
        <el-form-item label="AppID" prop="appId">
          <el-input v-model="form.appId" disabled placeholder="请输入平台生成AppId" />
        </el-form-item>
        <el-form-item label="微信AppID" prop="wxAppId">
          <el-input v-model="form.wxAppId" disabled placeholder="同步产品时自动获取" />
        </el-form-item>
        <el-form-item label="应用类型" prop="appType">
          <el-select v-model="form.appType" disabled placeholder="请选择应用类型" style="width: 100%">
            <el-option label="快应用" :value="1" />
            <el-option label="微信" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, Tickets, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { 
  getChangduProducts, 
  saveChangduProduct, 
  deleteChangduProduct, 
  syncChangduProducts,
  batchUpdateChangduProductStatus,
  batchDeleteChangduProducts,
  type ChangduProduct 
} from '@/api/changdu'

const loading = ref(false)
const submitting = ref(false)
const syncing = ref(false)
const productList = ref<ChangduProduct[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10
})

const selectedIds = ref<number[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive<ChangduProduct>({
  id: undefined,
  productName: '',
  distributorId: 0,
  appId: undefined,
  appType: 1,
  status: 1
})

const rules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  distributorId: [{ required: true, message: '请输入分销ID', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getChangduProducts(queryParams)
    productList.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('Fetch error:', error)
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: ChangduProduct[]) => {
  selectedIds.value = selection.map(item => item.id!)
}

const handleBatchCommand = (command: string) => {
  if (selectedIds.value.length === 0) return

  switch (command) {
    case 'enable':
      handleBatchStatus(1)
      break
    case 'disable':
      handleBatchStatus(0)
      break
    case 'delete':
      handleBatchDelete()
      break
  }
}

const handleBatchStatus = async (status: number) => {
  const actionText = status === 1 ? '启用' : '禁用'
  try {
    await batchUpdateChangduProductStatus(selectedIds.value, status)
    ElMessage.success(`批量${actionText}成功`)
    fetchData()
  } catch (error) {
    console.error('Batch status error:', error)
  }
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确定要批量删除选中的 ${selectedIds.value.length} 个产品吗？此操作不可撤销。`,
    '提示',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await batchDeleteChangduProducts(selectedIds.value)
      ElMessage.success('批量删除成功')
      fetchData()
    } catch (error) {
      console.error('Batch delete error:', error)
    }
  }).catch(() => {})
}

const handleSync = async () => {
  syncing.value = true
  try {
    await syncChangduProducts()
    ElMessage.success('同步成功')
    fetchData()
  } catch (error) {
    console.error('Sync error:', error)
  } finally {
    syncing.value = false
  }
}

const handleEdit = (row: ChangduProduct) => {
  dialogTitle.value = '编辑常读产品'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row: ChangduProduct) => {
  try {
    await deleteChangduProduct(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('Delete failed', error)
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await saveChangduProduct(form)
        ElMessage.success(form.id ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('Submit failed', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  Object.assign(form, {
    id: undefined,
    productName: '',
    distributorId: 0,
    appId: undefined,
    appType: 1,
    status: 1
  })
  if (formRef.value) formRef.value.resetFields()
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.product-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.account-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
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

.product-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.info-icon {
  width: 40px;
  height: 40px;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.product-text .name {
  font-weight: 600;
  color: #303133;
}

.product-text .sub-text {
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

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
