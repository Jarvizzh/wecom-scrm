package com.wecom.scrm.service.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenConsumeRecordDTO;
import com.wecom.scrm.entity.yuewen.YuewenProduct;
import com.wecom.scrm.entity.yuewen.YuewenConsumeRecord;
import com.wecom.scrm.repository.yuewen.YuewenProductRepository;
import com.wecom.scrm.repository.yuewen.YuewenConsumeRecordRepository;
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
public class YuewenConsumeService {

    private final YuewenConsumeRecordRepository consumeRepository;
    private final YuewenProductRepository productRepository;

    public Page<YuewenConsumeRecordDTO> listConsumeRecords(
            String appFlag, String openid, Long guid, String bookName,
            String sortField, String sortOrder, int page, int size) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "consumeTime");
        if (StringUtils.hasText(sortField)) {
            Sort.Direction direction = "ascending".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }

        Page<YuewenConsumeRecord> recordPage = consumeRepository.findByFilters(
                appFlag, openid, guid, bookName, PageRequest.of(page - 1, size, sort));
        
        List<YuewenConsumeRecordDTO> dtoList = convertToDtoList(recordPage.getContent());
        return new PageImpl<>(dtoList, recordPage.getPageable(), recordPage.getTotalElements());
    }

    private List<YuewenConsumeRecordDTO> convertToDtoList(List<YuewenConsumeRecord> records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> productMap = productRepository.findAll().stream()
                .collect(Collectors.toMap(YuewenProduct::getAppFlag, YuewenProduct::getProductName, (v1, v2) -> v1));

        return records.stream().map(record -> {
            YuewenConsumeRecordDTO dto = YuewenConsumeRecordDTO.fromEntity(record);
            dto.setProductName(productMap.getOrDefault(record.getAppFlag(), record.getAppFlag()));
            return dto;
        }).collect(Collectors.toList());
    }
}
