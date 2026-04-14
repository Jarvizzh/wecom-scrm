<template>
  <div class="mp-container">
    <el-card class="account-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><Monitor /></el-icon>
            <span>公众号列表</span>
          </div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">添加公众号</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="accounts" style="width: 100%" class="modern-table">
        <el-table-column prop="name" label="公众号信息">
          <template #default="scope">
            <div class="account-info-cell">
              <div class="info-icon">
                <el-icon><ChatLineRound /></el-icon>
              </div>
              <div class="account-text">
                <div class="name">{{ scope.row.name }}</div>
                <div class="appid">AppId: {{ scope.row.appId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="自动同步用户" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" round effect="light">
              {{ scope.row.status === 1 ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" :icon="DocumentCopy" @click="handleOpenCopy(scope.row)">复制</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加公众号' : '编辑公众号'"
      width="500px"
      append-to-body
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="padding: 20px 20px 0">
        <el-form-item label="公众号名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入公众号名称" />
        </el-form-item>
        <el-form-item label="AppID" prop="appId">
          <el-input v-model="form.appId" placeholder="请输入微信公众号 AppId" />
        </el-form-item>
        <el-form-item label="Secret" prop="secret">
          <el-input v-model="form.secret" type="password" show-password placeholder="请输入公众号 AppSecret" />
        </el-form-item>
        <el-form-item label="自动同步用户" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Copy to Enterprise Dialog -->
    <el-dialog
      v-model="copyDialogVisible"
      title="复制公众号配置"
      width="450px"
      append-to-body
    >
      <div style="padding: 10px 20px">
        <p style="margin-bottom: 20px; color: #606266;">
          您可以将 <strong>{{ currentAccount?.name }}</strong> 的配置同步到其他企业。同步后，目标企业将直接拥有该公众号的 AppID 和 Secret 权限。
        </p>
        <el-form label-position="top">
          <el-form-item label="目标企业" required>
            <el-select 
              v-model="targetCorpId" 
              placeholder="请选择目标企业" 
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="item in availableEnterprises"
                :key="item.corpId"
                :label="item.name"
                :value="item.corpId"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="copyDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="copying" :disabled="!targetCorpId" @click="submitCopy">
            确定复制
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Monitor, ChatLineRound, DocumentCopy } from '@element-plus/icons-vue'
import { getMpAccounts, saveMpAccount, deleteMpAccount, copyMpAccount } from '../../api/mp'
import { getEnterprises } from '../../api/enterprise'

const loading = ref(false)
const submitting = ref(false)
const accounts = ref([])

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref()
const form = ref({
  id: null,
  name: '',
  appId: '',
  secret: '',
  status: 1
})

// Copy Logic
const copyDialogVisible = ref(false)
const copying = ref(false)
const currentAccount = ref<any>(null)
const targetCorpId = ref('')
const availableEnterprises = ref<any[]>([])

const handleOpenCopy = async (row: any) => {
  currentAccount.value = row
  targetCorpId.value = ''
  copyDialogVisible.value = true
  
  try {
    const res: any = await getEnterprises()
    const currentCorpId = localStorage.getItem('currentCorpId')
    // Filter out current enterprise
    availableEnterprises.value = (res || []).filter((e: any) => e.corpId !== currentCorpId)
  } catch (error) {
    console.error('Failed to fetch enterprises:', error)
  }
}

const submitCopy = async () => {
  if (!targetCorpId.value) return
  
  copying.value = true
  try {
    await copyMpAccount({
      appId: currentAccount.value.appId,
      targetCorpId: targetCorpId.value
    })
    ElMessage.success('公众号配置已成功复制到目标企业')
    copyDialogVisible.value = false
  } catch (error) {
    console.error('Failed to copy account:', error)
  } finally {
    copying.value = false
  }
}

const rules = {
  name: [{ required: true, message: '请输入公众号名称', trigger: 'blur' }],
  appId: [{ required: true, message: '请输入 AppId', trigger: 'blur' }],
  secret: [{ required: true, message: '请输入 AppSecret', trigger: 'blur' }]
}

const fetchAccounts = async () => {
  loading.value = true
  try {
    const res: any = await getMpAccounts()
    accounts.value = res || []
  } catch (error) {
    console.error('Failed to fetch accounts:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogType.value = 'add'
  form.value = { id: null, name: '', appId: '', secret: '', status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await saveMpAccount(form.value)
        ElMessage.success(dialogType.value === 'add' ? '添加成功' : '修改成功')
        dialogVisible.value = false
        fetchAccounts()
      } catch (error) {
        console.error('Failed to save account:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该公众号配置吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMpAccount(row.id)
      ElMessage.success('删除成功')
      fetchAccounts()
    } catch (error) {
      console.error('Failed to delete account:', error)
    }
  })
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchAccounts()
})
</script>

<style scoped>
.mp-container {
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

.account-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.info-icon {
  width: 40px;
  height: 40px;
  background-color: #f0f9eb;
  color: #67c23a;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.account-text .name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.account-text .appid {
  font-size: 12px;
  color: #909399;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8faff;
  color: #606266;
  font-weight: 600;
}

.dialog-footer {
  padding-bottom: 10px;
}
</style>
