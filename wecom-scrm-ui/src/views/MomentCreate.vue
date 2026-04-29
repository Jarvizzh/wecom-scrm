<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/moments')" :title="isEdit ? '编辑朋友圈 / Edit Moment' : (isCopy ? '复制朋友圈 / Copy Moment' : '创建朋友圈 / New Moment')" />
    
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
              <el-input v-model="form.taskName" placeholder="建议包含日期和业务逻辑，例如：321促销朋友圈" />
            </el-form-item>
            
            <el-form-item label="发表员工">
              <el-radio-group v-model="isAllRange">
                <el-radio :label="true">全部员工</el-radio>
                <el-radio :label="false">指定员工</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-show="!isAllRange">
              <el-tree-select
                v-model="selectedTreeUserKeys"
                :data="departmentUserTree"
                multiple
                show-checkbox
                filterable
                node-key="id"
                :default-expanded-keys="expandedUserKeys"
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

            <el-form-item label="可见客户标签">
              <el-tree-select
                v-model="selectedTreeTagKeys"
                :data="tagTree"
                multiple
                show-checkbox
                filterable
                node-key="id"
                :default-expanded-keys="expandedTagKeys"
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
            
            <el-form-item label="发送时间">
              <el-radio-group v-model="form.sendType">
                <el-radio :value="0">立即发送</el-radio>
                <el-radio :value="1">定时发送</el-radio>
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

            <!-- Content -->
            <div class="section-title">
              <el-icon><ChatLineRound /></el-icon>
              <span>朋友圈内容 / Content</span>
            </div>
            
            <el-form-item label="文字内容" prop="text" class="content-form-item">
              <div class="message-editor-container">
                <div class="text-area-wrapper">
                  <el-input 
                    v-model="form.text" 
                    type="textarea" 
                    :rows="6" 
                    placeholder="请输入朋友圈文字内容 (非必填，但文字和附件不能同时为空)" 
                    maxlength="2000"
                    resize="none"
                  />
                  <div class="char-count">{{ form.text.length }}/2000</div>
                </div>
                
                <div class="attachment-box" v-if="form.attachments.length > 0">
                  <div v-for="(att, index) in form.attachments" :key="index" class="attachment-pill">
                    <div class="att-main">
                      <el-icon class="att-icon">
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
                      <el-icon class="op-btn edit" @click="editAttachment(index)"><EditPen /></el-icon>
                      <el-icon class="op-btn delete" @click="removeAttachment(index)"><Close /></el-icon>
                    </div>
                  </div>
                </div>

                <div class="editor-footer">
                  <el-button type="primary" link @click="openAddAttachment" v-if="form.attachments.length === 0">
                    <el-icon><Plus /></el-icon> 添加附件
                  </el-button>
                  <span class="footer-tip" v-else>朋友圈仅支持1个附件</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item style="margin-top: 40px">
              <div style="display: flex; justify-content: flex-end; width: 100%; gap: 10px;">
                <el-button size="large" @click="$router.back()">取消</el-button>
                <el-button type="primary" size="large" :loading="submitting" @click="submitForm">{{ isEdit ? '保存修改' : '发布朋友圈' }}</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- Preview -->
      <el-col :xs="24" :sm="24" :md="10" :lg="8" class="preview-col">
        <div class="sticky-preview">
          <div class="preview-integrated">
            <div class="preview-title">
              <el-icon><Monitor /></el-icon>
              效果预览 / Mobile Preview
            </div>
            <div class="mobile-wrapper">
              <MobilePreview 
                :text="form.text" 
                :attachments="form.attachments"
                type="moment"
              />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Attachment Selection Dialog -->
    <el-dialog v-model="attachmentDialogVisible" :title="editingIndex > -1 ? '编辑附件' : '添加附件'" width="550px">
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
                <img v-if="imageForm.picUrl" :src="imageForm.picUrl" class="upload-preview" />
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
             <el-form-item label="描述">
               <el-input v-model="linkForm.desc" placeholder="请输入链接描述" />
             </el-form-item>
             <el-form-item label="封面图">
               <el-upload
                class="attachment-uploader small"
                action=""
                :http-request="(options: any) => uploadFile(options, 'link')"
                :show-file-list="false"
              >
                <img v-if="linkForm.picUrl" :src="linkForm.picUrl" class="upload-preview small" />
                <el-icon v-else class="upload-icon small"><Plus /></el-icon>
              </el-upload>
             </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="小程序" name="miniprogram">
          <el-form :model="mpForm" label-width="90px">
             <el-form-item label="标题" required>
               <el-input v-model="mpForm.title" placeholder="请输入小程序标题" />
             </el-form-item>
             <el-form-item label="AppID" required>
               <el-input v-model="mpForm.appid" placeholder="请输入小程序AppID" />
             </el-form-item>
             <el-form-item label="页面路径" required>
               <el-input v-model="mpForm.page" placeholder="例如: pages/index/index" />
             </el-form-item>
             <el-form-item label="展示图" required>
                <el-upload
                  class="attachment-uploader small"
                  action=""
                  :http-request="(options: any) => uploadFile(options, 'miniprogram')"
                  :show-file-list="false"
                >
                  <img v-if="mpForm.picUrl" :src="mpForm.picUrl" class="upload-preview small" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createMoment, getMoment, updateMoment } from '@/api/moment'
