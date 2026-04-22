import request from './request'

export interface YuewenProduct {
  id?: number | null
  productName: string
  wxAppId: string
  appFlag: string
  status: number
  createTime?: string
}

export interface YuewenUser {
  id: number | null
  guid: string
  openid: string
  nickname: string
  avatar: string
  appFlag: string
  chargeAmount: number
  chargeNum: number
  isSubscribe: number
  registTime: string
  vipEndTime: string
  channelName: string
  bookName: string
  externalUserid: string
  yuewenUpdateTime: string
  productName?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  size: number
  number: number
}

// Yuewen Product Management
export function getProducts(params: any): Promise<PageResponse<YuewenProduct>> {
  return request({
    url: '/admin/yuewen/product',
    method: 'get',
    params
  })
}

export function saveProduct(data: Partial<YuewenProduct>) {
  return request({
    url: '/admin/yuewen/product',
    method: 'post',
    data
  })
}

export function deleteProduct(id: number) {
  return request({
    url: `/admin/yuewen/product/${id}`,
    method: 'delete'
  })
}

export function syncUsers(data: { appFlag: string, startTime?: string, endTime?: string }) {
  return request({
    url: '/admin/yuewen/product/sync',
    method: 'post',
    data
  })
}

export function syncUsersFromList(data: { appFlag: string, startTime?: string, endTime?: string }) {
  return request({
    url: '/admin/yuewen/user/sync',
    method: 'post',
    data
  })
}

export function getUsers(params: any): Promise<PageResponse<YuewenUser>> {
  return request({
    url: '/admin/yuewen/user',
    method: 'get',
    params
  })
}

export function getYuewenByCustomer(externalUserid: string): Promise<YuewenUser[]> {
  return request({
    url: `/admin/yuewen/user/customer/${externalUserid}`,
    method: 'get'
  })
}

export interface YuewenConsumeRecord {
  id: number | null
  appFlag: string
  openid: string
  guid: number
  orderId: string
  consumeId: string
  worthAmount: number
  freeAmount: number
  consumeTime: string
  bookId: number
  bookName: string
  chapterId: string
  chapterName: string
  productName?: string
}

export interface YuewenRechargeRecord {
  id: number | null
  appFlag: string
  appName: string
  amount: string
  ywOrderId: string
  orderId: string
  orderTime: string
  payTime: string
  orderStatus: number
  orderType: number
  openid: string
  guid: number
  nickname: string
  sex: number
  regTime: string
  subTime: string
  channelId: number
  channelName: string
  bookId: number
  bookName: string
  wxAppId: string
  itemName: string
  orderChannel: number
  productName?: string
}

export function getConsumeRecords(params: any): Promise<PageResponse<YuewenConsumeRecord>> {
  return request({
    url: '/admin/yuewen/user/consume',
    method: 'get',
    params
  })
}

export function syncConsumeRecords(data: { appFlag: string, startTime: string, endTime: string }) {
  return request({
    url: '/admin/yuewen/user/sync/consume',
    method: 'post',
    data
  })
}

export function getRechargeRecords(params: any): Promise<PageResponse<YuewenRechargeRecord>> {
  return request({
    url: '/admin/yuewen/recharge',
    method: 'get',
    params
  })
}

export function syncRechargeRecords(data: { appFlag: string, startTime?: string, endTime?: string }) {
  return request({
    url: '/admin/yuewen/recharge/sync',
    method: 'post',
    params: data
  })
}
