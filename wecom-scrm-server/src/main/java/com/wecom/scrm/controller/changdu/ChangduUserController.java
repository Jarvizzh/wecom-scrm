package com.wecom.scrm.controller.changdu;

import com.wecom.scrm.entity.changdu.ChangduUser;
import com.wecom.scrm.service.changdu.ChangduUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/changdu/users")
@RequiredArgsConstructor
public class ChangduUserController {

    private final ChangduUserService userService;

    @GetMapping
    public Page<ChangduUser> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) String openId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        return userService.getUsers(page, size, distributorId, openId, nickname, sortField, sortOrder);
    }

    @GetMapping("/customer/{externalUserid}")
    public List<ChangduUser> getByCustomer(@PathVariable String externalUserid) {
        return userService.findByCustomer(externalUserid);
    }

    @PostMapping("/sync")
    public void syncUsers(
            @RequestParam Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        userService.syncUsers(distributorId, startTime, endTime);
    }
}
