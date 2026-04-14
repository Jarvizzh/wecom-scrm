package com.wecom.scrm.controller;

import com.wecom.scrm.dto.CustomerRelationDTO;
import com.wecom.scrm.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Page<CustomerRelationDTO> getCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String unionid,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String mpAppId,
            @RequestParam(required = false) List<String> tagIds,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "false") boolean onlyDuplicates) {
        return customerService.getCustomers(page, size, customerName, unionid, employeeName, mpAppId, tagIds, status, onlyDuplicates);
    }
}
