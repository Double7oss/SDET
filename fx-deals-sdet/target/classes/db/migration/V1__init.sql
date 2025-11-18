CREATE TABLE IF NOT EXISTS deals (
    deal_id UUID PRIMARY KEY,
    from_currency CHAR(3) NOT NULL,
    to_currency CHAR(3) NOT NULL,
    deal_timestamp TIMESTAMPTZ NOT NULL,
    amount NUMERIC(19,4) NOT NULL CHECK (amount > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
