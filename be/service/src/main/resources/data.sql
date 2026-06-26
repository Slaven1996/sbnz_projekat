

INSERT IGNORE INTO departments (id, code, name, description) VALUES
    (1, 'DISP',  'Dispatch Center and Monitoring',
        'Responsible for continuous SCADA system monitoring, analysis of sensor data and issuing intervention orders from the office.'),
    (2, 'FIELD', 'Field Maintenance and Emergency Response Service',
        'Responsible for field work, calibration and repair of sensor equipment, and carrying out physical flood protection measures.'),
    (3, 'IT',    'IT and Expert System Configuration Department',
        'Responsible for managing infrastructure, the database and defining dynamic rule thresholds.');

-- Sifra za admina je "admin123", a za operatere "user123"
INSERT INTO users (id, name, last_name, email, password, role, active, department_id) VALUES
    (1, 'Nikola', 'Nikolic', 'admin@hydro.local',
        '$2a$10$s.9kj15rCaw9Lr9cPcSyU.hS0blod8gHHM0ZoO7Bgd61coW17p.tW', 'ADMIN', true, NULL),
    (2, 'Petar', 'Petrovic', 'petar@hydro.local',
        '$2a$12$7w2SDeragadWiZRR5j7zBu4t18U.5g5YK/QLSx0FJJ6ycBtvz.UIy', 'OPERATOR', true, 1),
    (3, 'Marko', 'Markovic', 'marko@hydro.local',
        '$2a$12$7w2SDeragadWiZRR5j7zBu4t18U.5g5YK/QLSx0FJJ6ycBtvz.UIy', 'OPERATOR', true, 2)
    ON DUPLICATE KEY UPDATE department_id = VALUES(department_id);

INSERT IGNORE INTO tag_units (id, code, unit, description) VALUES
    (1, 'CM',    'cm',    'Water level (centimeters)'),
    (2, 'M3S',   'm³/s',  'Flow rate (cubic meters per second)'),
    (3, 'STATE', 'state', 'Pump operational state');

INSERT IGNORE INTO zones (id, code, name, description) VALUES
    (1, 'WBD', 'West Bačka District',    'Administrative seat: Sombor'),
    (2, 'NBD', 'North Bačka District',   'Administrative seat: Subotica'),
    (3, 'SBD', 'South Bačka District',   'Administrative seat: Novi Sad'),
    (4, 'NBN', 'North Banat District',   'Administrative seat: Kikinda'),
    (5, 'CBN', 'Central Banat District', 'Administrative seat: Zrenjanin'),
    (6, 'SBN', 'South Banat District',   'Administrative seat: Pančevo'),
    (7, 'SRD', 'Srem District',          'Administrative seat: Sremska Mitrovica');

INSERT IGNORE INTO threshold_configs (id, location_type, parameter_type, normal_max, warning_max, critical_max) VALUES
    (1, 'RIVER',        'WATER_LEVEL', 200, 350, 500),
    (2, 'CANAL',        'WATER_LEVEL', 100, 180, 250),
    (3, 'RESERVOIR',    'WATER_LEVEL', 500, 700, 850),
    (4, 'PUMP_STATION', 'WATER_LEVEL', 150, 250, 350),
    (5, 'RIVER',        'FLOW_RATE',   2000, 3000, NULL),
    (6, 'CANAL',        'FLOW_RATE',    500,  900, NULL),
    (7, 'RESERVOIR',    'FLOW_RATE',    100,  300, NULL),
    (8, 'PUMP_STATION', 'FLOW_RATE',    100,  200, NULL);

