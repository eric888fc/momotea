package demo.bigwork.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.bigwork.dao.CartDAO;
import demo.bigwork.model.po.CartPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.util.ECPayUtil;

@RestController
public class ECPayController {

    @Value("${ecpay.merchantId}") private String merchantId;
    @Value("${ecpay.hashKey}") private String hashKey;
    @Value("${ecpay.hashIV}") private String hashIV;
    @Value("${ecpay.serviceUrl}") private String serviceUrl;
    @Value("${ecpay.client.back.url}") private String clientBackUrl; 
    @Value("${ecpay.return.url}") private String returnUrl; 

    private final AuthHelperService authHelperService;
    private final CartDAO cartDAO;

    @Autowired
    public ECPayController(AuthHelperService authHelperService, CartDAO cartDAO) {
        this.authHelperService = authHelperService;
        this.cartDAO = cartDAO;
    }

    @GetMapping("/createOrder")
    public String createOrder() throws Exception {
        // 1. 取得買家 (需登入)
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();

        // 2. 取得購物車並計算金額
        CartPO cart = cartDAO.findByUser_UserId(buyer.getUserId())
                .orElseThrow(() -> new Exception("您的購物車是空的"));
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new Exception("購物車內無商品，無法結帳");
        }

        BigDecimal totalAmount = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String itemNames = cart.getItems().stream()
            .map(item -> item.getProduct().getName() + " x " + item.getQuantity())
            .collect(Collectors.joining("#"));
        
        if (itemNames.length() > 200) itemNames = "E-Shop 線上購物結帳";

        // --- 3. 準備參數 ---
        String tradeNo = "TOSN" + System.currentTimeMillis(); 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String tradeDate = sdf.format(new Date());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", tradeDate);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(totalAmount.intValue())); 
        params.put("TradeDesc", "E-Shop Order");
        params.put("ChoosePayment", "ALL");
        params.put("ItemName", itemNames); 
        
        // (關鍵) 指定 SHA-256
        params.put("EncryptType", "1");

        // (關鍵) 放入 UserID 以便回傳時識別
        params.put("CustomField1", String.valueOf(buyer.getUserId()));

        params.put("ClientBackURL", clientBackUrl); 
        params.put("ReturnURL", returnUrl); 

        // 4. 產生檢查碼 (使用 Util 的 genCheckMacValue，會用 '+' 編碼)
        String checkMacValue = ECPayUtil.genCheckMacValue(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        // 5. 產生 HTML 表單
        StringBuilder form = new StringBuilder();
        form.append("<form id='ecpay' method='post' action='").append(serviceUrl).append("'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey())
                .append("' value='").append(entry.getValue()).append("'/>");
        }
        form.append("</form>");
        form.append("<script>document.getElementById('ecpay').submit();</script>");

        return form.toString();
    }
}