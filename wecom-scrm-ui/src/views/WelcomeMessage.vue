<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>客户欢迎语</span>
          <el-button type="primary" :icon="Plus" @click="$router.push('/welcome-message/create')">新建欢迎语</el-button>
        </div>
      </template>

      <el-table :data="welcomeMsgs" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="text" label="欢迎语内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="默认" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isDefault === 1 ? 'success' : 'info'">
              {{ row.isDefault === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sysUpdateTime" label="最后更新" width="180">
          <template #default="{ row }">
            {{ formatTime(row.sysUpdateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="$router.push('/welcome-message/edit/' + row.id)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-block">
        <el-pagination 
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
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
import { getWelcomeMsgs, deleteWelcomeMsg } from '@/api/welcomeMsg'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const welcomeMsgs = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(async () => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getWelcomeMsgs({
      page: currentPage.value,
      size: pageSize.value
    }) as any
    if (res && res.content) {
      welcomeMsgs.value = res.content
      total.value = res.totalElements
    } else {
      welcomeMsgs.value = res || []
      total.value = welcomeMsgs.value.length
    }
  } catch (e) {
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该欢迎语吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteWelcomeMsg(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const formatTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-block {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
