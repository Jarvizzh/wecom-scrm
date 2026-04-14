<template>
  <div class="user-container">
    <el-row :gutter="20">
      <!-- Left side: Department Tree -->
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card class="dept-card">
          <template #header>
            <div class="card-header">
              <div class="left">
                <el-icon><Management /></el-icon>
                <span>组织架构</span>
              </div>
            </div>
          </template>
          <el-input
            v-model="deptFilterText"
            placeholder="搜索部门"
            clearable
            style="margin-bottom: 20px"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-button 
            :type="currentDepartmentId === undefined ? 'primary' : ''" 
            class="all-users-btn"
            @click="handleViewAll"
          >
            <el-icon><UserFilled /></el-icon>
            <span>查看全部成员</span>
          </el-button>

          <el-tree
            ref="treeRef"
            :data="departmentTree"
            :props="defaultProps"
            node-key="departmentId"
            :filter-node-method="filterNode"
            :expand-on-click-node="false"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
            class="dept-tree"
          >
            <template #default="{ node }">
              <span class="custom-tree-node">
                <el-icon class="dept-icon"><Folder /></el-icon>
                <span class="dept-name">{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>
      <!-- Right side: User Table -->
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card class="user-card">
          <template #header>
            <div class="card-header">
              <div class="left">
                <el-icon><UserFilled /></el-icon>
                <span>员工列表</span>
              </div>
              <el-button 
                type="primary"
                plain
                :icon="Refresh"
                :loading="syncing" 
                @click="handleSync"
              >
                同步企业通讯录
              </el-button>
            </div>
          </template>

          <!-- Search bar -->
          <div class="search-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索姓名/账号" 
              style="width: 250px" 
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
              </template>
            </el-input>
          </div>

          <!-- Table list -->
          <el-table 
            v-loading="loading" 
            :data="tableData" 
            style="width: 100%" 
            class="modern-table"
          >
            <el-table-column prop="userid" label="员工信息" min-width="200">
              <template #default="scope">
                <div class="user-info-cell">
                  <el-avatar :size="40" :src="scope.row.avatar" class="user-avatar" :icon="UserFilled" />
                  <div class="user-text">
                    <div class="name">{{ scope.row.name }}</div>
                    <div class="id">账号: {{ scope.row.userid }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="departmentIds" label="所属部门" class-name="hidden-sm-and-down">
              <template #default="scope">
                {{ getDepartmentNames(scope.row.departmentIds) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" effect="light" round class="status-pill">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scrmStatus" label="SCRM状态" width="110">
              <template #default="scope">
                <el-switch
                  v-model="scope.row.scrmStatus"
                  :active-value="0"
                  :inactive-value="1"
                  active-text="正常"
                  inactive-text="封禁"
                  inline-prompt
                  @change="handleStatusChange(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="同步时间" width="160" class-name="hidden-md-and-down">
              <template #default="scope">
                {{ formatDateTime(scope.row.createTime) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-block">
            <el-pagination 
              background 
              layout="total, sizes, prev, pager, next" 
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElTree } from 'element-plus'
import { Search, Refresh, Folder, UserFilled, Management } from '@element-plus/icons-vue'
import { getUsers, getDepartments, updateUserStatus, syncUsers } from '../api/user'

const tableData = ref([])
const loading = ref(false)
const syncing = ref(false)

// Pagination & Search
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// Departments
const departmentList = ref<any[]>([])
const departmentTree = ref<any[]>([])
const deptFilterText = ref('')
const currentDepartmentId = ref<number | undefined>(undefined)
const treeRef = ref<InstanceType<typeof ElTree>>()
const departmentMap = ref<Record<number, string>>({})

const defaultProps = {
  children: 'children',
  label: 'name',
}

// Fetch departments and build tree
const fetchDepartments = async () => {
  try {
    const res: any = await getDepartments()
    departmentList.value = res || []
    
    // Build map for quick lookup
    departmentList.value.forEach(d => {
      departmentMap.value[d.departmentId] = d.name
    })

    // Build standard tree
    departmentTree.value = buildTree(departmentList.value)
  } catch (error) {
    console.error('Failed to fetch departments:', error)
  }
}

const buildTree = (departments: any[]) => {
  const nodeMap = new Map()
  departments.forEach(item => {
    nodeMap.set(item.departmentId, { ...item, children: [] })
  })

  const tree: any[] = []
  departments.forEach(item => {
    const node = nodeMap.get(item.departmentId)
    if (item.parentId === 0 || item.parentId === null) {
      tree.push(node)
    } else {
      const parentNode = nodeMap.get(item.parentId)
      if (parentNode) {
        parentNode.children.push(node)
      } else {
        tree.push(node)
      }
    }
  })
  return tree
}

// Tree node filter
watch(deptFilterText, (val) => {
  treeRef.value!.filter(val)
})

const filterNode = (value: string, data: any) => {
  if (!value) return true
  return data.name.includes(value)
}

const handleNodeClick = (data: any) => {
  currentDepartmentId.value = data.departmentId
  currentPage.value = 1
  fetchUsers()
}

const handleViewAll = () => {
  currentDepartmentId.value = undefined
  treeRef.value?.setCurrentKey(null as any)
  currentPage.value = 1
  fetchUsers()
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res: any = await getUsers({
      departmentId: currentDepartmentId.value,
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    tableData.value = res.content || []
    total.value = res.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch users:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchDepartments()
  await fetchUsers()
})

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchUsers()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUsers()
}

const handleSync = async () => {
  syncing.value = true
  try {
    await syncUsers()
    ElMessage.success('已触发后台同步通讯录任务，请稍后刷新查看最新数据')
    setTimeout(() => {
      fetchUsers()
    }, 2000)
  } catch (error) {
    console.error('Failed to sync users:', error)
  } finally {
    syncing.value = false
  }
}

const handleStatusChange = async (row: any) => {
  try {
    // 0: Normal, 1: Banned
    await updateUserStatus(row.userid, row.scrmStatus)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('Failed to update status:', error)
    // Revert status on failure
    row.scrmStatus = row.scrmStatus === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const getDepartmentNames = (deptIdsStr: string) => {
  if (!deptIdsStr) return '-'
  try {
    const ids = JSON.parse(deptIdsStr)
    if (Array.isArray(ids)) {
      return ids.map(id => departmentMap.value[id] || id).join(', ')
    }
  } catch (e) {
    // not a JSON array
  }
  return deptIdsStr
}

const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'danger'
    case 4: return 'warning'
    case 5: return 'info'
    default: return ''
  }
}

const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '已激活'
    case 2: return '已禁用'
    case 4: return '未激活'
    case 5: return '退出企业'
    default: return '未知'
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.user-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

@media (max-width: 768px) {
  .user-container {
    padding: 12px;
  }
  .dept-card {
    margin-bottom: 20px;
  }
}

.dept-card, .user-card {
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

.dept-tree {
  margin: 0 -10px;
  max-height: 600px;
  overflow-y: auto;
}

.all-users-btn {
  width: 100%;
  margin-bottom: 12px;
  justify-content: flex-start;
  padding-left: 12px;
}

.dept-tree :deep(.el-tree-node__content) {
  height: 40px;
  border-radius: 4px;
  margin: 2px 0;
}

.dept-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff !important;
  color: #409eff;
  font-weight: 600;
  border-left: 3px solid #409eff;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.dept-icon {
  color: #e6a23c;
  font-size: 16px;
}

.search-bar {
  margin-bottom: 24px;
  display: flex;
  gap: 16px;
}

.modern-table :deep(.el-table__header) th {
  background-color: #f8faff;
  color: #606266;
  font-weight: 600;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.user-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-text .name {
  font-weight: 600;
  color: #303133;
}

.user-text .id {
  font-size: 12px;
  color: #909399;
}

.status-pill {
  padding: 0 10px;
  height: 24px;
}

.pagination-block {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>
