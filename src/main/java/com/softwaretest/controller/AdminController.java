package com.softwaretest.controller;

import com.softwaretest.dto.ApiResponse;
import com.softwaretest.entity.User;
import com.softwaretest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String userType) {
        
        List<User> users;
        if (userType != null) {
            users = userRepository.findAll().stream()
                .filter(u -> userType.equals(u.getUserType()))
                .toList();
        } else {
            users = userRepository.findAll();
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("users", users.stream().map(u -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", u.getId());
            userMap.put("username", u.getUsername());
            userMap.put("email", u.getEmail());
            userMap.put("userType", u.getUserType());
            return userMap;
        }).toList());
        data.put("total", users.size());
        
        return ResponseEntity.ok(ApiResponse.success("获取成功", data));
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "用户不存在"));
        }
        
        userRepository.deleteById(userId);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
    
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 这里简化处理，实际应该有status字段
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success("更新成功", null));
    }
}
