-- V3__add_layout_and_result_data.sql
-- Adds report builder columns to the reporting schema.

ALTER TABLE report_definitions
    ADD COLUMN IF NOT EXISTS layout JSONB NOT NULL DEFAULT '[]';

ALTER TABLE report_executions
    ADD COLUMN IF NOT EXISTS result_data JSONB;
