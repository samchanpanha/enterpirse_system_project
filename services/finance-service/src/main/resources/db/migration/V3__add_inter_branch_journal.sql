-- V3__add_inter_branch_journal.sql
-- Adds from_branch_id + to_branch_id to journal entries for inter-branch postings
-- Adds the InterBranchClearing account type (handled in application layer)

ALTER TABLE journal_entries
  ADD COLUMN IF NOT EXISTS from_branch_id UUID;
ALTER TABLE journal_entries
  ADD COLUMN IF NOT EXISTS to_branch_id UUID;

CREATE INDEX IF NOT EXISTS idx_je_from_branch ON journal_entries(from_branch_id);
CREATE INDEX IF NOT EXISTS idx_je_to_branch ON journal_entries(to_branch_id);
