# Spring Boot 全端電商平台 (E-Shop Demo)

這是一個功能完整的全端電商平台專案，模擬一個多賣家 (Multi-Seller) 的市集（如蝦皮或 Amazon）。

* **後端 (Backend):** 使用 Java Spring Boot 構建，包含 JWT 安全認證、RESTful API、JPA 資料庫管理和多角色權限控制。
* **前端 (Frontend):** 使用 Vanilla JavaScript (ES6+), HTML5, 和 CSS3 構建，不依賴任何框架 (like React/Vue)，專注於透過 `fetch` API 實作非同步 (Async/Await) 頁面渲染。

---

## ✨ 核心架構亮點 (Core Architecture Highlights)

* **[多賣家拆單系統]** 購物車結帳時，後端會自動依據「賣家 ID」將一張總訂單**拆分**為多張子訂單 (`OrderPO`)，並分別進行庫存扣除與賣家餘額入帳。
* **[驗證購買評價]** 嚴謹的評價系統設計，買家**只能**對 `OrderItemPO` (已購買且完成的訂單項目) 進行評價 (`POST`) 或更新 (`PUT`)，杜絕假評論。
* **[RBAC 角色權限控制]** 使用 Spring Security + JWT，精準控制 API 存取權限，明確劃分 `ADMIN` (管理員), `BUYER` (買家), `SELLER` (賣家) 和 `PUBLIC` (公開) 的端點安全性。
* **[雙軌金流系統]**
    * **內部錢包**：買賣家皆擁有獨立的 `WalletPO` (電子錢包)，支援模擬儲值 (`topup`) 與提款 (`withdraw`) 的完整交易紀錄。
    * **第三方支付**：完整串接 **綠界科技 (ECPay)**。實作 CreateOrder (建立訂單) 與 Notify (非同步回調) 流程，並採用 SHA-256 加密驗證，確保交易資料完整性。
* **[遞迴分類查詢]** 商品分類支援「樹狀結構」，查詢父分類時會自動遞迴 (Recursion) 抓取該分類下所有子孫分類的商品。
* **[後台數據中心]** 管理員專屬儀表板，可視覺化檢視營運數據並匯出 Excel 報表。

---

## 📍 核心功能 (Features)

本平台區分為「公開訪客」、「買家」、「賣家」與「管理員」四種視角。

### 1. 公開功能 (Public)

* **角色分流入口:**
  <br>
  <img width="500" alt="角色選擇" src="https://github.com/user-attachments/assets/7f0449d8-f560-46d3-8f1c-89e183ae0fca" />

* **身分認證 (Auth):**
    * 買家/賣家 分離式登入與註冊介面。
    * 使用 **JWT (JSON Web Tokens)** 進行無狀態 API 身份驗證。
      <table>
        <tr>
          <td valign="top" width="50%"><img width="100%" alt="註冊介面" src="https://github.com/user-attachments/assets/324da606-cf18-4468-8853-0244f81eb5fd" /></td>
          <td valign="top" width="50%"><img width="100%" alt="登入介面" src="https://github.com/user-attachments/assets/631dae5f-73ad-4835-a3f5-e53cf91eca5c" /></td>
        </tr>
      </table>

* **帳戶安全:**
    * 支援「發送 Email 驗證碼」 (`/api/auth/send-code`) 進行註冊驗證。
      <img width="500" alt="Email驗證" src="https://github.com/user-attachments/assets/33879d45-67e2-40a9-ada2-064fb4b7df48" />
    * 完整的「忘記密碼」與「重設密碼」流程。
      <table>
        <tr>
          <td valign="top"><img width="100%" alt="忘記密碼" src="https://github.com/user-attachments/assets/76851239-4393-4aa1-9252-1e96e35fc5b3" /></td>
          <td valign="top"><img width="100%" alt="信箱接收" src="https://github.com/user-attachments/assets/45391832-f064-4648-a5d9-6149183b603e" /></td>
          <td valign="top"><img width="100%" alt="重設完成" src="https://github.com/user-attachments/assets/484058ea-2a60-44d0-a90d-5d760692f055" /></td>
        </tr>
      </table>

### 2. 買家中心 (BUYER)

* **商品瀏覽與檢索:**
    * 在 `products.html` 瀏覽或篩選上架商品。
      <img width="800" alt="商品列表" src="https://github.com/user-attachments/assets/abdd9ea2-f87e-4fad-bbcc-52c7ecabd83c" />
    * 商品詳情頁查看庫存與**真實顧客評價**。
      <img width="800" alt="商品詳情" src="https://github.com/user-attachments/assets/be2b4ebc-9228-415d-86d7-a983b1a0f170" />

