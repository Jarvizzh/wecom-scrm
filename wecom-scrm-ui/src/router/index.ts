import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页 / Dashboard' }
      },
      {
        path: 'customers',
        name: 'Customers',
        component: () => import('@/views/CustomerList.vue'),
        meta: { title: '客户管理 / Customers' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '内部员工 / Employees' }
      },
      {
        path: 'sync-logs',
        name: 'SyncLogs',
        component: () => import('@/views/SyncLogList.vue'),
        meta: { title: '同步日志 / Sync Logs' }
      },
      {
        path: 'wecom-events',
        name: 'WecomEvents',
        component: () => import('@/views/WecomEventList.vue'),
        meta: { title: '回调日志 / Callback Logs' }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: () => import('@/views/TagList.vue'),
        meta: { title: '标签管理 / Tags' }
      },
      {
        path: 'moments',
        name: 'Moments',
        component: () => import('@/views/MomentList.vue'),
        meta: { title: '客户朋友圈 / Moments' }
      },
      {
        path: 'moments/create',
        name: 'MomentCreate',
        component: () => import('@/views/MomentCreate.vue'),
        meta: { title: '创建朋友圈 / New Moment' }
      },
      {
        path: 'moments/edit/:id',
        name: 'MomentEdit',
        component: () => import('@/views/MomentCreate.vue'),
        meta: { title: '编辑朋友圈 / Edit Moment' }
      },
      {
        path: 'moments/detail/:id',
        name: 'MomentDetail',
        component: () => import('@/views/MomentDetail.vue'),
        meta: { title: '朋友圈详情 / Moment Details' }
      },
      {
        path: 'moments/copy/:id',
        name: 'MomentCopy',
        component: () => import('@/views/MomentCreate.vue'),
        meta: { title: '复制朋友圈 / Copy Moment' }
      },
      {
        path: 'customer-messages',
        name: 'CustomerMessages',
        component: () => import('@/views/CustomerMessageList.vue'),
        meta: { title: '客户群发 / Broadcast' }
      },
      {
        path: 'customer-messages/create',
        name: 'CustomerMessageCreate',
        component: () => import('@/views/CustomerMessageCreate.vue'),
        meta: { title: '新建群发 / New Broadcast' }
      },
      {
        path: 'customer-messages/edit/:id',
        name: 'CustomerMessageEdit',
        component: () => import('@/views/CustomerMessageCreate.vue'),
        meta: { title: '编辑群发 / Edit Broadcast' }
      },
      {
        path: 'customer-messages/detail/:id',
        name: 'CustomerMessageDetail',
        component: () => import('@/views/CustomerMessageDetail.vue'),
        meta: { title: '群发详情 / Broadcast Details' }
      },
      {
        path: 'customer-messages/copy/:id',
        name: 'CustomerMessageCopy',
        component: () => import('@/views/CustomerMessageCreate.vue'),
        meta: { title: '复制群发 / Copy Broadcast' }
      },
      {
        path: 'group-messages',
        name: 'GroupMessages',
        component: () => import('@/views/GroupMessageList.vue'),
        meta: { title: '客户群群发 / Group Broadcast' }
      },
      {
        path: 'group-messages/create',
        name: 'GroupMessageCreate',
        component: () => import('@/views/GroupMessageCreate.vue'),
        meta: { title: '新建群群发 / New Group Broadcast' }
      },
      {
        path: 'group-messages/edit/:id',
        name: 'GroupMessageEdit',
        component: () => import('@/views/GroupMessageCreate.vue'),
        meta: { title: '编辑群群发 / Edit Group Broadcast' }
      },
      {
        path: 'group-messages/detail/:id',
        name: 'GroupMessageDetail',
        component: () => import('@/views/GroupMessageDetail.vue'),
        meta: { title: '群发详情 / Group Broadcast Details' }
      },
      {
        path: 'group-messages/copy/:id',
        name: 'GroupMessageCopy',
        component: () => import('@/views/GroupMessageCreate.vue'),
        meta: { title: '复制群群发 / Copy Group Broadcast' }
      },
      {
        path: 'customer-groups',
        name: 'CustomerGroups',
        component: () => import('@/views/CustomerGroupList.vue'),
        meta: { title: '客户群 / Customer Groups' }
      },
      {
        path: 'welcome-message',
        name: 'WelcomeMessage',
        component: () => import('@/views/WelcomeMessage.vue'),
        meta: { title: '客户欢迎语 / Welcome Message' }
      },
      {
        path: 'welcome-message/create',
        name: 'WelcomeMessageCreate',
        component: () => import('@/views/WelcomeMessageCreate.vue'),
        meta: { title: '新建欢迎语 / New Welcome Message' }
      },
      {
        path: 'welcome-message/edit/:id',
        name: 'WelcomeMessageEdit',
        component: () => import('@/views/WelcomeMessageCreate.vue'),
        meta: { title: '编辑欢迎语 / Edit Welcome Message' }
      },
      {
        path: 'mp-accounts',
        name: 'MpAccounts',
        component: () => import('@/views/mp/MpAccountList.vue'),
        meta: { title: '公众号管理 / MP Accounts' }
      },
      {
        path: 'mp-users',
        name: 'MpUsers',
        component: () => import('@/views/mp/MpUserList.vue'),
        meta: { title: '公众号用户 / MP Users' }
      },
      {
        path: 'yuewen-products',
        name: 'YuewenProducts',
        component: () => import('@/views/yuewen/YuewenProductList.vue'),
        meta: { title: '阅文产品管理 / Yuewen Products' }
      },
      {
        path: 'yuewen-users',
        name: 'YuewenUsers',
        component: () => import('@/views/yuewen/YuewenUserList.vue'),
        meta: { title: '阅文用户列表 / Yuewen Users' }
      },
      {
        path: 'yuewen-consume',
        name: 'YuewenConsume',
        component: () => import('@/views/yuewen/YuewenConsumeList.vue'),
        meta: { title: '阅文消费记录 / Yuewen Consumption' }
      },
      {
        path: 'yuewen-recharge',
        name: 'YuewenRecharge',
        component: () => import('@/views/yuewen/YuewenRechargeList.vue'),
        meta: { title: '阅文充值记录 / Yuewen Recharge' }
      },
      {
        path: 'changdu-products',
        name: 'ChangduProducts',
        component: () => import('@/views/changdu/ChangduProductList.vue'),
        meta: { title: '常读产品管理 / Changdu Products' }
      },
      {
        path: 'changdu-users',
        name: 'ChangduUsers',
        component: () => import('@/views/changdu/ChangduUserList.vue'),
        meta: { title: '常读用户列表 / Changdu Users' }
      },
      {
        path: 'changdu-recharge',
        name: 'ChangduRecharge',
        component: () => import('@/views/changdu/ChangduRechargeList.vue'),
        meta: { title: '常读充值记录 / Changdu Recharge' }
      },
      {
        path: 'enterprises',
        name: 'Enterprises',
        component: () => import('@/views/EnterpriseList.vue'),
        meta: { title: '企业管理 / Enterprise Management' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录 / Login' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.name !== 'Login' && !token) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router
