<template>
  <div class="mobile-sim-container">
    <!-- Phone Frame -->
    <div class="phone-frame">
      <!-- Top Status Bar -->
      <div class="status-bar">
        <span>9:41 AM</span>
        <div class="status-icons">
          <el-icon><Connection /></el-icon>
          <el-icon><Opportunity /></el-icon>
          <el-icon><Cpu /></el-icon>
        </div>
      </div>
      
      <!-- Header -->
      <div class="wechat-header">
        <el-icon class="header-back"><ArrowLeft /></el-icon>
        <span class="header-title">{{ type === 'moment' ? '朋友圈' : '客服账号' }}</span>
        <el-icon class="header-more" v-if="type !== 'moment'"><MoreFilled /></el-icon>
        <el-icon class="header-camera" v-else><CameraFilled /></el-icon>
      </div>

      <!-- Chat View -->
      <div class="wechat-content" v-if="type !== 'moment'">
        <!-- Text Message if any -->
        <div class="msg-item" v-if="text">
          <div class="msg-avatar">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="msg-bubble text" style="white-space: pre-wrap;">
            {{ text }}
          </div>
        </div>

        <!-- Attachments -->
        <template v-for="(att, idx) in formattedAttachments" :key="idx">
          <div class="msg-item">
            <div class="msg-avatar">
              <el-icon><UserFilled /></el-icon>
            </div>
            
            <!-- Image Attachment -->
            <div v-if="att.msgtype === 'image'" class="msg-bubble image">
              <img 
                v-if="!imageErrors[idx]"
                :src="getImageUrl(att.image?.mediaId || att.image?.media_id, att.image?.picUrl || att.image?.pic_url)" 
                alt="image" 
                @error="handleImageError(idx)"
              />
              <div v-else class="preview-img-error">
                <el-icon><Warning /></el-icon>
                <span>图片已过期</span>
              </div>
            </div>

            <!-- Link Attachment -->
            <div v-else-if="att.msgtype === 'link'" class="msg-bubble link">
              <div class="link-info">
                <div class="link-title">{{ att.link?.title }}</div>
                <div class="link-desc-row">
                  <span class="link-desc">{{ att.link?.desc }}</span>
                  <img v-if="att.link?.picUrl" :src="att.link.picUrl" class="link-thumb" />
                </div>
              </div>
            </div>

            <!-- Mini Program Attachment -->
            <div v-else-if="att.msgtype === 'miniprogram'" class="msg-bubble miniprogram">
              <div class="mp-header">
                <el-icon size="14"><Compass /></el-icon>
                <span>{{ att.miniprogram?.title }}</span>
              </div>
              <div class="mp-content">
                <img 
                  v-if="!mpErrors[idx]"
                  :src="getImageUrl(att.miniprogram?.picMediaId || att.miniprogram?.pic_media_id, att.miniprogram?.picUrl || att.miniprogram?.pic_url)" 
                  class="mp-thumb" 
                  @error="handleMpError(idx)"
                />
                <div v-else class="preview-mp-error">
                  <el-icon><Warning /></el-icon>
                  <span>图片已过期</span>
                </div>
              </div>
              <div class="mp-footer">
                <el-icon size="12"><Link /></el-icon>
                <span>小程序</span>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- Moment View -->
      <div class="moment-content" v-else>
        <div class="moment-post">
          <div class="moment-avatar">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="moment-main">
            <div class="moment-user">客户经理</div>
            <div class="moment-text" v-if="text">{{ text }}</div>
            
            <div class="moment-attachments">
              <template v-for="(att, idx) in formattedAttachments" :key="idx">
                <!-- Image -->
                <div v-if="att.msgtype === 'image'" class="moment-image">
                  <img 
                    v-if="!imageErrors[idx]"
                    :src="getImageUrl(att.image?.mediaId || att.image?.media_id, att.image?.picUrl || att.image?.pic_url)" 
                    @error="handleImageError(idx)"
                  />
                  <div v-else class="preview-img-error moment-placeholder">
                    <el-icon><Warning /></el-icon>
                  </div>
                </div>

                <!-- Link -->
                <div v-else-if="att.msgtype === 'link'" class="moment-link-card">
                  <img v-if="att.link?.picUrl" :src="att.link.picUrl" class="link-thumb" />
                  <div class="link-title">{{ att.link?.title || '链接标题' }}</div>
                </div>

                <!-- Mini Program -->
                <div v-else-if="att.msgtype === 'miniprogram'" class="moment-link-card mp">
                  <img v-if="att.miniprogram?.picUrl" :src="att.miniprogram.picUrl" class="link-thumb" />
                  <div class="link-content">
                    <div class="link-title">{{ att.miniprogram?.title || '小程序标题' }}</div>
                    <div class="link-footer">
                      <el-icon size="12"><Compass /></el-icon>
                      <span>小程序</span>
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <div class="moment-footer">
              <span class="moment-time">刚刚</span>
              <div class="moment-actions">
                <div class="action-dot"></div>
                <div class="action-dot"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer (Only for Chat) -->
      <div class="wechat-footer" v-if="type !== 'moment'">
        <el-icon class="footer-icon"><Microphone /></el-icon>
        <div class="footer-input"></div>
        <el-icon class="footer-icon"><ChatLineRound /></el-icon>
        <el-icon class="footer-icon"><CirclePlus /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getImageUrl } from '@/api/media'
