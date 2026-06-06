--Password below is BCrypt("admin123").
INSERT IGNORE INTO users (user_code, name, last_name, email, password, role, active)
VALUES ('ADMIN-001', 'System', 'Administrator', 'admin@hydro.local',
        '$2a$10$s.9kj15rCaw9Lr9cPcSyU.hS0blod8gHHM0ZoO7Bgd61coW17p.tW', 'ADMIN', true);
