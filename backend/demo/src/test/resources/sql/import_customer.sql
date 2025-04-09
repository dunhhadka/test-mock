INSERT INTO accounts (id, user_name, email, password, role, is_active, create_date, modify_date)
VALUES (1, 'testUser', 'test@example.com', 'encodedOldPassword', 'DISPATCHER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO customers (id, name, phone, email, address, is_active, create_date, modify_date, create_by, modify_by)
VALUES (1, 'Test Customer', '1234567890', 'test@example.com', 'Test Address', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);