package com.wecom.scrm.listener.event;

import com.wecom.scrm.entity.WecomEventLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event for WeCom callback messages.
 */
@Getter
public class WecomEvent extends ApplicationEvent {
    private final WecomEventLog eventLog;
    private final int priority;

    public WecomEvent(Object source, WecomEventLog eventLog, int priority) {
        super(source);
        this.eventLog = eventLog;
        this.priority = priority;
    }
}
