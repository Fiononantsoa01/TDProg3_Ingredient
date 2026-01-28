ALTER TABLE ingredient
DROP COLUMN IF EXISTS id_dish;
create type unit_type as enum ('PCS', 'KG', 'L');

CREATE TABLE IF NOT EXISTS DishIngredient (
                                               id SERIAL PRIMARY KEY,
                                               id_dish INT NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
    id_ingredient INT NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC(10,2) NOT NULL,
    unit unit_type
    );

INSERT INTO dishingredient (id_dish, id_ingredient, quantity_required, unit)
VALUES
    (1, 1, 0.20, 'KG'),
    (1, 2, 0.15, 'KG'),
    (2, 3, 1.00, 'KG'),
    (4, 4, 0.30, 'KG'),
    (4, 5, 0.20, 'KG');

UPDATE dish SET price = 3500 WHERE id = 1;
UPDATE dish SET price = 12000 WHERE id = 2;
UPDATE dish SET price = NULL WHERE id = 3;
UPDATE dish SET price = 8000 WHERE id = 4;
UPDATE dish SET price = NULL WHERE id = 5;
