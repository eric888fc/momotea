# Spring Boot 全端電商平台 (E-Shop Demo)

這是一個功能完整的全端電商平台專案，模擬一個多賣家 (Multi-Seller) 的市集（如蝦皮或 Amazon）。

* **後端 (Backend):** 使用 Java Spring Boot 構建，包含 JWT 安全認證、RESTful API、JPA 資料庫管理和多角色權限控制。
* **前端 (Frontend):** 使用 Vanilla JavaScript (ES6+), HTML5, 和 CSS3 構建，不依賴任何框架 (like React/Vue)，專注於透過 `fetch` API 實作非同步 (Async/Await) 頁面渲染。

---

## ✨ 核心架構亮點 (Core Architecture Highlights)

* **[多賣家拆單]** 購物車結帳時，後端會自動依據「賣家 ID」將一張訂單**拆分**為多張子訂單 (`OrderPO`)，並分別進行庫存與餘額扣款。
* **[驗證購買評價]** 嚴謹的評價系統，買家**只能**對 `OrderItemPO` (已購買的訂單項目) 進行評價 (`POST`) 或更新 (`PUT`)。
* **[角色權限 (RBAC)]** 使用 Spring Security + JWT，精準控制 API 權限，明確劃分`ADMIN`, `BUYER`, `SELLER` 和 `PUBLIC` 的可存取端點。
* **[金流模擬]** 買賣家皆擁有獨立的 `WalletPO` (電子錢包)，支援買家儲值 (`topup`) 與賣家提款 (`withdraw`) 的完整金流驗證。
* **[遞迴分類查詢]** 商品分類支援「樹狀結構」，查詢時會自動遞迴抓取所有子分類的商品。
* **[第三方金流整合]** 完整串接 綠界科技 (ECPay) 金流服務。實作了完整的 CreateOrder (建立訂單) 與 Notify (非同步回調) 流程，並採用 SHA-256 加密，確保交易資料不被篡改。

---

## 📍 核心功能 (Features)

這個平台區分為「管理員」、「買家」和「賣家」三種視角。



### 1：公開 (Public)
* **角色選擇:**
      <img width="544" height="398" alt="image" src="https://github.com/user-attachments/assets/7f0449d8-f560-46d3-8f1c-89e183ae0fca" />

* **認證 (Auth):**
    * 買家/賣家 角色分離的登入/註冊 。
      <table>
        <tr>
          <td valign="top">
            <img width="400" alt="image" src="https://github.com/user-attachments/assets/324da606-cf18-4468-8853-0244f81eb5fd" />
          </td>
          <td valign="top">
            <img width="400" alt="image" src="https://github.com/user-attachments/assets/631dae5f-73ad-4835-a3f5-e53cf91eca5c" />
          </td>
        </tr>
      </table>
* **使用 JWT (JSON Web Tokens) 進行 API 身份驗證。**
    * 支援「發送Email驗證碼」 (`/api/auth/send-code`)。
      <img width="587" height="335" alt="image" src="https://github.com/user-attachments/assets/33879d45-67e2-40a9-ada2-064fb4b7df48" />
* **「忘記密碼」和「重設密碼」流程。**
      <table>
        <tr>
          <td valign="top">
            <img width="491" height="407" alt="image" src="https://github.com/user-attachments/assets/76851239-4393-4aa1-9252-1e96e35fc5b3" />
          </td>
          <td valign="top">
            <img width="646" height="304" alt="image" src="https://github.com/user-attachments/assets/45391832-f064-4648-a5d9-6149183b603e" />
          </td>
          <td valign="top">
            <img width="490" height="477" alt="image" src="https://github.com/user-attachments/assets/484058ea-2a60-44d0-a90d-5d760692f055" />
          </td>
        </tr>
      </table>

### 2.角色：買家 (BUYER)
* **商品瀏覽:**
    * 在 `products.html` 瀏覽/篩選所有上架商品。
      <img width="800" height="600" alt="image" src="https://github.com/user-attachments/assets/abdd9ea2-f87e-4fad-bbcc-52c7ecabd83c" />

    * 在 `product-detail.html` 查看商品詳情、庫存和**所有顧客評價**。
      <img width="827" height="893" alt="image" src="https://github.com/user-attachments/assets/be2b4ebc-9228-415d-86d7-a983b1a0f170" />
* **購物車 (Cart):**
    * 非同步將商品加入購物車 (`POST /api/cart/items`)。
    * 在 `cart.html` 中動態更新商品數量 (`PUT`) 和刪除商品 (`DELETE`)。
    * 購物車列表會與錢包餘額進行即時比較。
     <img width="1223" height="723" alt="image" src="https://github.com/user-attachments/assets/a38c4699-ae04-4836-9fae-55f2ff40276d" />



