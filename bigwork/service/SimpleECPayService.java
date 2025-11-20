package demo.bigwork.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class SimpleECPayService {

    @Value("${ecpay.payment.url}")
    private String ecpayUrl;
    @Value("${ecpay.merchant-id}")
    private String merchantId;
    @Value("${ecpay.hash-key}")
    private String hashKey;
    @Value("${ecpay.hash-iv}")
    private String hashIv;
    @Value("${ecpay.return-url}")
    private String returnUrl;
    @Value("${ecpay.client-back-url}")
    private String clientBackUrl;

    public String createPaymentForm(Integer amount, Long userId) {
        // 1. 準備參數 (必須按照字母順序排列，所以用 TreeMap)
        Map<String, String> params = new TreeMap<>();
        
        String tradeNo = "BW" + System.currentTimeMillis() + (int)(Math.random()*1000);
        String tradeDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));

        params.put("MerchantID", merchantId);
        params.put("MerchantTradeNo", tradeNo);
        params.put("MerchantTradeDate", tradeDate);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(amount));
        params.put("TradeDesc", "BigWork TopUp");
        params.put("ItemName", "電子錢包儲值金 " + amount + " 元");
        params.put("ReturnURL", returnUrl);
        params.put("ClientBackURL", clientBackUrl);
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");
        params.put("CustomField1", String.valueOf(userId));

        // 2. 產生檢查碼 (CheckMacValue)
        String checkMacValue = generateCheckMacValue(params);
        params.put("CheckMacValue", checkMacValue);

        // 3. 產生 HTML 表單
        StringBuilder html = new StringBuilder();
        html.append("<form id='ecpay-form' action='").append(ecpayUrl).append("' method='POST'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='").append(entry.getKey())
                .append("' value='").append(entry.getValue()).append("'>");
        }
        // 自動送出
        html.append("<script>document.getElementById('ecpay-form').submit();</script>");
        html.append("</form>");

        return html.toString();
    }
    
    // (核心) 綠界簽章演算法
    public String generateCheckMacValue(Map<String, String> params) {
        // 1. 排序 (TreeMap 已做) 並串接
        String queryString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        
        // 2. 前後加上 HashKey 和 HashIV
        String raw = "HashKey=" + hashKey + "&" + queryString + "&HashIV=" + hashIv;
        
        // 3. URL Encode (這是最容易錯的地方，綠界有特殊的 Encode 規則)
        String encoded = urlEncodeDotNet(raw).toLowerCase();
        
        // 4. SHA256 加密並轉大寫
        return DigestUtils.sha256Hex(encoded).toUpperCase();
    }
    
    // 模擬 .NET 的 UrlEncode (綠界要求)
    private String urlEncodeDotNet(String s) {
        String encoded = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encoded.replace("%2d", "-")
                      .replace("%5f", "_")
                      .replace("%2e", ".")
                      .replace("%21", "!")
                      .replace("%2a", "*")
                      .replace("%28", "(")
                      .replace("%29", ")");
    }
    
    // 驗證回傳的 CheckMacValue
    public boolean validateCheckMacValue(Map<String, String> params) {
        if (!params.containsKey("CheckMacValue")) return false;
        
        String receivedCheckMacValue = params.get("CheckMacValue");
        
        // 複製一份參數來計算，記得移除 CheckMacValue 本身
        Map<String, String> calcParams = new TreeMap<>(params);
        calcParams.remove("CheckMacValue");
        
        String calculated = generateCheckMacValue(calcParams);
        
        return calculated.equals(receivedCheckMacValue);
    }
}