package demo.bigwork.controller;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.vo.AdminProductSummaryVO;
import demo.bigwork.model.vo.AdminUserSummaryVO;
import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.AdminUserManageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系統管理員後台 - 會員管理 API
 * 路徑對應 admin-dashboard.js：
 *  /api/admin/users/buyers
 *  /api/admin/users/sellers
 *  /api/admin/users/buyers/{id}/orders
 *  /api/admin/users/sellers/{id}/orders
 *  /api/admin/users/sellers/{id}/products
 *  /api/admin/users/{id} (DELETE)
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserManageService adminUserManageService;

    public AdminUserController(AdminUserManageService adminUserManageService) {
        this.adminUserManageService = adminUserManageService;
    }

    // GET /api/admin/users/buyers
    @GetMapping("/buyers")
    public List<AdminUserSummaryVO> listBuyers() {
        return adminUserManageService.listUsersByRole(UserRole.BUYER);
    }

    // GET /api/admin/users/sellers
    @GetMapping("/sellers")
    public List<AdminUserSummaryVO> listSellers() {
        return adminUserManageService.listUsersByRole(UserRole.SELLER);
    }

    // GET /api/admin/users/buyers/{id}/orders
    @GetMapping("/buyers/{userId}/orders")
    public List<OrderResponseVO> listBuyerOrders(@PathVariable Long userId) {
        return adminUserManageService.listOrdersByBuyer(userId);
    }

    // GET /api/admin/users/sellers/{id}/orders
    @GetMapping("/sellers/{userId}/orders")
    public List<OrderResponseVO> listSellerOrders(@PathVariable Long userId) {
        return adminUserManageService.listOrdersBySeller(userId);
    }

    // GET /api/admin/users/sellers/{id}/products
    @GetMapping("/sellers/{userId}/products")
    public List<AdminProductSummaryVO> listSellerProducts(@PathVariable Long userId) {
        return adminUserManageService.listProductsBySeller(userId);
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            adminUserManageService.deleteUserByAdmin(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }
}