<template>
  <div class="app-container">
    <el-page-header @back="$router.push('/moments')" title="返回列表" content="任务详情 / Task Details" />
    
    <div v-loading="loading" class="detail-container">
      <el-row :gutter="24" style="align-items: stretch;" v-if="moment">
        <el-col :xs="24" :sm="24" :md="15" :xl="16">
          <!-- Basic Info -->
          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><InfoFilled /></el-icon>
                  <span>任务基本信息</span>
                </div>
                <div class="header-ops">
                  <el-tag :type="getStatusType(moment.status)" effect="dark" round class="mr-10">
                    {{ getStatusLabel(moment.status) }}
                  </el-tag>
                </div>
              </div>
            </template>
            <el-descriptions :column="isMobile ? 1 : 2" border size="large" class="custom-desc">
              <el-descriptions-item label="任务名称" :span="2">
                <span class="text-bold">{{ moment.taskName || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="生成时间">
                {{ formatDateTime(moment.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="创建人">
                {{ getUserName(moment.creatorUserid) }}
              </el-descriptions-item>
              <el-descriptions-item label="发送类型">
                <el-tag :type="moment.sendType === 0 ? 'success' : 'warning'" size="small" effect="light">
                  {{ moment.sendType === 0 ? '立即发送' : '定时发送' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="计划时间">
                {{ moment.sendType === 1 ? formatDateTime(moment.sendTime) : formatDateTime(moment.createTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- Visible Range -->
          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><UserFilled /></el-icon>
                  <span>发布范围明细</span>
                </div>
              </div>
            </template>
            <el-descriptions :column="1" border size="large" class="custom-desc">
              <el-descriptions-item label="执行员工">
                <template v-if="!visibleRange.senderList || visibleRange.senderList.length === 0">
                  <el-tag type="info" effect="plain" class="custom-tag">全部员工</el-tag>
                </template>
                <template v-else>
                  <div class="tags-wrapper">
                    <el-tag type="primary" effect="plain" class="custom-tag" v-for="uid in visibleRange.senderList" :key="uid">
                      {{ getUserName(uid) }}
                    </el-tag>
                  </div>
                </template>
              </el-descriptions-item>
              
              <el-descriptions-item label="可见客户">
                <template v-if="!visibleRange.externalContactList?.tagList?.length">
                  <el-tag type="info" effect="plain" class="custom-tag">全部客户</el-tag>
                </template>
                <template v-else>
                  <div class="tags-wrapper">
                    <el-tag type="success" effect="plain" class="custom-tag" v-for="tid in visibleRange.externalContactList.tagList" :key="tid">
                      {{ getTagName(tid) }}
                    </el-tag>
                  </div>
                </template>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- Content -->
          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><Document /></el-icon>
                  <span>朋友圈内容</span>
                </div>
              </div>
            </template>
            
            <div class="raw-content-section" v-if="moment.text">
              <div class="raw-label">文本内容 (Text)：</div>
              <div class="raw-text-box">{{ moment.text }}</div>
            </div>

            <div class="raw-content-section mt-15" v-if="attachments.length > 0">
              <div class="raw-label">附件清单 (Attachments：{{ attachments.length }})：</div>
              <div class="attachments-list">
                <div class="att-card" v-for="(att, idx) in attachments" :key="idx">
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
                        />
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
                        />
                        <span v-else class="v">-</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="att-body" v-else-if="att.msgtype === 'miniprogram'">
                    <div class="att-prop"><span class="k">标题 :</span> <span class="v">{{ att.miniprogram?.title || '-' }}</span></div>
                    <div class="att-prop"><span class="k">小程序AppID :</span> <span class="v copyable">{{ att.miniprogram?.appid || '-' }}</span></div>
                    <div class="att-prop"><span class="k">小程序路径 :</span> <span class="v">{{ att.miniprogram?.page || '-' }}</span></div>
                    <div class="att-prop">
                      <span class="k">展示图:</span> 
                      <div class="v-img-box">
                        <el-image 
                          v-if="att.miniprogram?.picMediaId || att.miniprogram?.picUrl"
                          class="thumb-preview" 
                          :src="getImageUrl(att.miniprogram?.picMediaId, att.miniprogram?.picUrl)" 
                          :preview-src-list="[getImageUrl(att.miniprogram?.picMediaId, att.miniprogram?.picUrl)]"
                          fit="cover"
                        />
                        <span v-else class="v">-</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="!moment.text && attachments.length === 0" class="empty-text">无内容信息</div>
          </el-card>

          <!-- Records -->
          <el-card class="detail-card mb-20" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><CircleCheckFilled /></el-icon>
                  <span>员工发布明细</span>
                </div>
                <el-button type="primary" link :loading="syncing" @click="handleSync">
                  <el-icon><Refresh /></el-icon> 刷新状态
                </el-button>
              </div>
            </template>
            <el-table :data="records" border stripe v-loading="recordLoading">
              <el-table-column prop="userid" label="执行员工" width="180">
                <template #default="{ row }">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <el-icon color="#409eff"><Avatar /></el-icon>
                    <span class="text-bold">{{ getUserName(row.userid) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="publishStatus" label="发布状态" width="150" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.publishStatus === 1 ? 'success' : 'info'" size="small">
                    {{ scope.row.publishStatus === 1 ? '已发布' : '未发布' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="publishTime" label="发布时间" min-width="180" align="center">
                <template #default="scope">
                  {{ formatDateTime(scope.row.publishTime) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <!-- Preview Column -->
        <el-col :xs="24" :sm="24" :md="9" :xl="8">
          <div class="preview-affix">
            <div class="preview-integrated">
              <div class="preview-title">
                <el-icon><Monitor /></el-icon>
                <span>客户视角终端预览</span>
              </div>
              <div class="preview-wrapper">
                 <MobilePreview :text="moment.text || ''" :attachments="attachments" type="moment" />
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { getMoment, getMomentRecords, syncMomentStatuses } from '@/api/moment'
import { getUsers } from '@/api/user'
import { getTagGroups, getTagsByGroup } from '@/api/tag'
import { ElMessage } from 'element-plus'
import { InfoFilled, UserFilled, Monitor, Document, CircleCheckFilled, Refresh, Avatar } from '@element-plus/icons-vue'
import { getImageUrl } from '@/api/media'
import { useResponsive } from '@/hooks/useResponsive'
import MobilePreview from './MobilePreview.vue'

const route = useRoute()
const { isMobile } = useResponsive()

const loading = ref(false)
const recordLoading = ref(false)
const syncing = ref(false)
const moment = ref<any>(null)
const records = ref([])
const userMap = reactive<Record<string, string>>({})
const tagMap = reactive<Record<string, string>>({})

const attachments = computed(() => {
  if (!moment.value?.attachments) return []
  try {
    return JSON.parse(moment.value.attachments)
  } catch (e) {
    return []
  }
})

const visibleRange = computed(() => {
  if (!moment.value?.visibleRangeUsers) return {}
  try {
    return JSON.parse(moment.value.visibleRangeUsers)
  } catch (e) {
    return {}
  }
})

const fetchData = async () => {
  const id = route.params.id as string
  if (!id) return
  
  loading.value = true
  try {
    moment.value = await getMoment(id)
    fetchRecords()
  } catch (e) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const fetchRecords = async () => {
  const id = route.params.id as string
  recordLoading.value = true
  try {
    records.value = await getMomentRecords(id) as any
  } catch (e) {
    ElMessage.error('获取明细失败')
  } finally {
    recordLoading.value = false
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
  } catch (e) {}
}

onMounted(() => {
  fetchData()
  loadBaseData()
})

const handleSync = async () => {
  syncing.value = true
  try {
    await syncMomentStatuses()
    ElMessage.success('同步请求已提交')
    setTimeout(fetchRecords, 2000)
  } catch (e) {
    ElMessage.error('同步失败')
  } finally {
    syncing.value = false
  }
}

const getStatusLabel = (status: number) => {
  const map: any = { 0: '处理中', 1: '已发布', 2: '发布失败', 3: '待发布' }
  return map[status] || '未知'
}

const getStatusType = (status: number) => {
  const map: any = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
  return map[status] || 'info'
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

const getUserName = (uid: string) => userMap[uid] || uid
const getTagName = (tid: string) => tagMap[tid] || tid
const formatDateTime = (val: string) => {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
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
.mt-15 {
  margin-top: 15px;
}
.mr-10 {
  margin-right: 10px;
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

/* Tags and Target Box */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.custom-tag {
  border-radius: 4px;
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

.empty-text {
  color: #909399;
  font-size: 13px;
  padding: 10px;
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
</style>