* **電子錢包 (Wallet):**
    * 在 `cart.html` 頁面查看錢包餘額。
    * 支援「模擬儲值」(`POST /api/wallet/topup`)。
    <img width="701" height="494" alt="image" src="https://github.com/user-attachments/assets/e2e2fe53-e140-4ef6-86cc-8158bc7995ed" />
    
* **個人資料(ProFile):**
    * 在 `buyer-profile.html` 修改個人資料。
    <img width="1223" height="708" alt="image" src="https://github.com/user-attachments/assets/07846e89-7c81-4018-8b85-ab96606acf40" />

* **結帳 (Checkout):**
    * **[核心架構] 多賣家拆單:** 結帳時 (POST /api/orders/checkout)，後端會自動偵測購物車中來自不同賣家的商品，並將其自動拆分為多張獨立的 OrderPO。
    * 結帳時會同時驗證「商品庫存」和「錢包餘額」。
    * **多元支付:**
    * 內部錢包:檢查餘額後直接扣款。
    * 綠界支付 (New): 支援信用卡/ATM。系統會計算檢查碼並導向綠界頁面，付款成功後透過 Webhook 自動回調後端，觸發建單、扣庫存與賣家入帳流程。
      <table>
        <tr>
          <td valign="top">
            <img width="1105" height="943" alt="image" src="https://github.com/user-attachments/assets/8f9f38ba-bb4d-446d-afd7-e0446103e70e" />
          </td>
          <td valign="top">
            <img width="1272" height="703" alt="image" src="https://github.com/user-attachments/assets/0602085b-bb93-4619-bfe9-037f6a3231f0" />
          </td>
        </tr>
      </table>
* **訂單管理 (Orders):**
    * 在 `orders.html` 瀏覽**所有**歷史訂單列表（`GET /api/orders/me`）。
      <img width="1232" height="823" alt="image" src="https://github.com/user-attachments/assets/49e2ac64-403d-43e4-b27c-737bb46de356" />
    * 在 `order-detail.html` 查看特定訂單的完整詳情，包括商品快照、價格快照和買家資訊。
      <img width="910" height="637" alt="image" src="https://github.com/user-attachments/assets/6fd28ce2-b860-4593-8a93-d379db975fec" />


* **評價系統 (Ratings):**
    * **[核心架構] 已驗證的購買評價:** 買家只能在 `order-detail.html` 頁面對**已購買的訂單項目 (`OrderItemPO`)** 進行評價。
    * 支援**首次建立評價** (`POST /api/ratings`)。
    * 支援**更新/更改評價** (`PUT /api/ratings/{ratingId}`)。
      <table>
        <tr>
          <td valign="top">
            <img width="607" height="496" alt="image" src="https://github.com/user-attachments/assets/16686c24-9c85-4dec-9e7d-d555c9aacd27" />
          </td>
          <td valign="top">
            <img width="608" height="494" alt="image" src="https://github.com/user-attachments/assets/3cae6aa2-313d-4daa-b370-c7dbed783368" />
          </td>
        </tr>
      </table>

### 3.角色：賣家 (SELLER)
* **商品管理:**
    * 在'seller-dashboard.html'上架商品。
    * 點擊已上架商品進行修改/刪除。
      <table>
        <tr>
          <td valign="top">
            <img width="936" height="935" alt="image" src="https://github.com/user-attachments/assets/877ff1fb-e4b9-4fe8-b345-f74ab523bc70" />
          </td>
          <td valign="top">
            <img width="600" height="900" alt="image" src="https://github.com/user-attachments/assets/7baf1eb0-f580-4162-bcb6-4619326b0b8b" />
          </td>
        </tr>
      </table>
* **收款帳戶 (Bank Account):**
    * 在 `seller-account.html` 查看銷售收入餘額&&設定或更新收款用的銀行帳戶。
    * 支援「模擬提款」(`POST /api/wallet/withdraw`)，提款前會驗證是否已設定銀行帳戶。
    * 後端使用 `@Valid` 和 `@Pattern` 驗證帳號格式（例如：8 位數字）。
    * 前端會正確顯示後端回傳的驗證錯誤訊息（例如：「銀行帳號必須為 8 位數字」）。
    * 為安全起見，查詢時後端只回傳**遮罩後**的帳號 (`accountNumberMasked`)。
      <img width="929" height="810" alt="image" src="https://github.com/user-attachments/assets/6ecd7d41-7afe-4822-b7e6-d79a99bf6a17" />
