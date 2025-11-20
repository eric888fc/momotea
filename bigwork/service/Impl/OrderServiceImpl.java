package demo.bigwork.service.Impl;

import demo.bigwork.dao.*; // 匯入所有 DAO
import demo.bigwork.model.enums.OrderStatus;
import demo.bigwork.model.enums.TransactionType;
import demo.bigwork.model.po.*; // 匯入所有 PO
import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    // (我們需要「大量」的 DAO 和 Service)
    private final AuthHelperService authHelperService;
    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final WalletDAO walletDAO;
    private final WalletTransactionDAO walletTransactionDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO; // (需要用來找賣家)

    @Autowired
    public OrderServiceImpl(AuthHelperService authHelperService, CartDAO cartDAO, 
                            CartItemDAO cartItemDAO, ProductDAO productDAO, 
                            WalletDAO walletDAO, WalletTransactionDAO walletTransactionDAO, 
                            OrderDAO orderDAO, UserDAO userDAO) {
        this.authHelperService = authHelperService;
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
        this.walletDAO = walletDAO;
        this.walletTransactionDAO = walletTransactionDAO;
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
    }


    /**
     * (核心！) 結帳
     */
    @Override
    @Transactional // (S極度重要！)
    public List<OrderResponseVO> checkoutFromMyCart() throws AccessDeniedException, Exception {
        
        // 1. (安全) 取得買家
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("買家 {} 正在結帳...", buyer.getEmail());

        // 2. (讀取) 取得購物車
        CartPO cart = cartDAO.findByUser_UserId(buyer.getUserId())
                .orElseThrow(() -> new Exception("找不到購物車"));
        
        // (關鍵) 手動初始化「所有」項目 (POJO, DAO 都已修正)
        Hibernate.initialize(cart.getItems());
        Set<CartItemPO> itemsInCart = cart.getItems();

        // 3. (驗證 1) 購物車是否為空？
        if (itemsInCart.isEmpty()) {
            logger.warn("結帳失敗：{} 的購物車是空的", buyer.getEmail());
            throw new Exception("您的購物車是空的");
        }
        logger.debug("購物車內有 {} 項商品", itemsInCart.size());

        // 4. (驗證 2) 檢查「所有」庫存
        // (我們必須在「扣款」前先檢查)
        for (CartItemPO item : itemsInCart) {
            ProductPO product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                logger.warn("結帳失敗：商品 {} 庫存不足 (庫存: {}, 欲購: {})", 
                    product.getName(), product.getStock(), item.getQuantity());
                throw new Exception("商品「" + product.getName() + "」庫存不足");
            }
        }
        logger.debug("所有商品庫存檢查通過");

        // 5. (驗證 3) 檢查「錢包餘額」
        // 5a. 計算「總價」
        BigDecimal totalCartPrice = itemsInCart.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 5b. 取得買家錢包
        WalletPO buyerWallet = walletDAO.findByUser_UserId(buyer.getUserId())
                .orElseThrow(() -> new Exception("找不到買家錢包"));
        
        // 5c. 比較餘額
        if (buyerWallet.getBalance().compareTo(totalCartPrice) < 0) {
            logger.warn("結帳失敗：買家 {} 餘額不足 (餘額: {}, 總價: {})", 
                buyer.getEmail(), buyerWallet.getBalance(), totalCartPrice);
            throw new Exception("錢包餘額不足");
        }
        logger.debug("錢包餘額檢查通過 (餘額: {}, 總價: {})", buyerWallet.getBalance(), totalCartPrice);

        // --- (驗證全部通過！開始執行「不可逆」操作) ---

        // 6. (執行 - 金流 - 買家)
        // 6a. 扣除買家餘額
        buyerWallet.setBalance(buyerWallet.getBalance().subtract(totalCartPrice));
        walletDAO.save(buyerWallet);
        
        // 6b. 建立買家「交易紀錄」 (PURCHASE)
        WalletTransactionPO buyerTx = new WalletTransactionPO(
                buyerWallet, TransactionType.PURCHASE, totalCartPrice.negate());
        walletTransactionDAO.save(buyerTx);
        logger.info("買家 {} 已扣款 {}", buyer.getEmail(), totalCartPrice);

        // 7. (執行 - 拆分訂單)
        // (關鍵) 
        // 我們必須將購物車「依照賣家」拆分成「多張訂單」
        Map<Long, List<CartItemPO>> itemsBySeller = itemsInCart.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getSeller().getUserId()));
        
        logger.info("購物車共包含 {} 位賣家的商品，將建立 {} 張訂單", itemsBySeller.size());

        List<OrderPO> createdOrders = new ArrayList<>(); // (用來存放新訂單)

        // 8. (執行 - 訂單 & 金流 - 賣家)
        // 迴圈處理「每一張」新訂單
        for (Map.Entry<Long, List<CartItemPO>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<CartItemPO> sellerItems = entry.getValue();

            // 8a. 取得賣家物件
            UserPO seller = userDAO.findById(sellerId)
                    .orElseThrow(() -> new Exception("找不到賣家, ID: " + sellerId));
            
            // 8b. 計算「這張訂單」的總價
            BigDecimal sellerOrderPrice = sellerItems.stream()
                    .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 8c. (金流) 幫賣家加錢
            WalletPO sellerWallet = walletDAO.findByUser_UserId(sellerId)
                    .orElseThrow(() -> new Exception("找不到賣家錢包, ID: " + sellerId));
            sellerWallet.setBalance(sellerWallet.getBalance().add(sellerOrderPrice));
            walletDAO.save(sellerWallet);
            
            // 8d. (金流) 建立賣家「交易紀錄」 (PAYMENT_RECEIVED)
            WalletTransactionPO sellerTx = new WalletTransactionPO(
                    sellerWallet, TransactionType.PAYMENT_RECEIVED, sellerOrderPrice);
            walletTransactionDAO.save(sellerTx);
            logger.info("已付款 {} 元給賣家 {}", sellerOrderPrice, seller.getEmail());

            // 8e. (訂單) 建立「訂單主檔 (OrderPO)」
            OrderPO newOrder = new OrderPO();
            newOrder.setBuyer(buyer);
            newOrder.setSeller(seller);
            newOrder.setTotalPrice(sellerOrderPrice);
            newOrder.setStatus(OrderStatus.COMPLETED); // (模擬)

            // 8f. (訂單) 建立「訂單明細 (OrderItemPO)」
            for (CartItemPO cartItem : sellerItems) {
                ProductPO product = cartItem.getProduct();
                
                // 建立「快照」
                OrderItemPO newOrderItem = new OrderItemPO();
                newOrderItem.setProduct(product);
                newOrderItem.setQuantity(cartItem.getQuantity());
                newOrderItem.setPricePerUnit(product.getPrice()); // (S儲存「當下」的價格)
                
                // (關鍵) 使用輔助方法，自動設定「雙向關聯」
                newOrder.addOrderItem(newOrderItem);
                
                // 8g. (庫存) 扣除「商品庫存」
                product.setStock(product.getStock() - cartItem.getQuantity());
                productDAO.save(product);
                logger.debug("商品 {} 庫存已扣除，剩餘: {}", product.getName(), product.getStock());
            }

            // 8h. (儲存) 儲存「訂單」
            // (因為 cascade = ALL, 這會「同時」儲存所有 OrderItemPO)
            OrderPO savedOrder = orderDAO.save(newOrder);
            createdOrders.add(savedOrder);
            logger.info("訂單 {} (for 賣家 {}) 已建立", savedOrder.getOrderId(), seller.getEmail());
        }

        // 9. (執行 - 清理)
        // (所有訂單都成功了) 清空購物車
        logger.info("結帳成功，正在清空買家 {} 的購物車...", buyer.getEmail());
        cartItemDAO.deleteAllByCart_CartId(cart.getCartId());

        // 10. (回傳)
        // (S極度關鍵) 我們必須回傳「已儲存」的 PO
        // 並且「手動初始化」它們的 LAZY 集合
        // 才能安全地轉換為 VO
        
        List<OrderResponseVO> responseVOs = new ArrayList<>();
        for (OrderPO order : createdOrders) {
            Hibernate.initialize(order.getItems()); // (手動初始化)
            responseVOs.add(new OrderResponseVO(order)); // (安全轉換)
        }
        
        logger.info("買家 {} 結帳完成，共成立 {} 張新訂單", buyer.getEmail(), responseVOs.size());
        return responseVOs;
    }


    /**
     * (查詢) 取得「我 (買家)」的所有訂單
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseVO> getMyOrdersAsBuyer() throws AccessDeniedException {
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("正在查詢買家 {} 的所有訂單...", buyer.getEmail());
        
        List<OrderPO> orders = orderDAO.findByBuyer_UserId(buyer.getUserId());
        
        // (S關鍵) 
        // 迴圈「手動初始化」所有訂單的 LAZY items
        for (OrderPO order : orders) {
            Hibernate.initialize(order.getItems());
            // (我們也需要賣家資訊，但 OrderResponseVO 會幫我們載入)
        }
        
        // (安全轉換)
        return orders.stream()
                .map(OrderResponseVO::new)
                .collect(Collectors.toList());
    }

    /**
     * (查詢) 取得「我 (買家)」的「單筆」訂單詳情
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseVO getMyOrderDetails(Long orderId) throws AccessDeniedException, EntityNotFoundException {
        UserPO buyer = authHelperService.getCurrentAuthenticatedBuyer();
        logger.info("買家 {} 正在查詢訂單詳情 (ID: {})", buyer.getEmail(), orderId);
        
        OrderPO order = orderDAO.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("找不到訂單, ID: " + orderId));
        
        // (S關鍵 - 安全) 
        // 驗證「所有權」
        if (!order.getBuyer().getUserId().equals(buyer.getUserId())) {
            logger.warn("權限不足：買家 {} 試圖查詢「不屬於」他的訂單 (ID: {})", 
                         buyer.getEmail(), orderId);
            throw new AccessDeniedException("您沒有權限查詢此訂單");
        }
        
        // (S關鍵) 
        // 手動初始化 LAZY items
        Hibernate.initialize(order.getItems());
        
        // (安全轉換)
        return new OrderResponseVO(order);
    }
    
    /**
     * (新) 實作：賣家查詢「所有」S收到的訂單
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseVO> getMyOrdersAsSeller() 
            throws AccessDeniedException {
        
        // 1. (安全) 驗證「角色」
        UserPO seller = authHelperService.getCurrentAuthenticatedSeller();
        logger.info("賣家 {} 正在查詢S收到的所有訂單", seller.getEmail());

        // 2. (查詢) 呼叫 DAO (這個方法我們之前就建好了)
        List<OrderPO> orders = orderDAO.findBySeller_UserId(seller.getUserId());
        
        // 3. (S關鍵 - 安全) 
        // 迴圈「手動初始化」所有訂單的 LAZY items 集合
        // (VO 建構子中的 .getBuyer() 和 .getProduct() 會在交易中安全地懶載入)
        for (OrderPO order : orders) {
            Hibernate.initialize(order.getItems());
        }
        
        // 4. (安全轉換)
        logger.info("賣家 {} 共查詢到 {} 筆訂單", seller.getEmail(), orders.size());
        return orders.stream()
                .map(OrderResponseVO::new) // (在交易內安全轉換)
                .collect(Collectors.toList());
    }
}