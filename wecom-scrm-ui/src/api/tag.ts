import request from './request';

export const getTagGroups = () => {
  return request.get('/admin/tags/groups');
};

export const getTagsByGroup = (groupId: string) => {
  return request.get(`/admin/tags/group/${groupId}`);
};

export const addCorpTag = (data: { groupName: string; tagName: string }) => {
  return request.post('/admin/tags/add', data);
};

export const deleteCorpTag = (params: { tagId?: string; groupId?: string }) => {
  return request.delete('/admin/tags/delete', { params });
};

export const markCustomerTags = (data: {
  userid: string;
  externalUserid: string;
  addTagIds?: string[];
  removeTagIds?: string[];
}) => {
  return request.post('/admin/tags/mark', data);
};

export const batchMarkCustomerTags = (data: {
  targets: Array<{ userid: string; externalUserid: string }>;
  addTagIds?: string[];
  removeTagIds?: string[];
  selectAll?: boolean;
  customerName?: string;
  unionid?: string;
  employeeName?: string;
  mpAppId?: string;
  tagIds?: string[];
  status?: number;
  onlyDuplicates?: boolean;
  // yuewen
  appFlag?: string;
  openid?: string;
  minAmount?: number;
  maxAmount?: number;
}) => {
  return request.post('/admin/tags/batch-mark', data);
};

export const syncTags = () => {
  return request.post('/admin/sync/tags');
};
