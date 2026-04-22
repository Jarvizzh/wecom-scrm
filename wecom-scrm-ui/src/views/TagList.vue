<template>
  <div class="tag-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="left">
            <el-icon><CollectionTag /></el-icon>
            <span>企业标签管理</span>
          </div>
          <div class="header-ops">
            <el-input
              v-model="groupSearchKeyword"
              placeholder="搜索标签组..."
              clearable
              style="width: 220px; margin-right: 12px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" plain :icon="Refresh" :loading="syncing" @click="handleSync">同步企微标签</el-button>
            <el-button type="primary" :icon="Plus" @click="showAddDialog = true">添加标签</el-button>
          </div>
        </div>
      </template>

      <el-table 
        :data="filteredTagGroups" 
        style="width: 100%" 
        v-loading="loading"
        row-key="groupId"
        default-expand-all
        class="modern-table"
      >
        <el-table-column type="expand">
          <template #default="props">
            <div class="expand-content">
              <div class="expand-header">
                <el-icon><PriceTag /></el-icon>
                <span>标签列表 ({{ props.row.tags?.length || 0 }})</span>
              </div>
              <div class="tag-list">
                <el-tag
                  v-for="tag in props.row.tags"
                  :key="tag.tagId"
                  closable
                  round
                  effect="light"
                  class="pill-tag"
                  @close="handleDeleteTag(tag.tagId)"
                >
                  {{ tag.name }}
                </el-tag>
                <div v-if="!props.row.tags || props.row.tags.length === 0" class="empty-tip">
                  暂无标签
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="标签组名称" min-width="150" sortable />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button type="danger" link @click="handleDeleteGroup(scope.row.groupId)">删除标签组</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAddDialog" title="添加企业标签" width="500px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="标签组名称">
          <el-input v-model="addForm.groupName" placeholder="输入现有组名或新组名" />
        </el-form-item>
        <el-form-item label="标签名称">
          <el-input v-model="addForm.tagName" placeholder="输入新标签名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { Refresh, Plus, CollectionTag, Search, PriceTag } from '@element-plus/icons-vue'
import { getTagGroups, getTagsByGroup, addCorpTag, deleteCorpTag, syncTags } from '@/api/tag'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const syncing = ref(false)
const submitting = ref(false)
const showAddDialog = ref(false)
const tagGroups = ref<any[]>([])
const groupSearchKeyword = ref('')
const addForm = reactive({
  groupName: '',
  tagName: ''
})

const filteredTagGroups = computed(() => {
  if (!groupSearchKeyword.value) return tagGroups.value
  const kw = groupSearchKeyword.value.toLowerCase()
  return tagGroups.value.filter(g => 
    g.groupName?.toLowerCase().includes(kw) || 
    g.groupId?.toLowerCase().includes(kw)
  )
})

const fetchTagGroups = async () => {
  loading.value = true
  try {
    const groups = (await getTagGroups()) as any
    // For each group, fetch its tags
    const groupsWithTags = await Promise.all(
      groups.map(async (g: any) => {
        const tags = (await getTagsByGroup(g.groupId)) as any
        return { ...g, tags }
      })
    )
    tagGroups.value = groupsWithTags
  } catch (error) {
    ElMessage.error('获取标签解析失败')
  } finally {
    loading.value = false
  }
}

const handleSync = async () => {
  syncing.value = true
  try {
    await syncTags()
    ElMessage.success('同步指令已下发，请稍候刷新')
    setTimeout(fetchTagGroups, 2000)
  } catch (error) {
    ElMessage.error('同步失败')
  } finally {
    syncing.value = false
  }
}

const submitAdd = async () => {
  if (!addForm.groupName || !addForm.tagName) {
    return ElMessage.warning('请填写组名和标签名')
  }
  submitting.value = true
  try {
    await addCorpTag(addForm)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    addForm.groupName = ''
    addForm.tagName = ''
    fetchTagGroups()
  } catch (error) {
    ElMessage.error('添加失败')
  } finally {
    submitting.value = false
  }
}

const handleDeleteTag = (tagId: string) => {
  ElMessageBox.confirm('确定删除该标签吗？同步到企微可能需要时间', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCorpTag({ tagId })
    ElMessage.success('删除成功')
    fetchTagGroups()
  })
}

const handleDeleteGroup = (groupId: string) => {
  ElMessageBox.confirm('确定删除整个标签组吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteCorpTag({ groupId })
    ElMessage.success('删除成功')
    fetchTagGroups()
  })
}

onMounted(() => {
  fetchTagGroups()
})
</script>

<style scoped>
.tag-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.el-card {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  font-size: 18px;
  color: #409eff;
}

.header-ops {
  display: flex;
  align-items: center;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8faff;
  color: #606266;
  font-weight: 600;
}

.expand-content {
  padding: 16px 40px;
  background-color: #fafafa;
}

.expand-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #909399;
  font-size: 13px;
  font-weight: 500;
}

.expand-header .el-icon {
  color: #409eff;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pill-tag {
  height: 28px;
  padding: 0 12px;
  border-color: #dcdfe6;
  background-color: #fff;
  transition: all 0.2s;
}

.pill-tag:hover {
  border-color: #409eff;
  color: #409eff;
  background-color: #ecf5ff;
}

.empty-tip {
  color: #c0c4cc;
  font-size: 12px;
  font-style: italic;
}
</style>
