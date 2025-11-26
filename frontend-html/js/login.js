document.addEventListener("DOMContentLoaded", () => {

    const API_BASE_URL = "http://localhost:8080";
    const params = new URLSearchParams(window.location.search);
    const role = params.get('role'); // "BUYER", "SELLER", "ADMIN"

    // (DOM 元素)
    const pageTitle = document.getElementById("page-title");
    const loginForm = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");
    
    const showRegisterLink = document.getElementById("show-register-link");
    const showLoginLink = document.getElementById("show-login-link");
    const switchContainer = document.querySelector(".toggle-link"); 

    const messageContainer = document.getElementById("message-container");
    const sendCodeBtn = document.getElementById("send-code-btn");
    const regEmailInput = document.getElementById("reg-email");
    const regCodeInput = document.getElementById("reg-code");
    
    // (取得登入輸入框的 Label 和 Input)
    const loginAccountLabel = document.querySelector("label[for='login-email']");
    const loginAccountInput = document.getElementById("login-email");

    // ==========================================
    // 1. 初始化頁面 (UI 調整)
    // ==========================================
    if (role === 'BUYER') {
        pageTitle.textContent = "買家 登入 / 註冊";
    } else if (role === 'SELLER') {
        pageTitle.textContent = "賣家 登入 / 註冊";
    } else if (role === 'ADMIN') {
        // --- 管理員 UI 設定 ---
        pageTitle.textContent = "管理員登入";
        
        // 修改 Label 和 Placeholder
        if (loginAccountLabel) loginAccountLabel.textContent = "管理員編號 (Admin Code):";
        if (loginAccountInput) loginAccountInput.placeholder = "請輸入管理員編號";
        
        // 隱藏註冊連結
        if (switchContainer) {
            switchContainer.style.display = 'none'; 
        } else if (showRegisterLink) {
            showRegisterLink.style.display = 'none';
        }
        
    } else {
        window.location.href = 'index.html'; 
        return; 
    }

    // ==========================================
    // 2. 表單切換 (僅非管理員有效)
    // ==========================================
    if (role !== 'ADMIN' && showRegisterLink) {
        showRegisterLink.addEventListener("click", (e) => {
            e.preventDefault();
            loginForm.style.display = 'none';
            registerForm.style.display = 'block';
            showMessage('', 'clear');
        });
    }

    if (role !== 'ADMIN' && showLoginLink) {
        showLoginLink.addEventListener("click", (e) => {
            e.preventDefault();
            loginForm.style.display = 'block';
            registerForm.style.display = 'none';
            showMessage('', 'clear');
        });
    }

    // ==========================================
    // 3. 登入邏輯 (API 路徑與參數分流)
    // ==========================================
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        
        const accountInput = loginAccountInput.value; 
        const passwordInput = loginForm.password.value;
        const expectedRole = role; 
        
        // 預設變數
        let loginUrl = "";
        let payload = {};

        // (★ URL 與 Payload 分流)
        if (role === 'ADMIN') {
            // 管理員專用 API
            loginUrl = `${API_BASE_URL}/api/auth/admin-login`;
            payload = { 
                adminCode: accountInput, 
                password: passwordInput 
            };
        } else {
            // 買家/賣家通用 API
            loginUrl = `${API_BASE_URL}/api/auth/login`;
            payload = { 
                email: accountInput, 
                password: passwordInput 
            };
        }

        try {
            const response = await fetch(loginUrl, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload),
            });

            if (response.ok) {
                const data = await response.json(); 
                const actualRole = data.role; 

                // (身分驗證)
                if (actualRole !== expectedRole) {
                    let expectedText = getRoleName(expectedRole);
                    let actualText = getRoleName(actualRole);
                    
                    showMessage(`登入失敗：您的帳號身分是「${actualText}」，但此頁面僅限「${expectedText}」登入。`, 'error');
                    return; 
                }

                // (儲存 Token + 角色)
                localStorage.setItem('token', data.token);
                localStorage.setItem('role', actualRole);

                // ★★★ 這裡是新增的部分：處理 ADMIN 額外資訊 ★★★
                if (actualRole === 'ADMIN') {
                    // 管理員：存 adminCode / name，給 admin-dashboard 使用
                    if (data.adminCode) {
                        localStorage.setItem('adminCode', data.adminCode);
                    }
                    if (data.name) {
                        localStorage.setItem('userName', data.name);
                    }
                } else {
                    // 一般會員：避免殘留舊的 adminCode
                    localStorage.removeItem('adminCode');
                }
                // ★★★ 以上新增結束，其餘不動 ★★★
                
                showMessage('登入成功！正在跳轉.', 'success');
                
                // (跳轉邏輯)
                setTimeout(() => {
                    if (actualRole === 'BUYER') {
                        window.location.href = 'products.html'; 
                    } else if (actualRole === 'SELLER') {
                        window.location.href = 'seller-dashboard.html'; 
                    } else if (actualRole === 'ADMIN') {
                        window.location.href = 'admin-dashboard.html'; 
                    }
                }, 1500);
                
            } else {
                // 失敗時讀取錯誤訊息
                const errorMessage = await response.text(); 
                showMessage(errorMessage, 'error'); 
            }
        } catch (error) {
            console.error('登入 API 錯誤:', error);
            showMessage("系統錯誤: " + error.message, 'error'); 
        }
    });

    // ==========================================
    // 4. 發送驗證碼 (僅 BUYER/SELLER 需要)
    // ==========================================
    if (sendCodeBtn && role !== 'ADMIN') {
        sendCodeBtn.addEventListener("click", async () => {
            const email = regEmailInput.value;
            if (!email) {
                showMessage('請先輸入 Email', 'error');
                return;
            }

            // UI 倒數鎖定
            sendCodeBtn.disabled = true;
            let countdown = 60;
            sendCodeBtn.textContent = `重新發送 (${countdown})`;
            const interval = setInterval(() => {
                countdown--;
                sendCodeBtn.textContent = `重新發送 (${countdown})`;
                if (countdown <= 0) {
                    clearInterval(interval);
                    sendCodeBtn.disabled = false;
                    sendCodeBtn.textContent = '獲取驗證碼';
                }
            }, 1000);

            const sendCodeUrl = `${API_BASE_URL}/api/auth/send-code`;
            
            try {
                const response = await fetch(sendCodeUrl, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ email: email }) 
                });
                
                if (response.ok) {
                    const successMessage = await response.text(); 
                    showMessage(successMessage, 'success'); 
                    console.log("開發模式提示: 請查看後端 Console 獲取驗證碼");
                } else {
                    let errorMessage = '發送失敗';
                    try {
                        const errorData = await response.json();
                        errorMessage = errorData.message || '發送失敗';
                    } catch (e) {
                        errorMessage = await response.text();
                    }
                    // 失敗時重置按鈕
                    clearInterval(interval);
                    sendCodeBtn.disabled = false;
                    sendCodeBtn.textContent = '獲取驗證碼';
                    showMessage(errorMessage, 'error');
                }
            } catch (error) {
                console.error('發送驗證碼 API 錯誤:', error);
                showMessage("網路錯誤: " + error.message, 'error');
                clearInterval(interval); 
                sendCodeBtn.disabled = false;
                sendCodeBtn.textContent = '獲取驗證碼';
            }
        });
    }

    // ==========================================
    // 5. 註冊邏輯 (僅 BUYER/SELLER 需要)
    // ==========================================
    if (registerForm && role !== 'ADMIN') {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault(); 
            
            const formData = {
                email: regEmailInput.value,
                password: registerForm.password.value,
                name: registerForm.name.value,
                phone: registerForm.phone.value,
                address: registerForm.address.value,
                code: regCodeInput.value 
            };
            
            // 根據 role 決定註冊 API
            const registerUrl = `${API_BASE_URL}/api/auth/register/${role.toLowerCase()}`;
            
            try {
                const response = await fetch(registerUrl, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(formData),
                });

                const data = await response.json(); // 註冊通常回傳 JSON (含 token)

                if (response.ok) {
                    const actualRole = data.role; 
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('role', actualRole); 
                    
                    showMessage('註冊成功！正在自動登入.', 'success');
                    
                    setTimeout(() => {
                        if (actualRole === 'BUYER') {
                            window.location.href = 'products.html'; 
                        } else if (actualRole === 'SELLER') {
                            window.location.href = 'seller-dashboard.html'; 
                        }
                    }, 1500);
                    
                } else {
                    showMessage(data.message || '註冊失敗', 'error');
                }
            } catch (error) {
                console.error('註冊 API 錯誤:', error);
                showMessage("系統錯誤: " + error.message, 'error');
            }
        });
    }

    // ==========================================
    // 輔助函式
    // ==========================================
    function showMessage(message, type = 'error') {
        if (!messageContainer) return;
        
        messageContainer.textContent = message;
        messageContainer.className = `message ${type}`;
        
        if (type === 'clear') {
             messageContainer.style.display = 'none';
        } else {
             messageContainer.style.display = 'block';
        }
    }

    function getRoleName(roleCode) {
        switch(roleCode) {
            case 'BUYER': return '買家';
            case 'SELLER': return '賣家';
            case 'ADMIN': return '管理員';
            default: return roleCode;
        }
    }
});