package demo.bigwork.controller;

import demo.bigwork.model.po.WalletPO;
import demo.bigwork.model.vo.TopUpRequestVO;
import demo.bigwork.model.vo.WalletResponseVO;
import demo.bigwork.model.vo.WithdrawalRequestVO;
import demo.bigwork.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

/**
 * (新) 錢包控制器
 * (安全) 此 Controller 已被 SecurityConfig 
 * 以 /api/wallet/** * 設定為 .authenticated() (必須登入)
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {
    
    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * API 端點：查詢「我的」錢包餘額
     * GET http://localhost:8080/api/wallet
     */
    @GetMapping
    public ResponseEntity<?> getMyWallet() {
        try {
            WalletPO wallet = walletService.getMyWallet();
            return ResponseEntity.ok(new WalletResponseVO(wallet));
            
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(401).body("未登入"); // 401
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage()); // 404
        }
    }

    /**
     * API 端點：「模擬」儲值
     * POST http://localhost:8080/api/wallet/topup
     */
    @PostMapping("/topup")
    public ResponseEntity<?> topUpMyWallet(@Valid @RequestBody TopUpRequestVO requestVO) {
        try {
            WalletPO updatedWallet = walletService.topUpMyWallet(requestVO.getAmount());
            return ResponseEntity.ok(new WalletResponseVO(updatedWallet));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(401).body("未登入"); // 401
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage()); // 404
        } catch (Exception e) {
            logger.error("儲值失敗", e);
            return ResponseEntity.badRequest().body("儲值失敗：" + e.getMessage()); // 400
        }
    }
    
    /**
     * (新) API 端點：「模擬」提款
     * * 這個 API 路徑 /api/wallet/** * * 只受到 .authenticated() 保護 (買家也能呼叫)
     * * 但是！Service 層的 .getCurrentAuthenticatedSeller()
     * * 會替我們「拒絕」買家，並拋出 AccessDeniedException
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFromMyWallet(
            @Valid @RequestBody WithdrawalRequestVO requestVO) {
        
        try {
            WalletPO updatedWallet = walletService.withdrawFromMyWallet(requestVO.getAmount());
            return ResponseEntity.ok(new WalletResponseVO(updatedWallet));

        } catch (AccessDeniedException e) {
            // (安全) 如果是「買家」Token 呼叫
            return ResponseEntity.status(403).body("只有賣家 (SELLER) 才能提款"); // 403
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage()); // 404
            
        } catch (Exception e) {
            // (業務) 
            // e.g., "餘額不足" 或 "請先設定收款帳戶"
            logger.warn("提款失敗：{}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage()); // 400
        }
    }
}