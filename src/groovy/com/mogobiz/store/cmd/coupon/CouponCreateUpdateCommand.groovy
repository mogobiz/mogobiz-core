package com.mogobiz.store.cmd.coupon

import com.mogobiz.store.domain.Product
import com.mogobiz.store.domain.ReductionRule
import com.mogobiz.store.domain.TicketType
import grails.validation.Validateable
/**
 * Created by yoannbaudy on 25/02/14.
 */
@Validateable
class CouponCreateUpdateCommand {
    Long id
    String name
    String code
    Boolean active
    Boolean catalogWise
    Long numberOfUses
    Calendar startDate
    Calendar endDate
    List<com.mogobiz.store.domain.Category> categories = [].withLazyDefault { new com.mogobiz.store.domain.Category() }
    List<Product> products = [].withLazyDefault { new Product() }
    List<TicketType> skus = [].withLazyDefault { new TicketType() }
    List<ReductionRule> rules = [].withLazyDefault { new ReductionRule() }
}
