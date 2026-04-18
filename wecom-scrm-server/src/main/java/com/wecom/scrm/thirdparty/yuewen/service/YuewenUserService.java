package com.wecom.scrm.thirdparty.yuewen.service;

import com.wecom.scrm.thirdparty.yuewen.entity.YuewenUser;
import com.wecom.scrm.thirdparty.yuewen.repository.YuewenUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class YuewenUserService {

    private final YuewenUserRepository userRepository;

    public Page<YuewenUser> listUsers(String appFlag, String openid, String sortField, String sortOrder, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "registTime");
        if (StringUtils.hasText(sortField)) {
            Sort.Direction direction = "ascending".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }
        return userRepository.findByFilters(appFlag, openid, PageRequest.of(page - 1, size, sort));
    }
}
