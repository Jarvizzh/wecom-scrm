<template>
  <div class="moment-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>客户朋友圈管理</span>
          <div class="header-actions">
            <el-button :icon="Refresh" :loading="syncing" @click="handleSync">同步状态</el-button>
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">创建朋友圈</el-button>
          </div>
        </div>
      </template>

      <!-- Moment Table -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="text" label="内容" min-width="180" show-overflow-tooltip />
        <el-table-column label="附件" width="120">
          <template #default="scope">
            <template v-if="getAttachments(scope.row).length > 0">
              <el-tag 
                v-for="(att, idx) in getAttachments(scope.row)" 
                :key="idx" 
                size="small" 
                type="warning"
                style="margin-bottom: 2px"
              >
                {{ getAttachmentLabel(att.msgtype) }}
              </el-tag>
            </template>
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column label="发表员工" min-width="150">
          <template #default="scope">
            <template v-if="getVisibleRange(scope.row)?.senderList?.length > 0">
              <el-tag 
                v-for="uid in getVisibleRange(scope.row).senderList" 
                :key="uid" 
                size="small" 
                style="margin-right: 5px"
              >
                {{ getUserName(uid) }}
              </el-tag>
            </template>
            <span v-else>全部员工</span>
          </template>
        </el-table-column>
        <el-table-column label="可见客户标签" min-width="150">
          <template #default="scope">
            <template v-if="getVisibleRange(scope.row)?.externalContactList?.tagList?.length > 0">
              <el-tag 
                v-for="tid in getVisibleRange(scope.row).externalContactList.tagList" 
                :key="tid" 
                size="small" 
                type="success"
                style="margin-right: 5px"
              >
                {{ getTagName(tid) }}
              </el-tag>
            </template>
            <span v-else>全部客户</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">详情</el-button>
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

      <!-- Create Moment Dialog -->
      <el-dialog v-model="createDialogVisible" title="创建朋友圈任务" :width="isMobile ? '95%' : '600px'" destroy-on-close>
        <el-form :model="momentForm" label-width="100px">
          <el-form-item label="内容">
            <el-input v-model="momentForm.text" type="textarea" :rows="4" placeholder="请输入朋友圈文字内容 (非必填)" />
          </el-form-item>

          <el-form-item label="附件类型">
            <el-radio-group v-model="attachmentType">
              <el-radio label="none">无附件</el-radio>
              <el-radio label="image">图片</el-radio>
              <el-radio label="link">链接</el-radio>
              <el-radio label="miniprogram">小程序</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- Image Attachment -->
          <div v-if="attachmentType === 'image'">
            <el-form-item label="上传图片" required>
              <el-upload
                class="avatar-uploader"
                action=""
                :http-request="uploadImage"
                :show-file-list="false"
              >
                <img v-if="imageForm.picUrl" :src="imageForm.picUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </div>

          <!-- Link Attachment -->
          <div v-if="attachmentType === 'link'">
            <el-form-item label="链接标题" required>
              <el-input v-model="linkForm.title" placeholder="请输入链接标题" />
            </el-form-item>
            <el-form-item label="链接地址" required>
              <el-input v-model="linkForm.url" placeholder="请输入链接 URL" />
            </el-form-item>
            <el-form-item label="链接描述">
              <el-input v-model="linkForm.desc" placeholder="请输入链接描述" />
            </el-form-item>
            <el-form-item label="封面图片">
              <el-upload
                class="avatar-uploader"
                action=""
                :http-request="uploadLinkCover"
                :show-file-list="false"
              >
                <img v-if="linkForm.picUrl" :src="linkForm.picUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </div>

          <!-- MiniProgram Attachment -->
          <div v-if="attachmentType === 'miniprogram'">
            <el-form-item label="小程序标题" required>
              <el-input v-model="mpForm.title" placeholder="请输入小程序消息标题" />
            </el-form-item>
            <el-form-item label="AppID" required>
              <el-input v-model="mpForm.appid" placeholder="请输入小程序 AppID" />
            </el-form-item>
            <el-form-item label="页面路径" required>
              <el-input v-model="mpForm.page" placeholder="请输入小程序页面路径" />
            </el-form-item>
            <el-form-item label="封面图片" required>
              <el-upload
                class="avatar-uploader"
                action=""
                :http-request="uploadMpCover"
                :show-file-list="false"
              >
                <img v-if="mpForm.picUrl" :src="mpForm.picUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </div>

          <el-form-item label="发表员工">
            <el-tree-select
              v-model="momentForm.visibleRange.senderList"
              :data="departmentUserTree"
              multiple
              show-checkbox
              filterable
              node-key="id"
              placeholder="选择负责发表的员工 (查部门或员工标题)"
              style="width: 100%"
              :props="{ label: 'label', children: 'children' }"
            >
              <template #default="{ data }">
                <div class="tree-node-content" :class="data.isUser ? 'user-node' : 'dept-node'">
                  <el-icon class="node-icon">
                    <UserFilled v-if="data.isUser" />
                    <Folder v-else />
                  </el-icon>
                  <span class="node-label">{{ data.label }}</span>
                </div>
              </template>
            </el-tree-select>
          </el-form-item>

          <el-form-item label="可见客户标签">
            <el-tree-select
              v-model="momentForm.visibleRange.tagList"
              :data="tagTree"
              multiple
              show-checkbox
              filterable
              node-key="id"
              placeholder="选择允许看见此朋友圈的客户标签 (不选则所有人可见)"
              style="width: 100%"
              :props="{ label: 'label', children: 'children' }"
            >
              <template #default="{ data }">
                <div class="tree-node-content" :class="data.isGroup ? 'group-node' : 'tag-node'">
                  <el-icon class="node-icon">
                    <Collection v-if="data.isGroup" />
                    <PriceTag v-else />
                  </el-icon>
                  <span class="node-label">{{ data.label }}</span>
                </div>
              </template>
            </el-tree-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreate" :loading="creating">发布任务</el-button>
        </template>
      </el-dialog>

      <!-- Records Dialog -->
      <el-dialog v-model="recordsDialogVisible" title="发布明细" :width="isMobile ? '95%' : '700px'">
        <el-table :data="recordData" stripe border>
          <el-table-column prop="userid" label="员工ID" width="150" />
          <el-table-column prop="publishStatus" label="发布状态" width="150" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.publishStatus === 1 ? 'success' : 'info'">
                {{ scope.row.publishStatus === 1 ? '已发布' : '未发布' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="publishTime" label="发布时间" min-width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.publishTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- Detail Dialog -->
      <el-dialog v-model="detailDialogVisible" title="朋友圈详情" :width="isMobile ? '95%' : '600px'">
        <div v-if="currentMoment" class="detail-view">
          <div class="detail-item">
            <div class="label">任务 ID:</div>
            <div class="value">{{ currentMoment.momentId || '未同步' }}</div>
          </div>
          <div class="detail-item">
            <div class="label">文字内容:</div>
            <div class="value text-content">{{ currentMoment.text || '(无文字内容)' }}</div>
          </div>
          <div class="detail-item">
            <div class="label">附件详情:</div>
            <div class="value">
              <template v-if="getAttachments(currentMoment).length > 0">
                <div v-for="(att, idx) in getAttachments(currentMoment)" :key="idx" class="attachment-box">
                  <el-tag size="small" type="warning" class="mb-5">{{ getAttachmentLabel(att.msgtype) }}</el-tag>
                  
                  <div v-if="att.msgtype === 'link' && att.link" class="detail-info">
                    <div class="info-row"><strong>标题:</strong> {{ att.link.title }}</div>
                    <div class="info-row"><strong>地址:</strong> {{ att.link.url }}</div>
                    <div class="info-row"><strong>描述:</strong> {{ att.link.desc }}</div>
                  </div>

                  <div v-if="att.msgtype === 'miniprogram' && att.miniprogram" class="detail-info">
                    <div class="info-row"><strong>标题:</strong> {{ att.miniprogram.title }}</div>
                    <div class="info-row"><strong>AppID:</strong> {{ att.miniprogram.appid }}</div>
                    <div class="info-row"><strong>页面:</strong> {{ att.miniprogram.page }}</div>
                  </div>

                  <div v-if="att.msgtype === 'image' && att.image" class="detail-info">
                    <div class="info-row"><strong>MediaID:</strong> {{ att.image.mediaId }}</div>
                  </div>
                </div>
              </template>
              <span v-else>无附件</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="label">发表员工:</div>
            <div class="value">
              <el-tag v-for="uid in getVisibleRange(currentMoment)?.senderList" :key="uid" size="small" class="mr-5">
                {{ getUserName(uid) }}
              </el-tag>
              <span v-if="!getVisibleRange(currentMoment)?.senderList?.length">全部员工</span>
            </div>
          </div>
          <div class="detail-item">
            <div class="label">可见标签:</div>
            <div class="value">
              <el-tag v-for="tid in getVisibleRange(currentMoment)?.externalContactList?.tagList" :key="tid" size="small" type="success" class="mr-5">
                {{ getTagName(tid) }}
              </el-tag>
              <span v-if="!getVisibleRange(currentMoment)?.externalContactList?.tagList?.length">全部客户</span>
            </div>
          </div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getMoments, createMoment, syncMomentStatuses } from '../api/moment'
import { uploadMedia } from '../api/media'
import { getUsers, getDepartments } from '../api/user'
import { getTagGroups, getTagsByGroup } from '../api/tag'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, Folder, UserFilled, Collection, PriceTag } from '@element-plus/icons-vue'
import { useResponsive } from '../hooks/useResponsive'

