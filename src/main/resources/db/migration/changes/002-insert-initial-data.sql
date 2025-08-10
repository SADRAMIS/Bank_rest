-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, role, enabled) 
VALUES ('admin', 'admin@bank.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', true);

-- Insert test user (password: user123)
INSERT INTO users (username, email, password, role, enabled) 
VALUES ('user', 'user@bank.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER', true);
