-- Hand-written on purpose: Exposed's generateMigrations does not detect column type changes.
--
-- Every timestamp column was created as TIMESTAMP (no time zone), and Exposed wrote the value in the JVM's default
-- zone. The server runs in UTC (container), so the existing values are UTC; `AT TIME ZONE 'UTC'` says so explicitly
-- instead of relying on the session's time zone.

ALTER TABLE users
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE USING updated_at AT TIME ZONE 'UTC';

ALTER TABLE legal_documents
    ALTER COLUMN effective_at TYPE TIMESTAMP WITH TIME ZONE USING effective_at AT TIME ZONE 'UTC';

ALTER TABLE legal_acceptances
    ALTER COLUMN accepted_at TYPE TIMESTAMP WITH TIME ZONE USING accepted_at AT TIME ZONE 'UTC';

ALTER TABLE sleep_records
    ALTER COLUMN bedtime TYPE TIMESTAMP WITH TIME ZONE USING bedtime AT TIME ZONE 'UTC',
    ALTER COLUMN fell_asleep_at TYPE TIMESTAMP WITH TIME ZONE USING fell_asleep_at AT TIME ZONE 'UTC',
    ALTER COLUMN woke_up_at TYPE TIMESTAMP WITH TIME ZONE USING woke_up_at AT TIME ZONE 'UTC',
    ALTER COLUMN got_up_at TYPE TIMESTAMP WITH TIME ZONE USING got_up_at AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE USING created_at AT TIME ZONE 'UTC';
