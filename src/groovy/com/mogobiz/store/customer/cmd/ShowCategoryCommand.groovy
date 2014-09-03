package com.mogobiz.store.customer.cmd

/**
 * Command Object pour la recherche de catégories
 */
@grails.validation.Validateable
class ShowCategoryCommand {

	Long id;
	Long catalogId;
	Long parentId;
	String queryJahia;
	Long pageSize = -1;
	Long pageOffset = 0;

	
	static constraints = {
		id(nullable: true, validator: {Long val, ShowCategoryCommand cmd ->
			return (val != null || cmd.catalogId != null || cmd.parentId != null || cmd.queryJahia != null)
		})
		catalogId(nullable: true, validator: {Long val, ShowCategoryCommand cmd ->
			return (val != null || cmd.id != null || cmd.parentId != null || cmd.queryJahia != null)
		})
		parentId(nullable: true, validator: {Long val, ShowCategoryCommand cmd ->
			return (val != null || cmd.id != null || cmd.catalogId != null || cmd.queryJahia != null)
		})
		queryJahia(nullable: true, validator: {String val, ShowCategoryCommand cmd ->
			return (val != null || cmd.id != null || cmd.catalogId != null || cmd.parentId != null)
		})
		pageSize(nullable: true, validator: {Long val, ShowCategoryCommand cmd -> return (val == null || val == -1 || val > 0)})
		pageOffset(nullable: true, min: 0L, validator: {Long val, ShowCategoryCommand cmd -> return (val == null ? (cmd.pageSize == null) : (cmd.pageSize != null))})
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
