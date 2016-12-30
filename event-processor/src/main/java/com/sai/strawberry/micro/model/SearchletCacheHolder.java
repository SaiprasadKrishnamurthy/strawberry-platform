package com.sai.strawberry.micro.model;

import com.sai.strawberry.api.Searchlet;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by saipkri on 30/12/16.
 */
public class SearchletCacheHolder {
    private ConcurrentHashMap<String, Searchlet> cache = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, Searchlet> getCache() {
        return cache;
    }

    public void setCache(ConcurrentHashMap<String, Searchlet> cache) {
        this.cache = cache;
    }
}
