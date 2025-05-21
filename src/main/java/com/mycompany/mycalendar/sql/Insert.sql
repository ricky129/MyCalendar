/**
 * Author:  ricky
 * Created: 26 Mar 2025
 */
USE MyCalendarDB;
TRUNCATE TABLE Events;
INSERT INTO Events (name, description, date, latitude, longitude) VALUES
    ('Project Meeting', 'Discuss project progress and next steps.', '2025-01-01 12:00:00', 51.5074000, -0.1278000),
    ('Project2 Meeting2', 'Discuss project progress and next steps.', '2025-01-01 12:00:00', 40.7128000, -74.0060000),
    ('Project3 Meeting3', 'Discuss project progress and next steps.', '2025-01-01 12:00:00', 48.8566000, 2.3522000),
    ('Team Building', 'Outdoor team activities and social gathering.', '2025-04-20 12:00:00', 34.0522000, -99.9999999),
    ('Product Launch', 'Official launch of the new product.', '2025-05-01 12:00:00', 35.6762000, 99.9999999),
    ('Conference', 'Industry conference on emerging technologies.', '2025-06-10 12:00:00', 52.5200000, 13.4050000),
    ('Training Session', 'Training on new software tools.', '2025-07-25 12:00:00', 37.7749000, -99.9999999),
    ('Holiday', 'Summer holiday break.', '2025-08-05 12:00:00', 41.9028000, 12.4964000),
    ('Workshop', 'Interactive workshop on data analysis.', '2025-09-12 12:00:00', 55.7558000, 37.6173000),
    ('Halloween Party', 'Office Halloween celebration.', '2025-10-31 12:00:00', 19.4326000, -99.1332000),
    ('Thanksgiving Celebration', 'Thanksgiving lunch at the office.', '2025-11-22 12:00:00', 43.6532000, -79.3832000),
    ('Christmas Party', 'Christmas party and gift exchange.', '2025-12-25 12:00:00', -33.8688000, 99.9999999),
    ('test', 'test.', '2025-02-27 12:00:00', 1.3521000, 99.9999999),
    ('test2', 'test2.', '2025-02-28 12:00:00', 22.3193000, 99.9999999),
    ('tst', 'test.', '2025-03-03 13:30:16', 25.2048000, 55.2708000);