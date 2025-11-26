package demo.bigwork.service.Impl;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import demo.bigwork.dao.OrderDAO;
import demo.bigwork.dao.UserDAO;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.OrderPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.AdminUserSummaryVO;
import demo.bigwork.model.vo.OrderResponseVO;
import demo.bigwork.service.AdminMemberService;

/**
 * 管理員的「會員管理」服務實作
 */
@Service
@Transactional(readOnly = true)
public class AdminMemberServiceImpl implements AdminMemberService {

    private static final Logger logger = LoggerFactory.getLogger(AdminMemberServiceImpl.class);

    private final UserDAO userDAO;
    private final OrderDAO orderDAO;

    public AdminMemberServiceImpl(UserDAO userDAO, OrderDAO orderDAO) {
        this.userDAO = userDAO;
        this.orderDAO = orderDAO;
    }

    @Override
    public List<AdminUserSummaryVO> listUsersByRole(UserRole role) {
        List<UserPO> users = userDAO.findByRole(role);

        // 依建立時間新到舊排序（如果 createdAt 允許為 null，可以加 null 判斷）
        users.sort((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()));

        return users.stream()
                .map(AdminUserSummaryVO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseVO> listOrdersByBuyer(Long buyerId) {
        List<OrderPO> orders = orderDAO.findByBuyer_UserId(buyerId);

        // 初始化 LAZY 的 items，避免離開 transaction 後爆 LazyInitializationException
        for (OrderPO order : orders) {
            Hibernate.initialize(order.getItems());
        }

        logger.info("管理員查詢買家 {} 的訂單，共 {} 筆", buyerId, orders.size());
        return orders.stream()
                .map(OrderResponseVO::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseVO> listOrdersBySeller(Long sellerId) {
        List<OrderPO> orders = orderDAO.findBySeller_UserId(sellerId);

        for (OrderPO order : orders) {
            Hibernate.initialize(order.getItems());
        }

        logger.info("管理員查詢賣家 {} 的訂單，共 {} 筆", sellerId, orders.size());
        return orders.stream()
                .map(OrderResponseVO::new)
                .collect(Collectors.toList());
    }
}