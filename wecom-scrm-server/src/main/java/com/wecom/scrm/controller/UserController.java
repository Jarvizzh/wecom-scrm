package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomDepartment;
import com.wecom.scrm.entity.WecomUser;
import com.wecom.scrm.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<WecomUser> getUsers(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return userService.getUsers(departmentId, page, size, keyword);
    }

    @GetMapping("/departments")
    public List<WecomDepartment> getDepartments() {
        return userService.getDepartments();
    }

    @PostMapping("/{userid}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String userid, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("scrmStatus");
        if (status == null) {
            return ResponseEntity.badRequest().body("scrmStatus is required");
        }
        userService.updateUserScrmStatus(userid, status);
        return ResponseEntity.ok("Status updated successfully");
    }
}