const { isMobile } = useResponsive()

const tableData = ref([])
const loading = ref(false)
const syncing = ref(false)
const creating = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const createDialogVisible = ref(false)
const recordsDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentMoment = ref<any>(null)
const recordData = ref([])
const allUsers = ref<any[]>([])
const allDepartments = ref<any[]>([])
const departmentUserTree = ref<any[]>([])
const tagTree = ref<any[]>([])

const momentForm = reactive({
  text: '',
  visibleRange: {
    senderList: [] as string[],
    tagList: [] as string[]
  }
})

const attachmentType = ref('none')

const imageForm = reactive({
  mediaId: '',
  picUrl: ''
})

const linkForm = reactive({
  title: '',
  url: '',
  desc: '',
  picUrl: '',
  mediaId: ''
})

const mpForm = reactive({
  title: '',
  appid: '',
  page: '',
  picUrl: '',
  mediaId: ''
})

const fetchMoments = async () => {
  loading.value = true
  try {
    const res = await getMoments({
      page: currentPage.value,
      size: pageSize.value
    }) as any
    if (res && res.content) {
      tableData.value = res.content
      total.value = res.totalElements
    } else {
      tableData.value = res || []
      total.value = tableData.value.length
    }
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchMoments()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchMoments()
}

const fetchDepartmentsAndUsers = async () => {
  try {
    const [deptRes, userRes] = await Promise.all([
      getDepartments(),
      getUsers({ page: 1, size: 1000 })
    ])
    allDepartments.value = deptRes as any || []
    allUsers.value = (userRes as any).content || []
    departmentUserTree.value = buildDepartmentUserTree(allDepartments.value, allUsers.value)
  } catch (error) {
    console.error('Failed to fetch departments and users', error)
  }
}

const fetchTagGroups = async () => {
  try {
    const groups = await getTagGroups() as any
    const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
       const tags = await getTagsByGroup(g.groupId) as any
       return { ...g, tags }
    }))
    
    tagTree.value = groupsWithTags.map(g => ({
      id: `group_${g.groupId}`,
      label: g.groupName,
      isGroup: true,
      children: (g.tags || []).map((t: any) => ({
        id: `tag_${t.tagId}`,
        tagId: t.tagId,
        label: t.name,
        isGroup: false
      }))
    }))
  } catch (error) {
    console.error('Failed to fetch Tags', error)
  }
}

