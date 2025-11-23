package demo.bigwork.controller;

import demo.bigwork.util.ECPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class NotifyController {

	@Value("${ecpay.hashKey}")
    private String hashKey;

    @Value("${ecpay.hashIV}")
    private String hashIV;

    @PostMapping("/notify")
    public String receiveNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        boolean isValid = ECPayUtil.checkMacValue(params, hashKey, hashIV);

        if (isValid) {
            String merchantTradeNo = params.get("MerchantTradeNo");
            String rtnCode = params.get("RtnCode"); // 1 表示付款成功
            System.out.println("訂單編號: " + merchantTradeNo + " 狀態: " + rtnCode);

            // TODO: 更新訂單狀態 (已付款)
            return "1|OK";
        } else {
            return "0|Error";
        }
    }
}
