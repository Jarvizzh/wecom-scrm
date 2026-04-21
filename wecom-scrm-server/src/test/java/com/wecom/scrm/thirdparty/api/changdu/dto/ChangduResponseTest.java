package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.wecom.scrm.thirdparty.api.changdu.constant.ChangduErrorCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangduResponseTest {

    @Test
    public void testGetMessage_WhenMessageIsNull() {
        ChangduResponse<Object> response = new ChangduResponse<>();
        response.setCode(410);
        response.setMessage(null);

        assertEquals(ChangduErrorCodes.INVALID_DISTRIBUTOR_ID.getMsg(), response.getMessage());
    }

    @Test
    public void testGetMessage_WhenMessageIsEmpty() {
        ChangduResponse<Object> response = new ChangduResponse<>();
        response.setCode(411);
        response.setMessage("");

        assertEquals(ChangduErrorCodes.SIGN_ERROR.getMsg(), response.getMessage());
    }

    @Test
    public void testGetMessage_WhenMessageIsPresent() {
        ChangduResponse<Object> response = new ChangduResponse<>();
        response.setCode(410);
        response.setMessage("Custom Message");

        assertEquals("Custom Message", response.getMessage());
    }

    @Test
    public void testIsSuccess() {
        ChangduResponse<Object> response = new ChangduResponse<>();
        response.setCode(200);
        assertEquals(true, response.isSuccess());

        response.setCode(410);
        assertEquals(false, response.isSuccess());
    }
}
