package demo.bigwork.controller;

import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * (新) 賣家訂單控制器 (賣家專用)
 * (安全) 此 Controller 的路徑已被 SecurityConfig
 * 以 /api/seller/orders/** 設定為 .hasRole("SELLER")
 */
@RestController
@RequestMapping("/api/seller/orders") // (注意我們的路徑)
public class SellerOrderController {

    private final OrderService orderService; // (我們重用 OrderService)

    public SellerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * API 端點：查詢「我 (賣家)」收到的所有訂單
     * GET http://localhost:8080/api/seller/orders/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyOrdersAsSeller() {
        try {
            List<OrderResponseVO> myOrders = orderService.getMyOrdersAsSeller();
            return ResponseEntity.ok(myOrders);

        } catch (AccessDeniedException e) {
            // (這不應發生，因為 SecurityConfig 已攔截)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        }
    }
}