* **購物車 (Cart):**
    * 即時計算總金額，並與錢包餘額進行比較提示。
      <img width="800" alt="購物車" src="https://github.com/user-attachments/assets/a38c4699-ae04-4836-9fae-55f2ff40276d" />

* **電子錢包 (Wallet):**
    * 檢視餘額與模擬儲值功能 (`POST /api/wallet/topup`)。
      <img width="500" alt="錢包儲值" src="https://github.com/user-attachments/assets/e2e2fe53-e140-4ef6-86cc-8158bc7995ed" />

* **個人資料 (Profile):**
    * 編輯個人資訊與預設收貨地址。
      <img width="800" alt="個人資料" src="https://github.com/user-attachments/assets/07846e89-7c81-4018-8b85-ab96606acf40" />

* **結帳與支付 (Checkout):**
    * **自動拆單機制**：系統自動將不同賣家的商品拆分為不同訂單。
    * **多元支付選擇**：
        1. **內部錢包**：直接扣除餘額。
        2. **綠界支付 (ECPay)**：支援信用卡/ATM。付款成功後透過 Webhook 自動回調後端更新訂單狀態。
      <table>
        <tr>
          <td valign="top" width="50%"><img width="100%" alt="綠界轉導頁" src="https://github.com/user-attachments/assets/8f9f38ba-bb4d-446d-afd7-e0446103e70e" /></td>
          <td valign="top" width="50%"><img width="100%" alt="綠界付款頁面" src="https://github.com/user-attachments/assets/0602085b-bb93-4619-bfe9-037f6a3231f0" /></td>
        </tr>
      </table>

* **訂單與評價:**
    * 瀏覽歷史訂單列表與詳細資訊。
      <img width="800" alt="訂單列表" src="https://github.com/user-attachments/assets/49e2ac64-403d-43e4-b27c-737bb46de356" />
      <img width="800" alt="訂單詳情" src="https://github.com/user-attachments/assets/6fd28ce2-b860-4593-8a93-d379db975fec" />
    * 對已完成的訂單項目撰寫評價。
      <table>
        <tr>
          <td valign="top" width="50%"><img width="100%" alt="評價輸入" src="https://github.com/user-attachments/assets/16686c24-9c85-4dec-9e7d-d555c9aacd27" /></td>
          <td valign="top" width="50%"><img width="100%" alt="評價顯示" src="https://github.com/user-attachments/assets/3cae6aa2-313d-4daa-b370-c7dbed783368" /></td>
        </tr>
      </table>

### 3. 賣家中心 (SELLER)

* **商品管理:**
    * 透過 `seller-dashboard` 上架、修改或下架商品。
      <table>
        <tr>
          <td valign="top" width="50%"><img width="100%" alt="商品管理列表" src="https://github.com/user-attachments/assets/877ff1fb-e4b9-4fe8-b345-f74ab523bc70" /></td>
          <td valign="top" width="50%"><img width="100%" alt="編輯商品" src="https://github.com/user-attachments/assets/7baf1eb0-f580-4162-bcb6-4619326b0b8b" /></td>
        </tr>
      </table>

* **財務管理:**
    * 設定提款銀行帳戶（支援格式驗證）。
    * 檢視銷售收入餘額並申請提款 (`withdraw`)。
      <img width="600" alt="賣家財務" src="https://github.com/user-attachments/assets/6ecd7d41-7afe-4822-b7e6-d79a99bf6a17" />

* **訂單與評價管理:**
    * 檢視所有收到的訂單與買家資訊。
      <img width="600" alt="賣家訂單" src="https://github.com/user-attachments/assets/145b1cf5-5155-4acb-bce2-059faf284ed4" />
    * 查看商品收到的評價內容。
      <img width="600" alt="賣家評價" src="https://github.com/user-attachments/assets/ad5c443e-7d04-4efb-89b3-8d8cd4906f05" />

### 4. 後台管理 (ADMIN)

* **管理員登入:**
  * 使用專屬管理員編號 (Admin Code) 進行登入。
  <img width="400" alt="管理員登入" src="https://github.com/user-attachments/assets/fe736ae0-d1de-45be-b5b1-006162699337" />

* **營運儀表板:**
    * 視覺化報表：訂單金額分佈、新會員成長比例、熱銷商品類別 Top 5 及財務報表。
      <table>
        <tr>
          <td valign="top" width="60%"><img width="100%" alt="營運報表1" src="https://github.com/user-attachments/assets/48d48ae4-31fa-4693-afab-628a139a39d2" /></td>
          <td valign="top" width="40%"><img width="100%" alt="營運報表2" src="https://github.com/user-attachments/assets/97f8f77a-0aa8-4f5c-aab6-3f7777522265" /></td>
        </tr>
      </table>
    * 支援 Excel 報表匯出功能。
      <img width="400" alt="匯出報表" src="https://github.com/user-attachments/assets/23c05cfb-7c48-4411-8295-b4b52eec8eb1" />

