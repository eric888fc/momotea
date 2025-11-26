package demo.bigwork.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.vo.AdminUserSummaryVO;
import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.AdminMemberService;

/**
 * 管理員「會員管理」API
 *
 * 路徑全部在 /api/admin/members 底下
 * 已經被 SecurityConfig 限制成 ADMIN 才能呼叫
 */
@RestController
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    /**
     * 買家管理：取得所有買家列表
     * GET /api/admin/members/buyers
     */
    @GetMapping("/buyers")
    public ResponseEntity<List<AdminUserSummaryVO>> listBuyers() {
        return ResponseEntity.ok(
                adminMemberService.listUsersByRole(UserRole.BUYER)
        );
    }

    /**
     * 賣家管理：取得所有賣家列表
     * GET /api/admin/members/sellers
     */
    @GetMapping("/sellers")
    public ResponseEntity<List<AdminUserSummaryVO>> listSellers() {
        return ResponseEntity.ok(
                adminMemberService.listUsersByRole(UserRole.SELLER)
        );
    }

    /**
     * 查詢某位買家的所有訂單
     * GET /api/admin/members/{userId}/orders/buyer
     */
    @GetMapping("/{userId}/orders/buyer")
    public ResponseEntity<List<OrderResponseVO>> listOrdersByBuyer(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                adminMemberService.listOrdersByBuyer(userId)
        );
    }

    /**
     * 查詢某位賣家的所有訂單
     * GET /api/admin/members/{userId}/orders/seller
     */
    @GetMapping("/{userId}/orders/seller")
    public ResponseEntity<List<OrderResponseVO>> listOrdersBySeller(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                adminMemberService.listOrdersBySeller(userId)
        );
    }
}