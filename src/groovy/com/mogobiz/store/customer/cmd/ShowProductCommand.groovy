package com.mogobiz.store.customer.cmd

/**
 * Command Object pour la recherche de produits
 */
@grails.validation.Validateable
class ShowProductCommand {

	Long id;
	Long categoryId;
	String queryJahia;
	String queryOrderBy;
	Long pageSize = -1;
	Long pageOffset = 0;

	
	static constraints = {
		id(nullable: true, validator: {Long val, ShowProductCommand cmd ->
			return (val != null || cmd.categoryId != null || cmd.queryJahia != null)
		})
		categoryId(nullable: true, validator: {Long val, ShowProductCommand cmd ->
			return (val != null || cmd.id != null || cmd.queryJahia != null)
		})
		queryJahia(nullable: true, validator: {String val, ShowProductCommand cmd ->
			return (val != null || cmd.categoryId != null || cmd.id != null)
		})
		pageSize(nullable: true, validator: {Long val, ShowProductCommand cmd -> return (val == null || val == -1 || val > 0)})
		pageOffset(nullable: true, min: 0L, validator: {Long val, ShowProductCommand cmd -> return (val == null ? (cmd.pageSize == null) : (cmd.pageSize != null))})
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
