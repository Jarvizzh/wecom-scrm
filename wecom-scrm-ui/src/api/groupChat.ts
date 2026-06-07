import request from './request'

export function syncGroupChats() {
  return request({
    url: '/admin/sync/group-chats',
    method: 'post'
  })
}

export function getGroupChatList(params: any) {
  return request({
    url: '/group-chat/list',
    method: 'get',
    params
  })
}

export function getGroupMembers(chatId: string) {
  return request({
    url: `/group-chat/${chatId}/members`,
    method: 'get'
  })
}

export function getCustomerGroupChats(externalUserid: string): Promise<any> {
  return request({
    url: `/group-chat/customer/${externalUserid}`,
    method: 'get'
  })
}
