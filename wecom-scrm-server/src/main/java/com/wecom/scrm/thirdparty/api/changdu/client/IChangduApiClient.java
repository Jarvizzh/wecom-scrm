package com.wecom.scrm.thirdparty.api.changdu.client;

import com.wecom.scrm.thirdparty.api.changdu.dto.*;
import java.util.List;

/**
 * Interface for interacting with Changdu API.
 */
public interface IChangduApiClient {

    /**
     * 3.1 获取账号绑定的分包信息
     *
     * @param request Request parameters.
     * @return API response containing package info list.
     */
    ChangduResponse<List<ChangduPackageInfo>> getPackageInfo(ChangduPackageInfoRequest request);

    /**
     * 3.2 获取用户信息列表
     *
     * @param request Request parameters.
     * @return API response containing user detail list.
     */
    ChangduResponse<List<ChangduUserDetail>> getUserList(ChangduUserListRequest request);

    /**
     * 3.3 获取用户充值事件
     *
     * @param request Request parameters.
     * @return API response containing recharge events.
     */
    ChangduResponse<List<ChangduRechargeEvent>> getUserRecharge(ChangduRechargeRequest request);

    /**
     * 3.4 获取用户消费记录
     *
     * @param request Request parameters.
     * @return API response containing purchase records.
     */
    ChangduResponse<List<ChangduPurchaseRecord>> getUserPurchase(ChangduPurchaseRequest request);

    /**
     * 3.5 获取用户阅读记录列表
     *
     * @param request Request parameters.
     * @return API response containing read records.
     */
    ChangduResponse<List<ChangduReadRecord>> getUserReadList(ChangduReadRecordRequest request);
}
