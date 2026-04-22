<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/group-messages')" :title="isEdit ? (isCopy ? '复制群群发 / Copy Group Broadcast' : '编辑群群发 / Edit Group Broadcast') : '新建群群发 / New Group Broadcast'" />
    
    <el-row :gutter="20" style="margin-top: 20px; align-items: stretch;">
      <el-col :xs="24" :sm="24" :md="14" :lg="16">
        <el-card>
          <el-form :model="form" :rules="rules" ref="formRef" :label-width="isMobile ? 'auto' : '120px'" :label-position="isMobile ? 'top' : 'left'">
        <!-- Task Info -->
        <div class="section-title">
          <el-icon><InfoFilled /></el-icon>
          <span>基本信息 / Basic Info</span>
        </div>
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="例如：新春群福利通知" />
        </el-form-item>
        
        <el-form-item label="发送时间">
          <el-radio-group v-model="form.sendType">
            <el-radio :value="0" :label="0">立即发送</el-radio>
            <el-radio :value="1" :label="1">定时发送</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="form.sendType === 1" label="设定时间" prop="sendTime">
          <el-date-picker
            v-model="form.sendTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <!-- Target Groups -->
        <div class="section-title">
          <el-icon><Filter /></el-icon>
          <span>发送对象 / Target Groups</span>
        </div>
        <el-form-item label="发送范围">
          <el-radio-group v-model="form.targetType">
            <el-radio :value="0" :label="0">所有客户群</el-radio>
            <el-radio :value="1" :label="1">筛选客户群</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <template v-if="form.targetType === 1">
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="createTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleDateRangeChange"
            />
          </el-form-item>
          
          <el-form-item label="发送群主">
            <el-radio-group v-model="form.isAllSenders">
              <el-radio label="all" value="all">全部群主</el-radio>
              <el-radio label="specified" value="specified">指定群主</el-radio>
            </el-radio-group>
            
            <div v-show="form.isAllSenders === 'specified'" style="margin-top: 10px; width: 100%">
              <el-tree-select
                v-model="selectedTreeKeys"
                :data="departmentUserTree"
                multiple
                show-checkbox
                filterable
                node-key="id"
                placeholder="请选择群主 (可按部门勾选)"
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
            </div>
          </el-form-item>
          
          <el-form-item label="群名关键字">
            <div class="info-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>支持多个关键字，群名满足任一关键字即可匹配</span>
            </div>
            <div class="keyword-input-wrapper">
              <el-tag
                v-for="tag in form.targetCondition.groupNameKeywords"
                :key="tag"
                closable
                round
                effect="plain"
                :disable-transitions="false"
                @close="handleCloseKeyword(tag)"
                class="keyword-pill-tag"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="inputVisible"
                ref="saveTagInput"
                v-model="inputValue"
                class="input-new-tag"
                size="small"
                style="width: 100px"
                @keyup.enter="handleInputConfirm"
                @blur="handleInputConfirm"
              />
              <el-button v-else class="button-new-tag" size="small" @click="showInput">
                + 新增关键字
              </el-button>
            </div>
          </el-form-item>
        </template>

        <div class="section-title">
          <el-icon><ChatLineRound /></el-icon>
          <span>消息内容 / Content</span>
        </div>
        <el-form-item label="群发内容" prop="text" class="content-form-item">
          <div class="message-editor-container">
            <div class="text-area-wrapper">
              <el-input 
                v-model="form.text" 
                type="textarea" 
                :rows="6" 
                placeholder="请输入群发消息文本..." 
                maxlength="600"
                resize="none"
              />
              <div class="char-count">{{ form.text.length }}/600</div>
            </div>
            
            <div class="attachment-box" v-if="form.attachments.length > 0">
              <div v-for="(att, index) in form.attachments" :key="index" class="attachment-pill">
                <div class="att-main">
                  <div class="att-thumb-wrapper" v-if="att.msgtype === 'image' || att.msgtype === 'miniprogram' || att.link?.picUrl">
                    <img 
                      :src="att.msgtype === 'image' ? getImageUrl(att.image.mediaId) : (att.msgtype === 'miniprogram' ? getImageUrl(att.miniprogram.picMediaId) : att.link.picUrl)" 
                      class="att-thumb"
                      @error="markAsExpired(index)"
                    />
                    <div v-if="att.isExpired" class="expired-overlay">
                      <el-icon><Warning /></el-icon>
                      <span>已过期</span>
                    </div>
                  </div>
                  <el-icon class="att-icon" v-else>
                    <Picture v-if="att.msgtype === 'image'" />
                    <Link v-else-if="att.msgtype === 'link'" />
                    <Compass v-else-if="att.msgtype === 'miniprogram'" />
                  </el-icon>
                  <span class="att-name">
                    <template v-if="att.msgtype === 'link'">{{ att.link?.title || '未命名链接' }}</template>
                    <template v-else-if="att.msgtype === 'miniprogram'">{{ att.miniprogram?.title || '未命名小程序' }}</template>
                    <template v-else-if="att.msgtype === 'image'">图片素材</template>
                  </span>
                </div>
                <div class="att-ops">
                  <el-icon class="op-btn copy" @click="copyAttachment(index)"><CopyDocument /></el-icon>
                  <el-icon class="op-btn edit" @click="editAttachment(index)"><EditPen /></el-icon>
                  <el-icon class="op-btn delete" @click="removeAttachment(index)"><Close /></el-icon>
                </div>
              </div>
            </div>

            <div class="editor-footer">
              <el-button type="primary" link @click="openAddAttachment" v-if="form.attachments.length < 9">
                <el-icon><Plus /></el-icon> 添加附件
              </el-button>
              <span class="footer-tip" v-else>已达到附件上限 (9个)</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item style="margin-top: 40px">
          <div style="display: flex; justify-content: flex-end; width: 100%; gap: 10px;">
            <el-button size="large" @click="$router.push('/group-messages')">取消</el-button>
            <el-button type="primary" size="large" :loading="submitting" @click="submitForm">{{ isEdit && !isCopy ? '保存修改' : '创建任务' }}</el-button>
          </div>
        </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="10" :lg="8" class="preview-col">
        <div class="preview-affix">
          <div class="preview-integrated">
            <div class="preview-title">
              <el-icon><Monitor /></el-icon>
              <span>实时效果预览</span>
            </div>
            <MobilePreview :text="form.text" :attachments="form.attachments" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Attachment Selection Dialog -->
    <el-dialog v-model="attachmentDialogVisible" title="添加附件" :width="isMobile ? '95%' : '550px'">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="图片" name="image">
          <el-form :model="imageForm" label-width="90px">
            <el-form-item label="选择图片" required>
              <el-upload
                class="attachment-uploader"
                action=""
                :http-request="(options: any) => uploadFile(options, 'image')"
                :show-file-list="false"
              >
                <img v-if="imageForm.picUrl" :src="imageForm.picUrl" class="upload-preview" @error="handleImageError('image')" />
                <el-icon v-else class="upload-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="链接" name="link">
          <el-form :model="linkForm" label-width="90px">
             <el-form-item label="标题" required>
               <el-input v-model="linkForm.title" placeholder="请输入链接标题" />
             </el-form-item>
             <el-form-item label="URL" required>
               <el-input v-model="linkForm.url" placeholder="请输入链接URL" />
             </el-form-item>
             <el-form-item label="摘要">
               <el-input v-model="linkForm.desc" placeholder="请输入链接描述" />
             </el-form-item>
             <el-form-item label="封面图" required>
               <el-upload
                class="attachment-uploader small"
                action=""
                :http-request="(options: any) => uploadFile(options, 'link')"
                :show-file-list="false"
              >
                <img v-if="linkForm.picUrl" :src="linkForm.picUrl" class="upload-preview small" @error="handleImageError('link')" />
                <el-icon v-else class="upload-icon small"><Plus /></el-icon>
              </el-upload>
             </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="小程序" name="miniprogram">
          <el-form :model="mpForm" label-width="90px">
             <el-form-item label="标题" required>
               <el-input v-model="mpForm.title" placeholder="请输入小程序标题" maxlength="20" show-word-limit />
             </el-form-item>
             <el-form-item label="AppID" required>
               <el-input v-model="mpForm.appid" placeholder="请输入小程序AppID" />
             </el-form-item>
             <el-form-item label="页面路径" required>
               <el-input v-model="mpForm.page" placeholder="例如: pages/index/index" />
             </el-form-item>
             <el-form-item label="封面图" required>
                <el-upload
                  class="attachment-uploader small"
                  action=""
                  :http-request="(options: any) => uploadFile(options, 'miniprogram')"
                  :show-file-list="false"
                >
                  <img v-if="mpForm.picUrl" :src="mpForm.picUrl" class="upload-preview small" @error="handleImageError('miniprogram')" />
                  <el-icon v-else class="upload-icon small"><Plus /></el-icon>
                </el-upload>
             </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="attachmentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAttachment">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { createGroupMessage, getGroupMessage, updateGroupMessage } from '@/api/groupMessage'
