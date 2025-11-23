package demo.bigwork.controller;

import java.text.SimpleDateFormat; // (新增) 用於格式化時間
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/createOrder")
    public String createOrder() throws Exception {
        // (修改 1) 動態產生不重複的訂單編號 (使用當前時間毫秒數)
        // 例如：TOSN1732180000123
        String tradeNo = "TOSN" + System.currentTimeMillis(); 
        
        // (修改 2) 動態產生當前交易時間 (格式必須為 yyyy/MM/dd HH:mm:ss)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String tradeDate = sdf.format(new Date());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantId);
        
        // (修改 3) 將變數填入，取代原本寫死的字串
        params.put("MerchantTradeNo", tradeNo); 
        params.put("MerchantTradeDate", tradeDate);
        
        params.put("PaymentType", "aio");
        params.put("TotalAmount", "199");
        params.put("TradeDesc", "測試交易");
        params.put("ChoosePayment", "ALL");
        params.put("ItemName", "商品一#商品二");
        params.put("ReturnURL", "http://localhost:8080/notify");

        // 計算 CheckMacValue
        String checkMacValue = ECPayUtil.genCheckMacValue(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        // 產生 HTML form
        StringBuilder form = new StringBuilder();
        form.append("<form id='ecpay' method='post' action='").append(serviceUrl).append("'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey())
                .append("' value='").append(entry.getValue()).append("'/>");
        }
        form.append("<input type='submit' value='送出付款'/>");
        form.append("</form>");
        form.append("<script>document.getElementById('ecpay').submit();</script>");

        return form.toString();
    }

}