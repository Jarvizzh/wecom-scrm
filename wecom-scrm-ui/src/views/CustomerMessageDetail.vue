<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/customer-messages')" title="返回列表" content="任务详情 / Task Details" />
    
    <div v-loading="loading" class="detail-container">
      <el-row :gutter="24" style="align-items: stretch;">
        <el-col :xs="24" :sm="24" :md="15" :xl="16">
          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><InfoFilled /></el-icon>
                  <span>任务基本信息</span>
                </div>
                <el-tag :type="getStatusType(taskInfo?.status)" effect="dark" round>
                  {{ getStatusLabel(taskInfo?.status) }}
                </el-tag>
              </div>
            </template>
            <el-descriptions :column="isMobile ? 1 : 2" border size="large" class="custom-desc">
              <el-descriptions-item label="任务名称" :span="2">
                <span class="text-bold">{{ taskInfo?.taskName || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="生成时间">
                {{ formatTime(taskInfo?.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="创建人">
                {{ taskInfo?.creatorUserid || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="发送类型">
                <el-tag :type="taskInfo?.sendType === 0 ? 'success' : 'warning'" size="small" effect="light">
                  {{ taskInfo?.sendType === 0 ? '立即发送' : '定时发送' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="发送时间">
                {{ taskInfo?.sendType === 1 ? formatTime(taskInfo?.sendTime) : formatTime(taskInfo?.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="失败原因" :span="2" v-if="taskInfo?.status === 3">
                <span class="text-danger"><el-icon><Warning /></el-icon> {{ taskInfo?.failMsg || '-' }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><UserFilled /></el-icon>
                  <span>发送对象明细</span>
                </div>
              </div>
            </template>
            <el-descriptions :column="1" border size="large" class="custom-desc">
              <el-descriptions-item label="执行员工">
                <template v-if="!taskInfo?.senderList || taskInfo.senderList.length === 0">
                  <el-tag type="info" effect="plain" class="custom-tag">全部员工 (所有配置了客户联系权限的员工)</el-tag>
                </template>
                <template v-else>
                  <div class="tags-wrapper">
                    <el-tag type="primary" effect="plain" class="custom-tag" v-for="uid in taskInfo.senderList" :key="uid">
                      {{ getUserName(uid) }}
                    </el-tag>
                  </div>
                </template>
              </el-descriptions-item>
              
              <el-descriptions-item label="目标客户">
                <template v-if="taskInfo?.targetType === 0">
                  <el-tag type="info" effect="plain" class="custom-tag">全部客户 (员工添加的所有外部联系人)</el-tag>
                </template>
                <template v-else>
                  <el-tag type="primary" effect="plain" class="custom-tag mb-10">条件筛选客户</el-tag>
                  <div class="filter-box" v-if="taskInfo?.targetCondition">
                    <div class="filter-item" v-if="taskInfo.targetCondition.addTimeStart">
                      <span class="f-label">添加时间：</span>
                      <span class="f-value">{{ taskInfo.targetCondition.addTimeStart }} <b>至</b> {{ taskInfo.targetCondition.addTimeEnd }}</span>
                    </div>
                    <div class="filter-item" v-if="taskInfo.targetCondition.includeTags?.length > 0">
                      <span class="f-label">包含标签：</span>
                      <span class="f-value flex-wrap">
                        <span class="condition-badge">{{ taskInfo.targetCondition.includeTagsAny ? '满足其一' : '同时满足' }}</span>
                        <el-tag size="small" type="success" effect="light" class="f-tag" v-for="t in taskInfo.targetCondition.includeTags" :key="t">
                          {{ getTagName(t) }}
                        </el-tag>
                      </span>
                    </div>
                    <div class="filter-item" v-if="taskInfo.targetCondition.excludeTags?.length > 0">
                      <span class="f-label">排除标签：</span>
                      <span class="f-value flex-wrap">
                        <el-tag size="small" type="danger" effect="light" class="f-tag" v-for="t in taskInfo.targetCondition.excludeTags" :key="t">
                          {{ getTagName(t) }}
                        </el-tag>
                      </span>
                    </div>
                  </div>
                </template>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><Document /></el-icon>
                  <span>消息内容</span>
                </div>
              </div>
            </template>
            
            <div class="raw-content-section" v-if="taskInfo?.content">
              <div class="raw-label">文本内容 (Text)：</div>
              <div class="raw-text-box">{{ taskInfo.content }}</div>
            </div>

            <div class="raw-content-section mt-15" v-if="taskInfo?.attachments?.length > 0">
              <div class="raw-label">附件清单 (Attachments：{{ taskInfo.attachments.length }})：</div>
              <div class="attachments-list">
                <div class="att-card" v-for="(att, idx) in taskInfo.attachments" :key="idx">
                  <div class="att-header">
                    <el-tag size="small" :type="getAttTagType(att.msgtype)">{{ getAttTypeName(att.msgtype) }}</el-tag>
                    <span class="att-idx">#{{ Number(idx) + 1 }}</span>
                  </div>
                  
                  <div class="att-body" v-if="att.msgtype === 'image'">
                    <div class="att-prop">
                      <span class="k">图片:</span> 
                      <div class="v-img-box">
                        <el-image 
                          class="thumb-preview" 
                          :src="getImageUrl(att.image?.mediaId, att.image?.picUrl)" 
                          :preview-src-list="[getImageUrl(att.image?.mediaId, att.image?.picUrl)]"
                          fit="cover"
                        >
                          <template #error>
                            <div class="image-error-placeholder">
                              <el-icon><Picture /></el-icon>
                              <span>图片已过期</span>
                            </div>
                          </template>
                        </el-image>
                      </div>
                    </div>
                  </div>
                  
                  <div class="att-body" v-else-if="att.msgtype === 'link'">
                    <div class="att-prop"><span class="k">标题 :</span> <span class="v truncate">{{ att.link?.title || '-' }}</span></div>
                    <div class="att-prop"><span class="k">链接 :</span> <a class="v copyable" :href="att.link?.url" target="_blank">{{ att.link?.url || '-' }}</a></div>
                    <div class="att-prop"><span class="k">描述 :</span> <span class="v">{{ att.link?.desc || '-' }}</span></div>
                    <div class="att-prop">
                      <span class="k">封面 :</span> 
                      <div class="v-img-box">
                        <el-image 
                          v-if="att.link?.picUrl"
                          class="thumb-preview" 
                          :src="att.link.picUrl" 
                          :preview-src-list="[att.link.picUrl]"
                          fit="cover"
                        >
                          <template #error>
                            <div class="image-error-placeholder">
                              <el-icon><Picture /></el-icon>
                              <span>图片已过期</span>
                            </div>
                          </template>
                        </el-image>
                        <span v-else class="v">-</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="att-body" v-else-if="att.msgtype === 'miniprogram'">
                    <div class="att-prop"><span class="k">标题 :</span> <span class="v">{{ att.miniprogram?.title || '-' }}</span></div>
                    <div class="att-prop"><span class="k">小程序AppID :</span> <span class="v copyable">{{ att.miniprogram?.appid || '-' }}</span></div>
                    <div class="att-prop"><span class="k">小程序路径 :</span> <span class="v">{{ att.miniprogram?.page || '-' }}</span></div>
                    <div class="att-prop">
                      <span class="k">封面:</span> 
                      <div class="v-img-box">
                        <el-image 
                          v-if="att.miniprogram?.picMediaId || att.miniprogram?.picUrl"
                          class="thumb-preview" 
                          :src="getImageUrl(att.miniprogram?.picMediaId, att.miniprogram?.picUrl)" 
                          :preview-src-list="[getImageUrl(att.miniprogram?.picMediaId, att.miniprogram?.picUrl)]"
                          fit="cover"
                        >
                          <template #error>
                            <div class="image-error-placeholder">
                              <el-icon><Picture /></el-icon>
                              <span>图片已过期</span>
                            </div>
                          </template>
                        </el-image>
                        <span v-else class="v">-</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <el-card v-if="taskInfo?.status === 2" class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><CircleCheckFilled /></el-icon>
                  <span>员工下发进度</span>
                </div>
                <el-button type="primary" link :loading="resultLoading" @click="fetchSendResult">
                  <el-icon><Refresh /></el-icon> 刷新进度
                </el-button>
              </div>
            </template>
            <el-table :data="sendResult?.taskList || []" border stripe v-loading="resultLoading">
              <el-table-column prop="userName" label="执行员工" width="180">
                <template #default="{ row }">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <el-icon color="#409eff"><Avatar /></el-icon>
                    <span class="text-bold">{{ row.userName || row.userId }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="发送进度" min-width="250">
                <template #default="{ row }">
                  <div class="send-progress-container">
                    <div class="progress-info">
                      <span class="p-item"><b class="c-total">{{ row.totalCount || 0 }}</b> 总数</span>
                      <span class="p-item"><b class="c-success">{{ row.successCount || 0 }}</b> 已发送</span>
                      <span class="p-item"><b class="c-fail">{{ row.failCount || 0 }}</b> 失败</span>
                    </div>
                    <el-progress 
                      :percentage="row.totalCount > 0 ? Math.round((row.successCount / row.totalCount) * 100) : 0" 
                      :status="row.successCount === row.totalCount && row.totalCount > 0 ? 'success' : ''"
                    />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="showMemberDetail(row)">
                    查看明细
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <!-- Member Detail Dialog -->
          <el-dialog v-model="memberDetailVisible" :title="`发送明细 - ${currentMember?.userName}`" :width="isMobile ? '95%' : '800px'">
            <el-table :data="memberDetail?.customerList || []" border stripe v-loading="memberDetailLoading" height="500">
              <el-table-column prop="customerName" label="客户" min-width="150" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : (row.status === 2 || row.status === 3 ? 'danger' : 'info')">
                    {{ row.status === 1 ? '已发送' : (row.status === 2 ? '非好友' : (row.status === 3 ? '接收上限' : '未发送')) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="sendTime" label="发送时间" width="180">
                <template #default="{ row }">
                  {{ formatTime(row.sendTime) }}
                </template>
              </el-table-column>
            </el-table>
          </el-dialog>

        </el-col>

        <!-- Message Content Preview -->
        <el-col :xs="24" :sm="24" :md="9" :xl="8">
          <div class="preview-affix">
            <div class="preview-integrated">
              <div class="preview-title">
                <el-icon><Monitor /></el-icon>
                <span>客户视角终端预览</span>
              </div>
              <div class="preview-wrapper">
                 <MobilePreview :text="taskInfo?.content || ''" :attachments="taskInfo?.attachments || []" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCustomerMessage, getCustomerMessageSendResult, getMemberSendResult } from '@/api/customerMessage'
import { getUsers } from '@/api/user'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { ElMessage } from 'element-plus'
import { InfoFilled, UserFilled, Monitor, Warning, Document, Picture, CircleCheckFilled, Refresh, Avatar } from '@element-plus/icons-vue'
import MobilePreview from './MobilePreview.vue'
import { getImageUrl } from '@/api/media'
import { useResponsive } from '@/hooks/useResponsive'

const { isMobile } = useResponsive()

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const taskInfo = ref<any>(null)
const allUsers = ref<any[]>([])
const allTagGroups = ref<any[]>([])
const sendResult = ref<any>(null)
const resultLoading = ref(false)
const memberDetailVisible = ref(false)
const memberDetail = ref<any>(null)
const memberDetailLoading = ref(false)
const currentMember = ref<any>(null)

const fetchSendResult = async () => {
  const id = Number(route.params.id)
  resultLoading.value = true
  try {
    const res = await getCustomerMessageSendResult(id)
    sendResult.value = res
  } catch (error) {
    console.error('Failed to fetch send result', error)
  } finally {
    resultLoading.value = false
  }
}

const showMemberDetail = async (member: any) => {
  currentMember.value = member
  memberDetailVisible.value = true
  memberDetailLoading.value = true
  try {
    const res = await getMemberSendResult(Number(route.params.id), member.userId)
    memberDetail.value = res
  } catch (error) {
    ElMessage.error('获取明细失败')
  } finally {
    memberDetailLoading.value = false
  }
}

onMounted(async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('缺少任务ID')
    router.back()
    return
  }

  loading.value = true
  try {
    try {
      const userRes = await getUsers({ page: 1, size: 1000 })
      allUsers.value = (userRes as any).content || []
    } catch (e) {
      console.warn('Failed to load users', e)
    }

    try {
      const groups = await getTagGroups() as any
      const groupsWithTags = await Promise.all(groups.map(async (g: any) => {
        const tags = await getTagsByGroup(g.groupId) as any
        return { ...g, tags }
      }))
      allTagGroups.value = groupsWithTags
    } catch (e) {
      console.warn('Failed to load tags', e)
    }

    const res = await getCustomerMessage(id) as any
    if (res) {
      taskInfo.value = {
        ...res,
        attachments: typeof res.attachments === 'string' ? JSON.parse(res.attachments) : (res.attachments || []),
        targetCondition: typeof res.targetCondition === 'string' ? JSON.parse(res.targetCondition) : (res.targetCondition || {}),
        senderList: typeof res.senderList === 'string' ? JSON.parse(res.senderList) : (res.senderList || [])
      }
      
      if (taskInfo.value.status === 2) {
        fetchSendResult()
      }
    }
  } catch (error) {
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
})

const getStatusLabel = (status: number | undefined) => {
  if (status === undefined) return '-'
  const labels = ['待发送', '发送中', '已下发', '发送失败']
  return labels[status] || '未知'
}

const getStatusType = (status: number | undefined) => {
  if (status === undefined) return 'info'
  const types = ['info', 'warning', 'success', 'danger']
  return types[status] || 'info'
}

const getUserName = (userid: string) => {
  const user = allUsers.value.find(u => u.userid === userid)
  return user ? user.name : userid
}

const getTagName = (tagId: string) => {
  for (const group of allTagGroups.value) {
    if (group.tags) {
      const tag = group.tags.find((t: any) => t.tagId === tagId)
      if (tag) return tag.name
    }
  }
  return tagId
}

const getAttTypeName = (type: string) => {
  const map: any = { image: '图片 (Image)', link: '图文链接 (Link)', miniprogram: '小程序 (MiniProgram)' }
  return map[type] || type
}

const getAttTagType = (type: string) => {
  if (type === 'image') return 'success'
  if (type === 'link') return 'primary'
  if (type === 'miniprogram') return 'warning'
  return 'info'
}

const formatTime = (time: string | number | undefined) => {
  if (!time) return '-'
  let date: Date
  if (typeof time === 'number') {
    date = new Date(time < 10000000000 ? time * 1000 : time)
  } else {
    if (/^\d+$/.test(time)) {
      const num = parseInt(time, 10)
      date = new Date(num < 10000000000 ? num * 1000 : num)
    } else {
      date = new Date(time.replace(' ', 'T'))
    }
  }
  
  if (isNaN(date.getTime())) return time.toString()
  
  const Y = date.getFullYear()
  const M = String(date.getMonth() + 1).padStart(2, '0')
  const D = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.detail-container {
  margin-top: 24px;
}
.mb-20 {
  margin-bottom: 20px;
}
.mb-10 {
  margin-bottom: 10px;
}
.mt-15 {
  margin-top: 15px;
}

/* Card Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-title .el-icon {
  color: var(--el-color-primary);
  font-size: 18px;
}

/* Descriptions Customization */
.custom-desc :deep(.el-descriptions__label) {
  width: 120px;
  color: #606266;
  background-color: #f8faff !important;
  font-weight: 500;
}
.custom-desc :deep(.el-descriptions__content) {
  color: #303133;
  line-height: 1.6;
}
.text-bold {
  font-weight: 600;
  font-size: 15px;
}
.text-danger {
  color: var(--el-color-danger);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Tags and Target Box */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.custom-tag {
  border-radius: 4px;
}
.filter-box {
  background: #f8faff;
  padding: 16px;
  border-radius: 6px;
  border: 1px solid #e4efff;
}
.filter-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}
.filter-item:last-child {
  margin-bottom: 0;
}
.f-label {
  color: #606266;
  width: 80px;
  flex-shrink: 0;
  padding-top: 2px;
}
.f-value {
  flex: 1;
  color: #303133;
  display: flex;
  align-items: center;
}
.flex-wrap {
  flex-wrap: wrap;
  gap: 8px;
}
.condition-badge {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 4px;
}

/* Raw Content Section */
.raw-label {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  font-size: 14px;
}
.raw-text-box {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  padding: 12px 16px;
  border-radius: 6px;
  color: #606266;
  white-space: pre-wrap;
  font-family: monospace;
  line-height: 1.5;
  font-size: 13px;
}
.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.att-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}
.att-header {
  background: #f8f8f8;
  padding: 8px 12px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.att-idx {
  color: #909399;
  font-size: 13px;
  font-family: monospace;
}
.att-body {
  padding: 12px;
  background: #fff;
}
.att-prop {
  margin-bottom: 8px;
  font-size: 13px;
  line-height: 1.5;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  text-align: left;
}
.att-prop:last-child {
  margin-bottom: 0;
}
.att-prop .k {
  color: #909399;
  width: 140px;
  flex-shrink: 0;
  text-align: left;
}
.att-prop .v {
  color: #303133;
  word-break: break-all;
}
.v.copyable {
  font-family: monospace;
  background: #f4f4f5;
  padding: 1px 4px;
  border-radius: 3px;
}
.v.truncate {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  line-clamp: 1;
  overflow: hidden;
}

.att-prop .v-img-box {
  flex: 1;
  display: flex;
  justify-content: flex-start;
}

.thumb-preview {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: transform 0.2s;
}

.thumb-preview:hover {
  transform: scale(1.05);
}

.image-error-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 12px;
  gap: 4px;
}

.image-error-placeholder .el-icon {
  font-size: 20px;
}

/* Preview Column */
.preview-affix {
  position: sticky;
  top: 24px;
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
.preview-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
  background: #f0f2f5;
  min-height: 600px;
}

.send-progress-container {
  padding: 8px 0;
}
.progress-info {
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}
.p-item {
  margin-right: 15px;
}
.p-item b {
  font-size: 16px;
  font-family: Arial;
}
.c-total { color: #409eff; }
.c-success { color: #67c23a; }
.c-fail { color: #f56c6c; }
</style>