import { getUsers, getDepartments } from '@/api/user'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { uploadImg, uploadTempMedia, getImageUrl } from '@/api/media'
import { ElMessage } from 'element-plus'
import { InfoFilled, ChatLineRound, UserFilled, Folder, Plus, Monitor, PriceTag, Collection, Picture, Link, Compass, Close, EditPen } from '@element-plus/icons-vue'
import { useResponsive } from '@/hooks/useResponsive'
import MobilePreview from './MobilePreview.vue'

const router = useRouter()
const route = useRoute()
const { isMobile } = useResponsive()

const formRef = ref()
const submitting = ref(false)
const isAllRange = ref(true)

const isEdit = computed(() => route.name === 'MomentEdit')
const isCopy = computed(() => route.name === 'MomentCopy')

const allUsers = ref<any[]>([])
const allDepartments = ref<any[]>([])
const departmentUserTree = ref<any[]>([])
const tagTree = ref<any[]>([])
const selectedTreeUserKeys = ref<string[]>([])
const selectedTreeTagKeys = ref<string[]>([])
const expandedUserKeys = ref<string[]>([])
const expandedTagKeys = ref<string[]>([])

// Attachment related
const attachmentDialogVisible = ref(false)
const activeTab = ref('image')
const editingIndex = ref(-1)
const imageForm = reactive({ mediaId: '', picUrl: '' })
const linkForm = reactive({ title: '', url: '', desc: '', picUrl: '', mediaId: '' })
const mpForm = reactive({ title: '', appid: '', page: '', picUrl: '', mediaId: '', picMediaId: '' })

