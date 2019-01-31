package com.gxy.auto.flush.cache.manage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.gxy.auto.flush.cache.annotation.Association;
import com.gxy.auto.flush.cache.annotation.AssociationCache;
import com.gxy.auto.flush.cache.annotation.CacheKey;
import com.gxy.auto.flush.cache.annotation.CacheTable;
import com.gxy.auto.flush.cache.entity.AssociationCacheInfo;
import com.gxy.auto.flush.cache.entity.CacheKeyInfo;
import com.gxy.auto.flush.cache.entity.CanalInfo;
import com.gxy.auto.flush.cache.entity.TableCacheInfo;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author guoxingyong
 * @since 2019/1/28 16:25
 */
/*
@Slf4j
public class Test {
    private final RedisTemplate<String, String> redisTemplate;
    private HashMap<String, TableCacheInfo> tableCacheInfoHashMap = new HashMap<>(16);

    private HashMap<String, List<AssociationCacheInfo>> associationCacheInfoHashMap = new HashMap<>(16);


    public Test(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void scanTableConfig(String prefix) {
        Reflections reflections = new Reflections(prefix);
        Set<Class<?>> tableClass = reflections.getTypesAnnotatedWith(CacheTable.class);
        for (Class<?> primitiveClass : tableClass) {
            CacheTable cacheTableAnnotation = primitiveClass.getAnnotation(CacheTable.class);
            String cachePrefix = cacheTableAnnotation.cachePrefix();
            String tableName = cacheTableAnnotation.tableName();
            if (Objects.equals(cachePrefix, "")) {
                cachePrefix = tableName;
            }
            TableCacheInfo tableCacheInfo = new TableCacheInfo();
            tableCacheInfo.setTableName(tableName);
            tableCacheInfo.setCachePrefix(cachePrefix);
            tableCacheInfo.setCacheClass(primitiveClass);
            ArrayList<CacheKeyInfo> cacheKeyInfoList = new ArrayList<>();
            Field[] declaredFields = primitiveClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                CacheKey cacheKey = declaredField.getAnnotation(CacheKey.class);
                if (cacheKey != null) {
                    CacheKeyInfo cacheKeyInfo = new CacheKeyInfo();
                    cacheKeyInfo.setExpireTime(cacheKey.expireTime());
                    cacheKeyInfo.setTimeUnit(cacheKey.timeUnit());
                    cacheKeyInfo.setFieldName(declaredField.getName());
                    cacheKeyInfoList.add(cacheKeyInfo);
                }
            }
            tableCacheInfo.setKeys(cacheKeyInfoList);
            if (tableCacheInfoHashMap.containsKey(tableName)) {
                log.warn("exits this table:{},please check annotation", tableName);
            } else {
                tableCacheInfoHashMap.put(tableName, tableCacheInfo);
            }
        }
        Set<Class<?>> tableCacheClass = reflections.getTypesAnnotatedWith(AssociationCache.class);

        for (Class<?> associationClass : tableCacheClass) {
            AssociationCache associationCacheAnnotation = associationClass.getAnnotation(AssociationCache.class);
            String cachePrefix = associationCacheAnnotation.cachePrefix();
            String tableName = associationCacheAnnotation.tableName();
            TimeUnit timeUnit = associationCacheAnnotation.timeUnit();
            int expireTime = associationCacheAnnotation.expireTime();
            Set<String> cacheKeySet = Arrays.stream(associationCacheAnnotation.cacheKey()).collect(Collectors.toSet());
            if (Objects.equals(cachePrefix, "")) {
                cachePrefix = associationClass.getSimpleName();
            }
            AssociationCacheInfo associationCacheInfo = new AssociationCacheInfo();
            associationCacheInfo.setTableName(tableName);
            associationCacheInfo.setCachePrefix(cachePrefix);
            associationCacheInfo.setCacheClass(associationClass);
            associationCacheInfo.setExpireTime(expireTime);
            associationCacheInfo.setTimeUnit(timeUnit);
            associationCacheInfo.setCacheKeysSpel(cacheKeySet);
            Field[] declaredFields = associationClass.getDeclaredFields();
            Map<String, String> associationMap = new HashMap<>();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                Association association = declaredField.getAnnotation(Association.class);
                if (association != null) {
                    String id = association.id();
                    associationMap.put(association.tableName(), id);
                    //如果关联的key没有作为cache key需要自动加上
                    TableCacheInfo tableCacheInfo = tableCacheInfoHashMap.get(association.tableName());
                    if (tableCacheInfo == null) {
                        log.warn("can't find this table cache info,tableName:{}", association.tableName());
                        continue;
                    }
                    List<CacheKeyInfo> keys = tableCacheInfo.getKeys();
                    boolean needCacheId = true;
                    for (CacheKeyInfo key : keys) {
                        if (Objects.equals(id, key.getFieldName())) {
                            needCacheId = false;
                            break;
                        }
                    }
                    if (needCacheId) {
                        CacheKeyInfo cacheKeyInfo = new CacheKeyInfo();
                        cacheKeyInfo.setExpireTime(0);
                        cacheKeyInfo.setTimeUnit(TimeUnit.SECONDS);
                        cacheKeyInfo.setFieldName(id);
                        keys.add(cacheKeyInfo);
                    }
                }
            }
            associationCacheInfo.setAssociationMap(associationMap);
            associationCacheInfoHashMap.compute(tableName, (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                v.add(associationCacheInfo);
                return v;
            });
        }
    }

    public void onInsert(CanalInfo canalInfo) {
        String tableName = canalInfo.getTable();
        if (tableName == null) {
            log.warn("tableName can't null");
            return;
        }
        TableCacheInfo tableCacheInfo = tableCacheInfoHashMap.get(tableName);
        if (tableCacheInfo == null) {
            log.warn("not exits this table cache config,tableName:", tableName);
            return;
        }
        List<CacheKeyInfo> keys = tableCacheInfo.getKeys();
        String cachePrefix = tableCacheInfo.getCachePrefix();
        List<JSONObject> data = canalInfo.getData();
        for (CacheKeyInfo key : keys) {
            String fieldName = key.getFieldName();
            int expireTime = key.getExpireTime();
            for (JSONObject datum : data) {
                Object o = datum.get(fieldName);
                if (o != null) {
                    redisTemplate.opsForValue().set(cachePrefix + o, datum.toJSONString());
                } else {
                    log.warn("db this columns value can be null,please check configure,columns:{}", fieldName);
                }
            }
        }
    }
    //issues 如果是改了key那么会导致原来的key存贮的值没有被清除
    public void onUpdate(CanalInfo canalInfo) {
        String tableName = canalInfo.getTable();
        if (tableName == null) {
            log.warn("tableName can't null");
            return;
        }
        TableCacheInfo tableCacheInfo = tableCacheInfoHashMap.get(tableName);
        if (tableCacheInfo == null) {
            log.warn("not exits this table cache config,tableName:", tableName);
            return;
        }
        List<CacheKeyInfo> keys = tableCacheInfo.getKeys();
        String cachePrefix = tableCacheInfo.getCachePrefix();
        List<JSONObject> data = canalInfo.getData();
        for (CacheKeyInfo key : keys) {
            String fieldName = key.getFieldName();
            int expireTime = key.getExpireTime();
            for (JSONObject datum : data) {
                Object o = datum.get(fieldName);
                if (o != null) {
                    redisTemplate.opsForValue().set(cachePrefix + o, datum.toJSONString());
                } else {
                    log.warn("db this columns value can be null,please check configure,columns:{}", fieldName);
                }
            }
        }
        List<AssociationCacheInfo> infoList = associationCacheInfoHashMap.get(tableName);
        if(infoList!=null){

        }
        List<AssociationCacheInfo> associationCacheInfos=associationCacheInfoHashMap.values().stream()
                .flatMap(Collection::stream)
                .filter(associationCacheInfo -> associationCacheInfo.getAssociationMap().containsKey(tableName))
                .collect(Collectors.toList());
        if (!associationCacheInfos.isEmpty()) {
            ExpressionParser parser = new SpelExpressionParser();
            for (AssociationCacheInfo associationCacheInfo : associationCacheInfos) {
                String cachePrefix1 = associationCacheInfo.getCachePrefix();
                for (JSONObject datum : data) {
                    Object cacheValue = null;
                    Object object = datum.toJavaObject(tableCacheInfo.getCacheClass());
                    //必须不为null
                    Set<Map.Entry<String, String>> entries = associationCacheInfo.getAssociationMap().entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        Object filedValue = datum.get(entry.getValue());
                        if (filedValue == null) {
                            log.error("filedValue is null");
                            return;
                        }
                        TableCacheInfo tableCacheInfo1 = tableCacheInfoHashMap.get(entry.getKey());
                        String key = tableCacheInfo1.getCachePrefix() + filedValue;
                        String jsonString = redisTemplate.opsForValue().get(key);
                        if (jsonString != null) {
                            cacheValue = JSON.parseObject(jsonString, associationCacheInfo.getCacheClass());
                            BeanUtils.copyProperties(object, cacheValue);
                        } else {
                            log.warn("can't find this association class:{} from redis", key);
                        }
                        break;
                    }
                    if (cacheValue != null) {
                        Set<String> cacheKeysSpel = associationCacheInfo.getCacheKeysSpel();
                        EvaluationContext context = new StandardEvaluationContext(cacheValue);
                        for (String key : cacheKeysSpel) {
                            try {
                                Expression expression = parser.parseExpression(key);
                                Object value = expression.getValue(context);
                                redisTemplate.opsForValue().set(cachePrefix1 + value, JSONObject.toJSONString(cacheValue));
                            } catch (ParseException e) {
                                log.error("can't parse this key:{}", key);
                            }
                        }
                    } else {
                        log.error("cacheValue is null");
                    }
                }
            }
        }
    }
}
*/