const buildDepartmentUserTree = (departments: any[], users: any[]) => {
  const nodeMap = new Map()
  departments.forEach(dept => {
    nodeMap.set(`dept_${dept.departmentId}`, {
      id: `dept_${dept.departmentId}`,
      label: dept.name,
      children: []
    })
  })
  
  users.forEach(user => {
    const userNode = { label: user.name, isUser: true }
    if (user.departmentIds) {
      try {
        const deptIds = JSON.parse(user.departmentIds)
        if (Array.isArray(deptIds)) {
          deptIds.forEach(deptId => {
            const parent = nodeMap.get(`dept_${deptId}`)
            if (parent) {
              parent.children.push({ ...userNode, id: `user_${deptId}_${user.userid}` })
            }
          })
        }
      } catch (e) {
        // Ignore JSON parse errors
      }
    }
  })
  
  const tree: any[] = []
  departments.forEach(dept => {
    const node = nodeMap.get(`dept_${dept.departmentId}`)
    if (dept.parentId === 0 || dept.parentId === null) {
      tree.push(node)
    } else {
      const parentNode = nodeMap.get(`dept_${dept.parentId}`)
      if (parentNode) {
        parentNode.children.push(node)
      } else {
        tree.push(node)
      }
    }
  })

  // Disable departments that have no employees
  const processDisabled = (node: any): boolean => {
    if (node.isUser) return false
    
    let hasUser = false
    if (node.children && node.children.length > 0) {
      for (const child of node.children) {
        if (!processDisabled(child)) {
          hasUser = true
        }
      }
    }
    node.disabled = !hasUser
    return node.disabled
  }
  
  tree.forEach(node => processDisabled(node))

  return tree
}

