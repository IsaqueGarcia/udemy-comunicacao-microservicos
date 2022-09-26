insert into CATEGORY (ID, DESCRIPTION) values (1000, 'Comic Books');
insert into CATEGORY (ID, DESCRIPTION) values (1001, 'Movies');
insert into CATEGORY (ID, DESCRIPTION) values (1002, 'Books');

insert into SUPPLIER (ID, NAME) values (1000, 'Panini Comics');
insert into SUPPLIER (ID, NAME) values (1001, 'Amazon');

insert into PRODUCT (ID, NAME, FK_SUPPLIER, FK_CATEGORY, QUANTITY_AVAILABLE, CREATED_AT) values (1000, 'Crise nas infinitas terras', 1000, 1000, 10, CURRENT_TIMESTAMP);

insert into PRODUCT (ID, NAME, FK_SUPPLIER, FK_CATEGORY, QUANTITY_AVAILABLE, CREATED_AT) values (1001, 'Interestelar', 1001, 1001, 5, CURRENT_TIMESTAMP);

insert into PRODUCT (ID, NAME, FK_SUPPLIER, FK_CATEGORY, QUANTITY_AVAILABLE, CREATED_AT) values (1002, 'Harry potter E A Pedra Filosofal', 1001, 1002, 3, CURRENT_TIMESTAMP);