* **會員管理:**
    * 統一管理所有買家與賣家帳號，具備查看訂單、商品及刪除帳號權限。
      <table>
        <tr>
          <td valign="top" width="50%"><img width="100%" alt="買家管理" src="https://github.com/user-attachments/assets/62997c5a-f0a4-4953-ad11-04eba0b83349" /></td>
          <td valign="top" width="50%"><img width="100%" alt="賣家管理" src="https://github.com/user-attachments/assets/8f8af622-6d43-4082-9044-47d9ed7e44ae" /></td>
        </tr>
      </table>

---

## 🛠️ 技術棧 (Technology Stack)

| 類別 | 技術 |
| :--- | :--- |
| **核心框架** | Java 21, Spring Boot 3.5.7 |
| **安全與認證** | Spring Security, JJWT (JSON Web Token 0.12.5) |
| **資料庫與 ORM** | MySQL 8.0, Spring Data JPA (Hibernate) |
| **工具與報表** | Apache POI 5.2.5 (Excel 匯出), Lombok, Spring Mail (Email 發送) |
| **前端 (Frontend)** | Vanilla JavaScript (ES6+ Async/Await), Fetch API, HTML5, CSS3 |
| **驗證 (Validation)** | `jakarta.validation` (`@Valid`, `@Pattern`) |

### 📦 後端關鍵依賴 (Backend Dependencies)
本專案 `pom.xml` 使用了以下關鍵組件：

* **`spring-boot-starter-security`**: 實作 RBAC 角色權限控制與 CSRF 防護。
* **`jjwt-api` / `jjwt-impl`**: 實作無狀態的 JWT Token 生成與解析。
* **`apache-poi` / `poi-ooxml`**: 用於後端生成與匯出 `.xlsx` 格式的營運報表。
* **`spring-boot-starter-mail`**: 實作 SMTP 郵件發送（如：註冊驗證碼）。
* **`commons-codec`**: 輔助加密與編碼工具 (SHA-256 計算)。
* **`mysql-connector-j`**: MySQL 資料庫驅動程式。
* **`lombok`**: 簡化 POJO 與 Log 程式碼。

---

## 🏛️ 專案架構 (Architecture)

### API 端點設計

| 路徑 | 控制器 | 目的 |
| :--- | :--- | :--- |
| `/api/auth/**` | `AuthController` | 處理登入、註冊、驗證碼與 Token 發放 |
| `/api/public/**` | `PublicProductController` | 公開的商品檢索與評價查詢 |
| `/api/profile/me` | `ProfileController` | (需登入) 個人資料管理 |
| `/api/wallet/**` | `WalletController` | (需登入) 錢包餘額查詢、儲值與提款 |
| `/api/cart/**` | `CartController` | (買家) 購物車 CRUD 操作 |
| `/api/orders/**` | `OrderController` | (買家) 結帳、拆單邏輯與訂單查詢 |
| `/api/ratings/**` | `RatingController` | (買家) 新增與更新評價 |
| `/api/seller/**` | `SellerController` | (賣家) 銀行帳戶、訂單與商品管理 |
| `/api/admin/**` | `AdminReportController` | (管理員) 報表數據與會員管理 |
| `/createOrder` | `ECPayController` | 建立綠界金流訂單 (轉導至付款頁面) |
| `/notify` | `NotifyController` | 接收綠界伺服器付款結果回調 (Webhook) |

---

## 🗃️ 資料庫架構 (Database Schema)

本專案圍繞著 `users`, `products`, 和 `orders` 三大核心實體構建。

<img width="800" alt="ER Diagram" src="https://github.com/user-attachments/assets/7f13b847-d0c1-4093-aab2-a2e7258f14ae" />

### 資料表介紹 (Table Definitions)

#### 1. 使用者 & 認證 (User & Auth)
* **`users`**: 核心使用者表。`role` 欄位區分 'BUYER' / 'SELLER' / 'ADMIN'。`admin_code` 欄位專門用於管理員登入。
* **`password_reset_tokens`**: 存放忘記密碼的一次性 Token，設定有效期限，關聯 `user_id`。

#### 2. 商品 & 分類 (Product & Catalog)
* **`categories`**: 商品分類表。`parent_category_id` 關聯自身 (Self-referencing) 以實現無限層級的**樹狀結構**。
* **`products`**: 商品主表。關聯 `seller_id` (賣家) 與 `category_id`，`image_url` 儲存圖片路徑。
* **`product_ratings`**: 商品評價表。**關鍵設計**：它不直接關聯 `products`，而是關聯 `order_item_id` (訂單細項)，確保只有**實際購買過**該商品的買家才能留下評價。