-- PODACI ZA ISTORIJSKU SIMULACIJU
-- ----------------------------------------------------------------------------
-- Dvije mjerne lokacije u okruzima Vojvodine cine visednevnu vremensku liniju
-- koja reprodukuje rezonovanje iz HydroMonitoringExamples:
--   * LOC_RIJEKA (RIVER, Juzna Backa, zona 3) -> scenarioEscalationOverTime:
--     nivo vode raste NORMAL -> ELEVATED -> HIGH -> CRITICAL pa ponovo opada,
--     vodeci rizik od poplave LOW -> MODERATE -> HIGH -> EXTREME -> LOW.
--   * LOC_PUMPA (PUMP_STATION, Juzni Banat, zona 6) -> scenarioFlowToOrangeAlert:
--     povisen nivo uz opadajuci kapacitet pumpi (FULL -> REDUCED -> MINIMAL)
--     daje MODERATE/HIGH rizik; u kombinaciji sa EXTREME na LOC_RIJEKA globalni
--     SystemAlert eskalira na ORANGE, a zatim se vraca na GREEN.
-- Pragovi: RIVER WL n200/w350/c500, FR n2000/w3000 | PUMP_STATION WL n150/w250/c350.
-- Konvencija za pumpe: 1.0=ACTIVE, 0.0=IDLE, -1.0=FAULTY.
-- ============================================================================

INSERT INTO locations (id, code, display_code, type, zone_id, pos_x, pos_y, active) VALUES
    (1, 'LOC_RIJEKA',  'Dunav - Novi Sad',         'RIVER',        3, 45.2517, 19.8369, true),
    (2, 'LOC_PUMPA',   'Pančevo',               'PUMP_STATION', 6, 44.8708, 20.6403, true),
    (3, 'LOC_SM_SAVA', 'Sava - Sremska Mitrovica', 'RIVER',        7, 44.9769, 19.6122, true),
    (4, 'LOC_ZR_BEGEJ','Begej - Zrenjanin',        'RIVER',        5, 45.3836, 20.3819, true),
    (5, 'LOC_BZ_DUNAV','Dunav - Bezdan',           'RIVER',        1, 45.8453, 18.9286, true),
    (6, 'LOC_SO_KANAL','DTD kanal - Sombor',       'CANAL',        1, 45.7742, 19.1122, true),
    (7, 'LOC_KI_KANAL','DTD kanal - Kikinda',      'CANAL',        4, 45.8294, 20.4653, true)
    ON DUPLICATE KEY UPDATE pos_x = VALUES(pos_x), pos_y = VALUES(pos_y);

INSERT IGNORE INTO weather_conditions (id, location_id, precipitation, last_update) VALUES
    (1, 1, 5.0, '2026-05-04 06:00:00'),
    (2, 2, 5.0, '2026-05-04 06:00:00'),
    (3, 3, 6.0, '2026-05-04 06:00:00'),
    (4, 4, 4.0, '2026-05-04 06:00:00'),
    (5, 5, 7.0, '2026-05-04 06:00:00'),
    (6, 6, 3.0, '2026-05-04 06:00:00'),
    (7, 7, 5.0, '2026-05-04 06:00:00');

INSERT IGNORE INTO weather_observations (id, location_code, observed_at, precipitation) VALUES
    ( 1, 'LOC_RIJEKA', '2026-05-04 06:00:00',  5.0),
    ( 2, 'LOC_RIJEKA', '2026-05-04 18:00:00', 25.0),
    ( 3, 'LOC_RIJEKA', '2026-05-05 06:00:00', 45.0),
    ( 4, 'LOC_RIJEKA', '2026-05-05 12:00:00', 55.0),
    ( 5, 'LOC_RIJEKA', '2026-05-05 18:00:00', 15.0),
    ( 6, 'LOC_RIJEKA', '2026-05-06 00:00:00',  6.0),
    ( 7, 'LOC_RIJEKA', '2026-05-06 12:00:00',  4.0),
    ( 8, 'LOC_PUMPA',  '2026-05-04 06:00:00',  3.0),
    ( 9, 'LOC_PUMPA',  '2026-05-05 12:00:00',  8.0),
    (10, 'LOC_PUMPA',  '2026-05-06 00:00:00',  4.0);

