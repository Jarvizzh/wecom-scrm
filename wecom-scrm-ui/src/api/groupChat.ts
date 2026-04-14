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
