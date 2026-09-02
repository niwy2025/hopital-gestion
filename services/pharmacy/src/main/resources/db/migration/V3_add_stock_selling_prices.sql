-- Le coût d'achat sert à la valorisation du stock. Le prix de vente est une
-- donnée distincte, propre à l'hôpital, utilisée à la délivrance et en caisse.
ALTER TABLE hospital_medicine_stocks
    ADD COLUMN unit_selling_price NUMERIC(18, 2);

UPDATE hospital_medicine_stocks
SET unit_selling_price = average_unit_cost
WHERE unit_selling_price IS NULL;

ALTER TABLE hospital_medicine_stocks
    ALTER COLUMN unit_selling_price SET NOT NULL,
    ADD CONSTRAINT ck_hospital_medicine_stocks_unit_selling_price
        CHECK (unit_selling_price >= 0);

ALTER TABLE stock_entries
    ADD COLUMN unit_selling_price NUMERIC(18, 2);

UPDATE stock_entries
SET unit_selling_price = unit_cost
WHERE unit_selling_price IS NULL;

ALTER TABLE stock_entries
    ALTER COLUMN unit_selling_price SET NOT NULL,
    ADD CONSTRAINT ck_stock_entries_unit_selling_price
        CHECK (unit_selling_price >= 0);
