package com.wecom.scrm.listener;

import com.wecom.scrm.listener.event.WecomEvent;
import com.wecom.scrm.service.WecomEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Asynchronous listener for WeCom events.
 */
@Slf4j
@Component
public class WecomEventListener {

    private final WecomEventService wecomEventService;

    public WecomEventListener(WecomEventService wecomEventService) {
        this.wecomEventService = wecomEventService;
    }

    /**
     * Handles WeCom events asynchronously after the database transaction is committed.
     * 
     * @Async("syncExecutor"): Executes this method in a separate thread from the pool.
     * @TransactionalEventListener: Ensures processing only starts AFTER the event log
     *                              is successfully committed to the database (AFTER_COMMIT).
     *                              This prevents "record not found" errors in the async thread.
     */
    @Async("eventProcessExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWecomEvent(WecomEvent event) {
        log.debug("Received WecomEvent for log ID: {}", event.getEventLog().getId());
        try {
            wecomEventService.processEvent(event.getEventLog());
        } catch (Exception e) {
            log.error("Error processing WecomEvent for log ID: {}", event.getEventLog().getId(), e);
        }
    }
}