INSERT IGNORE INTO sensors (id, location_id, tag_name, display_code, sensor_type, unit_id) VALUES
    (1, 1, 'RIJEKA_WL', 'Water level', 'WATER_LEVEL', 1),
    (2, 1, 'RIJEKA_FR', 'Flow rate',   'FLOW_RATE',   2),
    (3, 2, 'PUMPA_WL',  'Water level', 'WATER_LEVEL', 1),
    (4, 2, 'PUMPA_FR',  'Discharge',   'FLOW_RATE',   2),
    (5, 2, 'PUMPA_P1',  'Pump 1',      'PUMP_STATUS', 3),
    (6, 2, 'PUMPA_P2',  'Pump 2',      'PUMP_STATUS', 3),
    (7, 2, 'PUMPA_P3',  'Pump 3',      'PUMP_STATUS', 3),
    (8, 2, 'PUMPA_P4',  'Pump 4',      'PUMP_STATUS', 3),
    (9, 2, 'PUMPA_P5',  'Pump 5',      'PUMP_STATUS', 3),
    (10, 3, 'SM_WL', 'Water level', 'WATER_LEVEL', 1),
    (11, 3, 'SM_FR', 'Flow rate',   'FLOW_RATE',   2),
    (12, 4, 'ZR_WL', 'Water level', 'WATER_LEVEL', 1),
    (13, 4, 'ZR_FR', 'Flow rate',   'FLOW_RATE',   2),
    (14, 5, 'BZ_WL', 'Water level', 'WATER_LEVEL', 1),
    (15, 5, 'BZ_FR', 'Flow rate',   'FLOW_RATE',   2),
    (16, 6, 'SO_WL', 'Water level', 'WATER_LEVEL', 1),
    (17, 6, 'SO_FR', 'Flow rate',   'FLOW_RATE',   2),
    (18, 7, 'KI_WL', 'Water level', 'WATER_LEVEL', 1),
    (19, 7, 'KI_FR', 'Flow rate',   'FLOW_RATE',   2);

