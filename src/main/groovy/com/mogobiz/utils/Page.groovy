package com.mogobiz.utils

/**
 * Paged list
 */
class Page {
	/**
	 * List of renderer object
	 */
	List<Map> list;
	
	/**
	 * Total number of elements of the paged list
	 */
	def totalCount;
	
	/**
	 * Number max of elements per page
	 */
	def maxItemsPerPage;
	
	/**
	 * Offset of current page (starting to 0)
	 */
	def pageOffset;
	
	/**
	 * Total number of pages
	 */
	def pageCount;

	/**
	 * Indicate if exist other pages after current page
	 */
	boolean hasNext;
	
	/**
	 * Indicate if exist other pages before current page
	 */
	boolean hasPrevious;
	
	/**
	 * Number of elements of the current page
	 */
	int pageSize;
}
