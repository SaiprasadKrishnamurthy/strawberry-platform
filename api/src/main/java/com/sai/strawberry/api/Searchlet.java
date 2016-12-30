package com.sai.strawberry.api;

import org.elasticsearch.common.xcontent.XContentBuilder;

public abstract class Searchlet<T> {
    public abstract String toElasticsearchQuery(T criteria, XContentBuilder queryBuilder);

    public abstract T newSearchCriteria();

    private final EventConfig eventConfig;

    public Searchlet(final EventConfig eventConfig) {
        this.eventConfig = eventConfig;
    }

    public String eventConfigId() {
        return this.eventConfig.getConfigId();
    }

    public abstract Class<T> searchCriteriaClass();
}
