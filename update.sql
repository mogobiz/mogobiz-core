alter table coupon add column consumed int8;
update coupon set consumed = 0;
alter table coupon ALTER COLUMN consumed SET NOT NULL;

alter table es_env add column success bool;
update es_env set success = false;
alter table es_env ALTER COLUMN success SET NOT NULL;


alter table tag add column company_fk int8;
update tag set company_fk = 9 where id < 1000;
update tag set company_fk =31260 where id >= 1000;
alter table tag ALTER COLUMN company_fk SET NOT NULL;
delete from tag where id not in (select tag_id from product_tag);

alter table feature_value add constraint FKBA64DEE8BC891E59 foreign key (feature_fk) references feature;
alter table feature_value add constraint FKBA64DEE855ECAFB9 foreign key (product_fk) references product;

alter table tag add constraint FK1BF9A602DCF9 foreign key (company_fk) references company;


/* REASSIGN OWNED BY iper2010 to mogobiz; */
