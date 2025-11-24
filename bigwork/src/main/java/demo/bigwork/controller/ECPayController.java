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

    @Value("${ecpay.merchantId}")
    private String merchantId;

    @Value("${ecpay.hashKey}")
    private String hashKey;

    @Value("${ecpay.hashIV}")
    private String hashIV;

    @Value("${ecpay.serviceUrl}")
    private String serviceUrl;
    
    @Value("${ecpay.client.back.url}") 
    private String clientBackUrl; 
    
    // (請務必去 application.properties 設定這一行，填入您的 Ngrok 網址 + /notify)
    @Value("${ecpay.return.url}") 
    private String returnUrl; 

    private final AuthHelperService authHelperService;
    private final CartDAO cartDAO;

    @Autowired
    public ECPayController(AuthHelperService authHelperService, CartDAO cartDAO) {
        this.authHelperService = authHelperService;
        this.cartDAO = cartDAO;
    }

    @GetMapping("/createOrder")
    public String createOrder() throws Exception {
        // 1. 取得當前登入的買家 (從 Token)
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();

        // 2. 查詢購物車
        CartPO cart = cartDAO.findByUser_UserId(buyer.getUserId())
                .orElseThrow(() -> new Exception("您的購物車是空的"));
        
        // 3. 防呆檢查
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new Exception("購物車內無商品，無法結帳");
        }

        // 4. 計算實際總金額
        BigDecimal totalAmount = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 組合商品名稱 (綠界限制長度，這裡做簡單串接)
        // 格式：iPhone x 1#AirPods x 2
        String itemNames = cart.getItems().stream()
            .map(item -> item.getProduct().getName() + " x " + item.getQuantity())
            .collect(Collectors.joining("#"));
        
        // (若名稱太長，綠界會報錯，這裡做簡單截斷防呆)
        if (itemNames.length() > 200) {
            itemNames = "E-Shop 多樣商品合併結帳";
        }

        // --- 準備綠界參數 ---

        String tradeNo = "TOSN" + System.currentTimeMillis(); 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String tradeDate = sdf.format(new Date());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", tradeDate);
        params.put("PaymentType", "aio");
        
        // (關鍵) 使用計算出來的總金額 (轉為整數字串)
        params.put("TotalAmount", String.valueOf(totalAmount.intValue())); 
        
        params.put("TradeDesc", "E-Shop Shopping Cart");
        params.put("ChoosePayment", "ALL");
        
        // (關鍵) 使用真實商品名稱
        params.put("ItemName", itemNames); 
        
        // (關鍵) 將買家 ID 放入 CustomField1，回傳時 NotifyController 才能識別是誰
        params.put("CustomField1", String.valueOf(buyer.getUserId()));

        params.put("ClientBackURL", clientBackUrl); 
        params.put("ReturnURL", returnUrl); // 指向 Ngrok 的 /notify

        // 產生檢查碼
        String checkMacValue = ECPayUtil.genCheckMacValue(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        // 產生 HTML Form
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