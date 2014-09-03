package com.mogobiz.store.customer
import com.mogobiz.store.customer.cmd.AddToCartCommand
import com.mogobiz.store.customer.cmd.RemoveCartItemCommand
import com.mogobiz.store.customer.cmd.UpdateCartItemCommand
import com.mogobiz.store.domain.Company
import com.mogobiz.store.exception.CurrencyRateException
import com.mogobiz.ajax.AjaxResponse
import com.mogobiz.service.CartService
import com.mogobiz.store.vo.CartVO
import grails.converters.JSON
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletResponse

class CartController {
	CartService cartService
	//CART
    def addCoupon(String couponCode) {
        StoreSessionData sessionData = session.storeData
        if (sessionData.companyId)
        {
            if (!couponCode) {
                response.sendError HttpServletResponse.SC_BAD_REQUEST;
                return;
            }

            CartVO cartVO = getcartVO()
            try
            {
                AjaxResponse reponse = cartService.addCoupon(sessionData.companyId, getLocale(sessionData), sessionData.currency, cartVO, couponCode);
                render reponse.asMap() as JSON
            }
            catch (CurrencyRateException ex) {
                log.error(ex.message)
                response.sendError HttpServletResponse.SC_NOT_FOUND;
                return;
            }
        }
        else
        {
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
        }
    }

    def removeCoupon(String couponCode) {
        StoreSessionData sessionData = session.storeData
        if (sessionData.companyId)
        {
            if (!couponCode) {
                response.sendError HttpServletResponse.SC_BAD_REQUEST;
                return;
            }

            CartVO cartVO = getcartVO()
            try
            {
                AjaxResponse reponse = cartService.removeCoupon(sessionData.companyId, getLocale(sessionData), sessionData.currency, cartVO, couponCode);
                render reponse.asMap() as JSON
            }
            catch (CurrencyRateException ex) {
                log.error(ex.message)
                response.sendError HttpServletResponse.SC_NOT_FOUND;
                return;
            }
        }
        else
        {
            response.sendError HttpServletResponse.SC_UNAUTHORIZED
        }
    }

    def addToCart(AddToCartCommand cmd) {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			if (!cmd.validate()) {
				response.sendError HttpServletResponse.SC_BAD_REQUEST;
				return;
			}
			
			CartVO cartVO = getcartVO()
			try
			{
				AjaxResponse reponse = cartService.addItem(getLocale(sessionData), sessionData.currency, cartVO, cmd.ticketType, cmd.quantity, cmd.dateTime, cmd.registeredCartItems);
				render reponse.asMap() as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}	
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}

	def updateCartItem (UpdateCartItemCommand cmd) {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			if (!cmd.validate()) {
				response.sendError HttpServletResponse.SC_BAD_REQUEST;
				return;
			}
			
			CartVO cartVO = getcartVO()
			try
			{
				AjaxResponse reponse = cartService.updateItem(getLocale(sessionData), sessionData.currency, cartVO, cmd.cartItemId, cmd.quantity)	
				render reponse.asMap() as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}
	def removeCartItem(RemoveCartItemCommand cmd) {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			if (!cmd.validate()) {
				response.sendError HttpServletResponse.SC_BAD_REQUEST;
				return;
			}
			
			CartVO cartVO = getcartVO()
			try
			{
				AjaxResponse reponse = cartService.removeItem(getLocale(sessionData), sessionData.currency, cartVO, cmd.cartItemId)
				render reponse.asMap() as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}

	def getCart() {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			CartVO cartVO = getcartVO()
			try
			{
				Map map = cartService.renderCart(getLocale(sessionData), sessionData.currency, cartVO)	
				render map as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}

	def clearCart() {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			CartVO cartVO = getcartVO()
			try
			{
				AjaxResponse reponse = cartService.clear(getLocale(sessionData), sessionData.currency, cartVO)	
				render reponse.asMap() as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}
	
	def prepareBeforePayment(String countryCode, String stateCode) {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
            if (!countryCode) {
                response.sendError HttpServletResponse.SC_BAD_REQUEST;
                return;
            }
			CartVO cartVO = getcartVO()
			try
			{
				AjaxResponse reponse = cartService.prepareBeforePayment(Company.get(sessionData.companyId), countryCode, stateCode, sessionData.currency, cartVO)
				render reponse as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}

	def commit() {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			String transactionUuid = params["transactionUuid"]
			if (transactionUuid) {
				def result = [:]
				CartVO cartVO = getcartVO()
				try
				{
					AjaxResponse reponse = cartService.commit(cartVO, transactionUuid)
					render reponse as JSON
				}
				catch (CurrencyRateException ex) {
					log.error(ex.message)
					response.sendError HttpServletResponse.SC_NOT_FOUND;
					return;
				}
			}
			else {
				response.sendError HttpServletResponse.SC_BAD_REQUEST;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}
	
	def cancel() {
		StoreSessionData sessionData = session.storeData
		if (sessionData.companyId)
		{
			CartVO cartVO = getcartVO()
			try
			{
				AjaxResponse reponse = cartService.cancel(getLocale(sessionData), sessionData.currency, cartVO)
				render reponse as JSON
			}
			catch (CurrencyRateException ex) {
				log.error(ex.message)
				response.sendError HttpServletResponse.SC_NOT_FOUND;
				return;
			}
		}
		else
		{
			response.sendError HttpServletResponse.SC_UNAUTHORIZED
		}
	}

	/**
	 * Récupère le panier de la session ou 
	 * initialise un nouveau panier 
	 * (en reprenant les infos de tracking si disponible)
	 * @return
	 */
	private CartVO getcartVO() {
		if (!session.cartVO) {
			session.cartVO = cartService.initCart();
		}
		return session.cartVO
	}
	
	/**
	 * This method returns the locale obtain with the language of the request
	 * and the given country (in the sessionData)
	 * @param sessionData
	 * @return
	 */
	 private Locale getLocale(StoreSessionData sessionData) {
	 	Locale local = RequestContextUtils.getLocale(request);
	 	if (local != null && sessionData != null && sessionData.country) {
	 		return new Locale(local.language, sessionData.country)
	 	}
	 	else {
	 		return null;
	 	}
	 }	 
}
