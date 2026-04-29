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
        <el-table-column label="发送员工" min-width="180">
          <template #default="{ row }">
            <template v-if="!row.userIds || row.userIds === '[]' || row.userIds === ''">
              <el-tag type="info" size="small">全部员工</el-tag>
            </template>
            <div v-else class="tag-group">
              <el-tag 
                v-for="name in getUserNames(row.userIds)" 
                :key="name" 
                size="small" 
                class="name-tag"
              >
                {{ name }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="自动打标签" min-width="180">
          <template #default="{ row }">
            <div v-if="row.tagIds && row.tagIds !== '[]'" class="tag-group">
              <el-tag 
                v-for="name in getTagNames(row.tagIds)" 
                :key="name" 
                type="success" 
                size="small" 
                class="name-tag"
              >
                {{ name }}
              </el-tag>
            </div>
            <span v-else style="color: #909399; font-size: 12px;">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="text" label="欢迎语内容" min-width="250" show-overflow-tooltip />
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
import { getUsers } from '@/api/user'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const welcomeMsgs = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userMap = ref<Map<string, string>>(new Map())
const tagMap = ref<Map<string, string>>(new Map())

onMounted(async () => {
  await Promise.all([fetchData(), loadUsers(), loadTags()])
})

const loadUsers = async () => {
  try {
    const res = await getUsers({ page: 1, size: 1000 }) as any
    const list = res.content || []
    list.forEach((u: any) => userMap.value.set(u.userid, u.name))
  } catch (e) {
    console.error('加载用户失败', e)
  }
}

const loadTags = async () => {
  try {
    const groups = (await getTagGroups()) as any
    if (Array.isArray(groups)) {
      for (const g of groups) {
        const tags = (await getTagsByGroup(g.groupId)) as any
        if (Array.isArray(tags)) {
          tags.forEach(t => tagMap.value.set(t.tagId, t.name))
        }
      }
    }
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

const getUserNames = (userIdsJson: string) => {
  try {
    const ids = JSON.parse(userIdsJson)
    return ids.map((id: string) => userMap.value.get(id) || id)
  } catch (e) {
    return []
  }
}

const getTagNames = (tagIdsJson: string) => {
  try {
    const ids = JSON.parse(tagIdsJson)
    return ids.map((id: string) => tagMap.value.get(id) || id)
  } catch (e) {
    return []
  }
}

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

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.name-tag {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
