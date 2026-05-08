package com.wecom.scrm.dto;

import lombok.Getter;

@Getter
public enum WecomMsgType {
    IMAGE("image"),
    VIDEO("video"),
    LINK("link"),
    MINIPROGRAM("miniprogram");

    private final String value;

    WecomMsgType(String value) {
        this.value = value;
    }

    public static WecomMsgType fromValue(String value) {
        for (WecomMsgType type : WecomMsgType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
