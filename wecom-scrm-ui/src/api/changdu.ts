import request from './request'

export interface ChangduProduct {
  id?: number
  productName: string
  distributorId: number
  appId?: number
  appType?: number
  wxAppId?: string
  status: number
  createTime?: string
  updateTime?: string
}

export function getChangduProducts(params: { page: number; size: number }) {
  return request.get('/admin/changdu/products', { params })
}

export function syncChangduProducts() {
  return request.post('/admin/changdu/products/sync')
}

// User APIs
export function getChangduUsers(params: { 
  page: number; 
  size: number; 
  distributorId?: number; 
  openId?: string;
  nickname?: string;
  avatar?: string;
  sortField?: string;
  sortOrder?: string;
}) {
  return request.get('/admin/changdu/users', { params })
}

export function getChangduByCustomer(externalUserid: string): Promise<any[]> {
  return request.get(`/admin/changdu/users/customer/${externalUserid}`)
}

export function syncChangduUsers(data: { 
  distributorId: number; 
  startTime?: string; 
  endTime?: string 
}) {
  return request.post('/admin/changdu/users/sync', null, { params: data })
}

// Recharge APIs
export function getChangduRechargeRecords(params: {
  page: number;
  size: number;
  distributorId?: number;
  openId?: string;
  nickname?: string;
  tradeNo?: number;
}) {
  return request.get('/admin/changdu/recharge', { params })
}

export function syncChangduRechargeRecords(data: {
  distributorId: number;
  startTime?: string;
  endTime?: string;
}) {
  return request.post('/admin/changdu/recharge/sync', null, { params: data })
}

export function saveChangduProduct(data: ChangduProduct) {
  return request.post('/admin/changdu/products', data)
}

export function deleteChangduProduct(id: number) {
  return request.delete(`/admin/changdu/products/${id}`)
}

export function batchDeleteChangduProducts(ids: number[]) {
  return request.post('/admin/changdu/products/batch-delete', ids)
}

export function batchUpdateChangduProductStatus(ids: number[], status: number) {
  return request.post('/admin/changdu/products/batch-status', { ids, status })
}

export function getChangduProduct(id: number) {
  return request.get(`/admin/changdu/products/${id}`)
}
