-- Demo users, one per department, all with the USER role.
-- Development data only: loaded when FLYWAY_LOCATIONS includes classpath:db/seed
-- (the docker-compose default), never in tests, and must be left out in production.
--
-- Password of every account: Quadria2026 (BCrypt hash below).
-- The admin is not created here: AdminInitializer creates it from ADMIN_EMAIL / ADMIN_PASSWORD.
--
-- Repeatable migration: Flyway runs it again only when this file changes, and
-- ON CONFLICT leaves existing accounts (and their passwords) untouched.

INSERT INTO users (email, password, department, role) VALUES
    ('claire.martin@quadria.fr',   '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'RH',        'USER'),
    ('thomas.lefevre@quadria.fr',  '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'RH',        'USER'),
    ('julien.moreau@quadria.fr',   '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'FINANCE',   'USER'),
    ('nicolas.dubois@quadria.fr',  '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'IT',        'USER'),
    ('camille.laurent@quadria.fr', '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'MARKETING', 'USER'),
    ('pierre.durand@quadria.fr',   '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'DIRECTION', 'USER'),
    ('antoine.fontaine@quadria.fr','$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'SALES',     'USER'),
    ('lea.mercier@quadria.fr',     '$2a$10$A./om1.BooM5eostnxx0Ves/0AGjTF.sGZI0A8rXS5xS/XhUBUJL6', 'SUPPORT',   'USER')
ON CONFLICT (email) DO NOTHING;