* **訂單管理 (Orders):**
    * 在 `seller-orders.html` 瀏覽**所有「自己收到」**的訂單列表（`GET /api/seller/orders/me`）。
    * 卡片上會清楚顯示**買家是誰**以及該筆訂單包含的**商品項目**。
      <img width="956" height="849" alt="image" src="https://github.com/user-attachments/assets/145b1cf5-5155-4acb-bce2-059faf284ed4" />

* **評價管理 (Ratings):**
    * 在 `seller-ratings.html` 瀏覽**所有「自己商品收到」**的評價（`GET /api/seller/ratings/me`）。
    * 卡片上會清楚顯示**評價的商品**、**買家是誰**、星星數和評論內容。
      <img width="942" height="934" alt="image" src="https://github.com/user-attachments/assets/ad5c443e-7d04-4efb-89b3-8d8cd4906f05" />
      
### 4.角色：管理員 (ADMIN)
* **登入:**
  * 使用管理員編號登入。
  <img width="510" height="489" alt="image" src="https://github.com/user-attachments/assets/fe736ae0-d1de-45be-b5b1-006162699337" />
* **營運報表:**
    * 在 `admin-dashboard.html` 檢視訂單金額區間、新會員比例、熱銷商品類別 Top 5跟財務報表。
     <table>
        <tr>
          <td valign="top">
            <img width="1904" height="957" alt="image" src="https://github.com/user-attachments/assets/48d48ae4-31fa-4693-afab-628a139a39d2" />
          </td>
          <td valign="top">
            <img width="1440" height="935" alt="image" src="https://github.com/user-attachments/assets/97f8f77a-0aa8-4f5c-aab6-3f7777522265" />
          </td>
        </tr>
      </table>
---

## 🛠️ 技術棧 (Technology Stack)

| 類別 | 技術 |
| :--- | :--- |
| **後端 (Backend)** | Java, Spring Boot, Spring Security (JWT), JPA, ECPay AIO SDK Integration |
| **前端 (Frontend)** | Vanilla JavaScript (ES6+ Async/Await, Fetch API), HTML5, CSS3 |
| **資料庫 (Database)** | MySQL |
| **驗證 (Validation)** | `jakarta.validation` ( ` @Valid`, `@Pattern` ) |
| **Java 核心** | POJO (實體), VO (資料傳輸), DAO (儲存庫), Service, Controller 分層架構 |

---

## 🏛️ 專案架構 (Architecture)

### API 端點結構

| 路徑 | 控制器 | 目的 |
| :--- | :--- | :--- |
| `/api/auth/**` | `AuthController` | 處理所有登入、註冊和 Token |
| `/api/public/**` | `PublicProductController` | 公開的商品和評價查詢 |
| `/api/profile/me` | `ProfileController` | （需登入）獲取/更新個人資料 |
| `/api/wallet/**` | `WalletController` | （需登入）錢包餘額、儲值/提款 |
| `/api/cart/**` | `CartController` | （買家） 購物車管理 |
| `/api/orders/**` | `OrderController` | （買家） 結帳與訂單查詢 |
| `/api/ratings/**` | `RatingController` | （買家） 新增/更新評價 |
| `/api/seller/account` | `BankAccountController` | （賣家） 收款帳戶管理 |
| `/api/seller/orders/**` | `SellerOrderController` | （賣家） 查詢收到的訂單 |
| `/api/seller/ratings/**` | `SellerRatingController` | （賣家） 查詢收到的評價 |
| `/notify` | `NotifyController` | 開放給綠界伺服器呼叫 (無 Token)|
| `/createOrder` | `ECPayController` | 建立訂單 API|
| `/api/admin/**` | `AdminReportController` | 管理員 API |
---

## 🗃️ 資料庫架構 (Database Schema)

本專案圍繞著 `users`, `products`, 和 `orders` 三大核心實體構建。

<img width="937" height="880" alt="image" src="https://github.com/user-attachments/assets/7f13b847-d0c1-4093-aab2-a2e7258f14ae" />

### 資料表介紹 (Table Definitions)

#### 1. 使用者 & 認證 (User & Auth)
* **`users`**: 核心使用者表,`role` 欄位 ('BUYER' / 'SELLER' / 'ADMIN') 用於區分角色,'admin_code'用來存放管理員編號。
* **`password_reset_tokens`**: 存放「忘記密碼」時產生的一次性 Token。關聯 `user_id`。

