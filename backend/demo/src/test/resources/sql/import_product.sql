-- Thêm sản phẩm vào bảng products
INSERT INTO products (code, name, price_in, price_out, brand, description, storage_quantity, quantity_warning, image_url, unit, is_active, create_date, modify_date)
VALUES
    ('PROD001', 'Sản phẩm A', 50000, 70000, 'BrandX', 'Mô tả sản phẩm A', 100, 20, 'https://example.com/imageA.jpg', 'Hộp', TRUE, NOW(), NOW()),
    ('PROD002', 'Sản phẩm B', 30000, 45000, 'BrandY', 'Mô tả sản phẩm B', 50, 10, 'https://example.com/imageB.jpg', 'Chai', TRUE, NOW(), NOW()),
    ('PROD003', 'Sản phẩm C', 15000, 25000, 'BrandZ', 'Mô tả sản phẩm C', 5, 3, 'https://example.com/imageC.jpg', 'Gói', TRUE, NOW(), NOW());

-- Thêm lịch sử nhập xuất kho
INSERT INTO history_product (product_id, difference, note, quantity_left, action, product_name, price_in, price_out, create_date)
VALUES
    ((SELECT id FROM products WHERE code = 'PROD001'), 100, 'Nhập kho lần đầu', 100, 'IMPORT', 'Sản phẩm A', 50000, 70000, NOW()),
    ((SELECT id FROM products WHERE code = 'PROD002'), 50, 'Nhập kho lần đầu', 50, 'IMPORT', 'Sản phẩm B', 30000, 45000, NOW()),
    ((SELECT id FROM products WHERE code = 'PROD003'), 5, 'Nhập kho lần đầu', 5, 'IMPORT', 'Sản phẩm C', 15000, 25000, NOW());
