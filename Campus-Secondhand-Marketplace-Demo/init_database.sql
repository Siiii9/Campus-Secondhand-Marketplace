CREATE DATABASE IF NOT EXISTS campus_market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_market;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    city VARCHAR(50),
    gender VARCHAR(10),
    bank_account VARCHAR(16),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 0,
    merchant_level INT,
    shop_name VARCHAR(100),
    shop_status TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS user_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    business_license VARCHAR(255),
    id_card_front VARCHAR(255),
    id_card_back VARCHAR(255),
    audit_status TINYINT NOT NULL DEFAULT 0,
    audit_remark VARCHAR(255),
    auditor_id BIGINT,
    audit_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id INT DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    category_id INT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    original_price DECIMAL(10,2) NOT NULL,
    discount_price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    unit VARCHAR(20),
    is_negotiable TINYINT NOT NULL DEFAULT 0,
    condition_level VARCHAR(20),
    status TINYINT NOT NULL DEFAULT 0,
    sales_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    avg_rating DECIMAL(2,1),
    audit_status TINYINT NOT NULL DEFAULT 0,
    audit_time DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (merchant_id) REFERENCES user(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE IF NOT EXISTS product_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS merchant_level_config (
    level INT PRIMARY KEY,
    fee_rate DECIMAL(5,4) NOT NULL,
    min_transaction_amount DECIMAL(10,2),
    min_satisfaction DECIMAL(2,1)
);

CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    selected TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    points_deducted INT NOT NULL DEFAULT 0,
    points_deduct_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    actual_paid DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    paid_at DATETIME,
    received_at DATETIME,
    return_deadline DATETIME,
    is_returned TINYINT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (merchant_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS return_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 0,
    audit_time DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    frozen_balance DECIMAL(10,2) NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    points INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS points_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    reason VARCHAR(100),
    order_id BIGINT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (order_id) REFERENCES `order`(id)
);

CREATE TABLE IF NOT EXISTS transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    fee DECIMAL(10,2) NOT NULL DEFAULT 0,
    fee_rate DECIMAL(5,4) NOT NULL,
    net_amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    settled_at DATETIME,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (merchant_id) REFERENCES user(id),
    FOREIGN KEY (buyer_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    review_type VARCHAR(20) NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    product_id BIGINT,
    rating INT NOT NULL,
    content TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (from_user_id) REFERENCES user(id),
    FOREIGN KEY (to_user_id) REFERENCES user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS carousel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL
);

INSERT INTO merchant_level_config (level, fee_rate, min_transaction_amount, min_satisfaction) VALUES
(1, 0.001, 0, 0),
(2, 0.002, 1000, 4.0),
(3, 0.005, 5000, 4.2),
(4, 0.0075, 10000, 4.5),
(5, 0.01, 50000, 4.8);

INSERT INTO category (id, name, parent_id) VALUES
(1, '电子产品', 0),
(2, '手机', 1),
(3, '电脑', 1),
(4, '平板', 1),
(5, '学习用品', 0),
(6, '图书教材', 5),
(7, '文具', 5),
(8, '生活用品', 0),
(9, '衣物', 8),
(10, '化妆品', 8);

INSERT INTO user (id, username, password, real_name, phone, email, city, gender, bank_account, role, status, merchant_level, shop_name, shop_status, created_at) VALUES
(1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '管理员', '13800138000', 'admin@example.com', '北京', '男', '1234567890123456', 'ADMIN', 1, NULL, NULL, NULL, NOW());