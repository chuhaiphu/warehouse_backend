-- Sample Data SQL Script for Warehouse Management System
-- PostgreSQL Database

-- Insert Configuration data
INSERT INTO configuration (
    working_time_start, 
    working_time_end, 
    create_request_time_at_least, 
    time_to_allow_assign, 
    time_to_allow_confirm, 
    time_to_allow_cancel, 
    days_to_al_alow_extend, 
    max_allowed_days_for_extend, 
    max_allowed_days_for_import_request_process
) VALUES (
    '07:00:00',
    '17:00:00', 
    '04:00:00',
    '02:00:00',
    '01:00:00',
    '23:00:00',
    7,
    7,
    30
);

-- Insert Categories
INSERT INTO category (name, description) VALUES 
('Vải', 'Các loại cuộn vải'),
('Nút', 'Các loại nút quần áo'),
('Chỉ may', 'Các loại chỉ may'),
('Kim may', 'Các loại kim may'),
('Khóa kéo', 'Các loại khóa kéo'),
('Phụ liệu khác', 'Các loại phụ liệu may mặc khác');

-- Insert Providers (Original 6 + 40 additional providers = 46 total)
INSERT INTO provider (name, phone, address) VALUES 
-- Original 6 providers
('Công ty Dệt may Phong Phú', '02838131767', '48 Tăng Nhơn Phú, TP.HCM'),
('Công ty CP Dệt may Nam Định', '02283649555', '43 Tô Hiệu, Nam Định'),
('Công ty TNHH Phụ liệu may Việt Nam', '02438245688', 'Số 7 Lê Văn Lương, Hà Nội'),
('Công ty TNHH Thành Công', '02838257699', '36 Tây Thạnh, TP.HCM'),
('Công ty TNHH YKK Việt Nam', '0251383699', 'KCN Biên Hòa, Đồng Nai'),
('Công ty TNHH Phụ liệu may Phương Nam', '0283832145', 'Q.Tân Bình, TP.HCM'),

-- First batch of 20 additional providers (7-26)
('Công ty CP Dệt may Hà Nội', '02438761234', '123 Nguyễn Trãi, Hà Nội'),
('Công ty TNHH Vải cao cấp Sài Gòn', '02839876543', '456 Lê Lợi, TP.HCM'),
('Công ty Dệt may Đông Á', '02513567890', '789 Trần Hưng Đạo, Biên Hòa'),
('Công ty TNHH Phụ kiện Thời trang', '02438123456', '321 Hoàng Diệu, Hà Nội'),
('Công ty CP May mặc Bình Dương', '02743654321', '654 Dĩ An, Bình Dương'),
('Công ty TNHH Chỉ may Á Châu', '02838987654', '987 Phạm Văn Đồng, TP.HCM'),
('Công ty Dệt Kim Hà Nam', '02263456789', '111 Lý Thường Kiệt, Hà Nam'),
('Công ty TNHH Nút áo Việt Tiến', '02838234567', '222 Nguyễn Văn Cừ, TP.HCM'),
('Công ty CP Khóa kéo Đông Nam Á', '02513789012', '333 Võ Thị Sáu, Đồng Nai'),
('Công ty TNHH Vải jean Cao Bằng', '02693456781', '444 Hồ Chí Minh, Cao Bằng'),
('Công ty Dệt may Hải Phòng', '02253678901', '555 Lạch Tray, Hải Phòng'),
('Công ty TNHH Phụ liệu Quốc tế', '02438456789', '666 Cầu Giấy, Hà Nội'),
('Công ty CP Chỉ thêu Nghệ An', '02383567890', '777 Lê Hồng Phong, Nghệ An'),
('Công ty TNHH Kim may Precision', '02838567891', '888 Lê Văn Việt, TP.HCM'),
('Công ty Dệt may Thái Bình', '02273678902', '999 Trần Phú, Thái Bình'),
('Công ty TNHH Vải thun Elastic', '02438678903', '1010 Kim Mã, Hà Nội'),
('Công ty CP Nút nhựa Polymer', '02513789013', '1111 Nguyễn Ái Quốc, Đồng Nai'),
('Công ty TNHH Khóa cao cấp Premium', '02838890123', '1212 Đinh Tiên Hoàng, TP.HCM'),
('Công ty Dệt may Quảng Ninh', '02033901234', '1313 Hạ Long, Quảng Ninh'),
('Công ty TNHH Phụ kiện Fashion Plus', '02438012345', '1414 Tây Hồ, Hà Nội'),
('Công ty CP Vải cotton Natural', '02743123456', '1515 Thuận An, Bình Dương'),
('Công ty TNHH Chỉ may Professional', '02838234567', '1616 Gò Vấp, TP.HCM'),
('Công ty Dệt may Vĩnh Long', '02703345678', '1717 Nguyễn Huệ, Vĩnh Long'),
('Công ty TNHH Kim may Industrial', '02513456789', '1818 Long Thành, Đồng Nai'),
('Công ty CP Khóa kéo Universal', '02838567890', '1919 Tân Bình, TP.HCM'),
('Công ty TNHH Phụ liệu Tổng hợp', '02438678901', '2020 Đống Đa, Hà Nội'),

