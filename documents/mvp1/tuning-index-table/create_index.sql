-- SHOW INDEX FROM m_mobile_users
-- CREATE INDEX FROM

ALTER TABLE m_mobile_users ADD INDEX idx_username (USERNAME);
ALTER TABLE m_mobile_users ADD INDEX idx_email (EMAIL);
ALTER TABLE m_mobile_users ADD INDEX idx_mobile_uuid (MOBILE_UUID);