import { uploadImg, uploadTempMedia } from '@/api/media'
import { getUsers, getDepartments } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Plus, EditPen, InfoFilled, Filter, ChatLineRound, Picture, Link, Compass, Close, Monitor, Folder, UserFilled, CopyDocument, Warning } from '@element-plus/icons-vue'
import { onMounted, computed, ref, reactive, nextTick } from 'vue'
import MobilePreview from './MobilePreview.vue'
import { getImageUrl } from '@/api/media'
import { useResponsive } from '@/hooks/useResponsive'

const { isMobile } = useResponsive()

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)
const isCopy = computed(() => route.name === 'GroupMessageCopy')

const form = reactive({
  taskName: '',
  sendType: 0,
  sendTime: '',
  targetType: 0,
  targetCondition: {
    createTimeStart: '',
    createTimeEnd: '',
    groupNameKeywords: [] as string[],
    ownerUserids: [] as string[]
  },
  text: '',
  attachments: [] as any[],
  isAllSenders: 'all' // 'all', 'specified'
})

const staffList = ref<any[]>([])
const allDepartments = ref<any[]>([])
const departmentUserTree = ref<any[]>([])
const selectedTreeKeys = ref<string[]>([])
const createTimeRange = ref([])
const inputVisible = ref(false)
const inputValue = ref('')
const saveTagInput = ref(null)

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
    const userNode = { label: user.name, isUser: true, avatar: user.avatar, id: `user_${user.userid}` }
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
        // Fallback for non-JSON
        const parent = nodeMap.get(`dept_${user.departmentIds}`)
        if (parent) parent.children.push({ ...userNode, id: `user_${user.departmentIds}_${user.userid}` })
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
  return tree
}