-- Second batch of 20 additional providers (27-46)
('Công ty CP Dệt may Tân Thành', '02903456789', '2121 Tân Thành, Tiền Giang'),
('Công ty TNHH Vải Polyester Đại Việt', '02838345678', '2222 Quận 7, TP.HCM'),
('Công ty Dệt Kim An Giang', '02963567890', '2323 An Giang'),
('Công ty TNHH Chỉ may Coats Việt Nam', '02438567890', '2424 Long Biên, Hà Nội'),
('Công ty CP Nút kim loại Precision', '02513678901', '2525 Biên Hòa, Đồng Nai'),
('Công ty TNHH Khóa kéo Riri Việt Nam', '02838789012', '2626 Bình Tân, TP.HCM'),
('Công ty Dệt may Đức Giang', '02433890123', '2727 Gia Lâm, Hà Nội'),
('Công ty TNHH Vải kate Import Export', '02838901234', '2828 Quận 1, TP.HCM'),
('Công ty CP Chỉ synthetic Polytech', '02743012345', '2929 Bình Dương'),
('Công ty TNHH Kim may Groz Beckert VN', '02513123456', '3030 Long Thành, Đồng Nai'),
('Công ty Dệt may Hưng Yên', '02213234567', '3131 Hưng Yên'),
('Công ty TNHH Nút nhựa Polycom', '02838345678', '3232 Quận 12, TP.HCM'),
('Công ty CP Khóa kéo SBS Việt Nam', '02513456789', '3333 Nhơn Trạch, Đồng Nai'),
('Công ty TNHH Vải Jean Premium', '02438567890', '3434 Ba Đình, Hà Nội'),
('Công ty Dệt may Bắc Giang', '02043678901', '3535 Bắc Giang'),
('Công ty TNHH Chỉ may Madeira VN', '02838789012', '3636 Thủ Đức, TP.HCM'),
('Công ty CP Kim may Schmetz VN', '02513890123', '3737 Đồng Nai'),
('Công ty TNHH Khóa kéo Lampo VN', '02838901234', '3838 Bình Thạnh, TP.HCM'),
('Công ty Dệt may Nam Định 2', '02283012345', '3939 Nam Định'),
('Công ty TNHH Vải thun Lycra VN', '02743123456', '4040 Thuận An, Bình Dương'),
('Công ty CP Phụ liệu cao cấp Elite', '02438234567', '4141 Cầu Giấy, Hà Nội'),
('Công ty TNHH Nút gỗ tự nhiên Wood Pro', '02703345678', '4242 Cần Thơ'),
('Công ty Dệt may Textile Innovation', '02838456789', '4343 Quận 9, TP.HCM'),
('Công ty TNHH Chỉ may Gutermann VN', '02513567890', '4444 Biên Hòa, Đồng Nai');

-- Insert Items
INSERT INTO item (
    id, name, description, measurement_unit, total_measurement_value, measurement_value, 
    quantity, unit_type, days_until_due, minimum_stock_quantity, maximum_stock_quantity, 
    counting_minutes, configuration_id, category_id
) VALUES 
-- Vải từ Provider 1 và 2
('VAI-KT-001', 'Vải Kate', 'Vải kate 65/35', 'mét', 1500.0, 1.0, 1500, 'Vải', 730, 100, 2000, 5, 1, 1),
('VAI-KK-001', 'Vải Kaki', 'Vải kaki thun', 'mét', 1000.0, 1.0, 1000, 'Vải', 730, 150, 2500, 5, 1, 1),
('VAI-JE-001', 'Vải Jean', 'Vải jean 100% cotton', 'mét', 2000.0, 1.0, 2000, 'Vải', 730, 200, 3000, 5, 1, 1),
('VAI-TH-001', 'Vải Thun', 'Vải thun cotton 4 chiều', 'mét', 800.0, 1.0, 800, 'Vải', 730, 100, 1500, 5, 1, 1),

