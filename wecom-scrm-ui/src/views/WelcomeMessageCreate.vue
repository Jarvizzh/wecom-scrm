<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/welcome-message')" :title="isEdit ? '编辑欢迎语 / Edit Welcome Message' : '新建欢迎语 / New Welcome Message'" />
    
    <el-row :gutter="20" style="margin-top: 20px; align-items: stretch;">
      <el-col :span="14" :lg="16">
        <el-card>
          <el-form :model="form" ref="formRef" label-width="120px" :rules="rules">
            <div class="section-title">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息 / Basic Info</span>
            </div>
            
            <el-form-item label="欢迎语名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入欢迎语名称，如：默认欢迎语" />
            </el-form-item>
            
            <div class="section-title">
              <el-icon><Filter /></el-icon>
              <span>适用设置 / Targeting</span>
            </div>

            <el-form-item label="适用范围">
              <el-radio-group v-model="isAllRange">
                <el-radio :label="true">全部员工</el-radio>
                <el-radio :label="false">指定员工</el-radio>
              </el-radio-group>
              
              <div v-show="!isAllRange" style="margin-top: 10px; width: 100%">
                <el-tree-select
                  v-model="selectedTreeUserIds"
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
              </div>
            </el-form-item>

            <div class="section-title">
              <el-icon><PriceTag /></el-icon>
              <span>策略设置 / Strategy</span>
            </div>

            <el-form-item label="自动打标签">
              <el-tree-select
                v-model="selectedTreeTagIds"
                :data="tagTree"
                multiple
                show-checkbox
                filterable
                node-key="id"
                :default-expanded-keys="expandedTagKeys"
                placeholder="请选择标签 (客户添加后自动打标)"
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
              <div style="font-size: 12px; color: #909399; margin-top: 5px;">新客户添加员工后，系统将自动为客户打上选中的企业标签</div>
            </el-form-item>

            <div class="section-title">
              <el-icon><ChatLineRound /></el-icon>
              <span>消息内容 / Content</span>
            </div>
            
            <el-form-item label="欢迎语文字" prop="text" class="content-form-item">
              <div class="message-editor-container">
                <div class="text-area-wrapper">
                  <el-input 
                    v-model="form.text" 
                    type="textarea" 
                    :rows="6" 
                    placeholder="新客户添加员工后，将自动发送该文字消息..." 
                    maxlength="600"
                    resize="none"
                  />
                  <div class="char-count">{{ form.text.length }}/600</div>
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
                  <el-button type="primary" link @click="openAddAttachment" v-if="form.attachments.length < 9">
                    <el-icon><Plus /></el-icon> 添加附件
                  </el-button>
                  <span class="footer-tip" v-else>已达到附件上限 (9个)</span>
                </div>
              </div>
            </el-form-item>

            <el-form-item style="margin-top: 40px">
              <div style="display: flex; justify-content: flex-end; width: 100%; gap: 10px;">
                <el-button size="large" @click="$router.push('/welcome-message')">取消</el-button>
                <el-button type="primary" size="large" :loading="submitting" @click="submitForm">{{ isEdit ? '保存修改' : '创建欢迎语' }}</el-button>
              </div>
            </el-form-item>
            
          </el-form>
        </el-card>
      </el-col>
      
      <!-- Right Side Mobile Preview -->
      <el-col :span="10" :lg="8">
        <div class="preview-affix">
          <div class="preview-integrated">
            <div class="preview-title">
              <el-icon><Monitor /></el-icon>
              <span>实时效果预览</span>
            </div>
            <MobilePreview :text="form.text" :attachments="form.attachments" type="customer" />
          </div>
        </div>
      </el-col>
    </el-row>

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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getWelcomeMsg, saveWelcomeMsg } from '@/api/welcomeMsg'
import { getUsers, getDepartments } from '@/api/user'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { uploadImg, uploadTempMedia } from '@/api/media'
import { ElMessage } from 'element-plus'
import { Plus, EditPen, Picture, Link, Compass, Close, Monitor, InfoFilled, ChatLineRound, Filter, UserFilled, Folder, PriceTag, Collection } from '@element-plus/icons-vue'
import MobilePreview from './MobilePreview.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const submitting = ref(false)
const attachmentDialogVisible = ref(false)
const activeTab = ref('image')
const users = ref<any[]>([])
const departments = ref<any[]>([])
const departmentUserTree = ref<any[]>([])
const tagTree = ref<any[]>([])
const isAllRange = ref(true)
const selectedTreeUserIds = ref<string[]>([])
const selectedTreeTagIds = ref<string[]>([])
const expandedUserKeys = ref<string[]>([])
const expandedTagKeys = ref<string[]>([])

