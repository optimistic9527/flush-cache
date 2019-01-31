package com.gxy.auto.flush.cache.listener;

import com.alibaba.fastjson.JSONObject;
import com.gxy.auto.flush.cache.entity.AssociationCacheInfo;
import com.gxy.auto.flush.cache.entity.AssociationInfo;
import com.gxy.auto.flush.cache.entity.CanalInfo;
import com.gxy.auto.flush.cache.entity.TableCacheInfo;
import com.gxy.auto.flush.cache.manage.Test2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author guoxingyong
 * @since 2019/1/30 10:25
 */
@Component
@Slf4j
public class DefaultTableChangeListener implements TableChangeListener {

    @Autowired
    private Test2 test2;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override //因为update的时候相关DTO需要必须在redis中有一份数据的情况下才能进行级联更新,所以必须要一套跟SpringCache一样的，
    //标记在方法上用来帮助缓存必须的DTO信息，所有这里只用存储基础信息，关联信息由另外组件进行提供
    public void onInsert(CanalInfo canalInfo) {
        String tableName = canalInfo.getTable();
        List<JSONObject> newTableDataList = canalInfo.getData();
        HashMap<String, TableCacheInfo> tableCacheInfoHashMap = test2.getTableCacheInfoHashMap();
        TableCacheInfo tableCacheInfo = tableCacheInfoHashMap.get(tableName);
        if (tableCacheInfo == null) {
            log.warn("don't register this table:{}", tableName);
            return;
        }
        String primaryKey = tableCacheInfo.getPrimaryKey();
        Set<String> cacheKeys = tableCacheInfo.getCacheKeys();
        String cachePrefix = tableCacheInfo.getCachePrefix();
        for (JSONObject newTableData : newTableDataList) {
            String jsonString = newTableData.toJSONString();
            Object primaryKeyValue = newTableData.get(primaryKey);
            if (primaryKeyValue == null) {
                log.error("primaryKey can't get value,CanalInfo:{},please check", canalInfo.toString());
                continue;
            }
            redisTemplate.opsForValue().set(cachePrefix + primaryKeyValue, jsonString);
            for (String cacheKey : cacheKeys) {
                Object cacheKeyValue = newTableData.get(cacheKey);
                if (cacheKeyValue != null) {
                    redisTemplate.opsForValue().set(cachePrefix + cacheKeyValue, jsonString);
                }
            }
        }
    }

    @Override //fixme 不同数据库,表名称相同?
    public void onUpdate(CanalInfo canalInfo) {
        long l = System.currentTimeMillis();
        String tableName = canalInfo.getTable();
        List<JSONObject> newTableDataList = canalInfo.getData();//index 与 oldTableChangeData的index是一一对应的
        List<Map<String, ?>> oldTableChangeData = canalInfo.getOld();
        HashMap<String, TableCacheInfo> tableCacheInfoHashMap = test2.getTableCacheInfoHashMap();
        TableCacheInfo tableCacheInfo = tableCacheInfoHashMap.get(tableName);
        if (tableCacheInfo == null) {
            log.warn("don't register this table:{}", tableName);
            return;
        }
        tableCacheRefresh(newTableDataList, oldTableChangeData, tableCacheInfo);
        HashMap<String, List<AssociationCacheInfo>> associationCacheInfoHashMap = test2.getAssociationCacheInfoHashMap();
        List<AssociationCacheInfo> associationCacheInfoList = associationCacheInfoHashMap.get(tableName);
        if (associationCacheInfoList != null) {
            //刷新关联类中是已该table为主的类例如UserDTO->User
            tableAssociationCacheRefresh(newTableDataList, oldTableChangeData, tableCacheInfo, associationCacheInfoList);
        }
        HashMap<String, List<AssociationCacheInfo>> associationHashMap = test2.getAssociationHashMap();
        List<AssociationCacheInfo> associationCacheInfos = associationHashMap.get(tableName);
        if (associationCacheInfos != null) {
            //刷新关联类中含有该table信息的类 例如MerchantDTO(User)里面包含有User的信息
            associationTableCacheRefresh(tableName, newTableDataList, oldTableChangeData, tableCacheInfo, associationCacheInfos);
        }
        System.out.println(System.currentTimeMillis() - l);
    }