-- Nút từ Provider 3 và 6
('NUT-NH-001', 'Nút nhựa 4 lỗ', 'Nút nhựa màu trắng', 'bịch', 100.0, 1.0, 100, 'Nút', 365, 20, 200, 5, 1, 2),
('NUT-KL-001', 'Nút kim loại', 'Nút jean kim loại', 'bịch', 50.0, 1.0, 50, 'Nút', 365, 10, 100, 5, 1, 2),
('NUT-VS-001', 'Nút áo vest', 'Nút áo vest cao cấp', 'bịch', 30.0, 1.0, 30, 'Nút', 365, 5, 50, 5, 1, 2),
('NUT-GO-001', 'Nút gỗ', 'Nút gỗ tự nhiên', 'bịch', 40.0, 1.0, 40, 'Nút', 365, 8, 80, 5, 1, 2),

-- Chỉ từ Provider 3 và 4
('CHI-PL-001', 'Chỉ polyester', 'Chỉ may polyester 40/2', 'cuộn', 200.0, 1.0, 200, 'Chỉ', 730, 50, 500, 5, 1, 3),
('CHI-CT-001', 'Chỉ cotton', 'Chỉ may cotton 100%', 'cuộn', 150.0, 1.0, 150, 'Chỉ', 730, 30, 300, 5, 1, 3),
('CHI-JE-001', 'Chỉ jean', 'Chỉ may jean đặc biệt', 'cuộn', 100.0, 1.0, 100, 'Chỉ', 730, 20, 200, 5, 1, 3),
('CHI-TH-001', 'Chỉ thêu', 'Chỉ thêu đa màu', 'cuộn', 80.0, 1.0, 80, 'Chỉ', 730, 15, 150, 5, 1, 3),

-- Kim từ Provider 3 và 6
('KIM-TH-001', 'Kim may thường', 'Kim may size 90/14', 'hộp', 50.0, 1.0, 50, 'Kim', 365, 10, 100, 5, 1, 4),
('KIM-JE-001', 'Kim may jean', 'Kim may jean size 100/16', 'hộp', 40.0, 1.0, 40, 'Kim', 365, 8, 80, 5, 1, 4),
('KIM-DA-001', 'Kim may da', 'Kim may da đặc biệt', 'hộp', 30.0, 1.0, 30, 'Kim', 365, 5, 50, 5, 1, 4),
('KIM-TH-002', 'Kim thêu', 'Kim thêu các loại', 'hộp', 25.0, 1.0, 25, 'Kim', 365, 5, 40, 5, 1, 4),

-- Khóa kéo từ Provider 5
('KHO-NH-001', 'Khóa kéo nhựa', 'Khóa kéo nhựa 20cm', 'bịch', 100.0, 1.0, 100, 'Khóa', 365, 20, 200, 5, 1, 5),
('KHO-KL-001', 'Khóa kéo kim loại', 'Khóa kéo kim loại 15cm', 'bịch', 80.0, 1.0, 80, 'Khóa', 365, 15, 150, 5, 1, 5),
('KHO-JE-001', 'Khóa kéo jean', 'Khóa kéo jean YKK', 'bịch', 60.0, 1.0, 60, 'Khóa', 365, 10, 100, 5, 1, 5),
('KHO-AK-001', 'Khóa kéo áo khoác', 'Khóa kéo 2 chiều', 'bịch', 40.0, 1.0, 40, 'Khóa', 365, 8, 80, 5, 1, 5);

-- Insert Provider-Item relationships (Many-to-Many) - Realistic distribution based on textile industry
INSERT INTO provider_item (provider_id, item_id) VALUES 

-- VAI-KT-001 (Vải Kate - Popular fabric, many suppliers)
(1, 'VAI-KT-001'), (2, 'VAI-KT-001'), (7, 'VAI-KT-001'), (8, 'VAI-KT-001'), 
(16, 'VAI-KT-001'), (21, 'VAI-KT-001'), (27, 'VAI-KT-001'), (28, 'VAI-KT-001'), 
(34, 'VAI-KT-001'), (39, 'VAI-KT-001'), (43, 'VAI-KT-001'),

-- VAI-KK-001 (Vải Kaki - Popular workwear fabric)
(1, 'VAI-KK-001'), (9, 'VAI-KK-001'), (10, 'VAI-KK-001'), (21, 'VAI-KK-001'), 
(28, 'VAI-KK-001'), (29, 'VAI-KK-001'), (34, 'VAI-KK-001'), (43, 'VAI-KK-001'),

