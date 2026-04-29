<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/customer-messages')" :title="isEdit ? (isCopy ? '复制群发 / Copy Broadcast' : '编辑群发 / Edit Broadcast') : '新建群发 / New Broadcast'" />
    
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
          <el-input v-model="form.taskName" placeholder="建议包含日期和业务逻辑，例如：321促销活动" />
        </el-form-item>
        
        <el-form-item label="发送员工">
          <el-radio-group v-model="form.isAllSenders">
            <el-radio label="all" value="all">全部员工</el-radio>
            <el-radio label="specified" value="specified">指定员工</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-show="form.isAllSenders === 'specified'">
          <el-tree-select
            v-model="selectedTreeKeys"
            :data="departmentUserTree"
            multiple
            show-checkbox
            filterable
            node-key="id"
            placeholder="请选择员工 (可按部门勾选)"
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

        <!-- Target Customers -->
        <div class="section-title">
          <el-icon><Filter /></el-icon>
          <span>发送对象 / Target Customers</span>
        </div>
        <el-form-item label="发送范围">
          <el-radio-group v-model="form.targetType">
            <el-radio :value="0" :label="0">所有客户</el-radio>
            <el-radio :value="1" :label="1">筛选客户</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <template v-if="form.targetType === 1">
          <el-form-item label="添加时间">
            <el-date-picker
              v-model="addTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleDateRangeChange"
            />
          </el-form-item>
          
          <el-form-item label="包含标签">
            <el-button type="primary" plain size="small" @click="openTagDialog('include')">
              <el-icon><Plus /></el-icon>选择包含标签 ({{ form.targetCondition.includeTags.length }})
            </el-button>
            
            <div v-if="form.targetCondition.includeTags.length > 0" class="selected-items-summary mt-5">
              <el-tag
                v-for="tagId in form.targetCondition.includeTags"
                :key="tagId"
                closable
                round
                @close="removeTag('include', tagId)"
                class="pill-tag"
              >
                {{ getTagName(tagId) }}
              </el-tag>
            </div>
            
            <div v-if="form.targetCondition.includeTags.length > 1" style="margin-top: 5px">
              <el-radio-group v-model="form.targetCondition.includeTagsAny">
                <el-radio :value="true" :label="true">任一标签 (OR)</el-radio>
                <el-radio :value="false" :label="false">全部标签 (AND)</el-radio>
              </el-radio-group>
            </div>
          </el-form-item>
          
          <el-form-item label="排除标签">
            <el-button type="info" plain size="small" @click="openTagDialog('exclude')">
              <el-icon><Plus /></el-icon>选择排除标签 ({{ form.targetCondition.excludeTags.length }})
            </el-button>
            
            <div v-if="form.targetCondition.excludeTags.length > 0" class="selected-items-summary mt-5">
              <el-tag
                v-for="tagId in form.targetCondition.excludeTags"
                :key="tagId"
                closable
                round
                type="danger"
                @close="removeTag('exclude', tagId)"
                class="pill-tag"
              >
                {{ getTagName(tagId) }}
              </el-tag>
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
            <el-button size="large" @click="$router.push('/customer-messages')">取消</el-button>
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

    <!-- Tag Selection Dialog -->
    <el-dialog v-model="tagDialogVisible" :title="tagDialogType === 'include' ? '包含标签' : '排除标签'" width="600px">
       <div v-loading="tagsLoading" class="tag-selection-container">
          <div v-for="group in allTagGroups" :key="group.groupId" class="tag-group-box">
            <div class="tag-group-header">
              <span class="dot"></span>
              {{ group.groupName }}
            </div>
            <el-checkbox-group v-model="tempTagIds" class="pill-checkbox-group">
              <el-checkbox v-for="tag in group.tags" :key="tag.tagId" :label="tag.tagId" class="tag-pill-checkbox">
                {{ tag.name }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
       </div>
       <template #footer>
          <el-button @click="tagDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTagSelection">确定</el-button>
       </template>
    </el-dialog>

    <!-- Attachment Selection Dialog -->
    <el-dialog v-model="attachmentDialogVisible" title="添加附件" width="550px">
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
              <div class="upload-tip">支持上传图片附件，将通过 WeCom 素材库发送</div>
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
import { createCustomerMessage, getCustomerMessage, updateCustomerMessage } from '@/api/customerMessage'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { getUsers, getDepartments } from '@/api/user'
import { uploadImg, uploadTempMedia } from '@/api/media'
import { ElMessage } from 'element-plus'
import { Plus, EditPen, InfoFilled, Filter, ChatLineRound, Picture, Link, Compass, Close, Monitor, Folder, UserFilled, CopyDocument, Warning } from '@element-plus/icons-vue'
import { onMounted, computed, ref, reactive } from 'vue'
import MobilePreview from './MobilePreview.vue'
import { getImageUrl } from '@/api/media'
import { useResponsive } from '@/hooks/useResponsive'

const { isMobile } = useResponsive()
const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)
const isCopy = computed(() => route.name === 'CustomerMessageCopy')