-- ----------------------------------------------------------------------------
-- LOC_RIJEKA - eskalacija pa deeskalacija na rijeci.
-- WL:  180  300  400  470  600  420  300  150   (NORMAL..CRITICAL..NORMAL)
-- FR: 2200 2400 2800 3500 3800 2500 2300 2100   (NORMAL...HIGH...NORMAL)
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO trend_data (id, location_code, tag_name, log_time, tag_value) VALUES
    ( 1, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-04 06:00:00', 180),
    ( 2, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-04 06:00:00', 2200),
    ( 3, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-04 12:00:00', 300),
    ( 4, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-04 12:00:00', 2400),
    ( 5, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-04 18:00:00', 400),
    ( 6, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-04 18:00:00', 2800),
    ( 7, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-05 06:00:00', 470),
    ( 8, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-05 06:00:00', 3500),
    ( 9, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-05 12:00:00', 600),
    (10, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-05 12:00:00', 3800),
    (11, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-05 18:00:00', 420),
    (12, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-05 18:00:00', 2500),
    (13, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-06 06:00:00', 300),
    (14, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-06 06:00:00', 2300),
    (15, 'LOC_RIJEKA', 'RIJEKA_WL', '2026-05-06 12:00:00', 150),
    (16, 'LOC_RIJEKA', 'RIJEKA_FR', '2026-05-06 12:00:00', 2100);

-- ----------------------------------------------------------------------------
-- LOC_PUMPA - povisen nivo + opadanje pa oporavak kapaciteta pumpi (5 koraka).
-- WL: 120 200 300 200 120   FR: 150 (konstantan NORMAL ispust)
-- Broj aktivnih pumpi: 5(FULL) 3(REDUCED) 1(MINIMAL) 3(REDUCED) 5(FULL)
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO trend_data (id, location_code, tag_name, log_time, tag_value) VALUES
    -- 2026-05-04 06:00  WL NORMAL, sve pumpe ACTIVE -> kapacitet FULL
    (17, 'LOC_PUMPA', 'PUMPA_WL', '2026-05-04 06:00:00', 120),
    (18, 'LOC_PUMPA', 'PUMPA_FR', '2026-05-04 06:00:00', 150),
    (19, 'LOC_PUMPA', 'PUMPA_P1', '2026-05-04 06:00:00',  1),
    (20, 'LOC_PUMPA', 'PUMPA_P2', '2026-05-04 06:00:00',  1),
    (21, 'LOC_PUMPA', 'PUMPA_P3', '2026-05-04 06:00:00',  1),
    (22, 'LOC_PUMPA', 'PUMPA_P4', '2026-05-04 06:00:00',  1),
    (23, 'LOC_PUMPA', 'PUMPA_P5', '2026-05-04 06:00:00',  1),
    -- 2026-05-04 18:00  WL ELEVATED, P4 FAULTY + P5 IDLE -> kapacitet REDUCED (3/5)
    (24, 'LOC_PUMPA', 'PUMPA_WL', '2026-05-04 18:00:00', 200),
    (25, 'LOC_PUMPA', 'PUMPA_FR', '2026-05-04 18:00:00', 150),
    (26, 'LOC_PUMPA', 'PUMPA_P1', '2026-05-04 18:00:00',  1),
    (27, 'LOC_PUMPA', 'PUMPA_P2', '2026-05-04 18:00:00',  1),
    (28, 'LOC_PUMPA', 'PUMPA_P3', '2026-05-04 18:00:00',  1),
    (29, 'LOC_PUMPA', 'PUMPA_P4', '2026-05-04 18:00:00', -1),
    (30, 'LOC_PUMPA', 'PUMPA_P5', '2026-05-04 18:00:00',  0),
    -- 2026-05-05 12:00  WL HIGH, samo P1 ACTIVE -> kapacitet MINIMAL (1/5)
    (31, 'LOC_PUMPA', 'PUMPA_WL', '2026-05-05 12:00:00', 300),
    (32, 'LOC_PUMPA', 'PUMPA_FR', '2026-05-05 12:00:00', 150),
    (33, 'LOC_PUMPA', 'PUMPA_P1', '2026-05-05 12:00:00',  1),
    (34, 'LOC_PUMPA', 'PUMPA_P2', '2026-05-05 12:00:00',  0),
    (35, 'LOC_PUMPA', 'PUMPA_P3', '2026-05-05 12:00:00', -1),
    (36, 'LOC_PUMPA', 'PUMPA_P4', '2026-05-05 12:00:00', -1),
    (37, 'LOC_PUMPA', 'PUMPA_P5', '2026-05-05 12:00:00',  0),
    -- 2026-05-06 06:00  WL ELEVATED, pumpe se oporavljaju -> kapacitet REDUCED (3/5)
    (38, 'LOC_PUMPA', 'PUMPA_WL', '2026-05-06 06:00:00', 200),
    (39, 'LOC_PUMPA', 'PUMPA_FR', '2026-05-06 06:00:00', 150),
    (40, 'LOC_PUMPA', 'PUMPA_P1', '2026-05-06 06:00:00',  1),
    (41, 'LOC_PUMPA', 'PUMPA_P2', '2026-05-06 06:00:00',  1),
    (42, 'LOC_PUMPA', 'PUMPA_P3', '2026-05-06 06:00:00',  1),
    (43, 'LOC_PUMPA', 'PUMPA_P4', '2026-05-06 06:00:00',  0),
    (44, 'LOC_PUMPA', 'PUMPA_P5', '2026-05-06 06:00:00',  0),
    -- 2026-05-06 12:00  WL NORMAL, sve pumpe ACTIVE -> kapacitet FULL
    (45, 'LOC_PUMPA', 'PUMPA_WL', '2026-05-06 12:00:00', 120),
    (46, 'LOC_PUMPA', 'PUMPA_FR', '2026-05-06 12:00:00', 150),
    (47, 'LOC_PUMPA', 'PUMPA_P1', '2026-05-06 12:00:00',  1),
    (48, 'LOC_PUMPA', 'PUMPA_P2', '2026-05-06 12:00:00',  1),
    (49, 'LOC_PUMPA', 'PUMPA_P3', '2026-05-06 12:00:00',  1),
    (50, 'LOC_PUMPA', 'PUMPA_P4', '2026-05-06 12:00:00',  1),
    (51, 'LOC_PUMPA', 'PUMPA_P5', '2026-05-06 12:00:00',  1);
