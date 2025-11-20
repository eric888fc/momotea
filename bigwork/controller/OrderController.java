package demo.bigwork.controller;

import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (新) 訂單控制器
 * (安全) 此 Controller 已被 SecurityConfig
 * 以 /api/orders/** * 設定為 .hasRole("BUYER")
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * API 端點：(核心) 結帳
     * POST http://localhost:8080/api/orders/checkout
     * (注意：此 API 不需要 Request Body，它會自動從 Token 抓取買家)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutFromMyCart() {
        try {
            // (Service 會回傳「此次」建立的所有新訂單)
            List<OrderResponseVO> newOrders = orderService.checkoutFromMyCart();
            
            // (成功) 回傳 200 OK 和新訂單列表
            return ResponseEntity.ok(newOrders);

        } catch (AccessDeniedException e) {
            // (如果 Token 是 SELLER)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (Exception e) {
            // (關鍵) 
            // 這會捕捉到所有「業務邏輯」錯誤
            // e.g., "您的購物車是空的"
            // e.g., "商品「XXX」庫存不足"
            // e.g., "錢包餘額不足"
            logger.warn("結帳失敗：{}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        }
    }

    /**
     * API 端點：查詢「我 (買家)」的所有訂單
     * GET http://localhost:8080/api/orders/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyOrdersAsBuyer() {
        try {
            List<OrderResponseVO> myOrders = orderService.getMyOrdersAsBuyer();
            return ResponseEntity.ok(myOrders);

        } catch (AccessDeniedException e) {
            // (如果 Token 是 SELLER)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        }
    }

    /**
     * API 端點：查詢「我 (買家)」的「單筆」訂單詳情
     * GET http://localhost:8080/api/orders/me/{orderId}
     */
    @GetMapping("/me/{orderId}")
    public ResponseEntity<?> getMyOrderDetails(@PathVariable Long orderId) {
        try {
            OrderResponseVO orderDetails = orderService.getMyOrderDetails(orderId);
            return ResponseEntity.ok(orderDetails);

        } catch (AccessDeniedException e) {
            // (e.g., SELLER Token 或 試圖查詢「不屬於」你的訂單)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (EntityNotFoundException e) {
            // (e.g., 訂單 ID 找不到)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }
    }
}