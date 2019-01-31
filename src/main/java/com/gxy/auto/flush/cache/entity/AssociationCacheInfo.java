package com.gxy.auto.flush.cache.entity;

import lombok.Data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author guoxingyong
 * @since 2019/1/28 9:34
 */
@Data
public class AssociationCacheInfo {

    private String tableName;

    private String cachePrefix;

    private Class cacheClass;

    private int expireTime;

    private TimeUnit timeUnit;

    private String primaryKey;

    private Set<String> cacheKeysSpel;
    //tableName:AssociationInfo
    private Map<String, AssociationInfo> associationMap;
}
