package com.wecom.scrm.service;

import com.wecom.scrm.dto.CustomerRelationDTO;
import com.wecom.scrm.entity.WecomCustomerTag;
import com.wecom.scrm.entity.WecomMpUser;
import com.wecom.scrm.entity.WecomTag;
import com.wecom.scrm.repository.WecomCustomerRelationRepository;
import com.wecom.scrm.repository.WecomCustomerTagRepository;
import com.wecom.scrm.repository.WecomMpUserRepository;
import com.wecom.scrm.repository.WecomTagRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final WecomCustomerRelationRepository relationRepository;
    private final WecomCustomerTagRepository customerTagRepository;
    private final WecomTagRepository tagRepository;
    private final WecomMpUserRepository mpUserRepository;

    public CustomerService(WecomCustomerRelationRepository relationRepository,
                           WecomCustomerTagRepository customerTagRepository,
                           WecomTagRepository tagRepository,
                           WecomMpUserRepository mpUserRepository) {
        this.relationRepository = relationRepository;
        this.customerTagRepository = customerTagRepository;
        this.tagRepository = tagRepository;
        this.mpUserRepository = mpUserRepository;
    }

    public Page<CustomerRelationDTO> getCustomers(int page, int size, String customerName, String unionid, String employeeName, String mpAppId, List<String> tagIds, Integer status, boolean onlyDuplicates) {
        Sort sort = onlyDuplicates ? Sort.by(Sort.Direction.ASC, "externalUserid") : Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        
        String cleanCustomerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName.trim() : null;
        String cleanUnionid = (unionid != null && !unionid.trim().isEmpty()) ? unionid.trim() : null;
        String cleanEmployeeName = (employeeName != null && !employeeName.trim().isEmpty()) ? employeeName.trim() : null;
        String cleanMpAppId = (mpAppId != null && !mpAppId.trim().isEmpty()) ? mpAppId.trim() : null;
        
        // Strict sanitization of tagIds: remove nulls or blank strings
        List<String> cleanTagIds = (tagIds != null) ? tagIds.stream()
                .filter(tid -> tid != null && !tid.trim().isEmpty())
                .collect(Collectors.toList()) : null;
        boolean hasTags = (cleanTagIds != null && !cleanTagIds.isEmpty());
        if (!hasTags) {
            cleanTagIds = null;
        }
        
        Page<CustomerRelationDTO> customerPage = relationRepository.findCustomerRelations(cleanCustomerName, cleanUnionid, cleanEmployeeName, cleanMpAppId, cleanTagIds, hasTags, status, onlyDuplicates, pageable);
        
        if (customerPage.isEmpty()) {
            return customerPage;
        }

        // Batch fetch tags and MP info for these customers
        Set<String> unionids = customerPage.getContent().stream()
                .map(CustomerRelationDTO::getUnionid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, List<WecomMpUser>> mpUserMap = null;
        if (!unionids.isEmpty()) {
            List<WecomMpUser> mpUsers = mpUserRepository.findByUnionidIn(unionids);
            mpUserMap = mpUsers.stream().collect(Collectors.groupingBy(WecomMpUser::getUnionid));
        }

        for (CustomerRelationDTO dto : customerPage.getContent()) {
            // Fill Tags
            List<WecomCustomerTag> customerTags = customerTagRepository.findByExternalUseridAndUserid(dto.getExternalUserid(), dto.getEmployeeUserid());
            if (!customerTags.isEmpty()) {
                List<String> customerTagIds = customerTags.stream().map(WecomCustomerTag::getTagId).collect(Collectors.toList());
                dto.setTagIds(customerTagIds);
                List<String> tagNames = customerTagIds.stream()
                        .map(tid -> tagRepository.findByTagId(tid).map(WecomTag::getName).orElse(tid))
                        .collect(Collectors.toList());
                dto.setTagNames(String.join(",", tagNames));
            }

            // Fill MP Info
            if (dto.getUnionid() != null && mpUserMap != null && mpUserMap.containsKey(dto.getUnionid())) {
                List<WecomMpUser> mpUsers = mpUserMap.get(dto.getUnionid());
                String mpNames = mpUsers.stream().map(WecomMpUser::getMpName).filter(Objects::nonNull).distinct().collect(Collectors.joining(","));
                String mpOpenids = mpUsers.stream().map(WecomMpUser::getOpenid).filter(Objects::nonNull).collect(Collectors.joining(","));
                dto.setMpName(mpNames);
                dto.setMpOpenid(mpOpenids);
            }
        }

        return customerPage;
    }
}
