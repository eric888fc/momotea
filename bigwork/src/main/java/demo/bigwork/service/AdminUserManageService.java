package demo.bigwork.service;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.vo.AdminProductSummaryVO;
import demo.bigwork.model.vo.AdminUserSummaryVO;
import demo.bigwork.model.vo.OrderResponseVO;

import java.util.List;

public interface AdminUserManageService {

    List<AdminUserSummaryVO> listUsersByRole(UserRole role);

    List<OrderResponseVO> listOrdersByBuyer(Long buyerId);

    List<OrderResponseVO> listOrdersBySeller(Long sellerId);

    List<AdminProductSummaryVO> listProductsBySeller(Long sellerId);

    void deleteUserByAdmin(Long userId);
}