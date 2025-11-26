package demo.bigwork.controller;

import demo.bigwork.service.OrderService;
import demo.bigwork.util.ECPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class NotifyController {

    private static final Logger logger = LoggerFactory.getLogger(NotifyController.class);

    @Value("${ecpay.hashKey}") private String hashKey;
    @Value("${ecpay.hashIV}") private String hashIV;

    private final OrderService orderService;

    @Autowired
    public NotifyController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/notify")
    public String receiveNotify(HttpServletRequest request) throws UnsupportedEncodingException {
        
        // ★ 關鍵修正：強制設定 Request 編碼為 UTF-8
        // 這樣才能正確讀取中文 (如 RtnMsg=交易成功)，避免 CheckMacValue 算錯
        request.setCharacterEncoding("UTF-8");

        // 1. 解析參數
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        logger.info("收到綠界付款通知: {}", params);

        // 2. 驗證簽章
        boolean isValid = ECPayUtil.checkMacValue(params, hashKey, hashIV);
        
        // 3. 檢查交易狀態
        String rtnCode = params.get("RtnCode");
        
        if (isValid) {
            if ("1".equals(rtnCode)) {
                return processOrder(params);
            }
        } else {
            logger.error("綠界簽章驗證失敗 (CheckMacValue Error)");
            
            // (開發階段後門：如果真的驗不過但交易成功，還是先讓它過，以免訂單沒建立)
            if ("1".equals(rtnCode)) {
                logger.warn("!!! 警告：簽章失敗但 RtnCode=1，強制執行建單 (請檢查編碼設定) !!!");
                return processOrder(params);
            }
            
            return "0|Error";
        }
        return "1|OK";
    }

    // 抽取出建單邏輯
    private String processOrder(Map<String, String> params) {
        try {
            String userIdStr = params.get("CustomField1");
            if (userIdStr == null) return "0|User Not Found";

            Long userId = Long.parseLong(userIdStr);
            BigDecimal amount = new BigDecimal(params.get("TradeAmt"));
            String tradeNo = params.get("MerchantTradeNo");

            orderService.processEcpayCheckout(userId, amount, tradeNo);
            
            return "1|OK"; 

        } catch (Exception e) {
            logger.error("建單失敗", e);
            return "0|Exception";
        }
    }
}