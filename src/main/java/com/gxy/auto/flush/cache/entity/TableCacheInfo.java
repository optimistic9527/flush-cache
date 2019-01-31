package com.gxy.auto.flush.cache.entity;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * @author guoxingyong
 * @since 2019/1/28 9:17
 */
@Data
public class TableCacheInfo {

    private String tableName;

    private String cachePrefix;

    private Class cacheClass;

    private String primaryKey;

    private Set<String> cacheKeys;

}
