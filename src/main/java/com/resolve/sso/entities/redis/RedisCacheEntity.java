package com.resolve.sso.entities.redis;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RedisCacheEntity<T> {

    @JsonIgnore
    public String getRedisKey();

    @JsonIgnore
    public String getRedisValue();

    @JsonIgnore
    public void refreshRedisCache();

    @JsonIgnore
    public void persistInRedis();

    @JsonIgnore
    public <T> List<T> getAllFromRedis();

    @JsonIgnore
    public <T> List<T> getFromRedis(String key);

    @JsonIgnore
    public void delFromRedis();
}