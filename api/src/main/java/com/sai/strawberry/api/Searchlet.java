package com.sai.strawberry.api;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;

public abstract class Searchlet<T> {
    public abstract String toElasticsearchQuery(T criteria, XContentBuilder queryBuilder) throws Exception;

    public T newSearchCriteria() {
        try {
            return searchCriteriaClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final EventConfig eventConfig;

    public Searchlet(final EventConfig eventConfig) {
        this.eventConfig = eventConfig;
    }

    public String eventConfigId() {
        return this.eventConfig.getConfigId();
    }

    public Class<T> searchCriteriaClass() {
        try {
            return (Class<T>) Class.forName(StringUtils.substringBetween(this.getClass().getGenericSuperclass().toString(), "<", ">"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