#### 3. 購物車 & 訂單 (Cart & Order)
* **`carts`**: 購物車主表。採一對一關聯 `user_id` (買家)。
* **`cart_items`**: 購物車明細。存放商品 ID 與數量。
* **`orders`**: 訂單主表。包含 `buyer_id` 與 `seller_id`，支援**多賣家拆單**邏輯。
* **`order_items`**: 訂單項目表。記錄訂單成立當下的「商品快照」（如 `price_per_unit`），確保歷史訂單價格不會隨商品漲價而變動。

#### 4. 金流 & 帳戶 (Wallet & Finance)
* **`wallets`**: 電子錢包。每位使用者皆有一個錢包，儲存 `balance` (餘額)。
* **`wallet_transactions`**: 交易流水帳。記錄所有 `TOPUP` (儲值), `PAYMENT` (支付), `WITHDRAWAL` (提款) 操作。
* **`bank_accounts`**: 賣家銀行帳戶。儲存賣家提款所需的銀行資訊。

---

## 🚀 如何開始 (Getting Started)

### 必備環境 (Prerequisites)
* Java JDK (建議 17 或 21)
* Apache Maven
* MySQL (或相容資料庫)
* Eclipse IDE (需安裝 Lombok Plugin) 或 IntelliJ IDEA
* VS Code (前端開發用，建議安裝 `Live Server` 擴充)
* **Ngrok** (用於本地開發接收綠界金流回調)

### 1. 後端設定 (Spring Boot)

1.  **設定資料庫:**
    * 在 MySQL 中建立一個新的資料庫 (Schema) 命名為 `bigwork`。
    * 執行專案中 `SQL資料夾` 內的 `.sql` 腳本，匯入資料表結構與預設資料。
2.  **匯入專案:**
    * 在 Eclipse 中選擇 `Import` -> `Existing Maven Projects` 匯入 `bigwork` 專案。
3.  **設定檔:**
    * 開啟 `src/main/resources/application.properties`。
    * 修改 `spring.datasource.url`, `username`, `password` 以符合您的本地 MySQL 設定。
4.  **啟動後端:**
    * 執行 `BigworkApplication.java`。
    * 伺服器預設運行於 `http://localhost:8080`。

### 2. 前端設定 (HTML/JS)

1.  **開啟專案:**
    * 使用 VS Code 開啟 `frontend-html` 資料夾。
2.  **啟動伺服器:**
    * 在 `index.html` 檔案上按右鍵，選擇 `Open with Live Server`。
    * 瀏覽器將自動開啟 `http://127.0.0.1:5500/html/index.html` (Port 號可能依設定不同)。

### 3. 綠界金流 (ECPay) 與 Ngrok 設定

由於綠界金流需要一個**公開的網址**來發送付款結果通知 (Webhook)，本地開發 (`localhost`) 必須使用 Ngrok 建立隧道。

1.  **啟動 Ngrok:**
    * 創建一個Ngrok帳號，然後複製自己的token：
      <img width="1568" height="565" alt="image" src="https://github.com/user-attachments/assets/7216cf3c-f885-40b7-9d54-64579c30962d" />
    * 安裝並執行 Ngrok，先輸入複製的token後，再輸入以下指令將本地 8080 Port 對應到公開網址：
      ```bash
      ngrok http 8080
      ```
      *將下方設定檔中的網址替換為 Ngrok 產生的網址
      <img width="967" height="515" alt="image" src="https://github.com/user-attachments/assets/4030459d-4a35-48aa-8cdf-c278e8a731be" />

3.  **修改 application.properties:**
    * 確保設定檔中包含您的 ECPay 測試帳號與 Ngrok 回傳網址：

    ```properties
    # --- ECPay 綠界金流設定 ---
    ecpay.merchantId=3002607
    ecpay.hashKey=pwFHCqoQZGmho4w6
    ecpay.hashIV=EkRm7iFT261dpevs
    ecpay.serviceUrl=[https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5]
    
    # [重要] 請替換為您當下的 Ngrok 網址( Ngrok 產生的網址+/notify)
    ecpay.return.url=[https://gayla-unbriefed-unreluctantly.ngrok-free.dev/notify]
    
    # 支付成功後，使用者點擊「返回商店」會跳轉到的前端頁面
    ecpay.client.back.url=[http://127.0.0.1:5500/html/cart.html]
    ```

    > **⚠️ 注意：** 每次重新啟動 Ngrok (若非固定網域) 或更換測試環境時，請務必檢查 `ecpay.return.url` 是否正確，否則後端將無法接收付款成功通知。

---

## 📄 授權 (License)

本專案採用 MIT 授權。
