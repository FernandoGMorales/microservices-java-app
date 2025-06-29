-- data.sql
-- This file will be executed by Spring Boot to pre-populate the H2 in-memory database.

-- Insert initial users
INSERT INTO users (id, username) VALUES (1, 'john_doe');
INSERT INTO users (id, username) VALUES (2, 'jane_smith');
INSERT INTO users (id, username) VALUES (3, 'admin_user');

-- Insert initial products
-- Using ON CONFLICT(id) DO NOTHING for idempotency in case of multiple executions
INSERT INTO products (id, code, name, price, category) VALUES (101, 'PROD001', 'Laptop', 1200.00, 'Electronics');
INSERT INTO products (id, code, name, price, category) VALUES (102, 'PROD002', 'Smartphone', 800.00, 'Electronics');
INSERT INTO products (id, code, name, price, category) VALUES (103, 'PROD003', 'Java Programming Book', 50.00, 'Books');
INSERT INTO products (id, code, name, price, category) VALUES (104, 'PROD004', 'Coffee Mug', 15.00, 'HomeGoods');
INSERT INTO products (id, code, name, price, category) VALUES (105, 'PROD005', 'Mechanical Keyboard', 150.00, 'Electronics');

-- Insert initial discounts
INSERT INTO discounts (id, category, percentage) VALUES (1, 'Electronics', 10.00);
INSERT INTO discounts (id, category, percentage) VALUES (2, 'Books', 5.00);
-- No discount for 'HomeGoods' category currently, to show scenario where no discount applies.
