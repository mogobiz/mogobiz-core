alter table account drop constraint FKB9D38A2D4BE157D4;
alter table account drop constraint FKB9D38A2D602DCF9;
alter table account_xrole drop constraint FKE0E95C5C722BA4BD;
alter table account_xrole drop constraint FKE0E95C5CD411C165;
alter table album drop constraint FK5897E6F602DCF9;
alter table b_o_cart drop constraint FK28764B6D602DCF9;
alter table b_o_cart_item drop constraint FK6C2D04A583966C6F;
alter table b_o_cart_item_b_o_product drop constraint FK9CBD90E89ADC15EF;
alter table b_o_cart_item_b_o_product drop constraint FK9CBD90E8D5BC215E;
alter table b_o_product drop constraint FK6A96FD8255ECAFB9;
alter table b_o_product_consumption drop constraint FK7DEB1C5E482BBE0F;
alter table b_o_product_consumption drop constraint FK7DEB1C5EC732E1A3;
alter table b_o_ticket_type drop constraint FK929693A0AA1CE819;
alter table brand drop constraint FK59A4B87388BBDF6;
alter table brand drop constraint FK59A4B879ECA6F9;
alter table brand drop constraint FK59A4B87602DCF9;
alter table brand_property drop constraint FKC8B7CB8DBBA1B1F9;
alter table category drop constraint FK302BCFE7ACA6B63;
alter table category drop constraint FK302BCFE9ECA6F9;
alter table category drop constraint FK302BCFEA332E779;
alter table category drop constraint FK302BCFE602DCF9;
alter table company drop constraint FK38A73C7D4BE157D4;
alter table company drop constraint FK38A73C7D95DE64BC;
alter table company drop constraint FK38A73C7D66C87106;
alter table company drop constraint FK38A73C7DE9DA1D86;
alter table company_property drop constraint FKDBCB8F57602DCF9;
alter table consumption drop constraint FKCD71F39B3F4326A2;
alter table coupon drop constraint FKAF42D826B59D3920;
alter table coupon drop constraint FKAF42D826602DCF9;
alter table coupon_category drop constraint FK9DBEE0F7E2497A99;
alter table coupon_category drop constraint FK9DBEE0F75B0C66E5;
alter table coupon_product drop constraint FK8730C5D69035CA51;
alter table coupon_product drop constraint FK8730C5D655ECB00F;
alter table coupon_reduction_rule drop constraint FK3BE2B281169FC5DE;
alter table coupon_reduction_rule drop constraint FK3BE2B2818AF42876;
alter table coupon_ticket_type drop constraint FKC1AC11F4504E2EF;
alter table coupon_ticket_type drop constraint FKC1AC11F42FDE5D9E;
alter table date_period drop constraint FK4A56237255ECAFB9;
alter table es_env drop constraint FKB2DABDDC602DCF9;
alter table event drop constraint FK5C6729A793C84EF;
alter table event drop constraint FK5C6729A31A24C8F;
alter table event drop constraint FK5C6729A55ECAFB9;
alter table event_period_sale drop constraint FK22F2DE055ECAFB9;
alter table external_account drop constraint FK5A0D99B9793C84EF;
alter table feature drop constraint FKC5A27AF65B0C668F;
alter table feature drop constraint FKC5A27AF655ECAFB9;
alter table google_variation_mapping drop constraint FK60362A1CF0CF6B53;
alter table google_variation_mapping drop constraint FK60362A1C602DCF9;
alter table google_variation_mapping drop constraint FK60362A1CFD6CB975;
alter table google_variation_value drop constraint FKF3469E3FF0CF6B53;
alter table ibeacon drop constraint FK5F6619ED602DCF9;
alter table intra_day_period drop constraint FKF581D04555ECAFB9;
alter table location drop constraint FK714F9FB59319BF09;
alter table poi_type drop constraint FK1AF5740F49BA7844;
alter table product drop constraint FKED8DCCEF5B0C668F;
alter table product drop constraint FKED8DCCEFA797D6B4;
alter table product drop constraint FKED8DCCEFBBA1B1F9;
alter table product drop constraint FKED8DCCEFA99249AF;
alter table product drop constraint FKED8DCCEFF333CFD0;
alter table product drop constraint FKED8DCCEFBF86FC94;
alter table product drop constraint FKED8DCCEF9ECA6F9;
alter table product drop constraint FKED8DCCEF602DCF9;
alter table product drop constraint FKED8DCCEF1EE8688F;
alter table product2_resource drop constraint FK8AABBAA31A24C8F;
alter table product2_resource drop constraint FK8AABBAA55ECAFB9;
alter table product_property drop constraint FK61FB5F2555ECAFB9;
alter table product_tag drop constraint FKA71CAC4A912A004F;
alter table product_tag drop constraint FKA71CAC4A4347CEAF;
alter table role_permission drop constraint FKBD40D538D411C10F;
alter table role_permission drop constraint FKBD40D538F3E69A2F;
alter table seller drop constraint FKC9FF4F7F8201A451;
alter table seller_company drop constraint FKCC6A43BD602DD4F;
alter table seller_company drop constraint FKCC6A43BD3E1049D3;
alter table shipping_rule drop constraint FK2062CEED602DCF9;
alter table stock_calendar drop constraint FK89EECE472FDE5D48;
alter table stock_calendar drop constraint FK89EECE4755ECAFB9;
alter table suggestion drop constraint FK4763CA04BD55124F;
alter table suggestion drop constraint FK4763CA0455ECAFB9;
alter table tag drop constraint FK1BF9A9ECA6F9;
alter table tax_rate drop constraint FKEF7A58F4602DCF9;
alter table tax_rate_local_tax_rate drop constraint FK859561F3982D71D;
alter table tax_rate_local_tax_rate drop constraint FK859561F3279E7339;
alter table ticket_type drop constraint FK158DE48D8AC3CD9F;
alter table ticket_type drop constraint FK158DE48D6255814E;
alter table ticket_type drop constraint FK158DE48D6255F5AD;
alter table ticket_type drop constraint FK158DE48D62550CEF;
alter table ticket_type drop constraint FK158DE48D55ECAFB9;
alter table token drop constraint FK696B9F9793C84EF;
alter table user_permission drop constraint FK30BA72C3793C84EF;
alter table user_permission drop constraint FK30BA72C3F3E69A2F;
alter table user_property drop constraint FKC7D137C9793C84EF;
alter table variation drop constraint FKFB1DA2135B0C668F;
alter table variation_value drop constraint FKB8BB7C45284D93F9;
alter table xcatalog drop constraint FKD2AC68A1602DCF9;
alter table xresource drop constraint FK6BBFCC86A797D6B4;
alter table xresource drop constraint FK6BBFCC86BF86FC94;
alter table xresource drop constraint FK6BBFCC86602DCF9;
alter table xresource drop constraint FK6BBFCC861865E8F9;
drop table account cascade;
drop table account_xrole cascade;
drop table album cascade;
drop table b_o_cart cascade;
drop table b_o_cart_item cascade;
drop table b_o_cart_item_b_o_product cascade;
drop table b_o_product cascade;
drop table b_o_product_consumption cascade;
drop table b_o_ticket_type cascade;
drop table brand cascade;
drop table brand_property cascade;
drop table category cascade;
drop table company cascade;
drop table company_property cascade;
drop table consumption cascade;
drop table coupon cascade;
drop table coupon_category cascade;
drop table coupon_product cascade;
drop table coupon_reduction_rule cascade;
drop table coupon_ticket_type cascade;
drop table date_period cascade;
drop table es_env cascade;
drop table event cascade;
drop table event_period_sale cascade;
drop table external_account cascade;
drop table external_auth_login cascade;
drop table feature cascade;
drop table google_category cascade;
drop table google_content cascade;
drop table google_env cascade;
drop table google_variation_mapping cascade;
drop table google_variation_type cascade;
drop table google_variation_value cascade;
drop table ibeacon cascade;
drop table intra_day_period cascade;
drop table local_tax_rate cascade;
drop table location cascade;
drop table pending_email_confirmation cascade;
drop table permission cascade;
drop table poi_type cascade;
drop table product cascade;
drop table product2_resource cascade;
drop table product_property cascade;
drop table product_tag cascade;
drop table reduction_rule cascade;
drop table reduction_sold cascade;
drop table role_permission cascade;
drop table seller cascade;
drop table seller_company cascade;
drop table shipping cascade;
drop table shipping_carriers cascade;
drop table shipping_rule cascade;
drop table stock cascade;
drop table stock_calendar cascade;
drop table suggestion cascade;
drop table tag cascade;
drop table tax_rate cascade;
drop table tax_rate_local_tax_rate cascade;
drop table ticket_type cascade;
drop table token cascade;
drop table user_permission cascade;
drop table user_property cascade;
drop table uuid_data cascade;
drop table variation cascade;
drop table variation_value cascade;
drop table xcatalog cascade;
drop table xresource cascade;
drop table xrole cascade;
drop table xtranslation cascade;
drop sequence hibernate_sequence;