    private void associationTableCacheRefresh(String tableName, List<JSONObject> newTableDataList, List<Map<String, ?>> oldTableChangeData, TableCacheInfo tableCacheInfo, List<AssociationCacheInfo> associationCacheInfos) {
        for (int i = 0, length = newTableDataList.size(); i < length; i++) {
            JSONObject newTableData = newTableDataList.get(i);
            Map<String, ?> oldValueMap = oldTableChangeData.get(i);
            Object newTableInfo = newTableData.toJavaObject(tableCacheInfo.getCacheClass());
            for (AssociationCacheInfo associationCacheInfo : associationCacheInfos) {
                String cachePrefix = associationCacheInfo.getCachePrefix();
                String primaryKey = associationCacheInfo.getPrimaryKey();
                Set<String> cacheKeysSpel = associationCacheInfo.getCacheKeysSpel();
                Map<String, AssociationInfo> associationMap = associationCacheInfo.getAssociationMap();
                AssociationInfo associationInfo = associationMap.get(tableName);
                String filedName = associationInfo.getFiledName();
                String associationId = associationInfo.getAssociationId();
                Object associationValue;
                //如果关联id被修改需要删除关联的cache
                if (oldValueMap.containsKey(associationId)) {
                    associationValue = oldValueMap.get(associationId);
                } else {
                    associationValue = newTableData.get(associationId);
                }
                if (associationValue == null) {
                    log.error("?????");
                    return;
                }
                //获取原来缓存在redis中的value
                Object oldCacheValue = null;
                String oldCacheJsonString = redisTemplate.opsForValue().get(cachePrefix + associationValue);
                if (oldCacheJsonString != null) {
                    oldCacheValue = JSONObject.parseObject(oldCacheJsonString, associationCacheInfo.getCacheClass());
                }
                if (oldCacheValue != null) {
                    BeanWrapper beanWrapper = new BeanWrapperImpl(oldCacheValue);
                    beanWrapper.setPropertyValue(filedName, newTableInfo);
                    String newCacheValue = JSONObject.toJSONString(oldCacheValue);
                    cacheIfPresent(cachePrefix, beanWrapper, newCacheValue, primaryKey);
                    for (String cacheKey : cacheKeysSpel) {
                        if (cacheKey.contains(filedName + ".")) {
                            //说明已关联的类的某一个属性进行的缓存，所以这里需要去判断一下该值是不是更新的值，如果是需要删除老值
                            String associationCacheKey = cacheKey.substring(filedName.length() + 1, cacheKey.length());
                            if (oldValueMap.containsKey(associationCacheKey)) {
                                Object associationCacheKeyValue = oldValueMap.get(associationCacheKey);
                                redisTemplate.delete(cachePrefix + associationCacheKeyValue);
                            }
                        }
                        Object propertyValue = beanWrapper.getPropertyValue(cacheKey);
                        if (propertyValue != null) {
                            //todo 是否需要设置过期时间？
                            redisTemplate.opsForValue().set(cachePrefix + propertyValue, newCacheValue);
                        }
                    }
                }
            }
        }
    }

