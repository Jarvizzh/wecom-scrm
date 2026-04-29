package com.wecom.scrm.service;

import com.wecom.scrm.entity.*;
import com.wecom.scrm.repository.*;
import me.chanjar.weixin.cp.api.WxCpService;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.bean.external.contact.WxCpExternalContactInfo;
import me.chanjar.weixin.cp.bean.external.contact.ExternalContact;
import me.chanjar.weixin.cp.bean.external.contact.FollowedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class SyncServiceTest {

    private SyncService syncService;

    @Mock
    private WxCpServiceManager wxCpServiceManager;

    @Mock
    private WxCpService wxCpService;

    @Mock
    private WxCpExternalContactService externalContactService;

    @Mock
    private WecomUserRepository userRepository;

    @Mock
    private WecomCustomerRepository customerRepository;

    @Mock
    private WecomCustomerRelationRepository customerRelationRepository;

    @Mock
    private WecomSyncLogRepository syncLogRepository;

    @Mock
    private WecomTagGroupRepository tagGroupRepository;

    @Mock
    private WecomTagRepository tagRepository;

    @Mock
    private WecomCustomerTagRepository customerTagRepository;

    @Mock
    private WecomDepartmentRepository departmentRepository;

    @Mock
    private GroupChatRepository groupChatRepository;
    @Mock
    private GroupChatMemberRepository memberRepository;
    @Mock
    private java.util.concurrent.Executor bizAsyncExecutor;
    @Mock
    private java.util.concurrent.Executor syncCustomersExecutor;
    @Mock
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(wxCpServiceManager.getWxCpService()).thenReturn(wxCpService);
        when(wxCpService.getExternalContactService()).thenReturn(externalContactService);

        syncService = new SyncService(
                wxCpServiceManager,
                userRepository,
                customerRepository,
                customerRelationRepository,
                syncLogRepository,
                tagGroupRepository,
                tagRepository,
                customerTagRepository,
                departmentRepository,
                groupChatRepository,
                memberRepository,
                bizAsyncExecutor,
                syncCustomersExecutor,
                transactionTemplate
        );

        // Mock Executors to run tasks immediately
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(bizAsyncExecutor).execute(any(Runnable.class));

        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(syncCustomersExecutor).execute(any(Runnable.class));

        // Mock TransactionTemplate to execute callback immediately
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    public void testSyncAllCustomers() throws Exception {
        // 1. Mock internal user
        WecomUser staff = new WecomUser();
        staff.setUserid("staff123");
        staff.setStatus(1); // Active
        when(userRepository.findAll()).thenReturn(Collections.singletonList(staff));

        // 2. Mock external contact list for staff
        when(externalContactService.listExternalContacts("staff123")).thenReturn(Collections.singletonList("externalId999"));

        // 3. Mock external contact detail
        WxCpExternalContactInfo contactInfo = new WxCpExternalContactInfo();
        // Since ExternalContact is used as a type, we mock it
        ExternalContact externalContact = mock(ExternalContact.class);
        when(externalContact.getExternalUserId()).thenReturn("externalId999");
        when(externalContact.getName()).thenReturn("John Doe");
        when(externalContact.getType()).thenReturn(1);
        when(externalContact.getGender()).thenReturn(1);
        contactInfo.setExternalContact(externalContact);

        FollowedUser followedUser = new FollowedUser();
        followedUser.setUserId("staff123");
        followedUser.setRemark("VIP Customer");
        followedUser.setDescription("A very important customer");
        followedUser.setAddWay("1"); // String expected
        followedUser.setState("test_state");

        // Mock Tags
        FollowedUser.Tag tag1 = new FollowedUser.Tag();
        tag1.setTagId("tag_a");
        FollowedUser.Tag tag2 = new FollowedUser.Tag();
        tag2.setTagId("tag_b");
        // setTags expects Tag[] array
        followedUser.setTags(new FollowedUser.Tag[]{tag1, tag2});

        contactInfo.setFollowedUsers(Collections.singletonList(followedUser));

        when(externalContactService.getContactDetail("externalId999", null)).thenReturn(contactInfo);

        // 4. Mock repository behavior
        when(customerRepository.findByExternalUserid("externalId999")).thenReturn(Optional.empty());
        when(customerRelationRepository.findByExternalUserid("externalId999")).thenReturn(Collections.emptyList());
        when(customerRelationRepository.findByUseridAndExternalUserid("staff123", "externalId999")).thenReturn(Optional.empty());
        when(syncLogRepository.save(any())).thenReturn(new WecomSyncLog());

        // 5. Execute sync
        syncService.syncAllCustomers();

        // 6. Verify Customer saveAll
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<WecomCustomer>> customerCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(customerRepository).saveAll(customerCaptor.capture());
        WecomCustomer customer = customerCaptor.getValue().iterator().next();
        assertEquals("John Doe", customer.getName());
        assertEquals("externalId999", customer.getExternalUserid());

        // 7. Verify Relation saveAll
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<WecomCustomerRelation>> relationCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(customerRelationRepository).saveAll(relationCaptor.capture());
        WecomCustomerRelation relation = relationCaptor.getValue().iterator().next();
        assertEquals("staff123", relation.getUserid());
        assertEquals("VIP Customer", relation.getRemark());

        // 8. Verify Tags saveAll
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<WecomCustomerTag>> tagsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(customerTagRepository).saveAll(tagsCaptor.capture());
        verify(customerTagRepository).deleteByExternalUseridIn(anyList());
    }
}
