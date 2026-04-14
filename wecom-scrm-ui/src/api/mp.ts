import request from './request'

// MP Account management
export function getMpAccounts() {
  return request({
    url: '/admin/mp/accounts',
    method: 'get'
  })
}

export function saveMpAccount(data: any) {
  return request({
    url: '/admin/mp/accounts',
    method: 'post',
    data
  })
}

export function deleteMpAccount(id: number) {
  return request({
    url: `/admin/mp/accounts/${id}`,
    method: 'delete'
  })
}

// MP User management
export function getMpUsers(params: any) {
  return request({
    url: '/admin/mp/users',
    method: 'get',
    params
  })
}

export function syncMpUsers(appId: string) {
  return request({
    url: '/admin/mp/sync',
    method: 'post',
    params: { appId }
  })
}

export function copyMpAccount(data: { appId: string, targetCorpId: string }) {
  return request({
    url: '/admin/mp/accounts/copy',
    method: 'post',
    data
  })
}
