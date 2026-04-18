package com.wecom.scrm.thirdparty.api.yuewen.client;

import com.wecom.scrm.thirdparty.api.yuewen.dto.*;
import com.wecom.scrm.thirdparty.yuewen.api.dto.*;

/**
 * Interface for Yuewen API Client.
 */
public interface IYuewenAPIClient {

    /**
     * 4.1 获取产品标识列表（appflag）
     *
     * @param request Request parameters.
     * @return API response containing the app list.
     */
    YuewenResponse<YuewenAppListResponse> getAppList(YuewenAppListRequest request);

    /**
     * 4.2 获取充值记录
     *
     * @param request Request parameters.
     * @return API response containing recharge logs.
     */
    YuewenResponse<YuewenRechargeLogResponse> getRechargeLog(YuewenRechargeLogRequest request);

    /**
     * 4.3 获取用户信息
     *
     * @param request Request parameters.
     * @return API response containing user info.
     */
    YuewenResponse<YuewenUserInfoResponse> getUserInfo(YuewenUserInfoRequest request);

    /**
     * 4.4 获取消费记录
     *
     * @param request Request parameters.
     * @return API response containing consumption logs.
     */
    YuewenResponse<YuewenConsumeLogResponse> getConsumeLog(YuewenConsumeLogRequest request);
}
