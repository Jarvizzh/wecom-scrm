<template>
  <el-container class="layout-container">
    <el-aside v-if="!isMobile" :width="isCollapse ? '64px' : '200px'" class="aside" :class="{ 'is-collapsed': isCollapse }">
      <div class="logo">
        <div class="logo-main" v-show="!isCollapse">
          <img src="@/assets/logo.svg" class="logo-img" alt="logo" />
          <h2 class="logo-text">Venus</h2>
        </div>
        <div class="sidebar-toggle-top" @click="toggleCollapse">
          <el-icon v-if="isCollapse"><Expand /></el-icon>
          <el-icon v-else><Fold /></el-icon>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        :collapse="isCollapse"
        :collapse-transition="false"
      >
        <template v-if="currentCorpId">
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <span>首页看板</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><Avatar /></el-icon>
            <span>企微账号</span>
          </el-menu-item>

          <el-menu-item index="/customers">
            <el-icon><User /></el-icon>
            <span>客户管理</span>
          </el-menu-item>
          <el-menu-item index="/customer-groups">
            <el-icon><ChatSquare /></el-icon>
            <span>客户群管理</span>
          </el-menu-item>

          <el-menu-item index="/tags">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-sub-menu index="operations">
            <template #title>
              <el-icon><Operation /></el-icon>
              <span>客户运营</span>
            </template>
            <el-menu-item index="/welcome-message">
              <el-icon><Message /></el-icon>
              <span>客户欢迎语</span>
            </el-menu-item>
            <el-menu-item index="/moments">
              <el-icon><Promotion /></el-icon>
              <span>客户朋友圈</span>
            </el-menu-item>
            <el-menu-item index="/customer-messages">
              <el-icon><ChatDotRound /></el-icon>
              <span>客户群发</span>
            </el-menu-item>
            <el-menu-item index="/group-messages">
              <el-icon><ChatLineRound /></el-icon>
              <span>客户群群发</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="mp">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>微信公众号</span>
            </template>
            <el-menu-item index="/mp-accounts">
              <el-icon><Monitor /></el-icon>
              <span>账号管理</span>
            </el-menu-item>
            <el-menu-item index="/mp-users">
              <el-icon><User /></el-icon>
              <span>用户列表</span>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="thirdparty">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>第三方平台</span>
            </template>
            <el-sub-menu index="yuewen">
              <template #title>
                <el-icon><Notebook /></el-icon>
                <span>阅文小说</span>
              </template>
              <el-menu-item index="/yuewen-products">
                <el-icon><Tickets /></el-icon>
                <span>产品管理</span>
              </el-menu-item>
              <el-menu-item index="/yuewen-users">
                <el-icon><User /></el-icon>
                <span>用户列表</span>
              </el-menu-item>
              <el-menu-item index="/yuewen-recharge">
                <el-icon><Wallet /></el-icon>
                <span>充值记录</span>
              </el-menu-item>
               <el-menu-item index="/yuewen-consume">
                <el-icon><Wallet /></el-icon>
                <span>消费记录</span>
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="changdu">
              <template #title>
                <el-icon><Notebook /></el-icon>
                <span>常读小说</span>
              </template>
              <el-menu-item index="/changdu-products">
                <el-icon><Tickets /></el-icon>
                <span>产品管理</span>
              </el-menu-item>
              <el-menu-item index="/changdu-users">
                <el-icon><User /></el-icon>
                <span>用户列表</span>
              </el-menu-item>
              <el-menu-item index="/changdu-recharge">
                <el-icon><Wallet /></el-icon>
                <span>充值记录</span>
              </el-menu-item>
            </el-sub-menu>
          </el-sub-menu>
          <el-menu-item index="/sync-logs">
            <el-icon><Fold /></el-icon>
            <span>同步日志</span>
          </el-menu-item>
          <el-menu-item index="/wecom-events">
            <el-icon><Notification /></el-icon>
            <span>回调日志</span>
          </el-menu-item>
        </template>
        
        <el-menu-item index="/enterprises">
          <el-icon><Setting /></el-icon>
          <span>企业管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-drawer
      v-else
      v-model="drawerVisible"
      direction="ltr"
      size="240px"
      :with-header="false"
      class="mobile-drawer"
    >
      <div class="aside is-mobile-aside">
        <div class="logo">
          <div class="logo-main">
            <img src="@/assets/logo.svg" class="logo-img" alt="logo" />
            <h2 class="logo-text">Venus</h2>
          </div>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
          @select="drawerVisible = false"
        >
          <template v-if="currentCorpId">
            <el-menu-item index="/dashboard">
              <el-icon><DataLine /></el-icon>
              <span>首页看板</span>
            </el-menu-item>
            <el-menu-item index="/users">
              <el-icon><Avatar /></el-icon>
              <span>企微账号</span>
            </el-menu-item>
            <el-menu-item index="/customers">
              <el-icon><User /></el-icon>
              <span>客户管理</span>
            </el-menu-item>
            <el-menu-item index="/customer-groups">
              <el-icon><ChatSquare /></el-icon>
              <span>客户群管理</span>
            </el-menu-item>

            <el-menu-item index="/tags">
              <el-icon><PriceTag /></el-icon>
              <span>标签管理</span>
            </el-menu-item>
            <el-sub-menu index="operations">
              <template #title>
                <el-icon><Operation /></el-icon>
                <span>客户运营</span>
              </template>
              <el-menu-item index="/welcome-message">
                <el-icon><Message /></el-icon>
                <span>客户欢迎语</span>
              </el-menu-item>
              <el-menu-item index="/moments">
                <el-icon><Promotion /></el-icon>
                <span>客户朋友圈</span>
              </el-menu-item>
              <el-menu-item index="/customer-messages">
                <el-icon><ChatDotRound /></el-icon>
                <span>客户群发</span>
              </el-menu-item>
              <el-menu-item index="/group-messages">
                <el-icon><ChatLineRound /></el-icon>
                <span>客户群群发</span>
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="mp">
              <template #title>
                <el-icon><Connection /></el-icon>
                <span>微信公众号</span>
              </template>
              <el-menu-item index="/mp-accounts">
                <el-icon><Monitor /></el-icon>
                <span>账号管理</span>
              </el-menu-item>
              <el-menu-item index="/mp-users">
                <el-icon><User /></el-icon>
                <span>用户列表</span>
              </el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="thirdparty">
              <template #title>
                <el-icon><Connection /></el-icon>
                <span>第三方平台</span>
              </template>
              <el-sub-menu index="yuewen">
                <template #title>
                  <span>阅文</span>
                </template>
                <el-menu-item index="/yuewen-products">
                  <el-icon><Tickets /></el-icon>
                  <span>产品管理</span>
                </el-menu-item>
                <el-menu-item index="/yuewen-users">
                  <el-icon><User /></el-icon>
                  <span>用户列表</span>
                </el-menu-item>
                <el-menu-item index="/yuewen-consume">
                  <el-icon><Wallet /></el-icon>
                  <span>消费记录</span>
                </el-menu-item>
                <el-menu-item index="/yuewen-recharge">
                  <el-icon><Wallet /></el-icon>
                  <span>充值记录</span>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="changdu">
                <template #title>
                  <span>常读</span>
                </template>
                <el-menu-item index="/changdu-products">
                  <el-icon><Tickets /></el-icon>
                  <span>产品管理</span>
                </el-menu-item>
                <el-menu-item index="/changdu-users">
                  <el-icon><User /></el-icon>
                  <span>用户列表</span>
                </el-menu-item>
                <el-menu-item index="/changdu-recharge">
                  <el-icon><Wallet /></el-icon>
                  <span>充值记录</span>
                </el-menu-item>
              </el-sub-menu>
            </el-sub-menu>
            <el-menu-item index="/sync-logs">
              <el-icon><Fold /></el-icon>
              <span>同步日志</span>
            </el-menu-item>
            <el-menu-item index="/wecom-events">
              <el-icon><Notification /></el-icon>
              <span>回调日志</span>
            </el-menu-item>
          </template>
          
          <el-menu-item index="/enterprises">
            <el-icon><Setting /></el-icon>
            <span>企业管理</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-drawer>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div v-if="isMobile" class="mobile-menu-btn" @click="drawerVisible = true">
            <el-icon><Menu /></el-icon>
          </div>
          <div v-if="!isMobile" class="header-breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div v-else class="mobile-page-title">
            {{ currentRouteTitle }}
          </div>
        </div>
        <div class="header-user">
          <el-dropdown class="enterprise-selector" trigger="click" @command="handleCorpChange">
            <div class="enterprise-trigger" v-if="currentCorpName">
               <div class="enterprise-trigger-avatar">{{ currentCorpName.charAt(0) }}</div>
               <div class="enterprise-trigger-info">
                 <span class="enterprise-trigger-name">{{ currentCorpName || '选择企业' }}</span>
                 <span class="enterprise-trigger-badge">工作空间</span>
               </div>
               <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </div>
            <div class="enterprise-trigger empty" v-else>
               选择企业 <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </div>

            <template #dropdown>
              <el-dropdown-menu class="enterprise-dropdown-menu">
                <div class="dropdown-header">可用企业列表</div>
                <el-dropdown-item
                  v-for="item in enterprises"
                  :key="item.corpId"
                  :command="item.corpId"
                  :class="{ 'is-active': item.corpId === currentCorpId }"
                >
                  <div class="enterprise-option">
                    <div class="enterprise-option-avatar" :class="{'active-avatar': item.corpId === currentCorpId}">
                      {{ item.name.charAt(0) }}
                    </div>
                    <div class="enterprise-option-details">
                      <div class="enterprise-option-name">{{ item.name }}</div>
                      <div v-if="!isMobile" class="enterprise-option-id">{{ item.corpId }}</div>
                    </div>
                    <el-icon v-if="item.corpId === currentCorpId" class="check-icon"><Select /></el-icon>
                  </div>
                </el-dropdown-item>
                
                <div class="dropdown-footer" @click.stop="openAddDialog">
                  <el-icon><Plus /></el-icon> <span>管理与添加企业</span>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click">
            <span class="el-dropdown-link user-dropdown-link">
              <span v-if="!isMobile">{{ currentUserName }}</span>
              <el-avatar v-else :size="24" class="mobile-avatar">{{ currentUserName.charAt(0) }}</el-avatar>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <div v-if="isMobile" class="mobile-user-info">{{ currentUserName }}</div>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <template v-if="currentCorpId || route.path === '/enterprises'">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </template>
        <template v-else>
          <div class="empty-state-container">
            <el-empty description="请先在右上方选择一个企业开始工作">
               <template #extra>
                 <p v-if="enterprises.length > 0">点击右上角的企业切换按钮，选择一个已有的企业。</p>
                 <el-button v-else type="primary" @click="openAddDialog">立即去添加企业</el-button>
               </template>
            </el-empty>
          </div>
        </template>
      </el-main>
    </el-container>
    
    <EnterpriseFormDialog ref="addDialogRef" @success="handleEnterpriseSuccess" />
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import { 
  Setting, CaretBottom, ArrowDown, Plus, Select, Wallet,
  Expand, Fold, Notification, Menu, Tickets, Connection
} from '@element-plus/icons-vue'
import EnterpriseFormDialog from '@/views/EnterpriseFormDialog.vue'
import { useResponsive } from '@/hooks/useResponsive'

