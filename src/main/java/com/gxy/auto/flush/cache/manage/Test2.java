package com.gxy.auto.flush.cache.manage;

import com.gxy.auto.flush.cache.annotation.*;
import com.gxy.auto.flush.cache.entity.AssociationCacheInfo;
import com.gxy.auto.flush.cache.entity.AssociationInfo;
import com.gxy.auto.flush.cache.entity.TableCacheInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author guoxingyong
 * @since 2019/1/29 10:23
 */
@Slf4j
@Getter
public class Test2 {

    private HashMap<String, TableCacheInfo> tableCacheInfoHashMap = new HashMap<>(16);
    /**
     * 这个存贮的是{@link AssociationCache}中的tableName对应的关联的AssociationCacheInfo
     */
    private HashMap<String, List<AssociationCacheInfo>> associationCacheInfoHashMap = new HashMap<>(16);

    /**
     * 这个存贮的是{@link Association}中的tableName对应的关联的AssociationCacheInfo
     */
    private HashMap<String, List<AssociationCacheInfo>> associationHashMap = new HashMap<>(16);

    public Test2(String scanPackage) {
        scanTableConfig(scanPackage);
    }

    private void scanTableConfig(String prefix) {
        Reflections reflections = new Reflections(prefix);
        //所有的缓存的前缀不能相同,
        Set<String> cachePrefixSet = new HashSet<>();
        Set<Class<?>> tableClass = reflections.getTypesAnnotatedWith(CacheTable.class);
        acquirePriCacheTableInfo(tableClass, cachePrefixSet);
        Set<Class<?>> tableCacheClass = reflections.getTypesAnnotatedWith(AssociationCache.class);
        acquireAssociationCacheInfo(tableCacheClass, cachePrefixSet);
    }

    private void acquireAssociationCacheInfo(Set<Class<?>> tableCacheClass, Set<String> cachePrefixSet) {
        for (Class<?> associationClass : tableCacheClass) {
            //获取AssociationCache的基本信息
            AssociationCache associationCacheAnnotation = associationClass.getAnnotation(AssociationCache.class);
            String cachePrefix = associationCacheAnnotation.cachePrefix();
            String tableName = associationCacheAnnotation.tableName();
            TimeUnit timeUnit = associationCacheAnnotation.timeUnit();
            int expireTime = associationCacheAnnotation.expireTime();
            Set<String> cacheKeySet = Arrays.stream(associationCacheAnnotation.cacheKey()).collect(Collectors.toSet());
            if (Objects.equals(cachePrefix, "")) {
                cachePrefix = associationClass.getSimpleName();
            }
            if (!cachePrefixSet.add(cachePrefix)) {
                log.warn("this class:{} cachePrefix exist please check config,this class don't register", associationClass.getSimpleName());
                continue;
            }
            //tableName必须已经是tableCacheInfoHashMap注册过的tableName
            if (!tableCacheInfoHashMap.containsKey(tableName)) {
                log.warn("associationClass:{} table name:{} is't a register table name", associationClass, tableName);
                continue;
            }
            TableCacheInfo tableCacheInfo = tableCacheInfoHashMap.get(tableName);
            //组装成一个AssociationCacheInfo
            AssociationCacheInfo associationCacheInfo = new AssociationCacheInfo();
            associationCacheInfo.setTableName(tableName);
            associationCacheInfo.setCachePrefix(cachePrefix);
            associationCacheInfo.setCacheClass(associationClass);
            associationCacheInfo.setExpireTime(expireTime);
            associationCacheInfo.setTimeUnit(timeUnit);
            associationCacheInfo.setCacheKeysSpel(cacheKeySet);
            associationCacheInfo.setPrimaryKey(tableCacheInfo.getPrimaryKey());
            Field[] declaredFields = associationClass.getDeclaredFields();
            Map<String, AssociationInfo> associationMap = new HashMap<>();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                //获取关联的信息
                Association association = declaredField.getAnnotation(Association.class);
                if (association != null) {
                    String associationTableName = association.tableName();
                    TableCacheInfo associationTableInfo = tableCacheInfoHashMap.get(associationTableName);
                    //如果关联的key没有作为cache key需要自动加上
                    if (associationTableInfo == null) {
                        log.warn("can't find this table cache info,tableName:{}", associationTableName);
                        continue;
                    }
                    //关联的表必须跟存储的表的类是一致的
                    if (associationTableInfo.getCacheClass() != declaredField.getType()) {
                        log.warn("class:{},this @Association config error,Association tableName:{}", associationClass.getSimpleName(), associationTableName);
                        continue;
                    }
                    String id = association.id();
                    //存储了这个关联类的关联字段的属性值
                    AssociationInfo associationInfo = new AssociationInfo();
                    associationInfo.setFiledName(declaredField.getName());
                    associationInfo.setAssociationId(id);
                    associationMap.put(associationTableName, associationInfo);
                    associationHashMap.compute(associationTableName, (k, v) -> getAssociationCacheInfos(associationCacheInfo, v));
                    cacheKeySet.add(declaredField.getName()+"."+id);
                }
            }
            associationCacheInfo.setAssociationMap(associationMap);
            associationCacheInfoHashMap.compute(tableName, (k, v) -> getAssociationCacheInfos(associationCacheInfo, v));
        }
    }

    private List<AssociationCacheInfo> getAssociationCacheInfos(AssociationCacheInfo associationCacheInfo, List<AssociationCacheInfo> v) {
        if (v == null) {
            v = new ArrayList<>();
        }
        v.add(associationCacheInfo);
        return v;
    }

    private void acquirePriCacheTableInfo(Set<Class<?>> tableClass, Set<String> cachePrefixSet) {
        //遍历原始的class，这里的class
        for (Class<?> primitiveClass : tableClass) {
            CacheTable cacheTableAnnotation = primitiveClass.getAnnotation(CacheTable.class);
            String cachePrefix = cacheTableAnnotation.cachePrefix();
            String tableName = cacheTableAnnotation.tableName();
            if (Objects.equals(cachePrefix, "")) {
                cachePrefix = tableName;
            }
            if (!cachePrefixSet.add(cachePrefix)) {
                log.warn("this class:{} cachePrefix exist please check config,this class don't register", primitiveClass.getSimpleName());
                continue;
            }
            TableCacheInfo associationTableInfo = new TableCacheInfo();
            associationTableInfo.setTableName(tableName);
            associationTableInfo.setCachePrefix(cachePrefix);
            associationTableInfo.setCacheClass(primitiveClass);
            Set<String> cacheKeys = new HashSet<>();
            Field[] declaredFields = primitiveClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                PrimaryKey primaryKey = declaredField.getAnnotation(PrimaryKey.class);
                if (primaryKey != null) {
                    associationTableInfo.setPrimaryKey(declaredField.getName());
                    continue;
                }
                CacheKey cacheKey = declaredField.getAnnotation(CacheKey.class);
                if (cacheKey != null) {
                    cacheKeys.add(declaredField.getName());
                }
            }
            String primaryKey = associationTableInfo.getPrimaryKey();
            if (primaryKey == null) {
                log.warn("this class:{} don't exist primaryKey,please check config,this class don't register", primitiveClass.getSimpleName());
                continue;
            }
            cacheKeys.remove(primaryKey);
            associationTableInfo.setCacheKeys(cacheKeys);
            if (tableCacheInfoHashMap.containsKey(tableName)) {
                log.warn("exits this table:{},please check annotation", tableName);
            } else {
                tableCacheInfoHashMap.put(tableName, associationTableInfo);
            }
        }
    }
}