const openCreateDialog = () => {
  momentForm.text = ''
  momentForm.visibleRange.senderList = []
  momentForm.visibleRange.tagList = []
  attachmentType.value = 'none'
  
  imageForm.mediaId = ''
  imageForm.picUrl = ''
  
  linkForm.title = ''
  linkForm.url = ''
  linkForm.desc = ''
  linkForm.picUrl = ''
  linkForm.mediaId = ''
  
  mpForm.title = ''
  mpForm.appid = ''
  mpForm.page = ''
  mpForm.picUrl = ''
  mpForm.mediaId = ''
  
  createDialogVisible.value = true
}

const uploadImage = async (options: any) => {
  try {
    const res = await uploadMedia(options.file) as any
    imageForm.mediaId = res.media_id
    imageForm.picUrl = URL.createObjectURL(options.file)
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const uploadLinkCover = async (options: any) => {
  try {
    const res = await uploadMedia(options.file) as any
    linkForm.mediaId = res.media_id
    linkForm.picUrl = URL.createObjectURL(options.file)
    ElMessage.success('链接封面上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const uploadMpCover = async (options: any) => {
  try {
    const res = await uploadMedia(options.file) as any
    mpForm.mediaId = res.media_id
    mpForm.picUrl = URL.createObjectURL(options.file)
    ElMessage.success('小程序封面上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleCreate = async () => {
  if (!momentForm.text && attachmentType.value === 'none') {
    ElMessage.warning('内容和附件不能同时为空')
    return
  }
  
  creating.value = true
  try {
    const payload: any = {
      text: momentForm.text,
      attachments: [] as any[],
      visibleRange: null
    }
    
    if (attachmentType.value === 'image' && imageForm.mediaId) {
      payload.attachments.push({
        msgtype: 'image',
        image: { mediaId: imageForm.mediaId }
      })
    } else if (attachmentType.value === 'link') {
      payload.attachments.push({
        msgtype: 'link',
        link: { 
          title: linkForm.title,
          url: linkForm.url,
          desc: linkForm.desc,
          mediaId: linkForm.mediaId // Uses mediaId for cover mediaId
        }
      })
    } else if (attachmentType.value === 'miniprogram') {
      payload.attachments.push({
        msgtype: 'miniprogram',
        miniprogram: {
          title: mpForm.title,
          appid: mpForm.appid,
          page: mpForm.page,
          picMediaId: mpForm.mediaId
        }
      })
    }

    // Process selected senders from tree
    const selectedKeys = momentForm.visibleRange.senderList || []
    const uniqueSenders = new Set<string>()
    selectedKeys.forEach((key: string) => {
      if (!key.startsWith('dept_') && key.startsWith('user_')) {
        const parts = key.split('_')
        if (parts.length >= 3) {
          // parts are ['user', deptId, ...userid]
          uniqueSenders.add(parts.slice(2).join('_'))
        }
      }
    })
    const finalSenders = Array.from(uniqueSenders)
    
    // Process selected tags from tree
    const selectedTags = momentForm.visibleRange.tagList || []
    const finalTags = selectedTags
      .filter((key: string) => String(key).startsWith('tag_'))
      .map((key: string) => String(key).replace('tag_', ''))
      
    payload.visibleRange = finalSenders.length > 0 ? { senderList: finalSenders } : {}
    
    if (finalTags.length > 0) {
      payload.visibleRange.externalContactList = { tagList: finalTags }
    }
    
    if (Object.keys(payload.visibleRange).length === 0) {
      payload.visibleRange = null
    }

    await createMoment(payload)
    ElMessage.success('发布任务创建成功')
    createDialogVisible.value = false
    fetchMoments()
  } catch (error) {
    ElMessage.error('创建任务失败')
  } finally {
    creating.value = false
  }
}

const handleSync = async () => {
  syncing.value = true
  try {
    await syncMomentStatuses()
    ElMessage.success('已触发同步任务，请稍后刷新')
    setTimeout(fetchMoments, 2000)
  } catch (error) {
    ElMessage.error('触发同步失败')
  } finally {
    syncing.value = false
  }
}

const viewDetail = (row: any) => {
  currentMoment.value = row
  detailDialogVisible.value = true
}

const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

const getStatusLabel = (status: number) => {
  switch (status) {
    case 0: return '处理中'
    case 1: return '已确认'
    case 2: return '发布失败'
    default: return '未知'
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const getAttachments = (row: any) => {
  if (!row.attachments) return []
  try {
    return JSON.parse(row.attachments)
  } catch (e) {
    return []
  }
}

const getAttachmentLabel = (msgtype: string) => {
  switch (msgtype) {
    case 'image': return '图片'
    case 'link': return '链接'
    case 'video': return '视频'
    case 'miniprogram': return '小程序'
    default: return msgtype
  }
}

const getVisibleRange = (row: any) => {
  if (!row.visibleRangeUsers) return null
  try {
    return JSON.parse(row.visibleRangeUsers)
  } catch (e) {
    return null
  }
}

const getUserName = (userid: string) => {
  const user = allUsers.value.find(u => u.userid === userid)
  return user ? user.name : userid
}

const getTagName = (tagId: string) => {
  // Simple lookup in tagTree structure
  for (const group of tagTree.value) {
    const tag = group.children?.find((t: any) => t.tagId === tagId)
    if (tag) return tag.label
  }
  return tagId
}

onMounted(() => {
  fetchMoments()
  fetchDepartmentsAndUsers()
  fetchTagGroups()
})
</script>

<style scoped>
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
.help-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 5px;
}

/* Uploader styles */
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader :deep(.el-upload):hover {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 150px;
  height: 150px;
  text-align: center;
  line-height: 150px;
}
.avatar {
  width: 150px;
  height: 150px;
  display: block;
  object-fit: cover;
}

.attachment-detail {
  margin-bottom: 8px;
}
.detail-content {
  font-size: 13px;
  background-color: #f8f9fa;
  padding: 6px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
.detail-title {
  font-weight: bold;
  color: #303133;
  word-break: break-all;
}
.detail-sub {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
  margin-top: 2px;
}

.detail-view {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.detail-item {
  display: flex;
  gap: 10px;
}
.detail-item .label {
  font-weight: bold;
  width: 80px;
  color: #606266;
}
.detail-item .value {
  flex: 1;
}
.text-content {
  white-space: pre-wrap;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}
.attachment-box {
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  margin-bottom: 10px;
}
.detail-info {
  margin-top: 8px;
  font-size: 13px;
}
.info-row {
  margin-bottom: 4px;
}
.mb-5 { margin-bottom: 5px; }
.mr-5 { margin-right: 5px; }

/* Tree Node Styles */
.tree-node-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.dept-node, .group-node {
  font-weight: 600;
  color: #303133;
}

.user-node, .tag-node {
  color: #606266;
}

.node-icon {
  font-size: 16px;
  color: #909399;
}

.dept-node .node-icon {
  color: #409eff;
}

.group-node .node-icon {
  color: #67c23a;
}
</style>
