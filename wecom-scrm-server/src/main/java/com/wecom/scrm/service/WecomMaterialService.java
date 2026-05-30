package com.wecom.scrm.service;

import com.wecom.scrm.entity.WecomMaterial;
import com.wecom.scrm.repository.WecomMaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WecomMaterialService {

    private final WecomMaterialRepository materialRepository;

    public WecomMaterialService(WecomMaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public Page<WecomMaterial> getMaterials(String type, String title, String sourceType, int page, int size) {
        // Convert empty strings to null for repository query mapping
        String queryType = StringUtils.hasText(type) ? type : null;
        String queryTitle = StringUtils.hasText(title) ? title : null;
        String querySourceType = StringUtils.hasText(sourceType) ? sourceType : null;

        return materialRepository.findByFilters(
                queryType,
                queryTitle,
                querySourceType,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"))
        );
    }

    public Optional<WecomMaterial> getById(Long id) {
        return materialRepository.findById(id);
    }

    @Transactional
    public WecomMaterial save(WecomMaterial material) {
        if (material == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材数据不能为空");
        }
        if (!StringUtils.hasText(material.getTitle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材名称不能为空");
        }
        if (material.getTitle().trim().length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材名称不能超过 20 个字");
        }
        if (!StringUtils.hasText(material.getType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材类型不能为空");
        }

        // Validate type-specific constraints
        if ("TEXT".equals(material.getType())) {
            if (!StringUtils.hasText(material.getContent())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文案内容不能为空");
            }
            if (material.getContent().length() > 50) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文案内容不能超过 50 个字");
            }
        } else if ("IMAGE".equals(material.getType())) {
            if (!StringUtils.hasText(material.getContent())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片素材内容不能为空");
            }
        } else if ("H5".equals(material.getType())) {
            if (!StringUtils.hasText(material.getContent())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "H5 推广链接不能为空");
            }
            if (!material.getContent().startsWith("http")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "H5 推广链接必须以 http:// 或 https:// 开头");
            }
        } else if ("MINIPROGRAM".equals(material.getType())) {
            if (!StringUtils.hasText(material.getAppId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "小程序 AppID 不能为空");
            }
            if (!StringUtils.hasText(material.getPagePath())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "小程序页面路径不能为空");
            }
            material.setPagePath(formatMiniProgramPath(material.getPagePath()));
        }

        if (material.getId() == null) {
            log.info("Creating material: {}", material.getTitle());
        } else {
            log.info("Updating material ID {}: {}", material.getId(), material.getTitle());
        }
        return materialRepository.save(material);
    }

    @Transactional
    public List<WecomMaterial> saveBatch(List<WecomMaterial> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材数据列表不能为空");
        }
        for (WecomMaterial material : materials) {
            if (!StringUtils.hasText(material.getTitle())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材名称不能为空");
            }
            if (material.getTitle().trim().length() > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "素材名称不能超过 20 个字: " + material.getTitle());
            }
            if (!"TEXT".equals(material.getType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "批量导入仅支持文本类型素材");
            }
            if (!StringUtils.hasText(material.getContent())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文案内容不能为空");
            }
            if (material.getContent().length() > 50) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文案内容不能超过 50 个字: " + material.getContent());
            }
        }
        log.info("Batch saving {} materials", materials.size());
        return materialRepository.saveAll(materials);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting material ID: {}", id);
        materialRepository.deleteById(id);
    }

    private String formatMiniProgramPath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String trimmed = path.trim();
        int qIndex = trimmed.indexOf("?");
        if (qIndex > -1) {
            String mainPath = trimmed.substring(0, qIndex);
            String query = trimmed.substring(qIndex + 1);
            if (!mainPath.endsWith(".html")) {
                return mainPath + ".html?" + query;
            }
        } else {
            if (!trimmed.endsWith(".html")) {
                return trimmed + ".html";
            }
        }
        return trimmed;
    }
}
