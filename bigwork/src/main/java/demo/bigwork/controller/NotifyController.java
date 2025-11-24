package demo.bigwork.controller;

import demo.bigwork.service.OrderService; // (1) 匯入 OrderService
import demo.bigwork.util.ECPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class NotifyController {

    private static final Logger logger = LoggerFactory.getLogger(NotifyController.class);

    @Value("${ecpay.hashKey}")
    private String hashKey;

    @Value("${ecpay.hashIV}")
    private String hashIV;

    // (2) 注入 OrderService，用來處理結帳邏輯
    private final OrderService orderService;

    @Autowired
    public NotifyController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/notify")
    public String receiveNotify(HttpServletRequest request) {
        // 1. 將 HttpServletRequest 的參數轉為 Map
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        logger.info("收到綠界付款通知: {}", params);

        // 2. 驗證 CheckMacValue (確保資料沒被竄改)
        boolean isValid = ECPayUtil.checkMacValue(params, hashKey, hashIV);
        logger.info("付款isValid:{}",isValid);
        if (isValid) {
            // 3. 檢查 RtnCode (1 代表付款成功)
            String rtnCode = params.get("RtnCode");
            
            if ("1".equals(rtnCode)) {
                try {
                    // 4. 取出關鍵資料
                    // (注意：createOrder 時必須把 userId 塞入 CustomField1)
                    String customField1 = params.get("CustomField1");
                    
                    if (customField1 == null || customField1.isEmpty()) {
                        logger.error("嚴重錯誤：綠界回傳資料中找不到 UserID (CustomField1 為空)");
                        return "0|User Not Found";
                    }

                    Long userId = Long.parseLong(customField1);
                    BigDecimal amount = new BigDecimal(params.get("TradeAmt")); // 實際付款金額
                    String merchantTradeNo = params.get("MerchantTradeNo");     // 綠界訂單編號

                    System.out.println("訂單編號: " + merchantTradeNo + " 金額: " + amount + " 用戶ID: " + userId);

                    // 5. (核心) 呼叫 OrderService 執行「直接結帳」
                    // 這會建立訂單、扣庫存、並將錢轉入賣家錢包
                    orderService.processEcpayCheckout(userId, amount, merchantTradeNo);

                    return "1|OK";

                } catch (Exception e) {
                    logger.error("綠界結帳處理失敗", e);
                    return "0|Exception"; // 回傳錯誤給綠界 (綠界可能會重試)
                }
            } else {
                logger.warn("綠界付款失敗 (RtnCode={})", rtnCode);
                return "1|OK"; // 雖然付款失敗，但我們已經收到通知了，回傳 OK 讓綠界不要再寄了
            }
        } else {
            logger.error("綠界簽章驗證失敗 (CheckMacValue Invalid)，可能為偽造請求！");
            return "0|Error";
        }
    }
}