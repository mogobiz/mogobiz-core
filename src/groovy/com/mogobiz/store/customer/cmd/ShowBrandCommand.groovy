package com.mogobiz.store.customer.cmd

/**
 * Command Object pour la recherche de marques
 */
@grails.validation.Validateable
class ShowBrandCommand {

	Long id;
	String queryJahia;
	Long pageSize = -1;
	Long pageOffset = 0;
	
	
	static constraints = {
		id(nullable: true)
		queryJahia(nullable: true)
		pageSize(nullable: true, validator: {Long val, ShowBrandCommand cmd -> return (val == null || val == -1 || val > 0)})
		pageOffset(nullable: true, min: 0L, validator: {Long val, ShowBrandCommand cmd -> return (val == null ? (cmd.pageSize == null) : (cmd.pageSize != null))})
	}
	
	def getPagination()
	{
		def pagination = [:];
		if (pageSize != null && pageSize > 0)
		{
			pagination = [max: pageSize, offset: pageOffset]
		}
		return pagination;
	}
}
