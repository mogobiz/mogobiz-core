package com.mogobiz.store.cmd

/**
 * Created by yoannbaudy on 25/02/14.
 */
public class PagedListCommand {
    Long pageSize;
    Long pageOffset;

    def getPagination()
    {
        def pagination = [:];
        if (pageSize != null && pageSize > 0)
        {
            pagination = [max: pageSize, offset: (pageOffset ? Math.max(0, pageOffset * pageSize) : 0)]
        }
        return pagination;
    }

    List subList(List list) {
        if (pageSize != null && pageSize > 0)
        {
            int min = (pageOffset ? Math.max(0, pageOffset) : 0) * pageSize
            if (min < list.size()) {
                return list.subList((int)min, Math.min((int)(min + pageSize), list.size()))
            }
            return []
        }
        return list;
    }
}