-- VAI-JE-001 (Vải Jean - Very popular, many suppliers)
(1, 'VAI-JE-001'), (2, 'VAI-JE-001'), (10, 'VAI-JE-001'), (11, 'VAI-JE-001'), 
(16, 'VAI-JE-001'), (21, 'VAI-JE-001'), (28, 'VAI-JE-001'), (33, 'VAI-JE-001'), 
(39, 'VAI-JE-001'), (40, 'VAI-JE-001'), (43, 'VAI-JE-001'),

-- VAI-TH-001 (Vải Thun - Popular stretch fabric)
(2, 'VAI-TH-001'), (16, 'VAI-TH-001'), (22, 'VAI-TH-001'), (28, 'VAI-TH-001'), 
(29, 'VAI-TH-001'), (46, 'VAI-TH-001'), (43, 'VAI-TH-001'),

-- NUT-NH-001 (Nút nhựa - Common, many suppliers)
(3, 'NUT-NH-001'), (6, 'NUT-NH-001'), (8, 'NUT-NH-001'), (14, 'NUT-NH-001'), 
(17, 'NUT-NH-001'), (23, 'NUT-NH-001'), (31, 'NUT-NH-001'), (38, 'NUT-NH-001'),

-- NUT-KL-001 (Nút kim loại - Specialized)
(3, 'NUT-KL-001'), (14, 'NUT-KL-001'), (24, 'NUT-KL-001'), (31, 'NUT-KL-001'), (38, 'NUT-KL-001'),

-- NUT-VS-001 (Nút áo vest - High-end, fewer suppliers)
(2, 'NUT-VS-001'), (6, 'NUT-VS-001'), (14, 'NUT-VS-001'), (20, 'NUT-VS-001'), (41, 'NUT-VS-001'),

-- NUT-GO-001 (Nút gỗ - Natural, specialized)
(6, 'NUT-GO-001'), (17, 'NUT-GO-001'), (26, 'NUT-GO-001'), (42, 'NUT-GO-001'),

-- CHI-PL-001 (Chỉ polyester - Very common, many suppliers)
(3, 'CHI-PL-001'), (4, 'CHI-PL-001'), (6, 'CHI-PL-001'), (12, 'CHI-PL-001'), 
(22, 'CHI-PL-001'), (27, 'CHI-PL-001'), (30, 'CHI-PL-001'), (35, 'CHI-PL-001'), 
(40, 'CHI-PL-001'), (44, 'CHI-PL-001'),

-- CHI-CT-001 (Chỉ cotton - Natural fiber, good suppliers)
(4, 'CHI-CT-001'), (21, 'CHI-CT-001'), (23, 'CHI-CT-001'), (27, 'CHI-CT-001'), 
(30, 'CHI-CT-001'), (40, 'CHI-CT-001'), (44, 'CHI-CT-001'),

-- CHI-JE-001 (Chỉ jean - Specialized for denim)
(3, 'CHI-JE-001'), (4, 'CHI-JE-001'), (12, 'CHI-JE-001'), (13, 'CHI-JE-001'), 
(30, 'CHI-JE-001'), (40, 'CHI-JE-001'),

-- CHI-TH-001 (Chỉ thêu - Decorative, specialized)
(3, 'CHI-TH-001'), (13, 'CHI-TH-001'), (19, 'CHI-TH-001'), (22, 'CHI-TH-001'), 
(30, 'CHI-TH-001'), (40, 'CHI-TH-001'),

-- KIM-TH-001 (Kim may thường - Common, many suppliers)
(3, 'KIM-TH-001'), (14, 'KIM-TH-001'), (20, 'KIM-TH-001'), (24, 'KIM-TH-001'), 
(32, 'KIM-TH-001'), (37, 'KIM-TH-001'), (41, 'KIM-TH-001'),

-- KIM-JE-001 (Kim may jean - Specialized for denim)
(3, 'KIM-JE-001'), (6, 'KIM-JE-001'), (14, 'KIM-JE-001'), (24, 'KIM-JE-001'), 
(32, 'KIM-JE-001'), (37, 'KIM-JE-001'),

-- KIM-DA-001 (Kim may da - Specialized for leather)
(3, 'KIM-DA-001'), (6, 'KIM-DA-001'), (14, 'KIM-DA-001'), (32, 'KIM-DA-001'), (37, 'KIM-DA-001'),

