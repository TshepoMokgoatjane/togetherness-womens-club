-- ============================================
-- TEST DATA FOR PRESENTATION
-- Run this manually in MySQL Workbench or CLI
-- ============================================

-- Insert contributions for the last 6 months for existing members
-- Adjust member_id values to match your actual member IDs (1, 2, 3, 4)

-- 6 months ago
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, DATE_SUB(CURDATE(), INTERVAL 6 MONTH), 'REF-JAN-001', 'PAID', 1, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 6 MONTH), 'REF-JAN-002', 'PAID', 2, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 6 MONTH), 'REF-JAN-003', 'PAID', 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m')),
(NULL, NULL, NULL, 'MISSED', 4, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 6 MONTH), '%Y-%m'));

-- 5 months ago
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, DATE_SUB(CURDATE(), INTERVAL 5 MONTH), 'REF-FEB-001', 'PAID', 1, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 5 MONTH), 'REF-FEB-002', 'PAID', 2, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 5 MONTH), 'REF-FEB-003', 'PAID', 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 5 MONTH), 'REF-FEB-004', 'PAID', 4, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 5 MONTH), '%Y-%m'));

-- 4 months ago
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, DATE_SUB(CURDATE(), INTERVAL 4 MONTH), 'REF-MAR-001', 'PAID', 1, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 4 MONTH), 'REF-MAR-002', 'PAID', 2, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m')),
(NULL, NULL, NULL, 'MISSED', 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 4 MONTH), 'REF-MAR-004', 'PAID', 4, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 4 MONTH), '%Y-%m'));

-- 3 months ago
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'REF-APR-001', 'PAID', 1, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'REF-APR-002', 'PAID', 2, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'REF-APR-003', 'PAID', 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 3 MONTH), 'REF-APR-004', 'PAID', 4, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 3 MONTH), '%Y-%m'));

-- 2 months ago
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'REF-MAY-001', 'PAID', 1, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'REF-MAY-002', 'PAID', 2, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 2 MONTH), 'REF-MAY-003', 'PAID', 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m')),
(NULL, NULL, NULL, 'MISSED', 4, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), '%Y-%m'));

-- 1 month ago
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 'REF-JUN-001', 'PAID', 1, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 'REF-JUN-002', 'PAID', 2, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 'REF-JUN-003', 'PAID', 3, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m')),
(200.00, DATE_SUB(CURDATE(), INTERVAL 1 MONTH), 'REF-JUN-004', 'PAID', 4, DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m'));

-- Current month (mix of paid, pending, missed)
INSERT INTO contributions (amount, payment_date, reference, status, member_id, contribution_month)
VALUES
(200.00, CURDATE(), 'REF-JUL-001', 'PAID', 1, DATE_FORMAT(CURDATE(), '%Y-%m')),
(200.00, CURDATE(), 'REF-JUL-002', 'PAID', 2, DATE_FORMAT(CURDATE(), '%Y-%m')),
(NULL, NULL, NULL, 'PENDING', 3, DATE_FORMAT(CURDATE(), '%Y-%m')),
(NULL, NULL, NULL, 'MISSED', 4, DATE_FORMAT(CURDATE(), '%Y-%m'));