import {
  Link, Compass, ArrowLeft, MoreFilled, UserFilled, Microphone, ChatLineRound, CirclePlus,
  Connection, Opportunity, Cpu, Warning, CameraFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  text: String,
  type: {
    type: String,
    default: 'chat' // chat, moment
  },
  attachments: {
    type: [Array, String],
    default: () => []
  }
})

const formattedAttachments = computed<any[]>(() => {
  if (!props.attachments) return []
  try {
    return typeof props.attachments === 'string' ? JSON.parse(props.attachments) : props.attachments
  } catch (e) {
    return []
  }
})

const imageErrors = ref<any>({})
const mpErrors = ref<any>({})

const handleImageError = (idx: number) => {
  imageErrors.value[idx] = true
}

const handleMpError = (idx: number) => {
  mpErrors.value[idx] = true
}

// Reset errors when attachments change to avoid sticky "expired" states
watch(() => props.attachments, () => {
  imageErrors.value = {}
  mpErrors.value = {}
}, { deep: true })
</script>

<style scoped>
.mobile-sim-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: transparent;
  width: 100%;
}

.phone-frame {
  width: 375px;
  height: 667px;
  background: #f5f5f5;
  border: 12px solid #333;
  border-radius: 40px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transform: scale(0.85); /* Slightly smaller scale for common containers */
  margin: -40px 0;
}

.status-bar {
  height: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  font-size: 12px;
  color: #333;
}

.status-icons {
  display: flex;
  gap: 5px;
}

.wechat-header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  background: #f5f5f5;
  border-bottom: 0.5px solid #ddd;
}

.header-title {
  font-weight: 500;
  font-size: 16px;
}

.header-back, .header-more {
  font-size: 18px;
  cursor: pointer;
}

.wechat-content {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.wechat-content::-webkit-scrollbar {
  width: 4px;
}
.wechat-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 2px;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  background: #409EFF;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.msg-bubble {
  max-width: 250px;
  padding: 10px 12px;
  border-radius: 6px;
  position: relative;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-all;
}

.msg-bubble.text {
  background: #fff;
  color: #333;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.msg-bubble.text::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 14px;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid #fff;
}

.msg-bubble.image {
  background: transparent;
  padding: 0;
}

.msg-bubble.image img {
  max-width: 150px;
  border-radius: 4px;
  display: block;
}

.msg-bubble.link {
  background: #fff;
  width: 240px;
  padding: 12px;
  text-align: left;
}

.link-title {
  font-weight: 500;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}

.link-desc-row {
  display: flex;
  gap: 10px;
}

.link-desc {
  font-size: 12px;
  color: #888;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  line-clamp: 3;
  overflow: hidden;
}

.link-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 2px;
  flex-shrink: 0;
}

.msg-bubble.miniprogram {
  background: #fff;
  width: 240px;
  padding: 0;
  overflow: hidden;
  text-align: left;
}

.mp-header {
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #333;
}

.mp-content {
  padding: 0 12px;
}

.mp-thumb {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 4px;
}

.mp-footer {
  padding: 8px 12px;
  border-top: 0.5px solid #eee;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #888;
}

.preview-img-error, .preview-mp-error {
  background: #f5f7fa;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 12px;
}

.preview-img-error {
  width: 150px;
  height: 100px;
  border-radius: 4px;
}

.preview-mp-error {
  width: 100%;
  height: 180px;
  border-radius: 4px;
}

.preview-img-error .el-icon, .preview-mp-error .el-icon {
  font-size: 20px;
  color: #f56c6c;
}

.wechat-footer {
  height: 50px;
  background: #f7f7f7;
  border-top: 0.5px solid #ddd;
  display: flex;
  align-items: center;
  padding: 0 10px;
  gap: 10px;
}

.footer-icon {
  font-size: 24px;
  color: #333;
  cursor: pointer;
}

.footer-input {
  flex: 1;
  height: 36px;
  background: #fff;
  border-radius: 4px;
}

/* Moment Styles */
.moment-content {
  flex: 1;
  background: #fff;
  overflow-y: auto;
  padding-top: 20px;
}

.moment-post {
  display: flex;
  padding: 15px;
  gap: 12px;
}

.moment-avatar {
  width: 42px;
  height: 42px;
  background: #409EFF;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  flex-shrink: 0;
}

.moment-main {
  flex: 1;
}

.moment-user {
  color: #576b95;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 5px;
}

.moment-text {
  font-size: 15px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 10px;
  white-space: pre-wrap;
  word-break: break-all;
}

.moment-image img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
  display: block;
}

.moment-placeholder {
  width: 150px;
  height: 150px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.moment-link-card {
  background: #f7f7f7;
  padding: 8px;
  display: flex;
  gap: 10px;
  align-items: center;
  border-radius: 2px;
}

.moment-link-card .link-thumb {
  width: 45px;
  height: 45px;
  object-fit: cover;
  flex-shrink: 0;
}

.moment-link-card .link-title {
  font-size: 14px;
  color: #333;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.moment-link-card.mp .link-content {
  flex: 1;
}

.moment-link-card.mp .link-footer {
  font-size: 11px;
  color: #888;
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 2px;
}

.moment-footer {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.moment-time {
  font-size: 13px;
  color: #b1b1b1;
}

.moment-actions {
  width: 32px;
  height: 18px;
  background: #f7f7f7;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.action-dot {
  width: 3.5px;
  height: 3.5px;
  background: #576b95;
  border-radius: 50%;
}

.header-camera {
  font-size: 20px;
  color: #333;
}
</style>
