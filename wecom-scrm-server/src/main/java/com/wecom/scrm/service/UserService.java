package com.wecom.scrm.service;

import com.wecom.scrm.entity.WecomDepartment;
import com.wecom.scrm.entity.WecomUser;
import com.wecom.scrm.repository.WecomDepartmentRepository;
import com.wecom.scrm.repository.WecomUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final WecomUserRepository userRepository;
    private final WecomDepartmentRepository departmentRepository;

    public UserService(WecomUserRepository userRepository, WecomDepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public Page<WecomUser> getUsers(Long departmentId, int page, int size, String keyword) {
        Pageable jpqlPageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Pageable nativePageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "create_time"));
        
        if (departmentId != null && keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByDepartmentIdAndKeyword(String.valueOf(departmentId), keyword.trim(), nativePageable);
        } else if (departmentId != null) {
            return userRepository.findByDepartmentId(String.valueOf(departmentId), nativePageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByNameContainingOrUseridContaining(keyword.trim(), keyword.trim(), jpqlPageable);
        }
        
        return userRepository.findAll(jpqlPageable);
    }

    public List<WecomDepartment> getDepartments() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "order"));
    }

    public void updateUserScrmStatus(String userid, Integer scrmStatus) {
        WecomUser user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setScrmStatus(scrmStatus);
        userRepository.save(user);
    }
}
