<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>客户朋友圈管理</span>
          <div class="header-actions">
            <el-button :icon="Refresh" :loading="syncing" @click="handleSync">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="$router.push('/moments/create')">创建朋友圈</el-button>
          </div>
        </div>
      </template>

      <!-- Moment Table -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%" stripe>
        <el-table-column prop="id" label="ID" width="50" />
        <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="发表员工" min-width="180">
          <template #default="{ row }">
            <template v-if="!getVisibleRange(row)?.senderList?.length">
              <el-tag type="info" size="small">全部员工</el-tag>
            </template>
            <div v-else class="tag-group">
              <el-tag 
                v-for="uid in getVisibleRange(row).senderList" 
                :key="uid" 
                size="small" 
                class="name-tag"
              >
                {{ getUserName(uid) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="可见客户标签" min-width="180">
          <template #default="{ row }">
            <template v-if="!getVisibleRange(row)?.externalContactList?.tagList?.length">
              <el-tag type="info" size="small">全部客户</el-tag>
            </template>
            <div v-else class="tag-group">
              <el-tag 
                v-for="tid in getVisibleRange(row).externalContactList.tagList" 
                :key="tid" 
                size="small" 
                type="success"
                class="name-tag"
              >
                {{ getTagName(tid) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sendType" label="发送类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.sendType === 1 ? 'warning' : 'success'" size="small">
              {{ row.sendType === 1 ? '定时发送' : '立即发送' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发送时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.sendTime) || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
              type="primary" 
              link 
              :icon="CopyDocument"
              @click="handleCopy(scope.row)"
            >复制</el-button>
            <el-button 
              type="primary" 
              link 
              :icon="Edit"
              v-if="canOperate(scope.row)"
              @click="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button 
              type="danger" 
              link 
              :icon="Delete"
              v-if="canOperate(scope.row)"
              @click="handleDelete(scope.row)"
            >删除</el-button>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMoments, deleteMoment, syncMomentStatuses } from '@/api/moment'
import { getUsers } from '@/api/user'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete, CopyDocument } from '@element-plus/icons-vue'

const router = useRouter()


const tableData = ref([])
const loading = ref(false)
const syncing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const userMap = reactive<Record<string, string>>({})
const tagMap = reactive<Record<string, string>>({})

const fetchMoments = async () => {
  loading.value = true
  try {
    const res = await getMoments({
      page: currentPage.value,
      size: pageSize.value
    }) as any
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const loadBaseData = async () => {
  try {
    const [userRes, tagGroups] = await Promise.all([
      getUsers({ page: 1, size: 1000 }),
      getTagGroups()
    ])
    ;(userRes as any).content?.forEach((u: any) => userMap[u.userid] = u.name)
    
    if (Array.isArray(tagGroups)) {
      for (const g of tagGroups) {
        const tags = await getTagsByGroup(g.groupId) as any
        if (Array.isArray(tags)) {
          tags.forEach((t: any) => tagMap[t.tagId] = t.name)
        }
      }
    }
  } catch (e) {
    console.error('Load base data error:', e)
  }
}

onMounted(() => {
  fetchMoments()
  loadBaseData()
})

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchMoments()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchMoments()
}

const handleSync = async () => {
  syncing.value = true
  try {
    await syncMomentStatuses()
    ElMessage.success('同步请求已提交')
    fetchMoments()
  } catch (e) {
    ElMessage.error('同步失败')
  } finally {
    syncing.value = false
  }
}

const viewDetail = (row: any) => {
  router.push(`/moments/detail/${row.id}`)
}

const handleCopy = (row: any) => {
  router.push(`/moments/copy/${row.id}`)
}

const handleEdit = (row: any) => {
  router.push(`/moments/edit/${row.id}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该朋友圈任务吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteMoment(row.id)
      ElMessage.success('删除成功')
      fetchMoments()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

const canOperate = (row: any) => {
  const now = new Date()
  const sendTime = row.sendTime ? new Date(row.sendTime) : null
  return row.status === 3 && sendTime && sendTime > now
}

const getStatusLabel = (status: number) => {
  const map: any = { 0: '处理中', 1: '已发布', 2: '发布失败', 3: '待发布' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: any = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
  return map[status] || 'info'
}

const getVisibleRange = (row: any) => {
  try {
    return JSON.parse(row.visibleRangeUsers || '{}')
  } catch (e) {
    return {}
  }
}

const getUserName = (uid: string) => userMap[uid] || uid
const getTagName = (tid: string) => tagMap[tid] || tid
const formatDateTime = (val: string) => {
  if (!val) return ''
  return val.replace('T', ' ').substring(0, 19)
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

.header-actions {
  display: flex;
  gap: 10px;
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
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
