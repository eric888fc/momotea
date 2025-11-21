package demo.bigwork.model.vo;

import demo.bigwork.model.po.WalletPO;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WalletResponseVO {
    
    private Long walletId;
    private Long userId;
    private BigDecimal balance; // (新餘額)

    // (轉換器建構子)
    public WalletResponseVO(WalletPO wallet) {
        this.walletId = wallet.getWalletId();
        this.userId = wallet.getUser().getUserId();
        this.balance = wallet.getBalance();
    }
}