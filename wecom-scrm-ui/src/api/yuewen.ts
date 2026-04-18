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

export function syncUsers(data: { appFlag: string, startTime: string, endTime: string }) {
  return request({
    url: '/admin/yuewen/product/sync',
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
