
create type category as enum ('VEGETABLE','ANIMAL','MARINE','DAIRY','OTHER');
create type dish_type as enum ('START','MAIN','DESSERT')
create table dish(
                     id int primary key ,
                     name varchar(500),
                     dish_type dish_type
);
create table ingredient(
                           id int primary key ,
                           name varchar(100),
                           price numeric,
                           category category,
                           id_dish int references dish(id)
);
