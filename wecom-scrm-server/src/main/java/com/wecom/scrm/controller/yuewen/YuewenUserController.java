package com.wecom.scrm.controller.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenUser;
import com.wecom.scrm.service.yuewen.YuewenUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/yuewen/user")
@RequiredArgsConstructor
public class YuewenUserController {

    private final YuewenUserService userService;

    @GetMapping
    public Page<YuewenUser> list(
            @RequestParam(required = false) String appFlag,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.listUsers(appFlag, openid, sortField, sortOrder, page, size);
    }
}
