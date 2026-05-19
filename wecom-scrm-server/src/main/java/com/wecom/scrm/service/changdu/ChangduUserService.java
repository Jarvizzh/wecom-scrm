package com.wecom.scrm.service.changdu;

import com.wecom.scrm.dto.changdu.ChangduUserDTO;
import com.wecom.scrm.entity.changdu.ChangduUser;
import com.wecom.scrm.repository.changdu.ChangduUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChangduUserService {

    private final ChangduUserRepository userRepository;
    private final ChangduSyncService syncService;
    private final com.wecom.scrm.repository.WecomCustomerRelationRepository relationRepository;
    private final com.wecom.scrm.repository.WecomUserRepository wecomUserRepository;

    private List<ChangduUserDTO> convertToDtoList(List<ChangduUser> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> externalUserids = users.stream()
                .map(ChangduUser::getExternalId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<com.wecom.scrm.entity.WecomCustomerRelation>> relationMap = new java.util.HashMap<>();
        if (!externalUserids.isEmpty()) {
            List<com.wecom.scrm.entity.WecomCustomerRelation> allRelations = relationRepository.findByExternalUseridIn(externalUserids);
            relationMap = allRelations.stream().collect(Collectors.groupingBy(com.wecom.scrm.entity.WecomCustomerRelation::getExternalUserid));
        }

        List<String> allEmployeeUserids = relationMap.values().stream()
                .flatMap(List::stream)
                .map(com.wecom.scrm.entity.WecomCustomerRelation::getUserid)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> employeeNameMap = new java.util.HashMap<>();
        if (!allEmployeeUserids.isEmpty()) {
            employeeNameMap = wecomUserRepository.findByUseridIn(allEmployeeUserids).stream()
                    .collect(Collectors.toMap(com.wecom.scrm.entity.WecomUser::getUserid, com.wecom.scrm.entity.WecomUser::getName, (v1, v2) -> v1));
        }

        Map<String, List<com.wecom.scrm.entity.WecomCustomerRelation>> finalRelationMap = relationMap;
        Map<String, String> finalEmployeeNameMap = employeeNameMap;
        return users.stream().map(user -> {
            ChangduUserDTO dto = ChangduUserDTO.fromEntity(user);
            
            if (StringUtils.hasText(user.getExternalId())) {
                List<com.wecom.scrm.entity.WecomCustomerRelation> relations = finalRelationMap.get(user.getExternalId());
                if (relations != null && !relations.isEmpty()) {
                    String employeeNames = relations.stream()
                            .map(r -> finalEmployeeNameMap.getOrDefault(r.getUserid(), r.getUserid()))
                            .distinct()
                            .collect(Collectors.joining(", "));
                    dto.setEmployeeName(employeeNames);
                    
                    Integer status = relations.stream()
                            .map(com.wecom.scrm.entity.WecomCustomerRelation::getStatus)
                            .min(Integer::compare)
                            .orElse(null);
                    dto.setRelationStatus(status);
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public Page<ChangduUserDTO> getUsers(int page, int size, Long distributorId, String openId, String nickname, String sortField, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = "ascending".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        openId = StringUtils.hasText(openId) ? openId : null;
        nickname = StringUtils.hasText(nickname) ? nickname : null;

        Page<ChangduUser> userPage = userRepository.findUsers(distributorId, openId, nickname, pageable);
        List<ChangduUserDTO> dtoList = convertToDtoList(userPage.getContent());
        return new PageImpl<>(dtoList, userPage.getPageable(), userPage.getTotalElements());
    }

    @Async("bizAsyncExecutor")
    public void syncUsers(Long distributorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        long startTs = startTime.toEpochSecond(ZoneOffset.ofHours(8));
        long endTs = endTime.toEpochSecond(ZoneOffset.ofHours(8));
        syncService.syncUsers(distributorId, startTs, endTs);
    }

    public List<ChangduUserDTO> findByCustomer(String externalUserid) {
        List<ChangduUser> users = userRepository.findByExternalId(externalUserid);
        return convertToDtoList(users);
    }
}
