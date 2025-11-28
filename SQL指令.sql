-- 建立資料庫
CREATE DATABASE storedb
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 使用資料庫
USE storedb;

-- 建立 users 資料表
CREATE TABLE users (
    account  VARCHAR(50)  NOT NULL PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    balance  INT NOT NULL DEFAULT 0
);

-- 測試插入資料
INSERT INTO users (account, password, balance)
VALUES ('eric', '1234', 1000);
-- 刪除測試資料
DELETE FROM users WHERE account = 'eric';


-- 查詢資料
SELECT * FROM users;





