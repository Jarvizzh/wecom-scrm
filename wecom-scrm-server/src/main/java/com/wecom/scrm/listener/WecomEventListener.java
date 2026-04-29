package com.wecom.scrm.listener;

import com.wecom.scrm.listener.event.WecomEvent;
import com.wecom.scrm.service.WecomEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import org.springframework.beans.factory.annotation.Qualifier;
import java.util.concurrent.Executor;

/**
 * Asynchronous listener for WeCom events.
 */
@Slf4j
@Component
public class WecomEventListener {

    private final WecomEventService wecomEventService;
    private final Executor eventProcessExecutor;
    private final Executor highPriorityEventProcessExecutor;

    public WecomEventListener(WecomEventService wecomEventService,
                              @Qualifier("eventProcessExecutor") Executor eventProcessExecutor,
                              @Qualifier("highPriorityEventProcessExecutor") Executor highPriorityEventProcessExecutor) {
        this.wecomEventService = wecomEventService;
        this.eventProcessExecutor = eventProcessExecutor;
        this.highPriorityEventProcessExecutor = highPriorityEventProcessExecutor;
    }

    /**
     * Handles WeCom events asynchronously after the database transaction is committed.
     * 
     * Routes to highPriorityEventProcessExecutor if priority >= 9.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWecomEvent(WecomEvent event) {
        log.debug("Received WecomEvent for log ID: {}, Priority: {}", 
                event.getEventLog().getId(), event.getPriority());
        
        boolean isHighPriority = event.getPriority() >= 9;
        Executor executor = isHighPriority ? highPriorityEventProcessExecutor : eventProcessExecutor;

        executor.execute(() -> {
            try {
                wecomEventService.processEvent(event.getEventLog());
            } catch (Exception e) {
                log.error("Error processing WecomEvent for log ID: {}", event.getEventLog().getId(), e);
            }
        });
    }
}
