package com.wecom.scrm.service.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenUserDTO;
import com.wecom.scrm.entity.yuewen.YuewenProduct;
import com.wecom.scrm.entity.yuewen.YuewenUser;
import com.wecom.scrm.repository.yuewen.YuewenProductRepository;
import com.wecom.scrm.repository.yuewen.YuewenUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YuewenUserService {

    private final YuewenUserRepository userRepository;
    private final YuewenProductRepository productRepository;

    private List<YuewenUserDTO> convertToDtoList(List<YuewenUser> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> productMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(YuewenProduct::getAppFlag, YuewenProduct::getProductName, (v1, v2) -> v1));

        return users.stream().map(user -> {
            YuewenUserDTO dto = YuewenUserDTO.fromEntity(user);
            dto.setProductName(productMap.getOrDefault(user.getAppFlag(), user.getAppFlag()));
            return dto;
        }).collect(Collectors.toList());
    }

    public Page<YuewenUserDTO> listUsers(String appFlag, String openid, String nickname, Long minAmount, Long maxAmount, String sortField, String sortOrder, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "registTime");
        if (StringUtils.hasText(sortField)) {
            Sort.Direction direction = "ascending".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }
        Page<YuewenUser> userPage = userRepository.findByFilters(appFlag, openid, nickname, minAmount, maxAmount, PageRequest.of(page - 1, size, sort));
        List<YuewenUserDTO> dtoList = convertToDtoList(userPage.getContent());
        return new PageImpl<>(dtoList, userPage.getPageable(), userPage.getTotalElements());
    }

    public List<YuewenUserDTO> findByCustomer(String externalUserid) {
        List<YuewenUser> users = userRepository.findByExternalUserid(externalUserid);
        return convertToDtoList(users);
    }
}
