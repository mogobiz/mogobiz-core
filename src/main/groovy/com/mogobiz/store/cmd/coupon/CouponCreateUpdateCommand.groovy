package com.mogobiz.store.cmd.coupon

import com.mogobiz.store.domain.Catalog
import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ReductionRule
import com.mogobiz.store.domain.TicketType
import grails.validation.Validateable

class CouponCreateUpdateCommand implements Validateable{
    Long id
    String name
    String description
    String pastille
    String code
    Boolean active
    Boolean catalogWise
    Boolean anonymous
    Long numberOfUses
    Calendar startDate
    Calendar endDate
    List<com.mogobiz.store.domain.Category> categories = [].withLazyDefault { new com.mogobiz.store.domain.Category() }
    List<Product> products = [].withLazyDefault { new Product() }
    List<TicketType> skus = [].withLazyDefault { new TicketType() }
    List<ReductionRule> rules = [].withLazyDefault { new ReductionRule() }
    List<Catalog> catalogs = [].withLazyDefault { new Catalog() }
}
