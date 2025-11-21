// 後端 Auth API 的根路徑
const API_BASE = "http://localhost:8080/api/auth";

document.addEventListener("DOMContentLoaded", () => {
    const roleTabs = document.querySelectorAll(".role-tab");
    const selectedRoleInput = document.getElementById("selected-role");
    const roleHint = document.getElementById("role-hint");

    const messageContainer = document.getElementById("message-container");

    const loginForm = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");
    const showRegisterLink = document.getElementById("show-register-link");
    const showLoginLink = document.getElementById("show-login-link");
    const loginToRegisterP = document.getElementById("login-to-register");

    const loginAccountLabel = document.getElementById("login-account-label");
    const loginEmailInput = document.getElementById("login-email");
    const loginPasswordInput = document.getElementById("login-password");

    const regEmailInput = document.getElementById("reg-email");
    const regCodeInput = document.getElementById("reg-code");
    const regPasswordInput = document.getElementById("reg-password");
    const regNameInput = document.getElementById("reg-name");
    const regPhoneInput = document.getElementById("reg-phone");
    const regAddressInput = document.getElementById("reg-address");
    const sendCodeBtn = document.getElementById("send-code-btn");

    // ===== 通用訊息顯示 =====
    function setMessage(text, type = "info") {
        if (!messageContainer) return;
        messageContainer.textContent = text || "";
        messageContainer.className = "message";

        if (text) {
            if (type === "error") {
                messageContainer.classList.add("error-message");
            } else if (type === "success") {
                messageContainer.classList.add("success-message");
            }
        }
    }

    // ===== 角色切換 UI =====
    function updateUIForRole(role) {
        selectedRoleInput.value = role;

        if (role === "BUYER") {
            loginAccountLabel.textContent = "電子郵件 (Email):";
            roleHint.textContent = "目前身分：買家（使用 Email 登入，可線上註冊）";
            loginToRegisterP.style.display = "block";
        } else if (role === "SELLER") {
            loginAccountLabel.textContent = "電子郵件 (Email):";
            roleHint.textContent = "目前身分：賣家（使用 Email 登入，可線上註冊）";
            loginToRegisterP.style.display = "block";
        } else if (role === "ADMIN") {
            loginAccountLabel.textContent = "管理員編號:";
            roleHint.textContent = "目前身分：系統管理員（使用預先建立的管理員編號登入，不開放線上註冊）";
            loginToRegisterP.style.display = "none"; // 管理員不顯示註冊入口
            registerForm.style.display = "none";
            loginForm.style.display = "block";
            setMessage("");
        }
    }

    // 角色 tab 點擊
    roleTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            roleTabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");
            const role = tab.dataset.role;
            updateUIForRole(role);
        });
    });

    // ===== 登入 / 註冊表單切換 =====
    if (showRegisterLink) {
        showRegisterLink.addEventListener("click", (e) => {
            e.preventDefault();
            const role = selectedRoleInput.value;

            if (role === "ADMIN") {
                setMessage("系統管理員帳號需由後台預先建立，無法在線上註冊。", "error");
                return;
            }

            loginForm.style.display = "none";
            registerForm.style.display = "block";
            setMessage("");
        });
    }

    if (showLoginLink) {
        showLoginLink.addEventListener("click", (e) => {
            e.preventDefault();
            registerForm.style.display = "none";
            loginForm.style.display = "block";
            setMessage("");
        });
    }

    // ===== 送出驗證碼 =====
    if (sendCodeBtn) {
        sendCodeBtn.addEventListener("click", async () => {
            const email = regEmailInput.value.trim();
            if (!email) {
                setMessage("請先輸入 Email 再取得驗證碼。", "error");
                return;
            }

            try {
                const res = await fetch(`${API_BASE}/send-code`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email })
                });

                if (!res.ok) {
                    const text = await res.text();
                    throw new Error(text || "發送驗證碼失敗");
                }

                const msg = await res.text();
                setMessage(msg || "驗證碼已寄出，請至信箱查看。", "success");
            } catch (err) {
                console.error(err);
                setMessage(err.message || "發送驗證碼失敗，請稍後再試。", "error");
            }
        });
    }

    // ===== 登入處理 =====
    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            setMessage("");

            const role = selectedRoleInput.value;
            const account = loginEmailInput.value.trim();
            const password = loginPasswordInput.value.trim();

            if (!account || !password) {
                setMessage("請輸入帳號與密碼。", "error");
                return;
            }

            let url = "";
            let payload = {};

            if (role === "ADMIN") {
                url = `${API_BASE}/admin-login`;
                payload = {
                    adminCode: account,
                    password: password
                };
            } else {
                url = `${API_BASE}/login`;
                payload = {
                    email: account,
                    password: password
                };
            }

            try {
                const res = await fetch(url, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                if (!res.ok) {
                    const text = await res.text();
                    throw new Error(text || "登入失敗");
                }

                const data = await res.json();
                // { message, userId, name, email, role, token, adminCode }

                if (role === "ADMIN" && data.role !== "ADMIN") {
                    setMessage("此帳號不是系統管理員。", "error");
                    return;
                }

                // 儲存登入資訊
                localStorage.setItem("authToken", data.token);
                localStorage.setItem("userRole", data.role);
                if (data.name) {
                    localStorage.setItem("userName", data.name);
                } else {
                    localStorage.removeItem("userName");
                }
                if (data.adminCode) {
                    localStorage.setItem("adminCode", data.adminCode);
                } else {
                    localStorage.removeItem("adminCode");
                }

                setMessage(data.message || "登入成功。", "success");

                // 依角色導頁（檔名如有不同就改這裡）
                if (data.role === "BUYER") {
                    window.location.href = "buyer-home.html";
                } else if (data.role === "SELLER") {
                    window.location.href = "seller-home.html";
                } else if (data.role === "ADMIN") {
                    window.location.href = "admin-dashboard.html";
                }

            } catch (err) {
                console.error(err);
                setMessage(err.message || "登入失敗，請稍後再試。", "error");
            }
        });
    }

    // ===== 註冊處理（BUYER / SELLER） =====
    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            setMessage("");

            const role = selectedRoleInput.value;
            if (role === "ADMIN") {
                setMessage("系統管理員帳號無法在線上註冊。", "error");
                return;
            }

            const email = regEmailInput.value.trim();
            const verificationCode = regCodeInput.value.trim();
            const password = regPasswordInput.value.trim();
            const name = regNameInput.value.trim();
            const phone = regPhoneInput.value.trim();
            const address = regAddressInput.value.trim();

            if (!email || !verificationCode || !password || !name || !phone || !address) {
                setMessage("請完整填寫所有欄位。", "error");
                return;
            }

            const payload = {
                email,
                verificationCode,
                password,
                name,
                phone,
                address
            };

            const url =
                role === "SELLER"
                    ? `${API_BASE}/register/seller`
                    : `${API_BASE}/register/buyer`;

            try {
                const res = await fetch(url, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(payload)
                });

                if (!res.ok) {
                    const text = await res.text();
                    throw new Error(text || "註冊失敗");
                }

                setMessage("註冊成功，請回到登入頁使用新帳號登入。", "success");

                registerForm.reset();
                registerForm.style.display = "none";
                loginForm.style.display = "block";

            } catch (err) {
                console.error(err);
                setMessage(err.message || "註冊失敗，請稍後再試。", "error");
            }
        });
    }

    // 初始狀態：買家
    updateUIForRole("BUYER");
});