onMounted(async () => {
  // Load staff and departments
  try {
    const [userRes, deptRes] = await Promise.all([
      getUsers({ page: 1, size: 1000 }),
      getDepartments()
    ])
    staffList.value = (userRes as any).content || []
    allDepartments.value = deptRes as any || []
    departmentUserTree.value = buildDepartmentUserTree(allDepartments.value, staffList.value)
  } catch (e) {
    console.error('Failed to load initial data', e)
  }

  if (isEdit.value) {
    const id = route.params.id as string
    try {
      const taskRes = await getGroupMessage(id) as any
      if (taskRes) {
        const targetCondition = typeof taskRes.targetCondition === 'string' ? JSON.parse(taskRes.targetCondition) : (taskRes.targetCondition || {})
        const ownerUserids = targetCondition.ownerUserids || []
        
        // Map ownerUserids back to tree keys
        const initialKeys: string[] = []
        ownerUserids.forEach((uid: string) => {
          const findInTree = (nodes: any[]) => {
            nodes.forEach(n => {
              if (n.id && n.id.endsWith(`_${uid}`)) {
                initialKeys.push(n.id)
              }
              if (n.children) findInTree(n.children)
            })
          }
          findInTree(departmentUserTree.value)
        })
        selectedTreeKeys.value = initialKeys
        form.isAllSenders = ownerUserids.length === 0 ? 'all' : 'specified'

        Object.assign(form, {
          taskName: isCopy.value ? `${taskRes.taskName} (复制)` : taskRes.taskName,
          sendType: taskRes.sendType,
          sendTime: taskRes.sendTime,
          targetType: taskRes.targetType,
          text: taskRes.content,
          attachments: typeof taskRes.attachments === 'string' ? JSON.parse(taskRes.attachments) : (taskRes.attachments || []),
          targetCondition: {
            ...targetCondition,
            groupNameKeywords: targetCondition.groupNameKeywords || [],
            ownerUserids: ownerUserids
          }
        })
        
        if (form.targetCondition.createTimeStart && form.targetCondition.createTimeEnd) {
          createTimeRange.value = [form.targetCondition.createTimeStart, form.targetCondition.createTimeEnd] as any
        }
      }
    } catch (e) {
      ElMessage.error('加载任务详情失败')
    }
  }
})

