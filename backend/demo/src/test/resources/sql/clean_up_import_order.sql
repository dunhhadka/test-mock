DELETE FROM order_product;

DELETE FROM history_product;
DELETE FROM products;

-- Xóa dữ liệu theo thứ tự khóa ngoại để tránh lỗi FK constraint
DELETE FROM order_service;
DELETE FROM services;
DELETE FROM orders;
DELETE FROM accounts;