const form = reactive({
  taskName: '',
  text: '',
  sendType: 0,
  sendTime: '',
  visibleRange: {
    senderList: [] as string[],
    departmentList: [] as number[],
    externalContactList: {
      tagList: [] as string[]
    }
  },
  attachments: [] as any[]
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  sendTime: [{ required: true, message: '请选择定时发送时间', trigger: 'change' }]
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const openAddAttachment = () => {
  editingIndex.value = -1
  attachmentDialogVisible.value = true
}

const editAttachment = (index: number) => {
  editingIndex.value = index
  const att = form.attachments[index]
  activeTab.value = att.msgtype
  
  if (att.msgtype === 'image') {
    imageForm.mediaId = att.image.mediaId
    imageForm.picUrl = getImageUrl(att.image.mediaId, att.image.picUrl)
  } else if (att.msgtype === 'link') {
    linkForm.title = att.link.title
    linkForm.url = att.link.url
    linkForm.desc = att.link.desc
    linkForm.picUrl = att.link.picUrl
  } else if (att.msgtype === 'miniprogram') {
    mpForm.title = att.miniprogram.title
    mpForm.appid = att.miniprogram.appid
    mpForm.page = att.miniprogram.page
    mpForm.picUrl = getImageUrl(att.miniprogram.picMediaId, att.miniprogram.picUrl)
    mpForm.picMediaId = att.miniprogram.picMediaId
  }
  
  attachmentDialogVisible.value = true
}

const removeAttachment = (index: number) => {
  form.attachments.splice(index, 1)
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

const confirmAttachment = () => {
  let newAtt: any = null
  if (activeTab.value === 'image') {
    if (!imageForm.mediaId) return ElMessage.warning('请选择并上传图片')
    newAtt = {
      msgtype: 'image',
      image: { mediaId: imageForm.mediaId, picUrl: imageForm.picUrl }
    }
  } else if (activeTab.value === 'link') {
    if (!linkForm.title || !linkForm.url) return ElMessage.warning('请填写链接标题 and URL')
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
        picUrl: mpForm.picUrl,
        picMediaId: mpForm.picMediaId
      }
    }
  }

  if (editingIndex.value > -1) {
    form.attachments[editingIndex.value] = newAtt
  } else {
    form.attachments = [newAtt] // Strict limit to 1
  }
  attachmentDialogVisible.value = false
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
    const userNode = { label: user.name, isUser: true, id: `user_${user.userid}`, userid: user.userid }
    if (user.departmentIds) {
      try {
        const deptIds = JSON.parse(user.departmentIds)
        if (Array.isArray(deptIds)) {
          deptIds.forEach(deptId => {
            const parent = nodeMap.get(`dept_${deptId}`)
            if (parent) parent.children.push({ ...userNode, id: `user_${deptId}_${user.userid}` })
          })
        }
      } catch (e) {}
    }
  })
  
  const tree: any[] = []
  departments.forEach(dept => {
    const node = nodeMap.get(`dept_${dept.departmentId}`)
    if (dept.parentId === 0 || dept.parentId === null) tree.push(node)
    else {
      const parentNode = nodeMap.get(`dept_${dept.parentId}`)
      if (parentNode) parentNode.children.push(node)
      else tree.push(node)
    }
  })
  return tree
}