const validateSendTime = (_rule: any, value: any, callback: any) => {
  if (form.sendType === 1) {
    if (!value) {
      return callback(new Error('请选择定时发送时间'))
    }
    const sendTime = new Date(value.replace(/-/g, '/')).getTime()
    const now = new Date().getTime()
    if (sendTime <= now) {
      return callback(new Error('发送时间不得早于当前时间'))
    }
  }
  callback()
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  sendTime: [{ validator: validateSendTime, trigger: 'change' }]
}

const handleDateRangeChange = (val: any) => {
  if (val && val.length === 2) {
    form.targetCondition.createTimeStart = val[0]
    form.targetCondition.createTimeEnd = val[1]
  } else {
    form.targetCondition.createTimeStart = ''
    form.targetCondition.createTimeEnd = ''
  }
}

// Keyword logic
const handleCloseKeyword = (tag: string) => {
  form.targetCondition.groupNameKeywords.splice(form.targetCondition.groupNameKeywords.indexOf(tag), 1)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    (saveTagInput.value as any).focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value) {
    if (!form.targetCondition.groupNameKeywords.includes(inputValue.value)) {
      form.targetCondition.groupNameKeywords.push(inputValue.value)
    }
  }
  inputVisible.value = false
  inputValue.value = ''
}

// Attachment Logic (Same as CustomerMessageCreate)
const attachmentDialogVisible = ref(false)
const activeTab = ref('image')
const editingIndex = ref(-1)
const imageForm = reactive({ mediaId: '', picUrl: '' })
const linkForm = reactive({ title: '', url: '', desc: '', picUrl: '' })
const mpForm = reactive({ title: '', appid: '', page: '', picMediaId: '', picUrl: '' })

const openAddAttachment = () => {
  editingIndex.value = -1
  activeTab.value = 'image'
  Object.assign(imageForm, { mediaId: '', picUrl: '' })
  Object.assign(linkForm, { title: '', url: '', desc: '', picUrl: '' })
  Object.assign(mpForm, { title: '', appid: '', page: '', picMediaId: '', picUrl: '' })
  attachmentDialogVisible.value = true
}

const editAttachment = (index: number) => {
  const att = form.attachments[index]
  editingIndex.value = index
  activeTab.value = att.msgtype
  
  if (att.msgtype === 'image') {
    Object.assign(imageForm, { mediaId: att.image.mediaId, picUrl: getImageUrl(att.image.mediaId) })
  } else if (att.msgtype === 'link') {
    Object.assign(linkForm, { title: att.link.title, url: att.link.url, desc: att.link.desc || '', picUrl: att.link.picUrl || '' })
  } else if (att.msgtype === 'miniprogram') {
    Object.assign(mpForm, { 
      title: att.miniprogram.title, 
      appid: att.miniprogram.appid, 
      page: att.miniprogram.page, 
      picMediaId: att.miniprogram.picMediaId,
      picUrl: getImageUrl(att.miniprogram.picMediaId) 
    })
  }
  attachmentDialogVisible.value = true
}

