-- ============================================================
-- SplitSnap — Esquema de base de datos
-- Motor: PostgreSQL 15+
-- ============================================================

-- ── Extensión para UUIDs ──────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── USERS ─────────────────────────────────────────────────────
CREATE TABLE users (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    phone       VARCHAR(20),
    password    VARCHAR(255) NOT NULL,
    avatar_url  VARCHAR(500),
    credits     NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── GROUPS ────────────────────────────────────────────────────
CREATE TABLE groups (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    emoji       VARCHAR(10)  NOT NULL DEFAULT '📦',
    created_by  UUID        NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── GROUP_MEMBERS ─────────────────────────────────────────────
CREATE TABLE group_members (
    group_id    UUID        NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    joined_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

-- ── EXPENSES ──────────────────────────────────────────────────
CREATE TABLE expenses (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id    UUID        NOT NULL REFERENCES groups(id)  ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    amount      NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    paid_by     UUID        NOT NULL REFERENCES users(id),
    expense_date DATE        NOT NULL DEFAULT CURRENT_DATE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── EXPENSE_SPLITS ────────────────────────────────────────────
-- Cómo se divide el gasto entre los miembros
CREATE TABLE expense_splits (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_id  UUID        NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id),
    amount      NUMERIC(10,2) NOT NULL CHECK (amount >= 0)
);

-- ── EXPENSE_ITEMS ─────────────────────────────────────────────
-- Ítems individuales (cuando el gasto viene de OCR/escaner)
CREATE TABLE expense_items (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_id  UUID        NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    price       NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    quantity    INTEGER     NOT NULL DEFAULT 1
);

-- ── EXPENSE_ITEM_ASSIGNMENTS ──────────────────────────────────
-- Qué usuarios están asignados a cada ítem del recibo
CREATE TABLE expense_item_assignments (
    item_id     UUID        NOT NULL REFERENCES expense_items(id) ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id),
    PRIMARY KEY (item_id, user_id)
);

-- ── DEBTS ─────────────────────────────────────────────────────
CREATE TABLE debts (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id    UUID        NOT NULL REFERENCES groups(id)   ON DELETE CASCADE,
    expense_id  UUID        NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
    from_user   UUID        NOT NULL REFERENCES users(id),   -- quien debe
    to_user     UUID        NOT NULL REFERENCES users(id),   -- a quién le debe
    amount      NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    status      VARCHAR(10)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'PAID')),
    paid_with   VARCHAR(50),  -- yape, paypal, efectivo, credits
    paid_at     TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── CREDIT_TRANSACTIONS ───────────────────────────────────────
CREATE TABLE credit_transactions (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id),
    type        VARCHAR(10)  NOT NULL CHECK (type IN ('PURCHASE', 'SPEND')),
    amount      NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    debt_id     UUID        REFERENCES debts(id),  -- solo para tipo SPEND
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── ÍNDICES ───────────────────────────────────────────────────
CREATE INDEX idx_group_members_user    ON group_members(user_id);
CREATE INDEX idx_expenses_group        ON expenses(group_id);
CREATE INDEX idx_expenses_paid_by      ON expenses(paid_by);
CREATE INDEX idx_expense_splits_exp    ON expense_splits(expense_id);
CREATE INDEX idx_debts_group           ON debts(group_id);
CREATE INDEX idx_debts_from_user       ON debts(from_user);
CREATE INDEX idx_debts_to_user         ON debts(to_user);
CREATE INDEX idx_debts_status          ON debts(status);
CREATE INDEX idx_credit_tx_user        ON credit_transactions(user_id);
