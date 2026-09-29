INSERT INTO users (id, name, email, password_hash, role)
 VALUES 
 (1, 'customer1', 'customer1@gmail.com', '$2a$10$qmFWoU5Mmr76VDzo1v3/0.T3oYTbQ3kVdGsaYBPuhlFksnljHisu.', 'CUSTOMER'),
 (2, 'stylist1', 'stylist1@gmail.com', '$2a$10$qmFWoU5Mmr76VDzo1v3/0.T3oYTbQ3kVdGsaYBPuhlFksnljHisu.', 'STYLIST'),
 (3, 'stylist2', 'stylist2@gmail.com', '$2a$10$qmFWoU5Mmr76VDzo1v3/0.T3oYTbQ3kVdGsaYBPuhlFksnljHisu.', 'STYLIST'),
 (4, 'admin1', 'admin1@gmail.com', '$2a$10$qmFWoU5Mmr76VDzo1v3/0.T3oYTbQ3kVdGsaYBPuhlFksnljHisu.', 'ADMIN');

INSERT INTO  stylists (user_id, bio)
 VALUES 
 ('2', '美容師歴10年です'),
 ('3', '似合わせカットが得意です！');

INSERT INTO business_hour (stylist_id, day_of_week, start_time, end_time)
 VALUES 
 ('2', 'Mon', '08:00', '17:00'),
 ('2', 'Tue', '08:00', '19:00'),
 ('2', 'Wed', '08:00', '20:00'),
 ('2', 'Fri', '08:00', '17:00'),
 ('2', 'Sat', '08:00', '17:00'),
 ('3', 'Tue', '08:00', '17:00'),
 ('3', 'Wed', '08:00', '17:00'),
 ('3', 'Thu', '08:00', '17:00'),
 ('3', 'Sat', '08:00', '17:00'),
 ('3', 'Sun', '08:00', '17:00');

INSERT INTO menus (id, name, base_duration_minutes, base_price)
 VALUES 
 (1, 'CUT', '60', '5000'),
 (2, 'COLOR', '120', '9000'),
 (3, 'PERM', '180', '15000');


INSERT INTO stylists_menus (stylist_id, menu_id, duration_minutes, price)
 VALUES
 ('2', '1', '45', '5500'),
 ('2', '2', '90', '10000'),
 ('2', '3', '90', '15000'),
 ('3', '1', '90', '12000'),
 ('3', '2', '300', '12000'),
 ('3', '3', '60', '12000');



