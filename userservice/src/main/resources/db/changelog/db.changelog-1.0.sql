-- liquibase formatted sql

-- changeset your_name:1
CREATE TABLE Rooms (
    room_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_vip BOOLEAN NOT NULL DEFAULT FALSE
);
-- rollback DROP TABLE Rooms;

-- changeset your_name:2
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    wallet DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    photo_path VARCHAR(500),
    bonus_coins INTEGER NOT NULL DEFAULT 0,
    registration_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    birthday_date DATE,
    last_login TIMESTAMP,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE
);
-- rollback DROP TABLE Users;

-- changeset your_name:3
CREATE TABLE PC (
    pc_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    room_id INTEGER NOT NULL,
    CPU VARCHAR(100) NOT NULL,
    GPU VARCHAR(100) NOT NULL,
    RAM VARCHAR(100) NOT NULL,
    monitor VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_pc_room FOREIGN KEY (room_id) REFERENCES Rooms(room_id)
);
-- rollback DROP TABLE PC;

-- changeset your_name:4
CREATE TABLE Tariff (
    tariff_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price_per_hour DECIMAL(10,2) NOT NULL,
    is_vip BOOLEAN NOT NULL DEFAULT FALSE
);
-- rollback DROP TABLE Tariff;

-- changeset your_name:5
CREATE TABLE Category (
    category_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);
-- rollback DROP TABLE Category;

-- changeset your_name:6
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category_id INTEGER NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES Category(category_id)
);
-- rollback DROP TABLE Products;

-- changeset your_name:7
CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL
);
-- rollback DROP TABLE Orders;

-- changeset your_name:8
CREATE TABLE Order_Item (
    order_item_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES Products(product_id)
);
-- rollback DROP TABLE Order_Item;

-- changeset your_name:9
CREATE TABLE Session (
    session_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    pc_id INTEGER NOT NULL,
    tariff_id INTEGER NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    total_coast DECIMAL(10,2),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT fk_session_pc FOREIGN KEY (pc_id) REFERENCES PC(pc_id),
    CONSTRAINT fk_session_tariff FOREIGN KEY (tariff_id) REFERENCES Tariff(tariff_id)
);
-- rollback DROP TABLE Session;

-- changeset your_name:10
CREATE INDEX idx_users_login ON Users(login);
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_pc_room_id ON PC(room_id);
CREATE INDEX idx_products_category ON Products(category_id);
CREATE INDEX idx_order_items_order ON Order_Item(order_id);
CREATE INDEX idx_session_user ON Session(user_id);
CREATE INDEX idx_session_pc ON Session(pc_id);
CREATE INDEX idx_session_tariff ON Session(tariff_id);
-- rollback DROP INDEX idx_users_login, idx_users_email, idx_pc_room_id, idx_products_category, idx_order_items_order, idx_session_user, idx_session_pc, idx_session_tariff;
