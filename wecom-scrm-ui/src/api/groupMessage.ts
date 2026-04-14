import request from './request'

export interface GroupMessage {
  id?: number
  taskName: string
  sendType: number // 0: Immediate, 1: Scheduled
  sendTime?: string
  targetType: number // 0: All Groups, 1: Filtered Groups
  targetCondition?: GroupTargetCondition
  text: string
  attachments?: any[]
  status?: number // 0: Pending, 1: Sending, 2: Finished, 3: Failed
  failMsg?: string
  creatorUserid?: string
  createTime?: string
  targetCount?: number
}

export interface GroupTargetCondition {
  createTimeStart?: string
  createTimeEnd?: string
  groupNameKeywords?: string[]
}

export function createGroupMessage(data: GroupMessage) {
  return request({
    url: '/group-message',
    method: 'post',
    data
  })
}

export function getGroupMessageList() {
  return request({
    url: '/group-message/list',
    method: 'get'
  })
}

export function getGroupMessage(id: number | string) {
  return request({
    url: `/group-message/${id}`,
    method: 'get'
  })
}

export function updateGroupMessage(id: number | string, data: any) {
  return request({
    url: `/group-message/${id}`,
    method: 'put',
    data
  })
}

export function deleteGroupMessage(id: number | string) {
  return request({
    url: `/group-message/${id}`,
    method: 'delete'
  })
}

export function getGroupMessageSendResult(id: number | string) {
  return request({
    url: `/group-message/${id}/send-result`,
    method: 'get'
  })
}
