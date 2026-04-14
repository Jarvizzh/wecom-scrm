package com.wecom.scrm.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.repository.WecomEnterpriseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@DS("master")
@Service
public class WecomEnterpriseService {

    private final WecomEnterpriseRepository wecomEnterpriseRepository;

    public WecomEnterpriseService(WecomEnterpriseRepository wecomEnterpriseRepository) {
        this.wecomEnterpriseRepository = wecomEnterpriseRepository;
    }

    public List<WecomEnterprise> findAll() {
        return wecomEnterpriseRepository.findAll();
    }

    public WecomEnterprise findById(Long id) {
        return wecomEnterpriseRepository.findById(id).orElse(null);
    }

    public WecomEnterprise findByCorpId(String corpId) {
        return wecomEnterpriseRepository.findByCorpId(corpId);
    }

    public WecomEnterprise save(WecomEnterprise enterprise) {
        return wecomEnterpriseRepository.save(enterprise);
    }

    public void delete(Long id) {
        wecomEnterpriseRepository.deleteById(id);
    }
}
