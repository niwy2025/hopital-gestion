-- Une note libre ne peut pas servir de clé de rapprochement comptable.
-- Les mouvements issus d'une délivrance disposent désormais d'une référence
-- métier stable et indexée. Les historiques antérieurs restent inchangés.
ALTER TABLE stock_movements
    ADD COLUMN source_type VARCHAR(40),
    ADD COLUMN source_code VARCHAR(50);

CREATE INDEX idx_stock_movements_source
    ON stock_movements (source_type, source_code, occurred_at ASC);
