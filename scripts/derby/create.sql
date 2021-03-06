create table account (id bigint generated by default as identity, account_type varchar(255), active smallint not null, autosign smallint not null, birthdate timestamp, civility varchar(255), company_fk bigint, date_created timestamp not null, email varchar(255) not null unique, external_id varchar(255), first_name varchar(255), last_name varchar(255), last_updated timestamp not null, location_fk bigint, login varchar(255) not null unique, password varchar(255) not null, phone varchar(255), token varchar(255), token_secret varchar(255), uuid varchar(255) not null, primary key (id));
create table account_xrole (roles_fk bigint, role_id bigint);
create table album (id bigint generated by default as identity, company_fk bigint, date_created timestamp not null, description varchar(255), last_updated timestamp not null, name varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table b_o_cart (id bigint generated by default as identity, buyer varchar(255) not null, company_fk bigint not null, currency_code varchar(255) not null, currency_rate double not null, xdate timestamp not null, date_created timestamp not null, last_updated timestamp not null, price bigint not null, status varchar(255) not null, transaction_uuid varchar(255) not null unique, uuid varchar(255) not null, primary key (id));
create table b_o_cart_item (id bigint generated by default as identity, b_o_cart_fk bigint not null, code varchar(255) not null unique, date_created timestamp not null, end_date timestamp, end_price bigint not null, hidden smallint not null, last_updated timestamp not null, price bigint not null, quantity integer not null, start_date timestamp, tax float not null, ticket_type_fk bigint not null, total_end_price bigint not null, total_price bigint not null, uuid varchar(255) not null, primary key (id));
create table b_o_cart_item_b_o_product (b_o_products_fk bigint, boproduct_id bigint);
create table b_o_product (id bigint generated by default as identity, acquittement smallint not null, date_created timestamp not null, last_updated timestamp not null, price bigint not null, principal smallint not null, product_fk bigint, uuid varchar(255) not null, primary key (id));
create table b_o_product_consumption (consumptions_fk bigint, consumption_id bigint);
create table b_o_ticket_type (id bigint generated by default as identity, age integer not null, b_o_product_fk bigint not null, birthdate timestamp, date_created timestamp not null, email varchar(255), end_date timestamp, firstname varchar(255), last_updated timestamp not null, lastname varchar(255), phone varchar(255), price bigint not null, qrcode long varchar, qrcode_content long varchar, quantity integer not null, short_code varchar(255), start_date timestamp, ticket_type varchar(255), uuid varchar(255) not null, primary key (id));
create table brand (id bigint generated by default as identity, company_fk bigint not null, date_created timestamp not null, description long varchar, facebooksite varchar(255), hide smallint not null, ibeacon_fk bigint, last_updated timestamp not null, name varchar(255) not null, parent_fk bigint, twitter varchar(255), uuid varchar(255) not null, website varchar(255), primary key (id));
create table brand_property (id bigint generated by default as identity, brand_fk bigint not null, date_created timestamp not null, last_updated timestamp not null, name varchar(255) not null, uuid varchar(255) not null, value varchar(255), primary key (id));
create table category (id bigint generated by default as identity, catalog_fk bigint, company_fk bigint not null, date_created timestamp not null, deleted smallint not null, description long varchar, external_code varchar(255), google_category varchar(255), hide smallint not null, ibeacon_fk bigint, keywords varchar(255), last_updated timestamp not null, name varchar(255) not null, parent_fk bigint, position integer not null, sanitized_name varchar(255), uuid varchar(255) not null, primary key (id));
create table company (id bigint generated by default as identity, aes_password varchar(255) not null, api_key varchar(255), code varchar(255) not null, country_code varchar(255), currency_code varchar(255), date_created timestamp not null, default_language varchar(255) not null, email varchar(255), external_code varchar(255), gakey varchar(255), google_content_fk bigint, google_env_fk bigint, handling_time integer, last_updated timestamp not null, location_fk bigint, map_provider varchar(255), name varchar(255) not null, online_validation smallint not null, phone varchar(255), refund_policy varchar(255), return_policy integer, ship_from_fk bigint, shipping_carriers_fedex smallint, shipping_carriers_ups smallint, shipping_international smallint not null, start_date timestamp, stop_date timestamp, temp_session_id varchar(255), uuid varchar(255) not null, website varchar(255), weight_unit varchar(255), primary key (id));
create table company_property (id bigint generated by default as identity, company_fk bigint not null, date_created timestamp not null, last_updated timestamp not null, name varchar(255) not null, uuid varchar(255) not null, value varchar(255) not null, primary key (id));
create table consumption (id bigint generated by default as identity, b_o_ticket_type_fk bigint, xdate timestamp not null, date_created timestamp not null, last_updated timestamp not null, uuid varchar(255) not null, primary key (id));
create table coupon (id bigint generated by default as identity, active smallint not null, anonymous smallint not null, catalog_wise smallint not null, code varchar(255) not null unique, company_fk bigint not null, date_created timestamp not null, description long varchar, end_date timestamp, for_sale smallint not null, last_updated timestamp not null, name varchar(255) not null, number_of_uses bigint, reduction_sold_fk bigint, start_date timestamp, uuid varchar(255) not null, primary key (id));
create table coupon_category (categories_fk bigint, category_id bigint);
create table coupon_product (products_fk bigint, product_id bigint);
create table coupon_reduction_rule (rules_fk bigint, reduction_rule_id bigint);
create table coupon_ticket_type (ticket_types_fk bigint, ticket_type_id bigint);
create table date_period (id bigint generated by default as identity, date_created timestamp not null, end_date timestamp not null, last_updated timestamp not null, product_fk bigint, start_date timestamp not null, uuid varchar(255) not null, primary key (id));
create table es_env (id bigint generated by default as identity, active smallint not null, company_fk bigint not null, cron_expr varchar(255) not null, date_created timestamp not null, extra long varchar, idx varchar(255), last_updated timestamp not null, name varchar(255) not null, running smallint not null, url varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table event (id bigint generated by default as identity, xdate timestamp not null, date_created timestamp not null, description long varchar, last_updated timestamp not null, product_fk bigint, resource_fk bigint, user_fk bigint not null, uuid varchar(255) not null, xtype varchar(255) not null, primary key (id));
create table event_period_sale (id bigint generated by default as identity, date_created timestamp not null, event_date timestamp, event_start_time timestamp, last_updated timestamp not null, nb_ticket_sold bigint not null, product_fk bigint, uuid varchar(255) not null, primary key (id));
create table external_account (id bigint generated by default as identity, account_type varchar(255) not null, date_created timestamp not null, external_id varchar(255), last_updated timestamp not null, login varchar(255) not null, token varchar(255), token_secret varchar(255), user_fk bigint not null, uuid varchar(255) not null, primary key (id));
create table external_auth_login (id bigint generated by default as identity, account_type varchar(255) not null, birth_date varchar(255), city varchar(255), country varchar(255), date_created timestamp not null, email varchar(255), external_id varchar(255), first_name varchar(255), gender varchar(255), instant timestamp, last_name varchar(255), last_updated timestamp not null, login varchar(255) not null, mobile varchar(255), postal_code varchar(255), road1 varchar(255), road2 varchar(255), road3 varchar(255), road4 varchar(255), token varchar(255), token_secret varchar(255), uuid varchar(255) not null, primary key (id));
create table feature (id bigint generated by default as identity, category_fk bigint, date_created timestamp not null, domain varchar(255), external_code varchar(255), hide smallint not null, last_updated timestamp not null, name varchar(255) not null, position integer not null, product_fk bigint, uuid varchar(255) not null, value varchar(255), primary key (id));
create table google_category (id bigint generated by default as identity, date_created timestamp not null, lang varchar(255) not null, last_updated timestamp not null, name varchar(255) not null, parent_path varchar(512), path varchar(512) not null, uuid varchar(255) not null, primary key (id));
create table google_content (id bigint generated by default as identity, account_id varchar(255) not null, account_login varchar(255) not null, account_password varchar(255) not null, account_type varchar(255) not null, date_created timestamp not null, google_search smallint not null, last_updated timestamp not null, uuid varchar(255) not null, primary key (id));
create table google_env (id bigint generated by default as identity, active smallint not null, client_id varchar(255), client_secret varchar(255), client_token varchar(255), cron_expr varchar(255) not null, date_created timestamp not null, dry_run smallint not null, extra long varchar, last_updated timestamp not null, merchant_id varchar(255) not null, merchant_url varchar(255), running smallint not null, uuid varchar(255) not null, primary key (id));
create table google_variation_mapping (id bigint generated by default as identity, company_fk bigint not null, date_created timestamp not null, last_updated timestamp not null, mappings varchar(255) not null, type_fk bigint, uuid varchar(255) not null, value_fk bigint, primary key (id));
create table google_variation_type (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, uuid varchar(255) not null, xtype varchar(255) not null unique, primary key (id));
create table google_variation_value (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, type_fk bigint not null, uuid varchar(255) not null, value varchar(255) not null unique, primary key (id));
create table ibeacon (id bigint generated by default as identity, active smallint not null, company_fk bigint not null, date_created timestamp not null, end_date timestamp not null, last_updated timestamp not null, major varchar(255) not null, minor varchar(255) not null, name varchar(255) not null, start_date timestamp not null, uuid varchar(255) not null, primary key (id));
create table intra_day_period (id bigint generated by default as identity, date_created timestamp not null, end_date timestamp, last_updated timestamp not null, product_fk bigint, start_date timestamp, uuid varchar(255) not null, weekday1 smallint not null, weekday2 smallint not null, weekday3 smallint not null, weekday4 smallint not null, weekday5 smallint not null, weekday6 smallint not null, weekday7 smallint not null, primary key (id));
create table local_tax_rate (id bigint generated by default as identity, active smallint not null, country_code varchar(255), date_created timestamp not null, last_updated timestamp not null, rate float not null, state_code varchar(255), uuid varchar(255) not null, primary key (id));
create table location (id bigint generated by default as identity, city varchar(255), country_code varchar(255) not null, date_created timestamp not null, last_updated timestamp not null, latitude double, longitude double, postal_code varchar(255), road1 varchar(255), road2 varchar(255), road3 varchar(255), road_num varchar(255), state varchar(255), uuid varchar(255) not null, class varchar(255) not null, description varchar(255), detail long varchar, external_id varchar(255), is_main smallint, max_price float, min_price float, name varchar(255), picture varchar(255), picture_type varchar(255), poi_type_fk bigint, source varchar(255), video varchar(255), visibility varchar(255), primary key (id));
create table pending_email_confirmation (id bigint generated by default as identity, version bigint not null, confirmation_event varchar(80), confirmation_token varchar(80) not null unique, email_address varchar(80) not null, timestamp timestamp not null, user_token varchar(500), primary key (id));
create table permission (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, possible_actions varchar(255) not null, type varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table poi_type (id bigint generated by default as identity, code varchar(255) not null, date_created timestamp not null, icon_fk bigint, last_updated timestamp not null, uuid varchar(255) not null, xtype varchar(255) not null unique, primary key (id));
create table product (id bigint generated by default as identity, availability_date timestamp, brand_fk bigint, calendar_type varchar(255), category_fk bigint not null, code varchar(255) not null, company_fk bigint not null, creation_fk bigint, date_created timestamp not null, deleted smallint not null, description long varchar, description_as_text long varchar, external_code varchar(255), hide smallint not null, ibeacon_fk bigint, keywords varchar(255), last_updated timestamp not null, modification_date timestamp, name varchar(255) not null, nb_sales bigint not null, picture varchar(255), poi_fk bigint, price bigint not null, sanitized_name varchar(255) not null, seller_fk bigint, shipping_fk bigint, start_date timestamp, start_feature_date timestamp, state varchar(255), stock_display smallint, stop_date timestamp, stop_feature_date timestamp, tax_rate_fk bigint, uuid varchar(255) not null, xtype varchar(255) not null, primary key (id));
create table product2_resource (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, position integer not null, product_fk bigint not null, resource_fk bigint not null, uuid varchar(255) not null, primary key (id));
create table product_property (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, name varchar(255) not null, product_fk bigint not null, uuid varchar(255) not null, value varchar(255), primary key (id));
create table product_tag (tags_fk bigint, tag_id bigint);
create table reduction_rule (id bigint generated by default as identity, date_created timestamp not null, discount varchar(255), last_updated timestamp not null, quantity_max bigint, quantity_min bigint, uuid varchar(255) not null, x_purchased bigint, xtype varchar(255) not null, y_offered bigint, primary key (id));
create table reduction_sold (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, sold bigint not null, uuid varchar(255) not null, primary key (id));
create table role_permission (id bigint generated by default as identity, actions varchar(255) not null, date_created timestamp not null, last_updated timestamp not null, permission_fk bigint not null, role_fk bigint not null, target varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table seller (id bigint not null, admin smallint not null, agent smallint not null, sell smallint not null, validator smallint not null, primary key (id));
create table seller_company (companies_fk bigint, company_id bigint);
create table shipping (id bigint generated by default as identity, amount bigint not null, date_created timestamp not null, depth bigint not null, free smallint not null, height bigint not null, last_updated timestamp not null, linear_unit varchar(255) not null, uuid varchar(255) not null, weight bigint not null, weight_unit varchar(255) not null, width bigint not null, primary key (id));
create table shipping_carriers (id bigint generated by default as identity, fedex smallint not null, ups smallint not null, primary key (id));
create table shipping_rule (id bigint generated by default as identity, company_fk bigint, country_code varchar(255) not null, date_created timestamp not null, last_updated timestamp not null, max_amount bigint not null, min_amount bigint not null, price varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table stock (id bigint generated by default as identity, stock bigint, stock_out_selling smallint not null, stock_unlimited smallint not null, primary key (id));
create table stock_calendar (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, product_fk bigint not null, sold bigint not null, start_date timestamp, stock bigint not null, ticket_type_fk bigint not null, uuid varchar(255) not null, primary key (id));
create table suggestion (id bigint generated by default as identity, date_created timestamp not null, discount varchar(255), last_updated timestamp not null, pack_fk bigint not null, position integer not null, product_fk bigint not null, required smallint not null, uuid varchar(255) not null, primary key (id));
create table tag (id bigint generated by default as identity, date_created timestamp not null, ibeacon_fk bigint, last_updated timestamp not null, name varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table tax_rate (id bigint generated by default as identity, company_fk bigint, date_created timestamp not null, last_updated timestamp not null, name varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table tax_rate_local_tax_rate (local_tax_rates_fk bigint, local_tax_rate_id bigint);
create table ticket_type (id bigint generated by default as identity, availability_date timestamp, date_created timestamp not null, description long varchar, external_code varchar(255), gtin varchar(255), last_updated timestamp not null, max_order integer not null, min_order integer not null, mpn varchar(255), name varchar(255) not null, nb_sales bigint not null, picture_fk bigint, position integer, price bigint not null, product_fk bigint not null, sku varchar(255) not null, start_date timestamp, stock_stock bigint, stock_stock_out_selling smallint, stock_stock_unlimited smallint, stop_date timestamp, uuid varchar(255) not null, variation1_fk bigint, variation2_fk bigint, variation3_fk bigint, xprivate smallint, primary key (id));
create table token (id bigint generated by default as identity, client_id varchar(255) not null, date_created timestamp not null, expires_in integer not null, last_updated timestamp not null, redirect_u_r_i varchar(255) not null, scope varchar(255), state varchar(255), user_fk bigint not null, uuid varchar(255) not null, value varchar(255) not null, primary key (id));
create table user_permission (id bigint generated by default as identity, actions varchar(255) not null, date_created timestamp not null, last_updated timestamp not null, permission_fk bigint not null, target varchar(255) not null, user_fk bigint not null, uuid varchar(255) not null, primary key (id));
create table user_property (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, name varchar(255) not null, user_fk bigint not null, uuid varchar(255) not null, value varchar(255), primary key (id));
create table uuid_data (id bigint generated by default as identity, date_created timestamp not null, expire_date timestamp not null, last_updated timestamp not null, payload long varchar, uuid varchar(255) not null, xtype varchar(255) not null, primary key (id));
create table variation (id bigint generated by default as identity, category_fk bigint, date_created timestamp not null, external_code varchar(255), google_variation_type varchar(255), hide smallint not null, last_updated timestamp not null, name varchar(255) not null, position integer not null, uuid varchar(255) not null, primary key (id));
create table variation_value (id bigint generated by default as identity, date_created timestamp not null, external_code varchar(255), google_variation_value varchar(255), last_updated timestamp not null, position integer not null, uuid varchar(255) not null, value varchar(255) not null, variation_fk bigint not null, primary key (id));
create table xcatalog (id bigint generated by default as identity, activation_date timestamp not null, channels varchar(255), company_fk bigint not null, date_created timestamp not null, deleted smallint not null, description long varchar, external_code varchar(255), last_updated timestamp not null, name varchar(255) not null, social smallint not null, uuid varchar(255) not null, primary key (id));
create table xresource (id bigint generated by default as identity, account_type varchar(255), active smallint not null, album_fk bigint, code varchar(255), company_fk bigint, content long varchar, content_type varchar(255), creation_fk bigint, date_created timestamp not null, deleted smallint not null, description long varchar, external_code varchar(255), last_updated timestamp not null, name varchar(255), poi_fk bigint, sanitized_name varchar(255) not null, small_picture varchar(255), uploaded smallint not null, url varchar(255), uuid varchar(255) not null, xtype varchar(255) not null, primary key (id));
create table xrole (id bigint generated by default as identity, date_created timestamp not null, last_updated timestamp not null, name varchar(255) not null, uuid varchar(255) not null, primary key (id));
create table xtranslation (id bigint generated by default as identity, company_id bigint not null, date_created timestamp not null, lang varchar(255) not null, last_updated timestamp not null, target bigint not null, type varchar(255), uuid varchar(255) not null, value long varchar not null, primary key (id));
alter table account add constraint FKB9D38A2D4BE157D4 foreign key (location_fk) references location;
alter table account add constraint FKB9D38A2D602DCF9 foreign key (company_fk) references company;
alter table account_xrole add constraint FKE0E95C5C722BA4BD foreign key (roles_fk) references account;
alter table account_xrole add constraint FKE0E95C5CD411C165 foreign key (role_id) references xrole;
alter table album add constraint FK5897E6F602DCF9 foreign key (company_fk) references company;
alter table b_o_cart add constraint FK28764B6D602DCF9 foreign key (company_fk) references company;
alter table b_o_cart_item add constraint FK6C2D04A583966C6F foreign key (b_o_cart_fk) references b_o_cart;
alter table b_o_cart_item_b_o_product add constraint FK9CBD90E89ADC15EF foreign key (boproduct_id) references b_o_product;
alter table b_o_cart_item_b_o_product add constraint FK9CBD90E8D5BC215E foreign key (b_o_products_fk) references b_o_cart_item;
alter table b_o_product add constraint FK6A96FD8255ECAFB9 foreign key (product_fk) references product;
alter table b_o_product_consumption add constraint FK7DEB1C5E482BBE0F foreign key (consumption_id) references consumption;
alter table b_o_product_consumption add constraint FK7DEB1C5EC732E1A3 foreign key (consumptions_fk) references b_o_product;
alter table b_o_ticket_type add constraint FK929693A0AA1CE819 foreign key (b_o_product_fk) references b_o_product;
alter table brand add constraint FK59A4B87388BBDF6 foreign key (parent_fk) references brand;
alter table brand add constraint FK59A4B879ECA6F9 foreign key (ibeacon_fk) references ibeacon;
alter table brand add constraint FK59A4B87602DCF9 foreign key (company_fk) references company;
alter table brand_property add constraint FKC8B7CB8DBBA1B1F9 foreign key (brand_fk) references brand;
alter table category add constraint FK302BCFE7ACA6B63 foreign key (parent_fk) references category;
alter table category add constraint FK302BCFE9ECA6F9 foreign key (ibeacon_fk) references ibeacon;
alter table category add constraint FK302BCFEA332E779 foreign key (catalog_fk) references xcatalog;
alter table category add constraint FK302BCFE602DCF9 foreign key (company_fk) references company;
alter table company add constraint FK38A73C7D4BE157D4 foreign key (location_fk) references location;
alter table company add constraint FK38A73C7D95DE64BC foreign key (ship_from_fk) references location;
alter table company add constraint FK38A73C7D66C87106 foreign key (google_content_fk) references google_content;
alter table company add constraint FK38A73C7DE9DA1D86 foreign key (google_env_fk) references google_env;
alter table company_property add constraint FKDBCB8F57602DCF9 foreign key (company_fk) references company;
alter table consumption add constraint FKCD71F39B3F4326A2 foreign key (b_o_ticket_type_fk) references b_o_ticket_type;
alter table coupon add constraint FKAF42D826B59D3920 foreign key (reduction_sold_fk) references reduction_sold;
alter table coupon add constraint FKAF42D826602DCF9 foreign key (company_fk) references company;
alter table coupon_category add constraint FK9DBEE0F7E2497A99 foreign key (categories_fk) references coupon;
alter table coupon_category add constraint FK9DBEE0F75B0C66E5 foreign key (category_id) references category;
alter table coupon_product add constraint FK8730C5D69035CA51 foreign key (products_fk) references coupon;
alter table coupon_product add constraint FK8730C5D655ECB00F foreign key (product_id) references product;
alter table coupon_reduction_rule add constraint FK3BE2B281169FC5DE foreign key (rules_fk) references coupon;
alter table coupon_reduction_rule add constraint FK3BE2B2818AF42876 foreign key (reduction_rule_id) references reduction_rule;
alter table coupon_ticket_type add constraint FKC1AC11F4504E2EF foreign key (ticket_types_fk) references coupon;
alter table coupon_ticket_type add constraint FKC1AC11F42FDE5D9E foreign key (ticket_type_id) references ticket_type;
alter table date_period add constraint FK4A56237255ECAFB9 foreign key (product_fk) references product;
alter table es_env add constraint FKB2DABDDC602DCF9 foreign key (company_fk) references company;
alter table event add constraint FK5C6729A793C84EF foreign key (user_fk) references account;
alter table event add constraint FK5C6729A31A24C8F foreign key (resource_fk) references xresource;
alter table event add constraint FK5C6729A55ECAFB9 foreign key (product_fk) references product;
alter table event_period_sale add constraint FK22F2DE055ECAFB9 foreign key (product_fk) references product;
alter table external_account add constraint FK5A0D99B9793C84EF foreign key (user_fk) references account;
alter table feature add constraint FKC5A27AF65B0C668F foreign key (category_fk) references category;
alter table feature add constraint FKC5A27AF655ECAFB9 foreign key (product_fk) references product;
alter table google_variation_mapping add constraint FK60362A1CF0CF6B53 foreign key (type_fk) references google_variation_type;
alter table google_variation_mapping add constraint FK60362A1C602DCF9 foreign key (company_fk) references company;
alter table google_variation_mapping add constraint FK60362A1CFD6CB975 foreign key (value_fk) references google_variation_value;
alter table google_variation_value add constraint FKF3469E3FF0CF6B53 foreign key (type_fk) references google_variation_type;
alter table ibeacon add constraint FK5F6619ED602DCF9 foreign key (company_fk) references company;
alter table intra_day_period add constraint FKF581D04555ECAFB9 foreign key (product_fk) references product;
alter table location add constraint FK714F9FB59319BF09 foreign key (poi_type_fk) references poi_type;
create index emailconf_timestamp_Idx on pending_email_confirmation (timestamp);
create index emailconf_token_Idx on pending_email_confirmation (confirmation_token);
alter table poi_type add constraint FK1AF5740F49BA7844 foreign key (icon_fk) references xresource;
alter table product add constraint FKED8DCCEF5B0C668F foreign key (category_fk) references category;
alter table product add constraint FKED8DCCEFA797D6B4 foreign key (creation_fk) references event;
alter table product add constraint FKED8DCCEFBBA1B1F9 foreign key (brand_fk) references brand;
alter table product add constraint FKED8DCCEFA99249AF foreign key (seller_fk) references seller;
alter table product add constraint FKED8DCCEFF333CFD0 foreign key (tax_rate_fk) references tax_rate;
alter table product add constraint FKED8DCCEFBF86FC94 foreign key (poi_fk) references location;
alter table product add constraint FKED8DCCEF9ECA6F9 foreign key (ibeacon_fk) references ibeacon;
alter table product add constraint FKED8DCCEF602DCF9 foreign key (company_fk) references company;
alter table product add constraint FKED8DCCEF1EE8688F foreign key (shipping_fk) references shipping;
alter table product2_resource add constraint FK8AABBAA31A24C8F foreign key (resource_fk) references xresource;
alter table product2_resource add constraint FK8AABBAA55ECAFB9 foreign key (product_fk) references product;
alter table product_property add constraint FK61FB5F2555ECAFB9 foreign key (product_fk) references product;
alter table product_tag add constraint FKA71CAC4A912A004F foreign key (tags_fk) references product;
alter table product_tag add constraint FKA71CAC4A4347CEAF foreign key (tag_id) references tag;
alter table role_permission add constraint FKBD40D538D411C10F foreign key (role_fk) references xrole;
alter table role_permission add constraint FKBD40D538F3E69A2F foreign key (permission_fk) references permission;
alter table seller add constraint FKC9FF4F7F8201A451 foreign key (id) references account;
alter table seller_company add constraint FKCC6A43BD602DD4F foreign key (company_id) references company;
alter table seller_company add constraint FKCC6A43BD3E1049D3 foreign key (companies_fk) references seller;
alter table shipping_rule add constraint FK2062CEED602DCF9 foreign key (company_fk) references company;
alter table stock_calendar add constraint FK89EECE472FDE5D48 foreign key (ticket_type_fk) references ticket_type;
alter table stock_calendar add constraint FK89EECE4755ECAFB9 foreign key (product_fk) references product;
alter table suggestion add constraint FK4763CA04BD55124F foreign key (pack_fk) references product;
alter table suggestion add constraint FK4763CA0455ECAFB9 foreign key (product_fk) references product;
alter table tag add constraint FK1BF9A9ECA6F9 foreign key (ibeacon_fk) references ibeacon;
alter table tax_rate add constraint FKEF7A58F4602DCF9 foreign key (company_fk) references company;
alter table tax_rate_local_tax_rate add constraint FK859561F3982D71D foreign key (local_tax_rate_id) references local_tax_rate;
alter table tax_rate_local_tax_rate add constraint FK859561F3279E7339 foreign key (local_tax_rates_fk) references tax_rate;
alter table ticket_type add constraint FK158DE48D8AC3CD9F foreign key (picture_fk) references xresource;
alter table ticket_type add constraint FK158DE48D6255814E foreign key (variation2_fk) references variation_value;
alter table ticket_type add constraint FK158DE48D6255F5AD foreign key (variation3_fk) references variation_value;
alter table ticket_type add constraint FK158DE48D62550CEF foreign key (variation1_fk) references variation_value;
alter table ticket_type add constraint FK158DE48D55ECAFB9 foreign key (product_fk) references product;
alter table token add constraint FK696B9F9793C84EF foreign key (user_fk) references account;
alter table user_permission add constraint FK30BA72C3793C84EF foreign key (user_fk) references account;
alter table user_permission add constraint FK30BA72C3F3E69A2F foreign key (permission_fk) references permission;
alter table user_property add constraint FKC7D137C9793C84EF foreign key (user_fk) references account;
alter table variation add constraint FKFB1DA2135B0C668F foreign key (category_fk) references category;
alter table variation_value add constraint FKB8BB7C45284D93F9 foreign key (variation_fk) references variation;
alter table xcatalog add constraint FKD2AC68A1602DCF9 foreign key (company_fk) references company;
alter table xresource add constraint FK6BBFCC86A797D6B4 foreign key (creation_fk) references event;
alter table xresource add constraint FK6BBFCC86BF86FC94 foreign key (poi_fk) references location;
alter table xresource add constraint FK6BBFCC86602DCF9 foreign key (company_fk) references company;
alter table xresource add constraint FK6BBFCC861865E8F9 foreign key (album_fk) references album;
CREATE SEQUENCE mogobiz_sequence as BIGINT START WITH -10000000000 INCREMENT BY 1;