const form = reactive({
  taskName: '',
  sendType: 0,
  sendTime: '',
  targetType: 0,
  targetCondition: {
    addTimeStart: '',
    addTimeEnd: '',
    includeTags: [] as string[],
    includeTagsAny: true,
    excludeTags: [] as string[]
  },
  text: '',
  attachments: [] as any[],
  senderList: [] as string[],
  isAllSenders: 'all' // 'all', 'specified'
})

const selectedTreeKeys = ref<string[]>([])
const allUsers = ref<any[]>([])
const allDepartments = ref<any[]>([])
const departmentUserTree = ref<any[]>([])

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
        // Fallback for non-JSON string
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
  try {
    const [userRes, deptRes] = await Promise.all([
      getUsers({ page: 1, size: 1000 }),
      getDepartments()
    ])
    allUsers.value = (userRes as any).content || []
    allDepartments.value = deptRes as any || []
    departmentUserTree.value = buildDepartmentUserTree(allDepartments.value, allUsers.value)
    
    // Load tag groups for names mapping
    try {
      const groups = await getTagGroups() as any
      const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
        const tags = await getTagsByGroup(g.groupId) as any
        return { ...g, tags }
      }))
      allTagGroups.value = groupsWithTags
    } catch (e) {
      console.warn('Failed to load tag groups for preview', e)
    }

    if (isEdit.value) {
      const id = route.params.id as string
      try {
        const taskRes = await getCustomerMessage(id) as any
        if (taskRes) {
          const senderList = typeof taskRes.senderList === 'string' ? JSON.parse(taskRes.senderList) : (taskRes.senderList || [])
          
          // Map userids back to tree keys
          const initialKeys: string[] = []
          senderList.forEach((uid: string) => {
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
          form.isAllSenders = senderList.length === 0 ? 'all' : 'specified'
          
          Object.assign(form, {
            taskName: isCopy.value ? `${taskRes.taskName} (复制)` : taskRes.taskName,
            sendType: taskRes.sendType,
            sendTime: taskRes.sendTime,
            targetType: taskRes.targetType,
            text: taskRes.content,
            attachments: typeof taskRes.attachments === 'string' ? JSON.parse(taskRes.attachments) : (taskRes.attachments || []),
            targetCondition: typeof taskRes.targetCondition === 'string' ? JSON.parse(taskRes.targetCondition) : (taskRes.targetCondition || {
              addTimeStart: '',
              addTimeEnd: '',
              includeTags: [],
              includeTagsAny: true,
              excludeTags: []
            }),
            senderList: senderList
          })
          
          if (form.targetCondition.addTimeStart && form.targetCondition.addTimeEnd) {
            addTimeRange.value = [form.targetCondition.addTimeStart, form.targetCondition.addTimeEnd] as any
          }
        }
      } catch (e) {
        ElMessage.error('加载详情失败')
      }
    }
  } catch (e) {
    console.error('Failed to load data', e)
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
  return time.getTime() < Date.now() - 8.64e7 // Disable dates before today
}

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  // text: [{ required: true, message: '请输入发送内容', trigger: 'blur' }],
  sendTime: [{ validator: validateSendTime, trigger: 'change' }]
}

const addTimeRange = ref([])
const handleDateRangeChange = (val: any) => {
  if (val && val.length === 2) {
    form.targetCondition.addTimeStart = val[0]
    form.targetCondition.addTimeEnd = val[1]
  } else {
    form.targetCondition.addTimeStart = ''
    form.targetCondition.addTimeEnd = ''
  }
}


// Tag Selection Logic
const tagDialogVisible = ref(false)
const tagDialogType = ref('include')
const tagsLoading = ref(false)
const allTagGroups = ref<any[]>([])
const tempTagIds = ref<string[]>([])

const getTagName = (tagId: string) => {
  for (const group of allTagGroups.value) {
    if (group.tags) {
      const tag = group.tags.find((t: any) => t.tagId === tagId)
      if (tag) return tag.name
    }
  }
  return tagId
}

const removeTag = (type: 'include' | 'exclude', tagId: string) => {
  if (type === 'include') {
    form.targetCondition.includeTags = form.targetCondition.includeTags.filter(id => id !== tagId)
  } else {
    form.targetCondition.excludeTags = form.targetCondition.excludeTags.filter(id => id !== tagId)
  }
}

