<template>
  <div class="enterprise-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>企业管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加企业</el-button>
        </div>
      </template>

      <el-table 
        :data="tableData" 
        style="width: 100%" 
        v-loading="loading"
        :row-class-name="tableRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="企业名称" width="180">
          <template #default="scope">
            <div class="name-container">
              <span>{{ scope.row.name }}</span>
              <el-tag v-if="scope.row.corpId === currentCorpId" size="small" type="success" effect="dark" class="current-tag">当前</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="corpId" label="企业ID(CorpId)" />
        <el-table-column prop="agentId" label="应用ID(AgentId)" width="150" />
        <el-table-column prop="dbUrl" label="数据库地址" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="scope.row.corpId === currentCorpId ? 'success' : 'default'"
              @click="handleSwitch(scope.row.corpId)"
              :disabled="scope.row.corpId === currentCorpId"
            >
              切换
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <EnterpriseFormDialog ref="addDialogRef" @success="handleSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import EnterpriseFormDialog from './EnterpriseFormDialog.vue'

const currentCorpId = ref(localStorage.getItem('currentCorpId') || '')

const loading = ref(false)
const addDialogRef = ref()
const tableData = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/enterprises')
    tableData.value = res as any
  } catch (error) {
    console.error('Fetch error:', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  addDialogRef.value.open()
}

const handleEdit = (row: any) => {
  addDialogRef.value.open(row)
}

const confirmDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除企业 "${row.name}" 吗？`,
    '系统提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      handleDelete(row.id)
    })
    .catch(() => {
      // cancel
    })
}

const handleDelete = async (id: number) => {
  try {
    await request.delete(`/enterprises/${id}`)
    ElMessage.success('企业已成功下线')
    fetchData()
    window.dispatchEvent(new CustomEvent('refresh-enterprises'))
  } catch (err) {
    console.error('Delete error:', err)
  }
}

const handleSwitch = (corpId: string) => {
  localStorage.setItem('currentCorpId', corpId)
  currentCorpId.value = corpId
  ElMessage.success('已成功切换工作空间')
  setTimeout(() => {
    window.location.reload()
  }, 500)
}

const tableRowClassName = ({ row }: { row: any }) => {
  if (row.corpId === currentCorpId.value) {
    return 'current-row-highlight'
  }
  return ''
}

const handleSuccess = () => {
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.enterprise-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.current-row-highlight) {
  background-color: #ecf5ff !important;
}

:deep(.current-row-highlight td.el-table__cell) {
  background-color: #ecf5ff !important;
}

.name-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-tag {
  border-radius: 10px;
  font-weight: bold;
}
</style>