    private void tableAssociationCacheRefresh(List<JSONObject> newTableDataList, List<Map<String, ?>> oldTableChangeData, TableCacheInfo tableCacheInfo, List<AssociationCacheInfo> associationCacheInfoList) {
        for (int i = 0, length = newTableDataList.size(); i < length; i++) {
            JSONObject newTableData = newTableDataList.get(i);
            Map<String, ?> oldValueMap = oldTableChangeData.get(i);
            Object newTableInfo = newTableData.toJavaObject(tableCacheInfo.getCacheClass());
            for (AssociationCacheInfo associationCacheInfo : associationCacheInfoList) {
                String cachePrefix = associationCacheInfo.getCachePrefix();
                String primaryKey = associationCacheInfo.getPrimaryKey();
                Set<String> cacheKeysSpel = associationCacheInfo.getCacheKeysSpel();
                //获取原来缓存在redis中的value
                Object oldCacheValue = null;
                //primaryKey被修改了
                if (oldValueMap.containsKey(primaryKey)) {
                    Object oldKey = oldValueMap.get(primaryKey);
                    String oldCacheJsonString = redisTemplate.opsForValue().get(cachePrefix + oldKey);
                    if (oldCacheJsonString != null) {
                        oldCacheValue = JSONObject.parseObject(oldCacheJsonString, associationCacheInfo.getCacheClass());
                        redisTemplate.delete(cachePrefix + oldKey);
                    }
                } else {
                    Object primaryKeyValue = newTableData.get(primaryKey);
                    String oldCacheJsonString = redisTemplate.opsForValue().get(cachePrefix + primaryKeyValue);
                    if (oldCacheJsonString != null) {
                        oldCacheValue = JSONObject.parseObject(oldCacheJsonString, associationCacheInfo.getCacheClass());
                    }
                }
                if (oldCacheValue != null) {
                    //把新的值进行更新
                    BeanUtils.copyProperties(newTableInfo, oldCacheValue);
                    BeanWrapper beanWrapper = new BeanWrapperImpl(oldCacheValue);
                    String newCacheValue = JSONObject.toJSONString(oldCacheValue);
                    cacheIfPresent(cachePrefix, beanWrapper, newCacheValue, primaryKey);
                    for (String cacheKey : cacheKeysSpel) {
                        cacheIfPresent(cachePrefix, beanWrapper, newCacheValue, cacheKey);
                    }
                }
            }
        }
    }

    private void cacheIfPresent(String cachePrefix, BeanWrapper beanWrapper, String newCacheValue, String cacheKey) {
        Object propertyValue = beanWrapper.getPropertyValue(cacheKey);
        if (propertyValue != null) {
            //todo 是否需要设置过期时间？
            redisTemplate.opsForValue().set(cachePrefix + propertyValue, newCacheValue);
        }
    }

    //注册的tableCache表进行缓存刷新
    private void tableCacheRefresh(List<JSONObject> newTableDataList, List<Map<String, ?>> oldTableChangeData, TableCacheInfo tableCacheInfo) {
        String cachePrefix = tableCacheInfo.getCachePrefix();
        String primaryKey = tableCacheInfo.getPrimaryKey();
        Set<String> cacheKeys = tableCacheInfo.getCacheKeys();
        for (int i = 0, length = newTableDataList.size(); i < length; i++) {
            JSONObject newTableData = newTableDataList.get(i);
            Map<String, ?> oldValueMap = oldTableChangeData.get(i);
            removeCacheIfContains(cachePrefix, primaryKey, oldValueMap);
            for (String cacheKey : cacheKeys) {
                removeCacheIfContains(cachePrefix, cacheKey, oldValueMap);
            }
            Object tableObject = newTableData.toJavaObject(tableCacheInfo.getCacheClass());
            String cacheValue = newTableData.toJSONString();
            updateCacheIfNotNull(cachePrefix, primaryKey, tableObject, cacheValue);
            for (String cacheKey : cacheKeys) {
                updateCacheIfNotNull(cachePrefix, cacheKey, tableObject, cacheValue);
            }
        }
    }

    private void updateCacheIfNotNull(String cachePrefix, String key, Object tableObject, String cacheValue) {
        Object fieldValue = getFieldValueByName(key, tableObject);
        if (fieldValue != null) {
            redisTemplate.opsForValue().set(cachePrefix + fieldValue, cacheValue);
        } else {
            log.error("this table class:{} cache key:{} can be null,please check", tableObject, key);
        }
    }

    /**
     * fixme ?????
     *
     * @param cachePrefix the cachePrefix
     * @param key         key的值
     * @param oldValueMap 修改前的字段的value
     */
    private boolean removeCacheIfContains(String cachePrefix, String key, Map<String, ?> oldValueMap) {
        boolean result = oldValueMap.containsKey(key);
        if (result) {
            Object oldKey = oldValueMap.get(key);
            redisTemplate.delete(cachePrefix + oldKey);
        }
        return result;
    }

    @Override
    public void onDelete(CanalInfo canalInfo) {

    }

    private static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter);
            return method.invoke(o);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