const openTagDialog = async (type: string) => {
  tagDialogType.value = type
  tempTagIds.value = [...(type === 'include' ? form.targetCondition.includeTags : form.targetCondition.excludeTags)]
  tagDialogVisible.value = true
  
  if (allTagGroups.value.length === 0) {
    tagsLoading.value = true
    try {
      const groups = await getTagGroups() as any
      const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
        const tags = await getTagsByGroup(g.groupId) as any
        return { ...g, tags }
      }))
      allTagGroups.value = groupsWithTags
    } catch (e) {
      ElMessage.error('获取标签库失败')
    } finally {
      tagsLoading.value = false
    }
  }
}

const confirmTagSelection = () => {
  if (tagDialogType.value === 'include') {
    form.targetCondition.includeTags = [...tempTagIds.value]
  } else {
    form.targetCondition.excludeTags = [...tempTagIds.value]
  }
  tagDialogVisible.value = false
}

// Attachment Logic
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
  await (formRef.value as any).validate(async (valid: boolean) => {
    if (valid) {
      // Check for expired attachments
      const expiredAtt = form.attachments.find(att => att.isExpired)
      if (expiredAtt) {
        return ElMessage.error('存在已过期的附件图片，请重新上传或删除后再提交')
      }

      submitting.value = true
      try {
        if (form.isAllSenders === 'specified') {
          // Extract userids from tree keys (user_deptId_userid)
          const uniqueSenders = new Set<string>()
          selectedTreeKeys.value.forEach(key => {
            if (key.startsWith('user_')) {
              const parts = key.split('_')
              if (parts.length >= 3) {
                uniqueSenders.add(parts.slice(2).join('_'))
              }
            }
          })
          form.senderList = Array.from(uniqueSenders)
        } else {
          form.senderList = []
        }
        if (isEdit.value && !isCopy.value) {
          await updateCustomerMessage(route.params.id as string, form as any)
          ElMessage.success('更新群发任务成功')
        } else {
          await createCustomerMessage(form as any)
          ElMessage.success(isCopy.value ? '复制并创建群发任务成功' : '创建群发任务成功')
        }
        router.push('/customer-messages')
      } catch (e) {
        ElMessage.error('创建失败')
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
  line-height: 1.6;
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
  padding: 5px 0;
}

.attachment-row {
  display: flex;
  align-items: center;
  padding: 8px 15px;
  gap: 10px;
  transition: background 0.2s;
}

.attachment-row:hover {
  background: #f5f7fa;
}

.att-actions-left {
  display: flex;
  align-items: center;
}

.att-content {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.att-type {
  color: #303133;
}

.att-text {
  color: #606266;
}

.att-actions-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-btn {
  cursor: pointer;
  font-size: 16px;
  color: #909399;
  transition: color 0.2s;
}

.action-btn.remove:hover {
  color: #f56c6c;
}

.action-btn.edit:hover {
  color: #409eff;
}

.action-btn.drag {
  cursor: move;
}

.editor-footer {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.footer-tip {
  font-size: 12px;
  color: #909399;
}

.tag-selection-container {
  max-height: 400px;
  overflow-y: auto;
}

.tag-group-box {
  margin-bottom: 24px;
}

.tag-group-header {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  text-align: left;
}

.tag-group-header .dot {
  width: 6px;
  height: 6px;
  background: #409eff;
  border-radius: 50%;
  margin-right: 8px;
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
  color: #e6a23c; /* Warning/Gold color for folders */
}

.user-node {
  font-weight: normal;
  color: #606266;
}

.user-node .node-icon {
  color: #409eff; /* Primary blue for users */
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

.mt-5 { margin-top: 5px; }
.mt-10 { margin-top: 10px; }

.el-checkbox {
  margin-bottom: 10px;
  margin-right: 10px;
}

/* Uploader Styles */
.attachment-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.attachment-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.upload-icon.small {
  width: 80px;
  height: 80px;
  line-height: 80px;
  font-size: 20px;
}

.upload-preview {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-preview.small {
  width: 80px;
  height: 80px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
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

/* Pill Checkbox Styles */
.tag-selection-container {
  max-height: 450px;
}

.pill-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 5px 0;
}

.tag-pill-checkbox {
  margin: 0 !important;
}

.tag-pill-checkbox :deep(.el-checkbox__inner) {
  display: none;
}

.tag-pill-checkbox :deep(.el-checkbox__label) {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
  color: #606266;
  font-size: 13px;
  transition: all 0.2s;
  user-select: none;
  line-height: 1;
}

.tag-pill-checkbox:hover :deep(.el-checkbox__label) {
  border-color: #409eff;
  color: #409eff;
}

.tag-pill-checkbox.is-checked :deep(.el-checkbox__label) {
  border-color: #409eff;
  background: #409eff;
  color: white;
  font-weight: 500;
}
</style>
