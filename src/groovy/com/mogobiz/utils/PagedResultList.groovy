package com.mogobiz.utils

import com.mogobiz.store.cmd.PagedListCommand
import org.hibernate.*
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projection
import org.hibernate.transform.ResultTransformer

/**
 */
class PagedResultList extends grails.orm.PagedResultList {

    public PagedResultList(PagedListCommand cmd, List list) {
        super(null, new EmptyCriteria())
        this.totalCount = list.size()
        this.list = cmd.subList(list)
    }

    private class EmptyCriteria implements org.hibernate.Criteria {

        @Override
        String getAlias() {
            return null
        }

        @Override
        Criteria setProjection(Projection projection) {
            return null
        }

        @Override
        Criteria add(Criterion criterion) {
            return null
        }

        @Override
        Criteria addOrder(Order order) {
            return null
        }

        @Override
        Criteria setFetchMode(String s, FetchMode fetchMode) throws HibernateException {
            return null
        }

        @Override
        Criteria setLockMode(LockMode lockMode) {
            return null
        }

        @Override
        Criteria setLockMode(String s, LockMode lockMode) {
            return null
        }

        @Override
        Criteria createAlias(String s, String s2) throws HibernateException {
            return null
        }

        @Override
        Criteria createAlias(String s, String s2, int i) throws HibernateException {
            return null
        }

        @Override
        Criteria createAlias(String s, String s2, int i, Criterion criterion) throws HibernateException {
            return null
        }

        @Override
        Criteria createCriteria(String s) throws HibernateException {
            return null
        }

        @Override
        Criteria createCriteria(String s, int i) throws HibernateException {
            return null
        }

        @Override
        Criteria createCriteria(String s, String s2) throws HibernateException {
            return null
        }

        @Override
        Criteria createCriteria(String s, String s2, int i) throws HibernateException {
            return null
        }

        @Override
        Criteria createCriteria(String s, String s2, int i, Criterion criterion) throws HibernateException {
            return null
        }

        @Override
        Criteria setResultTransformer(ResultTransformer resultTransformer) {
            return null
        }

        @Override
        Criteria setMaxResults(int i) {
            return null
        }

        @Override
        Criteria setFirstResult(int i) {
            return null
        }

        @Override
        boolean isReadOnlyInitialized() {
            return false
        }

        @Override
        boolean isReadOnly() {
            return false
        }

        @Override
        Criteria setReadOnly(boolean b) {
            return null
        }

        @Override
        Criteria setFetchSize(int i) {
            return null
        }

        @Override
        Criteria setTimeout(int i) {
            return null
        }

        @Override
        Criteria setCacheable(boolean b) {
            return null
        }

        @Override
        Criteria setCacheRegion(String s) {
            return null
        }

        @Override
        Criteria setComment(String s) {
            return null
        }

        @Override
        Criteria setFlushMode(FlushMode flushMode) {
            return null
        }

        @Override
        Criteria setCacheMode(CacheMode cacheMode) {
            return null
        }

        @Override
        List list() throws HibernateException {
            return null
        }

        @Override
        ScrollableResults scroll() throws HibernateException {
            return null
        }

        @Override
        ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
            return null
        }

        @Override
        Object uniqueResult() throws HibernateException {
            return null
        }
    }
}
