package com.wecom.scrm.thirdparty.api.changdu.constant;

import lombok.Getter;

/**
 * Error codes for Changdu Content Distribution Platform API.
 */
@Getter
public enum ChangduErrorCodes {

    /**
     * 正确返回，无需处理
     */
    SUCCESS(200, "请求成功"),

    /**
     * distributor_id 无效：与商务人员确认合作状态
     */
    INVALID_DISTRIBUTOR_ID(410, "distributor_id 无效：与商务人员确认合作状态"),

    /**
     * sign 错误：签名计算错误（排查代码）或者密钥失效（联系商务）
     */
    SIGN_ERROR(411, "sign 错误：签名计算错误（排查代码）或者密钥失效（联系商务）"),

    /**
     * timestamp 不合法：见文档 1.2 节关于 ts 字段的说明
     */
    INVALID_TIMESTAMP(412, "timestamp 不合法：见文档 1.2 节关于 ts 字段的说明"),

    /**
     * 功能已停用：小程序相关能力已停用，请使用公众号对应能力
     */
    FUNCTION_DISABLED(413, "功能已停用：小程序相关能力已停用，请使用公众号对应能力"),

    /**
     * 访问速度过快：降低 QPS，单个接口控制到 1 以内
     */
    RATE_LIMIT_EXCEEDED(500, "访问速度过快：降低 QPS，单个接口控制到 1 以内"),

    /**
     * 内部错误：稍后重试，大面积持续失败请联系研发团队
     */
    INTERNAL_ERROR(501, "内部错误：稍后重试，大面积持续失败请联系研发团队");

    private final int code;
    private final String msg;

    ChangduErrorCodes(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Get enum by error code.
     *
     * @param code The error code.
     * @return The error code enum, or null if not found.
     */
    public static ChangduErrorCodes fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChangduErrorCodes errorCode : ChangduErrorCodes.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * Get message by error code.
     *
     * @param code The error code.
     * @return The error message.
     */
    public static String getMsgByCode(Integer code) {
        ChangduErrorCodes errorCode = fromCode(code);
        if (errorCode != null) {
            return errorCode.getMsg();
        }
        return null;
    }
}
