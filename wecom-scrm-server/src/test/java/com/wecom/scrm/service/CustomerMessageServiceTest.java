package com.wecom.scrm.service;

import com.wecom.scrm.dto.CustomerMessageDTO;
import com.wecom.scrm.entity.WecomCustomerMessage;
import com.wecom.scrm.entity.WecomCustomerTag;
import com.wecom.scrm.repository.WecomCustomerTagRepository;
import com.wecom.scrm.repository.WecomCustomerRelationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class CustomerMessageServiceTest {

    @Autowired
    private CustomerMessageService messageService;

    @Autowired
    private WecomCustomerRelationRepository relationRepository;

    @Autowired
    private WecomCustomerTagRepository customerTagRepository;

    @Autowired
    private javax.persistence.EntityManager entityManager;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        customerTagRepository.deleteAll();
        relationRepository.deleteAll();
    }

    @Test
    public void testFilteringLogic() throws Exception {
        // Setup data
        saveRelationNative("user1", "ext1", LocalDateTime.now().minusDays(5));
        saveRelationNative("user1", "ext2", LocalDateTime.now().minusDays(10));
        saveRelationNative("user2", "ext3", LocalDateTime.now().minusDays(2));

        saveTag("ext1", "user1", "tagA");
        saveTag("ext1", "user1", "tagB");
        saveTag("ext2", "user1", "tagA");
        saveTag("ext3", "user2", "tagC");

        // Case 1: All
        WecomCustomerMessage msgAll = new WecomCustomerMessage();
        msgAll.setTargetType(0);
        List<String> idsAll = messageService.findMatchedExternalUserids(msgAll);
        assertThat(idsAll).hasSize(3).containsExactlyInAnyOrder("ext1", "ext2", "ext3");

        // Case 2: Filter by Tag ANY (A or C)
        WecomCustomerMessage msgAny = new WecomCustomerMessage();
        msgAny.setTargetType(1);
        msgAny.setTargetCondition("{\"includeTags\":[\"tagA\",\"tagC\"],\"includeTagsAny\":true}");
        List<String> idsAny = messageService.findMatchedExternalUserids(msgAny);
        assertThat(idsAny).hasSize(3).containsExactlyInAnyOrder("ext1", "ext2", "ext3");

        // Case 3: Filter by Tag ALL (A and B)
        WecomCustomerMessage msgAllTags = new WecomCustomerMessage();
        msgAllTags.setTargetType(1);
        msgAllTags.setTargetCondition("{\"includeTags\":[\"tagA\",\"tagB\"],\"includeTagsAny\":false}");
        List<String> idsAllTags = messageService.findMatchedExternalUserids(msgAllTags);
        assertThat(idsAllTags).hasSize(1).containsExactlyInAnyOrder("ext1");

        // Case 4: Exclude Tag A
        WecomCustomerMessage msgExclude = new WecomCustomerMessage();
        msgExclude.setTargetType(1);
        msgExclude.setTargetCondition("{\"excludeTags\":[\"tagA\"]}");
        List<String> idsExclude = messageService.findMatchedExternalUserids(msgExclude);
        assertThat(idsExclude).hasSize(1).containsExactlyInAnyOrder("ext3");

        // Case 5: Time Range (last 3 days)
        WecomCustomerMessage msgTime = new WecomCustomerMessage();
        msgTime.setTargetType(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        msgTime.setTargetCondition("{\"addTimeStart\":\"" + LocalDateTime.now().minusDays(3).format(formatter) + "\"}");
        List<String> idsTime = messageService.findMatchedExternalUserids(msgTime);
        assertThat(idsTime).hasSize(1).containsExactlyInAnyOrder("ext3");
    }

    @Test
    public void testCreateMessageWithPastTime() {
        CustomerMessageDTO.CreateRequest request = new CustomerMessageDTO.CreateRequest();
        request.setTaskName("Past Task");
        request.setSendType(1);
        request.setSendTime(LocalDateTime.now().minusMinutes(5));

        assertThatThrownBy(() -> messageService.createMessageTask(request, "tester"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("发送时间不得早于当前时间");
    }

    private void saveRelationNative(String userid, String externalUserid, LocalDateTime createTime) {
        entityManager.createNativeQuery("INSERT INTO wecom_customer_relation (userid, external_userid, status, create_time, add_way) VALUES (?, ?, ?, ?, ?)")
                .setParameter(1, userid)
                .setParameter(2, externalUserid)
                .setParameter(3, 0)
                .setParameter(4, createTime)
                .setParameter(5, 1)
                .executeUpdate();
    }

    private void saveTag(String externalUserid, String userid, String tagId) {
        WecomCustomerTag tag = new WecomCustomerTag();
        tag.setExternalUserid(externalUserid);
        tag.setUserid(userid);
        tag.setTagId(tagId);
        customerTagRepository.save(tag);
    }
}
