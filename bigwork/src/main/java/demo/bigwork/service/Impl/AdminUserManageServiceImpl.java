package demo.bigwork.service.Impl;

import demo.bigwork.dao.OrderDAO;
import demo.bigwork.dao.ProductDAO;
import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.OrderPO;
import demo.bigwork.model.po.ProductPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.AdminProductSummaryVO;
import demo.bigwork.model.vo.AdminUserSummaryVO;
import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.AdminUserManageService;
import org.hibernate.Hibernate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminUserManageServiceImpl implements AdminUserManageService {

    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public AdminUserManageServiceImpl(UserDAO userDAO,
                                      OrderDAO orderDAO,
                                      ProductDAO productDAO) {
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
    }

    @Override
    public List<AdminUserSummaryVO> listUsersByRole(UserRole role) {
        List<UserPO> users = userDAO.findByRole(role);
        // 依建立時間由新到舊排序（避免 null NPE）
        users.sort(Comparator.comparing(UserPO::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return users.stream()
                .map(AdminUserSummaryVO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseVO> listOrdersByBuyer(Long buyerId) {
        List<OrderPO> list = orderDAO.findByBuyer_UserId(buyerId);
        list.forEach(o -> Hibernate.initialize(o.getItems()));
        return list.stream()
                .map(OrderResponseVO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseVO> listOrdersBySeller(Long sellerId) {
        List<OrderPO> list = orderDAO.findBySeller_UserId(sellerId);
        list.forEach(o -> Hibernate.initialize(o.getItems()));
        return list.stream()
                .map(OrderResponseVO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminProductSummaryVO> listProductsBySeller(Long sellerId) {
        List<ProductPO> list = productDAO.findBySeller_UserId(sellerId);
        return list.stream()
                .map(AdminProductSummaryVO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUserByAdmin(Long userId) {
        try {
            userDAO.deleteById(userId);
        } catch (DataIntegrityViolationException ex) {
            // 有外鍵約束（還有訂單或商品），丟一個比較好懂的訊息給前端
            throw new IllegalStateException("該會員仍有相關訂單或商品，無法直接刪除", ex);
        }
    }
}