const formRef = ref()
const form = reactive({
  id: null as number | null,
  name: '',
  text: '',
  attachments: [] as any[],
  tagIds: [] as string[],
  userIds: '[]',
  departmentIds: '[]',
  isDefault: 0
})

const rules = {
  name: [{ required: true, message: '请输入欢迎语名称', trigger: 'blur' }]
}

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
    Object.assign(imageForm, { mediaId: att.image.media_id || att.image.mediaId, picUrl: att.image.pic_url || att.image.picUrl || '' })
  } else if (att.msgtype === 'link') {
    Object.assign(linkForm, { 
      title: att.link.title, 
      url: att.link.url, 
      desc: att.link.desc || '', 
      picUrl: att.link.picUrl || att.link.pic_url || '' 
    })
  } else if (att.msgtype === 'miniprogram') {
    Object.assign(mpForm, { 
      title: att.miniprogram.title, 
      appid: att.miniprogram.appid, 
      page: att.miniprogram.page, 
      picMediaId: att.miniprogram.pic_media_id || att.miniprogram.picMediaId,
      picUrl: att.miniprogram.pic_url || att.miniprogram.picUrl || '' 
    })
  }
  attachmentDialogVisible.value = true
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
    const userNode = { label: user.name, isUser: true, avatar: user.avatar, id: `user_${user.userid}` }
    const deptIdsStr = user.departmentIds || user.departments
    if (deptIdsStr) {
      try {
        const deptIds = typeof deptIdsStr === 'string' ? JSON.parse(deptIdsStr) : deptIdsStr
        if (Array.isArray(deptIds)) {
          deptIds.forEach(deptId => {
            const parent = nodeMap.get(`dept_${deptId}`)
            if (parent) {
              parent.children.push({ ...userNode, id: `user_${deptId}_${user.userid}` })
            }
          })
        }
      } catch (e) {
        const parent = nodeMap.get(`dept_${deptIdsStr}`)
        if (parent) parent.children.push({ ...userNode, id: `user_${deptIdsStr}_${user.userid}` })
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
  await Promise.all([loadTargetingData(), loadTagGroups()])
  
  if (isEdit.value) {
    const id = Number(route.params.id)
    try {
      const row = await getWelcomeMsg(id) as any
      if (row) {
        Object.assign(form, row)
        if (typeof row.attachments === 'string') {
          form.attachments = JSON.parse(row.attachments)
        }

        if (row.tagIds) {
          try {
            const rawTagIds = typeof row.tagIds === 'string' ? JSON.parse(row.tagIds) : row.tagIds
            form.tagIds = rawTagIds
            // Map raw tagIds to tree node IDs
            const mappedTagKeys = rawTagIds.map((tid: string) => `tag_${tid}`)
            selectedTreeTagIds.value = mappedTagKeys
            
            // Expand groups that contain selected tags
            const tagGroupKeys = new Set<string>()
            mappedTagKeys.forEach((key: string) => {
              const groupNode = tagTree.value.find(g => g.children?.some((t: any) => t.id === key))
              if (groupNode) tagGroupKeys.add(groupNode.id)
            })
            expandedTagKeys.value = Array.from(tagGroupKeys)
          } catch (e) {
            form.tagIds = []
            selectedTreeTagIds.value = []
          }
        } else {
          form.tagIds = []
          selectedTreeTagIds.value = []
        }
        
        // Handle range selection matching in tree
        const uids = form.userIds ? JSON.parse(form.userIds) : []
        const initialKeys: string[] = []
        uids.forEach((uid: string) => {
          const findInTree = (nodes: any[]) => {
            nodes.forEach(n => {
              // Match user nodes that end with the userid
              if (n.isUser && n.id && n.id.endsWith(`_${uid}`)) {
                initialKeys.push(n.id)
              }
              if (n.children) findInTree(n.children)
            })
          }
          findInTree(departmentUserTree.value)
        })
        
        selectedTreeUserIds.value = initialKeys
        isAllRange.value = uids.length === 0
        
        // Expand departments that contain selected users
        const deptKeys = new Set<string>()
        initialKeys.forEach((key: string) => {
          if (key.startsWith('user_')) {
            const parts = key.split('_')
            if (parts.length >= 2) {
              deptKeys.add(`dept_${parts[1]}`)
            }
          }
        })
        expandedUserKeys.value = Array.from(deptKeys)
        // Wait for next tick to ensure v-model sets correctly
      }
    } catch (e) {
      ElMessage.error('加载详情失败，请重试')
    }
  }
})