const copyAttachment = (index: number) => {
  if (form.attachments.length >= 9) {
    return ElMessage.warning('附件数量已达限制（9个）')
  }
  const att = JSON.parse(JSON.stringify(form.attachments[index]))
  form.attachments.splice(index + 1, 0, att)
  ElMessage.success('复制成功')
}

const uploadFile = async (options: any, type: string) => {
  try {
    let res: any
    if (type === 'link') {
      res = await uploadImg(options.file)
      linkForm.picUrl = res.pic_url
    } else {
      res = await uploadTempMedia(options.file)
      if (type === 'image') {
        imageForm.mediaId = res.media_id
        imageForm.picUrl = URL.createObjectURL(options.file)
      } else if (type === 'miniprogram') {
        mpForm.picMediaId = res.media_id
        mpForm.picUrl = URL.createObjectURL(options.file)
      }
    }
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}

const handleImageError = (type: string) => {
  if (type === 'image') {
    imageForm.picUrl = ''
    imageForm.mediaId = ''
  } else if (type === 'miniprogram') {
    mpForm.picUrl = ''
    mpForm.picMediaId = ''
  } else if (type === 'link') {
    linkForm.picUrl = ''
  }
  ElMessage.warning('图片已过期或无法加载，请重新上传')
}

const confirmAttachment = () => {
  let newAtt: any = null
  if (activeTab.value === 'image') {
    if (!imageForm.mediaId) return ElMessage.warning('请选择并上传图片')
    newAtt = {
      msgtype: 'image',
      image: { mediaId: imageForm.mediaId }
    }
  } else if (activeTab.value === 'link') {
    if (!linkForm.title || !linkForm.url || !linkForm.picUrl) return ElMessage.warning('请填写链接标题、URL并上传封面图')
    newAtt = {
      msgtype: 'link',
      link: { 
        title: linkForm.title,
        url: linkForm.url,
        desc: linkForm.desc,
        picUrl: linkForm.picUrl
      }
    }
  } else {
    if (!mpForm.title || !mpForm.appid || !mpForm.picMediaId) return ElMessage.warning('请填写必填项（标题/AppID/展示图）')
    newAtt = {
      msgtype: 'miniprogram',
      miniprogram: {
        title: mpForm.title,
        appid: mpForm.appid,
        page: mpForm.page,
        picMediaId: mpForm.picMediaId
      }
    }
  }

  if (editingIndex.value > -1) {
    form.attachments[editingIndex.value] = newAtt
  } else {
    form.attachments.push(newAtt)
  }
  attachmentDialogVisible.value = false
}

const removeAttachment = (index: number) => {
  form.attachments.splice(index, 1)
}

const markAsExpired = (index: number) => {
  if (form.attachments[index]) {
    form.attachments[index].isExpired = true
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  // Sync tree selection to form
  const uniqueOwners = new Set<string>()
  selectedTreeKeys.value.forEach(key => {
    if (key.startsWith('user_')) {
      const parts = key.split('_')
      if (parts.length >= 3) {
        uniqueOwners.add(parts.slice(2).join('_'))
      }
    }
  })
  form.targetCondition.ownerUserids = Array.from(uniqueOwners)

  await (formRef.value as any).validate(async (valid: boolean) => {
    if (valid) {
      // Check for expired attachments
      const expiredAtt = form.attachments.find(att => att.isExpired)
      if (expiredAtt) {
        return ElMessage.error('存在已过期的附件图片，请重新上传或删除后再提交')
      }
      submitting.value = true
      try {
        if (isEdit.value && !isCopy.value) {
          await updateGroupMessage(route.params.id as string, form as any)
          ElMessage.success('更新任务成功')
        } else {
          await createGroupMessage(form as any)
          ElMessage.success('创建任务成功')
        }
        router.push('/group-messages')
      } catch (e) {
        ElMessage.error('操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.preview-affix {
  position: sticky;
  top: 20px;
}

@media (max-width: 992px) {
  .preview-col {
    margin-top: 24px;
  }
}
.tree-node-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0;
}

.node-icon {
  font-size: 16px;
  color: #909399;
}

.dept-node {
  font-weight: 600;
  color: #303133;
}

.dept-node .node-icon {
  color: #e6a23c;
}

.user-node {
  font-weight: normal;
  color: #606266;
}

.user-node .node-icon {
  color: #409eff;
}

.preview-card {
  border: none;
  background: transparent;
  box-shadow: none;
}
.preview-header {
  font-weight: 500;
  font-size: 16px;
  text-align: center;
}

.content-form-item :deep(.el-form-item__content) {
  display: block;
}
.message-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  width: 100%;
  overflow: hidden;
}
.text-area-wrapper {
  position: relative;
  padding: 10px;
}
.text-area-wrapper :deep(.el-textarea__inner) {
  border: none;
  padding: 0;
  box-shadow: none;
  font-size: 14px;
}
.char-count {
  position: absolute;
  right: 15px;
  bottom: 10px;
  font-size: 12px;
  color: #909399;
}
.attachment-box {
  border-top: 1px solid #f0f0f0;
}
.attachment-row {
  display: flex;
  align-items: center;
  padding: 8px 15px;
  gap: 10px;
}
.att-content {
  flex: 1;
  font-size: 14px;
  color: #606266;
  text-align: left;
}
.editor-footer {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.attachment-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.upload-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}
.upload-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}
.upload-icon.small {
  width: 80px;
  height: 80px;
  line-height: 80px;
  font-size: 20px;
}
.upload-preview.small {
  width: 80px;
  height: 80px;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.keyword-input-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
}

/* New Section Title Styles */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 30px 0 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
  color: #303133;
  font-weight: 600;
  font-size: 16px;
}

.section-title .el-icon {
  font-size: 18px;
  color: #409eff;
}

/* Attachment Pill Styles */
.attachment-box {
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  background: #fcfcfc;
  border-top: 1px solid #f0f2f5;
}

.attachment-pill {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 4px 8px;
  gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.attachment-pill:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.att-main {
  display: flex;
  align-items: center;
  gap: 6px;
  max-width: 180px;
}

.att-icon {
  font-size: 16px;
  color: #909399;
}

.att-name {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.att-thumb-wrapper {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  flex-shrink: 0;
  position: relative;
}

.expired-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 10px;
  gap: 2px;
  border-radius: 4px;
}

.expired-overlay .el-icon {
  font-size: 14px;
  color: #f56c6c;
}

.att-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.att-ops {
  display: flex;
  gap: 6px;
  border-left: 1px solid #ebeef5;
  padding-left: 6px;
}

.op-btn {
  cursor: pointer;
  font-size: 14px;
  color: #909399;
  transition: color 0.2s;
}

.op-btn:hover.edit {
  color: #409eff;
}

.op-btn:hover.copy {
  color: #67c23a;
}

.op-btn:hover.delete {
  color: #f56c6c;
}

/* Integrated Preview Styles */
.preview-integrated {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.preview-title {
  padding: 15px;
  background: #f8faff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.preview-title .el-icon {
  color: #409eff;
}

.keyword-pill-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  height: 32px;
  padding: 0 12px;
  border-radius: 16px;
  border-color: #dcdfe6;
  color: #606266;
  background-color: #f5f7fa;
}

.keyword-pill-tag :deep(.el-tag__close) {
  color: #909399;
}

.keyword-pill-tag :deep(.el-tag__close:hover) {
  background-color: #409eff;
  color: #fff;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background: #f0f2f5;
  color: #909399;
  font-size: 12px;
}

.user-name {
  font-size: 14px;
}

.user-id {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Selected Summary Styles */
.selected-items-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.pill-tag {
  border: 1px solid #dcdfe6;
  background-color: #f8faff;
  transition: all 0.2s;
}

.pill-tag:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.item-pill {
  padding: 0 10px;
  height: 28px;
}

.pill-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.mini-avatar {
  background: #f0f2f5;
  color: #909399;
  font-size: 8px;
}

.mt-10 { margin-top: 10px; }

.info-tip {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
  background-color: #f8faff;
  padding: 6px 12px;
  border-radius: 4px;
}

.info-tip .el-icon {
  font-size: 14px;
  color: #409eff;
}
</style>
