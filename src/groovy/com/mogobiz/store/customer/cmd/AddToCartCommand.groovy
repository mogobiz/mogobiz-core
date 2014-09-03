package com.mogobiz.store.customer.cmd

import com.mogobiz.store.domain.TicketType
import com.mogobiz.store.vo.RegisteredCartItemVO

@grails.validation.Validateable
public class AddToCartCommand {

	TicketType ticketType;
	int quantity;
	Calendar dateTime;
	List<RegisteredCartItemVO> registeredCartItems =  [].withLazyDefault { new RegisteredCartItemVO() };
	
	
	static constraints = {
		ticketType(nullable: false)
	}
}
