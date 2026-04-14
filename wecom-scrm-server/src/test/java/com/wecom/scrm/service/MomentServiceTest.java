package com.wecom.scrm.service;

import com.wecom.scrm.dto.MomentDTO;
import com.wecom.scrm.entity.WecomMoment;
import com.wecom.scrm.repository.WecomMomentRecordRepository;
import com.wecom.scrm.repository.WecomMomentRepository;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.external.WxCpAddMomentResult;
import me.chanjar.weixin.cp.bean.external.WxCpAddMomentTask;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.bean.external.WxCpGetMomentTaskResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MomentServiceTest {

    private MomentService momentService;

    @Mock
    private WxCpServiceManager wxCpServiceManager;

    @Mock
    private WxCpService wxCpService;

    @Mock
    private WxCpExternalContactService externalContactService;

    @Mock
    private WecomMomentRepository momentRepository;

    @Mock
    private WecomMomentRecordRepository momentRecordRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(wxCpServiceManager.getWxCpService()).thenReturn(wxCpService);
        when(wxCpService.getExternalContactService()).thenReturn(externalContactService);
        momentService = new MomentService(wxCpServiceManager, momentRepository, momentRecordRepository);
    }

    @Test
    public void testCreateMomentTask() throws Exception {
        MomentDTO.CreateRequest request = new MomentDTO.CreateRequest();
        request.setText("Hello World");
        
        MomentDTO.VisibleRange range = new MomentDTO.VisibleRange();
        range.setSenderList(Collections.singletonList("staff123"));
        request.setVisibleRange(range);

        WxCpAddMomentResult addResult = new WxCpAddMomentResult();
        addResult.setJobId("job_123");
        when(externalContactService.addMomentTask(any(WxCpAddMomentTask.class))).thenReturn(addResult);

        WecomMoment savedMoment = new WecomMoment();
        savedMoment.setId(1L);
        savedMoment.setJobid("job_123");
        savedMoment.setText("Hello World");
        when(momentRepository.save(any(WecomMoment.class))).thenReturn(savedMoment);

        WecomMoment result = momentService.createMomentTask(request, "admin");

        assertNotNull(result);
        assertEquals("job_123", result.getJobid());
        verify(externalContactService).addMomentTask(any(WxCpAddMomentTask.class));
        verify(momentRepository).save(any(WecomMoment.class));
    }

    @Test
    public void testSyncMomentStatus() throws Exception {
        WecomMoment moment = new WecomMoment();
        moment.setId(1L);
        moment.setJobid("job_123");
        moment.setStatus(0);

        WxCpGetMomentTaskResult taskResult = new WxCpGetMomentTaskResult();
        taskResult.setStatus(1); // finished
        WxCpGetMomentTaskResult.TaskResult innerResult = new WxCpGetMomentTaskResult.TaskResult();
        innerResult.setMomentId("moment_abc");
        taskResult.setResult(innerResult);
        
        when(externalContactService.getMomentTaskResult("job_123")).thenReturn(taskResult);
        when(externalContactService.getMomentTask(eq("moment_abc"), any(), any())).thenReturn(null);
        when(momentRepository.save(any(WecomMoment.class))).thenReturn(moment);

        momentService.syncMomentStatus(moment);

        assertEquals("moment_abc", moment.getMomentId());
        assertEquals(1, moment.getStatus());
        verify(momentRepository).save(moment);
    }
}
