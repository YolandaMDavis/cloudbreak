-- // BUG-108475
-- Migration SQL that makes the change goes here.

ALTER TABLE rdsconfig ALTER COLUMN owner DROP NOT NULL;
ALTER TABLE rdsconfig ALTER COLUMN account DROP NOT NULL;
ALTER TABLE rdsconfig DROP CONSTRAINT IF EXISTS uk_rdsconfig_account_name;