-- Schema-per-service. Both live in one MySQL container for local
-- convenience, but each service connects only to its own and there
-- are no cross-schema joins or foreign keys.

CREATE DATABASE IF NOT EXISTS shopflow_orders
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS shopflow_fulfilment
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;