-- The price used for a dispense must remain immutable even if the stock price
-- changes later. It is the authoritative invoice total sent to accounting.
ALTER TABLE stock_movements
    ADD COLUMN unit_selling_price NUMERIC(18, 2);

-- Existing deliveries predate the immutable selling-price field. The current
-- price is only a compatibility bridge for those historical rows; all new
-- dispensing movements persist the price at the time of delivery.
UPDATE stock_movements movement
SET unit_selling_price = stock.unit_selling_price
FROM hospital_medicine_stocks stock
WHERE movement.stock_id = stock.id
  AND movement.type = 'DISPENSING'
  AND movement.unit_selling_price IS NULL;

ALTER TABLE stock_movements
    ADD CONSTRAINT ck_stock_movements_dispensing_selling_price
        CHECK (type <> 'DISPENSING' OR unit_selling_price IS NOT NULL);
