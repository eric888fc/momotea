package demo.bigwork.service;

import demo.bigwork.model.po.WalletPO;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;

public interface WalletService {
    
    /**
     * (新) 取得「當前登入者」的錢包
     */
    WalletPO getMyWallet() throws EntityNotFoundException;
    
    /**
     * (新) 為「當前登入者」的錢包儲值
     */
    WalletPO topUpMyWallet(BigDecimal amount) throws EntityNotFoundException;
    //從「當前登入者」的錢包提款
    WalletPO withdrawFromMyWallet(BigDecimal amount) throws Exception;
}