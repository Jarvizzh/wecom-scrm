package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.service.EnterpriseManagementService;
import com.wecom.scrm.service.WecomEnterpriseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enterprises")
public class EnterpriseController {

    private final EnterpriseManagementService enterpriseManagementService;
    private final WecomEnterpriseService wecomEnterpriseService;

    public EnterpriseController(EnterpriseManagementService enterpriseManagementService,
                                WecomEnterpriseService wecomEnterpriseService) {
        this.enterpriseManagementService = enterpriseManagementService;
        this.wecomEnterpriseService = wecomEnterpriseService;
    }

    @GetMapping
    public List<WecomEnterprise> listEnterprises() {
        return wecomEnterpriseService.findAll();
    }

    @PostMapping
    public WecomEnterprise addEnterprise(@RequestBody WecomEnterprise enterprise) {
        return enterpriseManagementService.addEnterprise(enterprise);
    }

    @PutMapping("/{id}")
    public WecomEnterprise updateEnterprise(@PathVariable Long id, @RequestBody WecomEnterprise enterprise) {
        return enterpriseManagementService.updateEnterprise(id, enterprise);
    }

    @DeleteMapping("/{id}")
    public void deleteEnterprise(@PathVariable Long id) {
        enterpriseManagementService.deleteEnterprise(id);
    }
}
