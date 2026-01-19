-- ============================================
-- NightFlow Test Data: Venues
-- ============================================
-- Bu dosya E2E testleri için venue verisi seed eder.
-- Sadece yoksa ekler (ON CONFLICT DO NOTHING).
-- ============================================

INSERT INTO venues (id, name, address, city, capacity, description, created_at, updated_at)
VALUES 
    (1, 'Test Arena', '123 Test Street', 'Istanbul', 5000, 'Test venue for E2E', NOW(), NOW()),
    (2, 'Demo Stadium', '456 Demo Avenue', 'Ankara', 10000, 'Demo venue for testing', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequence to avoid conflicts
SELECT setval('venues_id_seq', (SELECT COALESCE(MAX(id), 1) FROM venues));