#### 2. 商品 & 分類 (Product & Catalog)
* **`categories`**: 商品分類表。`parent_category_id` 欄位（關聯自己）用於實現**樹狀**分類結構。
* **`products`**: 商品主表。關聯 `seller_id` (賣家) 和 `category_id` (分類)。
* **`product_ratings`**: 商品評價表。(關鍵) 它**不**直接關聯 `products`，而是關聯 `order_item_id`，以確保只有**已購買**的買家才能評價。

#### 3. 購物車 & 訂單 (Cart & Order)
* **`carts`**: 購物車主表。每個買家 (`user_id`) 擁有一個購物車 (一對一)。
* **`cart_items`**: 購物車項目表。存放當前在購物車中的商品 (`product_id`) 和數量。
* **`orders`**: 訂單主表。(關鍵) 包含 `buyer_id` 和 `seller_id`，用於實現「多賣家拆單」。
* **`order_items`**: 訂單項目表。這是訂單成立時的「商品快照」，儲存了當時的 `price_per_unit` (單價快照)，確保歷史價格不會變動。

#### 4. 金流 & 帳戶 (Wallet & Finance)
* **`wallets`**: 電子錢包。每個 `user_id` (無論買家或賣家) 都有一個錢包 (一對一)，用於儲存 `balance` (餘額)。
* **`wallet_transactions`**: 錢包交易紀錄。`wallets` 的流水帳，記錄 `TOPUP`, `PAYMENT` (支付), `WITHDRAWAL` (提款) 等所有變動。
* **`bank_accounts`**: 賣家銀行帳戶。`user_id` (賣家) 用於提款的銀行資料 (一對一)。

---

## 🚀 如何開始 (Getting Started)

### 必備環境 (Prerequisites)
* Java JDK (建議 17 或 21)
* Apache Maven
* MySQL (或相容的資料庫)
* Eclipse (安裝lombok)
* VS Code (或任何用於前端的編輯器，並安裝 `Live Server` 擴充功能)

### 1. 後端 (Spring Boot)

1.  **設定資料庫:**
    * 在您的 MySQL 中建立一個新的資料庫 (Schema)，名稱為 `bigwork`。
    * (關鍵) 找到專案中的 `SQL資料夾`，並執行內部的 `.sql` 腳本以匯入資料表結構與範例資料。
    
2.  **設定 Spring Boot 專案:**
    * 在 Eclipse (或 IDE) 中「Import」 `bigwork` 專案 (作為一個 Maven 專案)。
    * (關鍵) 進入 `src/main/resources/application.properties`。
    * 更改 `spring.datasource.url`, `username`, `password` 以符合您本地的 MySQL 設定。
3.  **啟動後端:**
    * 找到並執行 `BigworkApplication.java` (主程式)。
    * 伺服器將運行在 `http://localhost:8080`跟`http://localhost:5500`。

### 2. 前端 (HTML/JS)
1.  **不需安裝:** 這是一個靜態網頁專案。
2.  **開啟檔案:**
    * 在 VS Code 中打開 `frontend-html` 資料夾。
    * 在 `index.html` 上按右鍵，選擇 `Open with Live Server`。
    * （推薦：使用 `Live Server` 來避免 CORS 跨域問題）。

3.  **開始使用:**
    * 您的瀏覽器將自動打開 `http://127.0.0.1:5500/html/index.html`。
    * 點擊「註冊」並分別建立一個 `BUYER` 和一個 `SELLER` 帳號即可開始測試所有功能。

### 3. 設定綠界金流與 Ngrok(先安裝ngrok.exe):

1. **開啟Ngrok:**
輸入:'ngrok http --domain=gayla-unbriefed-unreluctantly.ngrok-free.dev 8080'
<img width="965" height="512" alt="image" src="https://github.com/user-attachments/assets/47617845-a9d4-4cd1-979f-8a93da773752" />

2. **檢查 application.properties:**確保您的設定檔中包含正確的測試帳號資訊與 Ngrok 回傳網址：
```
# --- ECPay 綠界金流設定 ---
ecpay.merchantId=3002607
ecpay.hashKey=pwFHCqoQZGmho4w6
ecpay.hashIV=EkRm7iFT261dpevs
ecpay.serviceUrl=https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5
# 在本地開發時，不能寫 localhost，必須用 Ngrok
ecpay.return.url=https://gayla-unbriefed-unreluctantly.ngrok-free.dev/notify
# 支付成功後，使用者點擊「返回商店」會跳轉到的前端頁面
ecpay.client.back.url=http://127.0.0.1:5500/html/cart.html
```

注意：每次測試都要讓Ngrok保持開啟狀態，不然後端收不到綠界回傳的資料。
---
## 📄 授權 (License)

本專案採用 MIT 授權。