const { isMobile } = useResponsive()
const drawerVisible = ref(false)
const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const route = useRoute()
const router = useRouter()

const enterprises = ref<any[]>([])
const currentCorpId = ref<string>('')
const addDialogRef = ref()

const activeMenu = computed(() => {
  return route.path
})

const currentRouteTitle = computed(() => {
  return route.meta.title || 'Dashboard'
})

const currentUserName = computed(() => {
  return localStorage.getItem('username') || 'Admin'
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('currentCorpId')
  router.push('/login')
}

const currentCorpName = computed(() => {
  const ent = enterprises.value.find(e => e.corpId === currentCorpId.value)
  return ent ? ent.name : ''
})

const fetchEnterprises = async () => {
  try {
    const res = await request.get('/enterprises')
    enterprises.value = res as any
    const saved = localStorage.getItem('currentCorpId')
    if (saved && enterprises.value.find(e => e.corpId === saved)) {
      currentCorpId.value = saved
    }
  } catch (err) {
    console.error('Failed to load enterprises', err)
  }
}

const handleCorpChange = (val: string) => {
  if (val === currentCorpId.value) return;
  localStorage.setItem('currentCorpId', val)
  window.location.reload()
}


const openAddDialog = () => {
  addDialogRef.value.open()
}

const handleEnterpriseSuccess = () => {
  fetchEnterprises()
  // If we are on the enterprises page, it will refresh itself via its own mounted hook or success listener
  // but we refresh the top-level list here too for the selector
}

onMounted(() => {
  fetchEnterprises()
  window.addEventListener('refresh-enterprises', fetchEnterprises)
})

onUnmounted(() => {
  window.removeEventListener('refresh-enterprises', fetchEnterprises)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
}

.aside {
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  border-bottom: 1px solid #1f2d3d;
  background-color: #2b3b4d;
}

.logo-main {
  display: flex;
  align-items: center;
}

.logo-img {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: cover;
}

.logo-text {
  margin: 0 0 0 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar-toggle-top {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #bfcbd9;
  font-size: 20px;
  transition: all 0.3s;
  border-radius: 4px;
}

.sidebar-toggle-top:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #409EFF;
}

.is-collapsed .logo {
  justify-content: center;
  padding: 0;
}

.is-collapsed .sidebar-toggle-top {
  width: 100%;
  height: 100%;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* Custom scrollbar for navigation menu */
.el-menu-vertical::-webkit-scrollbar {
  width: 6px;
}

.el-menu-vertical::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.el-menu-vertical::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.el-menu-vertical::-webkit-scrollbar-track {
  background: #304156;
}

.header {
  background-color: #fff;
  color: #333;
  height: 60px;
  line-height: 60px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Premium Enterprise Selector Styles */
.enterprise-selector {
  margin-right: 10px;
}

.enterprise-trigger {
  display: flex;
  align-items: center;
  padding: 6px 16px 6px 8px;
  background: #f4f6f8;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid transparent;
}

.enterprise-trigger:hover {
  background: #eef2f6;
  border-color: #dcdfe6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.enterprise-trigger-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #53a8ff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  margin-right: 10px;
  box-shadow: 0 2px 6px rgba(64,158,255,0.3);
}

.enterprise-trigger-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  margin-right: 8px;
}

.enterprise-trigger-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.enterprise-trigger-badge {
  font-size: 10px;
  color: #909399;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }
  .header-breadcrumb {
    display: none;
  }
  .mobile-menu-btn {
    font-size: 20px;
    margin-right: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    color: #606266;
  }
  .mobile-page-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  .enterprise-trigger {
    padding: 4px 8px;
  }
  .enterprise-trigger-info {
    display: none;
  }
  .enterprise-trigger-avatar {
    margin-right: 4px;
  }
  .header-user {
    gap: 10px;
  }
  .main-content {
    padding: 12px;
  }
}

.mobile-user-info {
  padding: 10px 16px;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 4px;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.mobile-avatar {
  background-color: #409EFF;
  font-size: 12px;
}

/* Dropdown Menu Styles */
.enterprise-dropdown-menu {
  width: 280px;
  max-width: 90vw;
  padding: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.dropdown-header {
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

.enterprise-option {
  display: flex;
  align-items: center;
  padding: 8px 4px;
  width: 100%;
}

.enterprise-option-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #f0f2f5;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  margin-right: 12px;
  transition: all 0.3s ease;
}

.enterprise-option-avatar.active-avatar {
  background: linear-gradient(135deg, #409EFF, #53a8ff);
  color: white;
  box-shadow: 0 4px 12px rgba(64,158,255,0.3);
}

.enterprise-option-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.enterprise-option-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
}

.enterprise-option-id {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.check-icon {
  color: #409EFF;
  font-size: 16px;
  font-weight: bold;
}

:deep(.el-dropdown-item) {
  padding: 4px 16px;
}

:deep(.el-dropdown-item.is-active) {
  background-color: #f0f7ff;
}

:deep(.el-dropdown-item:hover:not(.is-active)) {
  background-color: #f5f7fa;
}

.dropdown-footer {
  padding: 14px 16px;
  font-size: 13px;
  color: #409EFF;
  font-weight: 500;
  display: flex;
  align-items: center;
  cursor: pointer;
  background: #fafafa;
  border-top: 1px solid #ebeef5;
  transition: background 0.2s ease;
}

.dropdown-footer:hover {
  background: #f0f7ff;
}

.dropdown-footer .el-icon {
  margin-right: 6px;
  font-size: 16px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  position: relative;
  overflow-x: hidden;
}

.is-mobile-aside {
  width: 100% !important;
  height: 100%;
}

:deep(.mobile-drawer .el-drawer__body) {
  padding: 0;
  background-color: #304156;
}

.empty-state-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
