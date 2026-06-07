INSERT IGNORE INTO users (name, last_name, email, password, role, active)
VALUES ('Nikola', 'Nikolic', 'admin@hydro.local',
        '$2a$10$s.9kj15rCaw9Lr9cPcSyU.hS0blod8gHHM0ZoO7Bgd61coW17p.tW', 'ADMIN', true);

INSERT IGNORE INTO users (name, last_name, email, password, role, active)
VALUES ('Petar', 'Petrovic', 'petar@hydro.local',
        '$2a$12$7w2SDeragadWiZRR5j7zBu4t18U.5g5YK/QLSx0FJJ6ycBtvz.UIy', 'OPERATOR', true);
INSERT IGNORE INTO users (name, last_name, email, password, role, active)
VALUES ('Marko', 'Markovic', 'marko@hydro.local',
        '$2a$12$7w2SDeragadWiZRR5j7zBu4t18U.5g5YK/QLSx0FJJ6ycBtvz.UIy', 'OPERATOR', true);

INSERT IGNORE INTO zones (code, name, description) VALUES
    ('WBD', 'West Bačka District',    'Administrative seat: Sombor'),
    ('NBD', 'North Bačka District',   'Administrative seat: Subotica'),
    ('SBD', 'South Bačka District',   'Administrative seat: Novi Sad'),
    ('NBN', 'North Banat District',   'Administrative seat: Kikinda'),
    ('CBN', 'Central Banat District', 'Administrative seat: Zrenjanin'),
    ('SBN', 'South Banat District',   'Administrative seat: Pančevo'),
    ('SRD', 'Srem District',          'Administrative seat: Sremska Mitrovica');

INSERT IGNORE INTO threshold_configs (location_type, parameter_type, normal_max, warning_max, critical_max) VALUES
    ('RIVER',        'WATER_LEVEL', 200, 350, 500),
    ('CANAL',        'WATER_LEVEL', 100, 180, 250),
    ('RESERVOIR',    'WATER_LEVEL', 500, 700, 850),
    ('PUMP_STATION', 'WATER_LEVEL', 150, 250, 350),
    ('RIVER',        'FLOW_RATE',   2000, 3000, NULL),
    ('CANAL',        'FLOW_RATE',    500,  900, NULL),
    ('RESERVOIR',    'FLOW_RATE',    100,  300, NULL),
    ('PUMP_STATION', 'FLOW_RATE',    100,  200, NULL);
