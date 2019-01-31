package com.gxy.auto.flush.cache.entity;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author guoxingyong
 * @since 2019/1/26 10:41
 */
@Data
public class CacheKeyInfo {

    private int expireTime;

    private TimeUnit timeUnit;

    private String fieldName;
}
