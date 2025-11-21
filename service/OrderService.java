package demo.bigwork.service;

import demo.bigwork.model.vo.OrderResponseVO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface OrderService {

	/**
	 * (核心業務) 結帳！ 從「我」的購物車建立一筆或多筆訂單
	 *
	 * @return 此次結帳所產生的「所有」新訂單
	 * @throws AccessDeniedException (如果不是 BUYER)
	 * @throws Exception             (e.g., 庫存不足, 餘額不足, 購物車為空)
	 */
	List<OrderResponseVO> checkoutFromMyCart() throws AccessDeniedException, Exception;

	/**
	 * (查詢) 取得「我 (買家)」的所有訂單
	 *
	 * @return
	 * @throws AccessDeniedException (如果不是 BUYER)
	 */
	List<OrderResponseVO> getMyOrdersAsBuyer() throws AccessDeniedException;

	/**
	 * (查詢) 取得「我 (買家)」的「單筆」訂單詳情 (Service 內部必須驗證所有權)
	 *
	 * @param orderId
	 * @return
	 * @throws AccessDeniedException   (如果不是 BUYER 或「不擁有」此訂單)
	 * @throws EntityNotFoundException (如果訂單不存在)
	 */
	OrderResponseVO getMyOrderDetails(Long orderId) throws AccessDeniedException, EntityNotFoundException;

	List<OrderResponseVO> getMyOrdersAsSeller() throws AccessDeniedException;

}