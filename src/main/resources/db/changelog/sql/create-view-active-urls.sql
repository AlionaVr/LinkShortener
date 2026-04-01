CREATE OR REPLACE VIEW active_urls AS
SELECT *
FROM urls
WHERE expires_at IS NULL
   OR expires_at > NOW();