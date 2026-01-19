INSERT INTO ticket_categories (
    id, event_id, name, price, total_quantity, sold_quantity, reserved_quantity, status, created_at, updated_at
) VALUES (
    1, '1', 'General Admission', 100.00, 100, 0, 0, 'AVAILABLE', NOW(), NOW()
) ON CONFLICT (id) DO NOTHING;

-- Seed 50 available tickets for Event 1 (Category 1)
-- Only insert if not exists (using ticket_code uniqueness is trickier without values, but we can verify count)
INSERT INTO tickets (
    ticket_code, category_id, status, created_at
) 
SELECT 
    gen_random_uuid(), 
    1, 
    'AVAILABLE', 
    NOW() 
FROM generate_series(1, 50)
WHERE NOT EXISTS (SELECT 1 FROM tickets WHERE category_id = 1);