const loadData = async () => {
  try {
    const [deptRes, userRes, tagGroups] = await Promise.all([
      getDepartments(),
      getUsers({ page: 1, size: 1000 }),
      getTagGroups()
    ])
    allDepartments.value = deptRes as any || []
    allUsers.value = (userRes as any).content || []
    departmentUserTree.value = buildDepartmentUserTree(allDepartments.value, allUsers.value)

    const groupsWithTags = await Promise.all((tagGroups as any).map(async (g: any) => {
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
  } catch (e) {
    ElMessage.error('加载基础数据失败')
  }
}

onMounted(async () => {
  await loadData()
  if (isEdit.value || isCopy.value) {
    try {
      const data = await getMoment(route.params.id as string) as any
      form.taskName = isCopy.value ? `${data.taskName} (复制)` : data.taskName
      form.text = data.text || ''
      form.sendType = data.sendType
      form.sendTime = data.sendTime.replace('T', ' ').substring(0, 19)
      
      if (data.attachments) {
        form.attachments = JSON.parse(data.attachments)
      }
      
      if (data.visibleRangeType === 1 && data.visibleRangeUsers) {
        const range = JSON.parse(data.visibleRangeUsers)
        form.visibleRange = range
        isAllRange.value = false
        
        // Map back to tree keys
        const userKeys: string[] = []
        const userExpandKeys: string[] = []
        if (range.senderList) {
          range.senderList.forEach((uid: string) => {
            const findAndAdd = (nodes: any[], path: string[] = []) => {
              for (const node of nodes) {
                if (node.isUser && node.userid === uid) {
                  userKeys.push(node.id)
                  userExpandKeys.push(...path)
                }
                if (node.children) findAndAdd(node.children, [...path, node.id])
              }
            }
            findAndAdd(departmentUserTree.value)
          })
        }
        if (range.departmentList) {
          range.departmentList.forEach((did: number) => userKeys.push(`dept_${did}`))
        }
        selectedTreeUserKeys.value = Array.from(new Set(userKeys))
        expandedUserKeys.value = Array.from(new Set(userExpandKeys))

        if (range.externalContactList?.tagList) {
          const tids = range.externalContactList.tagList
          selectedTreeTagKeys.value = tids.map((tid: string) => `tag_${tid}`)
          
          const tagExpandKeys: string[] = []
          tagTree.value.forEach(group => {
            if (group.children.some((t: any) => tids.includes(t.tagId))) {
              tagExpandKeys.push(group.id)
            }
          })
          expandedTagKeys.value = tagExpandKeys
        }
      } else {
        isAllRange.value = true
      }
    } catch (e) {
      ElMessage.error('加载朋友圈数据失败')
    }
  }
})

const submitForm = async () => {
  formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      if (!form.text.trim() && form.attachments.length === 0) {
        return ElMessage.warning('朋友圈文字内容和附件不能同时为空')
      }

      submitting.value = true
      try {
        // Prepare visible range
        if (isAllRange.value) {
          form.visibleRange.senderList = []
          form.visibleRange.departmentList = []
        } else {
          const uniqueSenders = new Set<string>()
          selectedTreeUserKeys.value.forEach(key => {
            if (key.startsWith('user_')) {
              const parts = key.split('_')
              if (parts.length >= 2) uniqueSenders.add(parts[parts.length - 1])
            }
          })
          form.visibleRange.senderList = Array.from(uniqueSenders)
          
          form.visibleRange.departmentList = selectedTreeUserKeys.value
            .filter(k => k.startsWith('dept_'))
            .map(k => Number(k.replace('dept_', '')))
        }

        form.visibleRange.externalContactList.tagList = selectedTreeTagKeys.value
          .filter(k => k.startsWith('tag_'))
          .map(k => k.replace('tag_', ''))

        if (isEdit.value) {
          await updateMoment(route.params.id as string, form)
          ElMessage.success('更新任务成功')
        } else {
          await createMoment(form)
          ElMessage.success(isCopy.value ? '复制成功' : '发布任务成功')
        }
        router.push('/moments')
      } catch (e) {
        ElMessage.error(isEdit.value ? '更新失败' : (isCopy.value ? '复制失败' : '发布失败'))
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
  bottom: 0;
  font-size: 12px;
  color: #909399;
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
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
  max-width: 100%;
}

.att-main {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.att-icon {
  font-size: 16px;
  color: var(--el-color-primary);
}

.att-name {
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.att-ops {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-left: 6px;
  border-left: 1px solid #ebeef5;
}

.op-btn {
  font-size: 14px;
  color: #909399;
  cursor: pointer;
  padding: 2px;
  transition: all 0.2s;
}

.op-btn:hover.edit {
  color: var(--el-color-primary);
}

.op-btn:hover.delete {
  color: var(--el-color-danger);
}

.editor-footer {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.footer-tip {
  font-size: 12px;
  color: #909399;
}

.form-actions {
  margin-top: 40px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.sticky-preview {
  position: sticky;
  top: 20px;
}

.preview-integrated {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.preview-title {
  padding: 16px;
  background: linear-gradient(180deg, #f8faff 0%, #ffffff 100%);
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.preview-title .el-icon {
  color: var(--el-color-primary);
  font-size: 18px;
}

.mobile-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #f5f7fa;
}

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.node-icon {
  font-size: 16px;
}

.user-node .node-icon {
  color: #409eff;
}

.dept-node .node-icon {
  color: #e6a23c;
}

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
</style>
