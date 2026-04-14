package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomMpAccount;
import com.wecom.scrm.entity.WecomMpUser;
import com.wecom.scrm.service.MpService;
import com.wecom.scrm.dto.CopyMpAccountRequest;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信公众号
 */
@RestController
@RequestMapping("/api/admin/mp")
public class MpController {

    private final MpService mpService;

    public MpController(MpService mpService) {
        this.mpService = mpService;
    }

    @GetMapping("/accounts")
    public List<WecomMpAccount> getAccounts() {
        return mpService.getAllAccounts();
    }

    @PostMapping("/accounts")
    public WecomMpAccount saveAccount(@RequestBody WecomMpAccount account) {
        return mpService.saveAccount(account);
    }

    @DeleteMapping("/accounts/{id}")
    public void deleteAccount(@PathVariable Long id) {
        mpService.deleteAccount(id);
    }

    @GetMapping("/users")
    public Page<WecomMpUser> getUsers(@RequestParam(required = false) String mpAppId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "subscribeTime") String orderBy,
                                      @RequestParam(defaultValue = "descending") String order) {
        // Map Element Plus order names to Spring Data Sort.Direction
        Sort.Direction direction = 
                "ascending".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Sort sort = Sort.by(direction, orderBy);
        
        // PageRequest is 0-indexed in Spring Data JPA
        return mpService.getUsers(mpAppId, keyword, PageRequest.of(page - 1, size, sort));
    }

    @PostMapping("/sync")
    public void syncUsers(@RequestParam String appId) {
        mpService.syncUsers(appId, DynamicDataSourceContextHolder.peek());
    }

    @PostMapping("/accounts/copy")
    public void copyAccount(@RequestBody CopyMpAccountRequest request) {
        mpService.copyToEnterprise(request);
    }
}
