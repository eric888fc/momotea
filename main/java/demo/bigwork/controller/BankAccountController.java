package demo.bigwork.controller;

import demo.bigwork.model.po.BankAccountPO;
import demo.bigwork.model.vo.BankAccountRequestVO;
import demo.bigwork.model.vo.BankAccountResponseVO;
import demo.bigwork.service.BankAccountService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * (新) 賣家收款帳戶控制器
 * (安全) 此 Controller 已被 SecurityConfig
 * 以 /api/seller/** * 設定為 .hasRole("SELLER")
 */
@RestController
@RequestMapping("/api/seller/account") // (注意我們的新路徑)
public class BankAccountController {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountController.class);
    private final BankAccountService bankAccountService;

    @Autowired
    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    /**
     * API 端點：查詢「我的」收款帳戶
     * GET http://localhost:8080/api/seller/account
     */
    @GetMapping
    public ResponseEntity<?> getMyBankAccount() {
        try {
            Optional<BankAccountPO> accountOpt = bankAccountService.getMyBankAccount();
            
            if (accountOpt.isEmpty()) {
                // (業務) 
                // 還沒設定過，回傳 404 (找不到) 是合理的
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("尚未設定收款帳戶");
            }
            
            // (成功) 轉換為 VO 並回傳
            return ResponseEntity.ok(new BankAccountResponseVO(accountOpt.get()));
            
        } catch (AccessDeniedException e) {
            // (安全) 如果是買家 Token
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        }
    }

    /**
     * API 端點：建立或更新「我的」收款帳戶
     * POST http://localhost:8080/api/seller/account
     * (教授提醒)
     * 我們用 POST，因為它同時處理「新增(Create)」和「更新(Update)」
     * 也可以用 PUT，兩者皆可
     */
    @PostMapping
    public ResponseEntity<?> createOrUpdateMyBankAccount(
            @Valid @RequestBody BankAccountRequestVO requestVO) {
        
        try {
            BankAccountPO savedAccount = bankAccountService.createOrUpdateMyBankAccount(requestVO);
            // (成功) 回傳 VO
            return ResponseEntity.ok(new BankAccountResponseVO(savedAccount));

        } catch (AccessDeniedException e) {
            // (安全) 如果是買家 Token
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage()); // 403
        } catch (Exception e) {
            logger.error("更新收款帳戶失敗", e);
            return ResponseEntity.badRequest().body("更新失敗：" + e.getMessage()); // 400
        }
    }
}