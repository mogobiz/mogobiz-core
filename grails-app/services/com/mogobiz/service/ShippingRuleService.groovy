/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.service

import com.mogobiz.store.cmd.PagedListCommand
import com.mogobiz.store.cmd.ShippingRuleCommand
import com.mogobiz.store.domain.Company
import com.mogobiz.store.domain.Seller
import com.mogobiz.store.domain.ShippingRule
import grails.orm.PagedResultList
import grails.transaction.Transactional

@Transactional
class ShippingRuleService {

    PagedResultList list(Seller seller, PagedListCommand cmd) {
        if (seller?.company == null || cmd == null) {
            throw new IllegalArgumentException()
        }

        return ShippingRule.createCriteria().list(cmd.getPagination()) {
            company {
                eq "id", seller.company.id
            }
            order("countryCode", "asc")
        }
    }

    ShippingRule save(Seller seller, ShippingRuleCommand cmd) {
        if (seller?.company == null || cmd == null || !cmd.validate()) {
            throw new IllegalArgumentException()
        }
        List<ShippingRule> shippingRules = ShippingRule.findAllByCompanyAndCountryCode(seller.company, cmd.countryCode)

        ShippingRule foundRule = shippingRules.find {
            (it.minAmount > cmd.minAmount || it.maxAmount >= cmd.minAmount) &&
            (it.minAmount <= cmd.maxAmount) && (it.id != cmd.id || cmd.id == null)
        }

        if (foundRule != null) {
            foundRule.errors.rejectValue("minAmount", "company.shipping.rule.errors.invalidInterval")
            return foundRule
        }

        if (cmd.id != null) {
            ShippingRule shippingRule = ShippingRule.get(cmd.id)

            if (shippingRule == null) {
                shippingRule = new ShippingRule()
                shippingRule.errors.rejectValue("id", "unknown")
                return shippingRule
            }

            if (shippingRule.company.id != seller.company.id) {
                shippingRule.errors.rejectValue("id", "unknown")
                return shippingRule
            } else {
                shippingRule.delete()
            }
        }
        ShippingRule shippingRule = new ShippingRule()
        shippingRule.properties = cmd.properties
        shippingRule.company = seller.company

        if (shippingRule.validate()) {

            shippingRule.save()
        }
        return shippingRule
    }

    ShippingRule delete(Seller seller, Long id) {
        if (seller?.company == null || id == null) {
            throw new IllegalArgumentException()
        }

        ShippingRule shippingRule = ShippingRule.get(id)
        if (shippingRule == null) {
            shippingRule = new ShippingRule()
            shippingRule.errors.rejectValue("id", "unknown")
            return shippingRule
        }

        if (shippingRule.company.id != seller.company.id) {
            shippingRule.errors.rejectValue("id", "unknown")
            return shippingRule
        }
        shippingRule.delete()

        return shippingRule
    }
}
