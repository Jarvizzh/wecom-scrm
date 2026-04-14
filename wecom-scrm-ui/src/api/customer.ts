import request from './request';

interface FetchCustomersParams {
  page: number;
  size: number;
  customerName?: string;
  unionid?: string;
  employeeName?: string;
  mpAppId?: string;
  tagIds?: string[];
  status?: number;
  onlyDuplicates?: boolean;
}

export const getCustomers = (params: FetchCustomersParams) => {
  return request.get('/admin/customers', { params });
};

export const syncCustomers = () => {
  return request.post('/admin/sync/customers');
};
