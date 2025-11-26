// (安全檢查)
(function() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token || role !== 'BUYER') {
        alert('您沒有權限訪問此頁面，請以「買家」身分登入。');
        window.location.href = 'login.html?role=BUYER';
    }
})();

document.addEventListener("DOMContentLoaded", () => {

    const API_BASE_URL = "http://localhost:8080";
    const token = localStorage.getItem('token');

    // (DOM 元素 - 原始)
    const messageContainer = document.getElementById("message-container");
    const userEmailSpan = document.getElementById("user-email");
    const logoutBtn = document.getElementById("logout-btn");
    const cartTableContainer = document.getElementById("cart-table-container");
    const subtotalAmountSpan = document.getElementById("subtotal-amount");
    const totalAmountSpan = document.getElementById("total-amount");
    const clearCartBtn = document.getElementById("clear-cart-btn");
    const checkoutBtn = document.getElementById("checkout-btn");
    
    // (新增) 綠界按鈕
    const ecpayCheckoutBtn = document.getElementById("ecpay-checkout-btn");

    // --- (錢包相關 DOM 元素) ---
    const walletBalanceSpan = document.getElementById("wallet-balance");
    const showTopupModalBtn = document.getElementById("show-topup-modal-btn");
    const topupModalOverlay = document.getElementById("topup-modal-overlay");
    const closeTopupModalBtn = document.getElementById("close-topup-modal-btn");
    const cancelTopupBtn = document.getElementById("cancel-topup-btn");
    const confirmTopupBtn = document.getElementById("confirm-topup-btn");
    const topupAmountInput = document.getElementById("topup-amount");
    const topupForm = document.getElementById("topup-form");

    // --- (狀態變數) ---
    let currentWalletBalance = 0; // 追蹤當前餘額


    // --- (A) 導覽列 & 登出 邏輯 ---
    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        alert('您已成功登出！');
        window.location.href = 'index.html';
    });

    // --- (B) 獲取個人資料 ---
    async function fetchMyProfile() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/profile/me`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const user = await response.json();
                userEmailSpan.textContent = `你好, ${user.name}`;
            } else {
                userEmailSpan.textContent = "無法載入使用者";
            }
        } catch (error) {
            console.error('獲取個人資料 API 錯誤:', error);
        }
    }

    // --- (新增) 查詢我的錢包餘額 (GET /api/wallet) ---
    async function fetchMyWallet() {
        if (!token) {
            walletBalanceSpan.textContent = '請先登入';
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/wallet`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const walletVO = await response.json();
                currentWalletBalance = parseFloat(walletVO.balance);
                walletBalanceSpan.textContent = `$${currentWalletBalance.toFixed(2)}`;

            } else if (response.status === 404) {
                walletBalanceSpan.textContent = '$0.00 (未初始化)';
                currentWalletBalance = 0;
            } else {
                const errorData = await response.text();
                console.error('取得錢包失敗:', errorData);
                walletBalanceSpan.textContent = '載入失敗';
                currentWalletBalance = 0;
            }

        } catch (error) {
            console.error('API 錯誤 (錢包):', error);
            walletBalanceSpan.textContent = '連線錯誤';
            currentWalletBalance = 0;
        }
    }


    // --- (C) (核心) 獲取我的購物車內容 (GET /api/cart) ---
    async function fetchMyCart() {
        try {
            const response = await fetch(`${API_BASE_URL}/api/cart`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const cartVO = await response.json();
                renderCart(cartVO);

                checkBalanceForCheckout(cartVO.totalPrice || 0);

            } else {
                const errorText = await response.text();
                showMessage(`載入購物車失敗: ${errorText}`, 'error');
                cartTableContainer.innerHTML = '<p>無法載入購物車內容。</p>';
                renderCartSummary({ items: [], totalPrice: 0 });
                checkBalanceForCheckout(0);
            }
        } catch (error) {
            console.error('獲取購物車 API 錯誤:', error);
            showMessage('無法連線到伺服器', 'error');
        }
    }

    // --- (新增) 結帳餘額檢查邏輯 ---
    function checkBalanceForCheckout(totalPrice) {
        const totalPriceNum = parseFloat(totalPrice);

        // 更新錢包結帳按鈕文字
        checkoutBtn.textContent = `錢包餘額結帳 (TWD $${totalPriceNum.toFixed(2)})`;

        const cartIsEmpty = totalPriceNum === 0;
        const balanceIsInsufficient = currentWalletBalance < totalPriceNum;

        // 1. 處理錢包按鈕狀態
        if (cartIsEmpty) {
            checkoutBtn.disabled = true;
            checkoutBtn.textContent = '購物車是空的';
            // 綠界按鈕也要 disable
            if (ecpayCheckoutBtn) ecpayCheckoutBtn.disabled = true;
        } else if (balanceIsInsufficient) {
            checkoutBtn.disabled = true;
            const neededAmount = totalPriceNum - currentWalletBalance;
            checkoutBtn.textContent = `餘額不足 (缺 $${neededAmount.toFixed(2)})`;
            
            // (重要) 就算錢包餘額不足，綠界按鈕應該要是可以按的！
            if (ecpayCheckoutBtn) ecpayCheckoutBtn.disabled = false;
        } else {
            // 餘額足夠
            checkoutBtn.disabled = false;
            if (ecpayCheckoutBtn) ecpayCheckoutBtn.disabled = false;
        }
    }


    // --- (D) 繪製購物車表格 ---
    function renderCart(cartVO) {

        if (!cartVO.items || cartVO.items.length === 0) {
            cartTableContainer.innerHTML = '<p class="info-text">您的購物車是空的！</p>';
            clearCartBtn.disabled = true;
            checkoutBtn.disabled = true;
            if (ecpayCheckoutBtn) ecpayCheckoutBtn.disabled = true;
            renderCartSummary(cartVO);
            return;
        }

        clearCartBtn.disabled = false;

        let tableHtml = `
            <table class="cart-table">
                <thead>
                    <tr>
                        <th>商品名稱</th>
                        <th>單價</th>
                        <th>數量</th>
                        <th>小計</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
        `;

        cartVO.items.forEach(item => {

            tableHtml += `
                <tr data-cart-item-id="${item.cartItemId}">
                    <td class="cart-item-name">${item.productName}</td>
                    <td>TWD $${item.unitPrice.toFixed(2)}</td> 
                    <td>
                        <input type="number" 
                               class="cart-quantity-input" 
                               value="${item.quantity}" 
                               min="1" 
                               data-cart-item-id="${item.cartItemId}"
                               data-current-quantity="${item.quantity}">
                    </td>
                    <td>TWD $${item.itemTotalPrice.toFixed(2)}</td> 
                    <td>
                        <button class="delete-item-btn" data-cart-item-id="${item.cartItemId}">
                            🗑
                        </button>
                    </td>
                </tr>
            `;
        });

        tableHtml += `
                </tbody>
            </table>
        `;

        cartTableContainer.innerHTML = tableHtml;
        renderCartSummary(cartVO);

        document.querySelectorAll('.delete-item-btn').forEach(button => {
            button.addEventListener('click', handleDeleteItem);
        });
        document.querySelectorAll('.cart-quantity-input').forEach(input => {
            input.addEventListener('change', handleUpdateQuantity);
        });
    }

    // --- (E) 繪製結帳摘要 ---
    function renderCartSummary(cartVO) {
        const subtotal = cartVO.totalPrice || 0;
        const total = cartVO.totalPrice || 0;

        subtotalAmountSpan.textContent = `TWD $${subtotal.toFixed(2)}`;
        totalAmountSpan.textContent = `TWD $${total.toFixed(2)}`;
    }

    // --- (J) (核心) 處理錢包結帳 (POST /api/orders/checkout) ---
    checkoutBtn.addEventListener('click', handleCheckout);

    async function handleCheckout() {
        if (!confirm('您確定要從購物車結帳嗎？這將會從您的錢包餘額中扣款。')) {
            return;
        }

        const cartResponse = await fetch(`${API_BASE_URL}/api/cart`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const cartVO = cartResponse.ok ? await cartResponse.json() : { totalPrice: 0 };
        const totalPrice = cartVO.totalPrice || 0;

        if (currentWalletBalance < totalPrice) {
            showMessage('結帳失敗：錢包餘額不足，請先儲值。', 'error');
            checkBalanceForCheckout(totalPrice);
            return;
        }

        checkoutBtn.disabled = true;
        clearCartBtn.disabled = true;
        if (ecpayCheckoutBtn) ecpayCheckoutBtn.disabled = true;

        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/checkout`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const responseBody = await response.json();

            if (response.ok) {
                showMessage(`結帳成功！共成立 ${responseBody.length} 張訂單。您的購物車已清空。`, 'success');

                setTimeout(async () => {
                    await fetchMyWallet();
                    await fetchMyCart();
                }, 1000);

            } else {
                showMessage(responseBody.message || '結帳失敗，請檢查購物車內容或錢包餘額。', 'error');
            }

        } catch (error) {
            console.error('結帳 API 錯誤:', error);
            showMessage('無法連線到伺服器或發生未預期錯誤', 'error');
        } finally {
            checkoutBtn.disabled = false;
            clearCartBtn.disabled = false;
            if (ecpayCheckoutBtn) ecpayCheckoutBtn.disabled = false;
        }
    }

    // --- (新增) 處理綠界支付結帳 (GET /createOrder) ---
    if (ecpayCheckoutBtn) {
        ecpayCheckoutBtn.addEventListener('click', handleEcpayCheckout);
    }

    async function handleEcpayCheckout() {
        if (!confirm('確定要使用綠界 (ECPay) 進行付款嗎？將跳轉至第三方支付頁面。')) {
            return;
        }

        // 鎖定按鈕避免重複點擊
        ecpayCheckoutBtn.disabled = true;
        ecpayCheckoutBtn.textContent = "連接綠界中...";

        try {
            // 1. 呼叫後端 /createOrder (注意：這是 Controller 路徑，不是 /api 開頭)
            const response = await fetch(`${API_BASE_URL}/createOrder`, {
                method: 'GET',
                headers: {
                    // ★ 關鍵：必須帶 Token，後端才能知道是誰在買，並去資料庫抓購物車
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                // 2. 後端會回傳一段完整的 HTML Form (包含自動 submit 的 script)
                const htmlForm = await response.text();

                // 3. 將 HTML 放入一個隱藏的 div 中
                const div = document.createElement('div');
                div.style.display = 'none'; // 隱藏
                div.innerHTML = htmlForm;
                document.body.appendChild(div);

                // 4. 找到 form 並提交 (通常後端回傳的 script 會自動提交，但保險起見我們也可以手動)
                const form = div.querySelector('form');
                if (form) {
                    console.log("正在跳轉至綠界...");
                    form.submit();
                } else {
                    showMessage("綠界表單產生錯誤，請稍後再試。", "error");
                    ecpayCheckoutBtn.disabled = false;
                    ecpayCheckoutBtn.textContent = "💳 綠界支付 (信用卡/ATM)";
                }

            } else {
                const errorText = await response.text();
                console.error("綠界訂單建立失敗:", errorText);
                // 嘗試解析 JSON
                try {
                    const errJson = JSON.parse(errorText);
                    showMessage(`綠界結帳失敗: ${errJson.message || errorText}`, 'error');
                } catch (e) {
                    showMessage(`綠界結帳失敗: ${errorText}`, 'error');
                }
                
                ecpayCheckoutBtn.disabled = false;
                ecpayCheckoutBtn.textContent = "💳 綠界支付 (信用卡/ATM)";
            }

        } catch (error) {
            console.error('綠界 API 連線錯誤:', error);
            showMessage('無法連線到伺服器', 'error');
            ecpayCheckoutBtn.disabled = false;
            ecpayCheckoutBtn.textContent = "💳 綠界支付 (信用卡/ATM)";
        }
    }


    // --- (L) (新增) 處理儲值 (POST /api/wallet/topup) ---
    // --- (L) (*** 關鍵修正 2 ***) 處理儲值 Modal 邏輯 ---

    // (定義開啟和關閉的「輔助函數」)
    function openTopupModal() {
        if (topupModalOverlay) {
            // modal.css 使用 display: flex 來置中
            topupModalOverlay.style.display = 'flex';
            topupAmountInput.focus();
        }
    }

    function closeTopupModal() {
        if (topupModalOverlay) {
            topupModalOverlay.style.display = 'none';
        }
    }

    // (綁定「開啟」按鈕)
    if (showTopupModalBtn) {
        showTopupModalBtn.addEventListener('click', openTopupModal);
    }

    // (綁定「關閉」按鈕們)
    [closeTopupModalBtn, cancelTopupBtn].forEach(btn => {
        if (btn) {
            btn.addEventListener('click', closeTopupModal);
        }
    });

    // (綁定「點擊背景遮罩」來關閉)
    if (topupModalOverlay) {
        topupModalOverlay.addEventListener('click', (e) => {
            // (重要) 只有在點擊「遮罩背景」(#topup-modal-overlay) 時才關閉
            // 如果點擊的是「內容框」(.modal-content)，則不關閉
            if (e.target === topupModalOverlay) {
                closeTopupModal();
            }
        });
    }

    // (綁定「確認儲值」按鈕)
    if (topupForm) {
        topupForm.addEventListener('submit', handleTopUp);
    }

    async function handleTopUp(e) {
        e.preventDefault(); // (重要) 阻止表單預設的提交刷新頁面

        const amount = parseFloat(topupAmountInput.value);

        if (isNaN(amount) || amount <= 0) {
            showMessage('請輸入有效的儲值金額。', 'error');
            return;
        }

        confirmTopupBtn.disabled = true;
        const requestBody = { amount: amount };

        try {
            const response = await fetch(`${API_BASE_URL}/api/wallet/topup`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(requestBody)
            });

            const data = await response.json();

            if (response.ok) {
                showMessage(`成功儲值 TWD $${amount.toFixed(2)}！`, 'success');
                closeTopupModal(); // (儲值成功後關閉視窗)

                await fetchMyWallet(); // (重新抓取錢包)
                await fetchMyCart();   // (重新抓取購物車 - 為了更新結帳按鈕狀態)

            } else {
                showMessage(data.message || '儲值失敗', 'error');
            }

        } catch (error) {
            console.error('儲值 API 錯誤:', error);
            showMessage('儲值失敗：無法連線到伺服器', 'error');
        } finally {
            confirmTopupBtn.disabled = false;
        }
    }

    // --- (K) 初始化 (保持不變) ---
    async function init() {
        await fetchMyProfile();
        await fetchMyWallet();
        await fetchMyCart();
    }


    // --- (F) (核心) 處理刪除單一項目 (DELETE /api/cart/items/{cartItemId}) ---
    async function handleDeleteItem(event) {
        const cartItemId = event.currentTarget.dataset.cartItemId;

        if (!confirm('您確定要從購物車移除此商品嗎？')) {
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/cart/items/${cartItemId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.status === 204) {
                showMessage('商品已從購物車移除！', 'success');
                fetchMyCart();
            } else {
                const errorText = await response.text();
                showMessage(`移除商品失敗: ${errorText}`, 'error');
            }

        } catch (error) {
            console.error('移除商品 API 錯誤:', error);
            showMessage('無法連線到伺服器', 'error');
        }
    }

    // --- (G) (核心) 處理更新商品數量 (PUT /api/cart/items/{cartItemId}) ---
    async function handleUpdateQuantity(event) {
        const input = event.currentTarget;
        const cartItemId = input.dataset.cartItemId;
        const newQuantity = parseInt(input.value, 10);
        const currentQuantity = parseInt(input.dataset.currentQuantity, 10);

        // 1. 基本驗證
        if (newQuantity <= 0) {
            showMessage('數量必須大於 0', 'error');
            input.value = currentQuantity;
            return;
        }

        if (newQuantity === currentQuantity) {
            return;
        }

        // 2. 呼叫 API
        try {
            const response = await fetch(`${API_BASE_URL}/api/cart/items/${cartItemId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    quantity: newQuantity
                })
            });

            if (response.ok) {
                const updatedCartVO = await response.json();
                showMessage('商品數量已更新！', 'success');
                await fetchMyWallet();
                renderCart(updatedCartVO);
                checkBalanceForCheckout(updatedCartVO.totalPrice || 0);

            } else {
                const errorBody = await response.text();
                let errorMessage = '庫存不足或數量無效';

                try {
                    const errorData = JSON.parse(errorBody);
                    if (errorData && errorData.message) {
                        errorMessage = errorData.message;
                    } else {
                        errorMessage = errorBody;
                    }
                } catch (e) {
                    if (errorBody) {
                        errorMessage = errorBody;
                    }
                }

                showMessage(`更新數量失敗: ${errorMessage}`, 'error');
                input.value = currentQuantity;
            }

        } catch (error) {
            console.error('更新數量 API 錯誤:', error);
            showMessage('無法連線到伺服器', 'error');
            input.value = currentQuantity;
        }
    }

    // --- (H) (核心) 處理清空購物車 (DELETE /api/cart) ---
    clearCartBtn.addEventListener('click', async () => {
        if (!confirm('您確定要清空整個購物車嗎？')) {
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/cart`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.status === 204) {
                showMessage('購物車已清空！', 'success');
                fetchMyCart();
            } else {
                const errorText = await response.text();
                showMessage(`清空購物車失敗: ${errorText}`, 'error');
            }

        } catch (error) {
            console.error('清空購物車 API 錯誤:', error);
            showMessage('無法連線到伺服器', 'error');
        }
    });

    // (輔助功能：顯示訊息)
    function showMessage(message, type = 'error') {
        messageContainer.textContent = message;
        messageContainer.className = `message ${type}`;
        messageContainer.style.display = 'block';

        setTimeout(() => {
            messageContainer.style.display = 'none';
            messageContainer.textContent = '';
            messageContainer.className = 'message';
        }, 3000);
    }

    init();
});