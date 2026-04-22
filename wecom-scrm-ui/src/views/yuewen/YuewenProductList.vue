<template>
  <div class="product-container mp-container">
    <el-card class="box-card account-card">
      <template #header>
        <div class="card-header">
          <div class="header-left left">
            <el-icon><Tickets /></el-icon>
            <span class="title">阅文产品管理</span>
          </div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增产品</el-button>
        </div>
      </template>

      <el-table :data="products" style="width: 100%" v-loading="loading" class="modern-table">
        <el-table-column label="产品信息" width="200">
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
        <el-table-column prop="appFlag" label="AppFlag" min-width="150">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.appFlag }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="wxAppId" label="微信AppID" min-width="180">
          <template #default="scope">
            <span class="monospace-id">{{ scope.row.wxAppId }}</span>
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
        <el-table-column label="操作" width="150" fixed="right" align="center">
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
          v-model:current-page="page"
          v-model:page-size="size"
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
      :title="form.id ? '编辑产品' : '新增产品'"
      width="500px"
      @closed="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="AppFlag" prop="appFlag">
          <el-input v-model="form.appFlag" placeholder="请输入阅文分配的APPFLAG" />
        </el-form-item>
        <el-form-item label="微信AppID" prop="wxAppId">
          <el-input v-model="form.wxAppId" placeholder="请输入微信公众号AppId" />
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
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getProducts, saveProduct, deleteProduct, type YuewenProduct } from '@/api/yuewen'
import { ElMessage } from 'element-plus'
import { Plus, Reading } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const products = ref<YuewenProduct[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  productName: '',
  wxAppId: '',
  appFlag: '',
  status: 1
})

const rules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  wxAppId: [{ required: true, message: '请输入微信公众号AppId', trigger: 'blur' }],
  appFlag: [{ required: true, message: '请输入APPFLAG', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getProducts({ page: page.value, size: size.value })
    products.value = res.content
    total.value = res.totalElements
  } catch (error) {
    console.error('Failed to fetch products', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    productName: row.productName,
    wxAppId: row.wxAppId,
    appFlag: row.appFlag,
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('Delete failed', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await saveProduct(form)
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
    id: null,
    productName: '',
    wxAppId: '',
    appFlag: '',
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

.tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
