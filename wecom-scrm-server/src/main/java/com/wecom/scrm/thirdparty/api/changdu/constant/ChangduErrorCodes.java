package com.wecom.scrm.thirdparty.api.changdu.constant;

/**
 * Error codes for Changdu Content Distribution Platform API.
 */
public class ChangduErrorCodes {

    /**
     * 正确返回，无需处理
     */
    public static final int SUCCESS = 200;

    /**
     * distributor_id 无效：与商务人员确认合作状态
     */
    public static final int INVALID_DISTRIBUTOR_ID = 410;

    /**
     * sign 错误：签名计算错误（排查代码）或者密钥失效（联系商务）
     */
    public static final int SIGN_ERROR = 411;

    /**
     * timestamp 不合法：见文档 1.2 节关于 ts 字段的说明
     */
    public static final int INVALID_TIMESTAMP = 412;

    /**
     * 功能已停用：小程序相关能力已停用，请使用公众号对应能力
     */
    public static final int FUNCTION_DISABLED = 413;

    /**
     * 访问速度过快：降低 QPS，单个接口控制到 1 以内
     */
    public static final int RATE_LIMIT_EXCEEDED = 500;

    /**
     * 内部错误：稍后重试，大面积持续失败请联系研发团队
     */
    public static final int INTERNAL_ERROR = 501;

    private ChangduErrorCodes() {
        // Private constructor to prevent instantiation
    }
}
