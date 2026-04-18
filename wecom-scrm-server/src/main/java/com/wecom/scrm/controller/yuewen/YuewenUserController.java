package com.wecom.scrm.controller.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenUserDTO;
import com.wecom.scrm.service.yuewen.YuewenUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/yuewen/user")
@RequiredArgsConstructor
public class YuewenUserController {

    private final YuewenUserService userService;

    @GetMapping
    public Page<YuewenUserDTO> list(
            @RequestParam(required = false) String appFlag,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) Long minAmount,
            @RequestParam(required = false) Long maxAmount,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.listUsers(appFlag, openid, minAmount, maxAmount, sortField, sortOrder, page, size);
    }

    @GetMapping("/customer/{externalUserid}")
    public List<YuewenUserDTO> getByCustomer(@PathVariable String externalUserid) {
        return userService.findByCustomer(externalUserid);
    }
}
