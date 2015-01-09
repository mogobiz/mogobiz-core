
alter table category add column return_max_delay int8;
update category set return_max_delay = 0;
alter table category ALTER COLUMN return_max_delay SET NOT NULL;

alter table product add column return_max_delay int8;
update product set return_max_delay = 0;
alter table product ALTER COLUMN return_max_delay SET NOT NULL;

alter table xcatalog add column return_max_delay int8;
update xcatalog set return_max_delay = 0;
alter table xcatalog ALTER COLUMN return_max_delay SET NOT NULL;


/* REASSIGN OWNED BY iper2010 to mogobiz; */