-- KIM-TH-002 (Kim thêu - Embroidery needles)
(6, 'KIM-TH-002'), (14, 'KIM-TH-002'), (20, 'KIM-TH-002'), (24, 'KIM-TH-002'), 
(32, 'KIM-TH-002'), (37, 'KIM-TH-002'),

-- KHO-NH-001 (Khóa kéo nhựa - Common, multiple brands)
(5, 'KHO-NH-001'), (9, 'KHO-NH-001'), (15, 'KHO-NH-001'), (18, 'KHO-NH-001'), 
(25, 'KHO-NH-001'), (33, 'KHO-NH-001'), (36, 'KHO-NH-001'),

-- KHO-KL-001 (Khóa kéo kim loại - Quality brands)
(5, 'KHO-KL-001'), (6, 'KHO-KL-001'), (15, 'KHO-KL-001'), (18, 'KHO-KL-001'), 
(25, 'KHO-KL-001'), (33, 'KHO-KL-001'), (36, 'KHO-KL-001'),

-- KHO-JE-001 (Khóa kéo jean - Premium brands)
(5, 'KHO-JE-001'), (9, 'KHO-JE-001'), (25, 'KHO-JE-001'), (33, 'KHO-JE-001'), 
(36, 'KHO-JE-001'),

-- KHO-AK-001 (Khóa kéo áo khoác - Technical, fewer suppliers)
(5, 'KHO-AK-001'), (18, 'KHO-AK-001'), (25, 'KHO-AK-001'), (36, 'KHO-AK-001');

-- Insert Stored Locations
INSERT INTO stored_location (zone, floor, row, batch, is_used, is_fulled, maximum_capacity_for_item, current_capacity, item_id) VALUES 
-- Section A (Vải)
('A', '1', '1', 'A101', false, false, 500, 0, 'VAI-KT-001'),
('A', '1', '2', 'A102', false, false, 500, 0, 'VAI-JE-001'),
('A', '2', '1', 'A201', false, false, 500, 0, 'VAI-KK-001'),
('A', '2', '2', 'A202', false, false, 500, 0, 'VAI-TH-001'),

-- Section B (Nút)
('B', '1', '1', 'B101', false, false, 500, 0, 'NUT-NH-001'),
('B', '1', '2', 'B102', false, false, 500, 0, 'NUT-KL-001'),
('B', '2', '1', 'B201', false, false, 500, 0, 'NUT-VS-001'),
('B', '2', '2', 'B202', false, false, 500, 0, 'NUT-GO-001'),

-- Section C (Chỉ)
('C', '1', '1', 'C101', false, false, 500, 0, 'CHI-PL-001'),
('C', '1', '2', 'C102', false, false, 500, 0, 'CHI-CT-001'),
('C', '2', '1', 'C201', false, false, 500, 0, 'CHI-JE-001'),
('C', '2', '2', 'C202', false, false, 500, 0, 'CHI-TH-001'),

-- Section D (Kim)
('D', '1', '1', 'D101', false, false, 500, 0, 'KIM-TH-001'),
('D', '1', '2', 'D102', false, false, 500, 0, 'KIM-JE-001'),
('D', '2', '1', 'D201', false, false, 500, 0, 'KIM-DA-001'),
('D', '2', '2', 'D202', false, false, 500, 0, 'KIM-TH-002'),

-- Section E (Khóa kéo)
('E', '1', '1', 'E101', false, false, 500, 0, 'KHO-NH-001'),
('E', '1', '2', 'E102', false, false, 500, 0, 'KHO-KL-001'),
('E', '2', '1', 'E201', false, false, 500, 0, 'KHO-JE-001'),
('E', '2', '2', 'E202', false, false, 500, 0, 'KHO-AK-001'),

-- Section F (Additional storage for duplicated items)
('F', '1', '1', 'F101', false, false, 500, 0, 'KHO-NH-001'),
('F', '1', '2', 'F102', false, false, 500, 0, 'KHO-KL-001'),
('F', '2', '1', 'F201', false, false, 500, 0, 'KHO-JE-001'),
('F', '2', '2', 'F202', false, false, 500, 0, 'KHO-AK-001');

-- Reset sequences to ensure proper auto-increment values
SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));
SELECT setval('provider_id_seq', (SELECT MAX(id) FROM provider));
SELECT setval('configuration_id_seq', (SELECT MAX(id) FROM configuration));
SELECT setval('stored_location_id_seq', (SELECT MAX(id) FROM stored_location));

COMMIT; 