<template>
  <div class="sys-user-container">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><Setting /></el-icon>
            <span>系统用户管理</span>
          </div>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" style="width: 100%" class="modern-table">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="isSuperAdmin" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.isSuperAdmin ? 'danger' : 'success'">
              {{ scope.row.isSuperAdmin ? '超级管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="0"
              :inactive-value="1"
              @change="handleStatusChange(scope.row)"
              :disabled="scope.row.username === 'admin'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPassword(scope.row)">重置密码</el-button>
            <el-button 
              link 
              type="danger" 
              @click="handleDelete(scope.row)"
              :disabled="scope.row.username === 'admin'"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="超级管理员" prop="isSuperAdmin">
          <el-checkbox v-model="form.isSuperAdmin" :disabled="form.username === 'admin'">是</el-checkbox>
        </el-form-item>
        <el-form-item v-if="!form.isSuperAdmin" label="菜单权限">
          <el-checkbox-group v-model="form.permissions">
            <el-row>
              <el-col v-for="route in allRoutes" :key="route.path" :span="12">
                <el-checkbox :label="route.path">{{ route.label }}</el-checkbox>
              </el-col>
            </el-row>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- Password Reset Dialog -->
    <el-dialog v-model="pwdDialogVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="80px">
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPassword" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Plus } from '@element-plus/icons-vue'
import { getSysUsers, createSysUser, updateSysUser, updateSysUserPassword, deleteSysUser } from '@/api/sysUser'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const allRoutes = [
  { path: '/dashboard', label: '首页看板' },
  { path: '/users', label: '企微账号' },
  { path: '/customers', label: '客户管理' },
  { path: '/customer-groups', label: '客户群管理' },
  { path: '/tags', label: '标签管理' },
  { path: '/welcome-message', label: '客户欢迎语' },
  { path: '/moments', label: '客户朋友圈' },
  { path: '/customer-messages', label: '客户群发' },
  { path: '/group-messages', label: '客户群群发' },
  { path: '/mp-accounts', label: '公众号管理' },
  { path: '/mp-users', label: '公众号用户' },
  { path: '/yuewen-products', label: '阅文产品管理' },
  { path: '/yuewen-users', label: '阅文用户列表' },
  { path: '/yuewen-recharge', label: '阅文充值记录' },
  { path: '/yuewen-consume', label: '阅文消费记录' },
  { path: '/changdu-products', label: '常读产品管理' },
  { path: '/changdu-users', label: '常读用户列表' },
  { path: '/changdu-recharge', label: '常读充值记录' },
  { path: '/sync-logs', label: '同步日志' },
  { path: '/wecom-events', label: '回调日志' },
  { path: '/enterprises', label: '企业管理' }
]

const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  isSuperAdmin: false,
  status: 0,
  permissions: [] as string[]
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const pwdDialogVisible = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({
  id: null as any,
  password: ''
})
const pwdRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }]
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await getSysUsers()
    tableData.value = res || []
  } catch (error) {
    console.error('Failed to fetch sys users:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    avatar: '',
    isSuperAdmin: false,
    status: 0,
    permissions: []
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, {
    ...row,
    permissions: row.permissions ? JSON.parse(row.permissions) : []
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        const data = {
          ...form,
          permissions: JSON.stringify(form.permissions)
        }
        if (isEdit.value) {
          await updateSysUser(data)
          ElMessage.success('更新成功')
        } else {
          await createSysUser(data)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchUsers()
      } catch (error) {
        console.error('Failed to submit user:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleStatusChange = async (row: any) => {
  try {
    await updateSysUser({
      ...row,
      permissions: row.permissions // Already a string from API or handled in update
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 0 ? 1 : 0
  }
}

const handleResetPassword = (row: any) => {
  pwdForm.id = row.id
  pwdForm.password = ''
  pwdDialogVisible.value = true
}

const submitPassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitting.value = true
      try {
        await updateSysUserPassword(pwdForm.id, { password: pwdForm.password })
        ElMessage.success('密码重置成功')
        pwdDialogVisible.value = false
      } catch (error) {
        console.error('Failed to reset password:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSysUser(row.id)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (error) {
      console.error('Failed to delete user:', error)
    }
  })
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.sys-user-container {
  padding: 24px;
}

.user-card {
  border-radius: 8px;
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
  font-weight: 600;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8faff;
}
</style>
