package com.sai.strawberry.api;

public abstract class Searchlet<T> {
    public abstract String toElasticsearchQuery(T criteria);

    public abstract T newSearchCriteria();

    private final EventConfig eventConfig;

    public Searchlet(final EventConfig eventConfig) {
        this.eventConfig = eventConfig;
    }

    public String eventConfigId() {
        return this.eventConfig.getConfigId();
    }
}
