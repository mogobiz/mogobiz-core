package com.mogobiz.service
import com.mogobiz.store.exception.CurrencyRateException
import com.mogobiz.store.vo.CartItemVO
import com.mogobiz.store.vo.CartVO
import com.mogobiz.store.vo.CouponVO
/**
 * Service en charge du panier. Il offre notamment les méthodes d'ajout, de modification
 * et de suppression des items du panier et la méthode de finalisation du panier.
 */
class CartJahiaService extends CartService {

    @Override
    Map renderPriceCoupon(Locale locale, String currencyCode, CouponVO coupon) throws CurrencyRateException {
        Map price = [:]
        price["price"] = coupon.price
        return price
    }

    @Override
    Map renderPriceCart(Locale locale, String currencyCode, CartVO cart) throws CurrencyRateException {
        Map price = [:]
        price["price"] = cart.price
        price["reduction"] = cart.reduction
        price["finalPrice"] = cart.finalPrice
        return price
    }

    @Override
    Map renderPriceCartItem(Locale locale, String currencyCode, CartItemVO cartItem) throws CurrencyRateException {
        Map price = [:]
        price["price"] = cartItem.price
        price["endPrice"] = cartItem.endPrice
        price["totalPrice"] = cartItem.totalPrice
        price["totalEndPrice"] = cartItem.totalEndPrice
        return price
    }
}

