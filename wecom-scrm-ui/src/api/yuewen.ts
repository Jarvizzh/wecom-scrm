import request from './request'

// Yuewen Product Management
export function getProducts(params: any) {
  return request({
    url: '/admin/yuewen/product',
    method: 'get',
    params
  })
}

export function saveProduct(data: any) {
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

export function syncUsers(data: any) {
  return request({
    url: '/admin/yuewen/product/sync',
    method: 'post',
    data
  })
}

export function getUsers(params: any) {
  return request({
    url: '/admin/yuewen/user',
    method: 'get',
    params
  })
}