const loadTagGroups = async () => {
  try {
    const groups = (await getTagGroups()) as any
    const groupsWithTags = await Promise.all(
      groups.map(async (g: any) => {
        try {
          const tags = (await getTagsByGroup(g.groupId)) as any
          return { ...g, tags }
        } catch (e) {
          return { ...g, tags: [] }
        }
      })
    )
    
    // Build Tag Tree structure
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
    console.error('加载标签组失败', e)
  }
}

watch(selectedTreeTagIds, (keys) => {
  const finalTags = keys
    .filter(key => key.startsWith('tag_'))
    .map(key => key.replace('tag_', ''))
  form.tagIds = finalTags
})

const loadTargetingData = async () => {
  try {
    const [uRes, dRes] = await Promise.all([
      getUsers({ page: 1, size: 1000 }),
      getDepartments()
    ])
    users.value = (uRes as any).content || []
    departments.value = dRes as any || []
    departmentUserTree.value = buildDepartmentUserTree(departments.value, users.value)
  } catch (e) {
    console.error('加载员工/部门数据失败', e)
  }
}

watch(() => isAllRange.value, (val) => {
  if (val) {
    selectedTreeUserIds.value = []
  }
})

watch(selectedTreeUserIds, (keys) => {
  const uniqueSenders = new Set<string>()
  keys.forEach(key => {
    if (key.startsWith('user_')) {
      const parts = key.split('_')
      if (parts.length >= 3) {
        uniqueSenders.add(parts.slice(2).join('_'))
      } else if (parts.length === 2) {
        uniqueSenders.add(parts[1])
      }
    }
  })
  form.userIds = JSON.stringify(Array.from(uniqueSenders))
  form.departmentIds = '[]'
})

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
      image: { media_id: imageForm.mediaId, pic_url: imageForm.picUrl }
    }
  } else if (activeTab.value === 'link') {
    if (!linkForm.title || !linkForm.url) return ElMessage.warning('请填写链接标题和URL')
    newAtt = {
      msgtype: 'link',
      link: { 
        title: linkForm.title,
        url: linkForm.url,
        desc: linkForm.desc,
        picUrl: linkForm.picUrl,
        pic_url: linkForm.picUrl
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
        pic_url: mpForm.picUrl,
        pic_media_id: mpForm.picMediaId
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

const submitForm = async () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return
    
    if (!form.text.trim() && form.attachments.length === 0) {
      return ElMessage.warning('欢迎语文字和附件不能同时为空')
    }

    // Clear range if "All" is selected
    if (isAllRange.value) {
      form.userIds = '[]'
      form.departmentIds = '[]'
    }

    submitting.value = true
    try {
      const data = {
        ...form,
        attachments: JSON.stringify(form.attachments),
        tagIds: JSON.stringify(form.tagIds)
      }
      await saveWelcomeMsg(data)
      ElMessage.success('保存成功')
      router.push('/welcome-message')
    } catch (e) {
      ElMessage.error('保存失败')
    } finally {
      submitting.value = false
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

.tree-node-content {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tree-node-content .node-icon {
  font-size: 16px;
}

.user-node .node-icon {
  color: #409eff;
}

.dept-node .node-icon {
  color: #e6a23c;
}

/* Mobile Preview Panel */
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
