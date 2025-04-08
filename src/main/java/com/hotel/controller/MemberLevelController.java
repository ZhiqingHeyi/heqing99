package com.hotel.controller;

import com.hotel.common.ApiResponse;
import com.hotel.entity.MemberLevelChange;
import com.hotel.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member-level")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    @PostMapping("/change")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MemberLevelChange> changeMemberLevel(
            @RequestBody MemberLevelChange change) {
        return ApiResponse.success(memberLevelService.changeMemberLevel(change));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<MemberLevelChange>> getMemberLevelHistory(
            Pageable pageable) {
        return ApiResponse.success(memberLevelService.getMemberLevelHistory(pageable));
    }

    @GetMapping("/check-upgrade")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> checkAndHandleUpgrade() {
        return ApiResponse.success(memberLevelService.checkAndHandleUpgrade());
    }
} 