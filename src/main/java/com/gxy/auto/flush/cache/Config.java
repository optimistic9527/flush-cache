package com.gxy.auto.flush.cache;

import com.alibaba.fastjson.JSONObject;
import com.gxy.auto.flush.cache.domain.EquipBin;
import com.gxy.auto.flush.cache.domain.EquipDispenser;
import com.gxy.auto.flush.cache.domain.Equipment;
import com.gxy.auto.flush.cache.entity.AssociationCacheInfo;
import com.gxy.auto.flush.cache.entity.TableCacheInfo;
import com.gxy.auto.flush.cache.manage.Test2;
import com.gxy.auto.flush.cache.mapper.EquipBinMapper;
import com.gxy.auto.flush.cache.mapper.EquipDispenserMapper;
import com.gxy.auto.flush.cache.mapper.EquipmentMapper;
import com.gxy.auto.flush.cache.pojo.EquipBinDTO;
import com.gxy.auto.flush.cache.pojo.EquipDispenserDTO;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 缓存预热
 * @author guoxingyong
 * @since 2019/1/28 22:17
 */
@Component
public class Config {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    EquipBinMapper equipBinMapper;
    @Autowired
    EquipmentMapper equipmentMapper;
    @Autowired
    EquipDispenserMapper equipDispenserMapper;
    @Autowired
    Test2 test2;

    @PostConstruct
    private void init() {
        List<EquipBin> equipBins = equipBinMapper.find();
        HashMap<String, TableCacheInfo> tableCacheInfoHashMap = test2.getTableCacheInfoHashMap();
        HashMap<String, List<AssociationCacheInfo>> associationCacheInfoHashMap = test2.getAssociationCacheInfoHashMap();
        equipBins.forEach(equipBin -> {
            TableCacheInfo h_equip_bin = tableCacheInfoHashMap.get("h_equip_bin");
            String primaryKey = h_equip_bin.getPrimaryKey();
            Set<String> cacheKeys = h_equip_bin.getCacheKeys();
            BeanWrapper beanWrapper=new BeanWrapperImpl(equipBin);
            redisTemplate.opsForValue().set("h_equip_bin" + beanWrapper.getPropertyValue(primaryKey), JSONObject.toJSONString(equipBin));
            for (String cacheKey : cacheKeys) {
                redisTemplate.opsForValue().set("h_equip_bin" + beanWrapper.getPropertyValue(cacheKey), JSONObject.toJSONString(equipBin));
            }
        });
        List<EquipBinDTO> equipBinDTO = equipBinMapper.findEquipBinDTO();
        for (EquipBinDTO binDTO : equipBinDTO) {
            List<AssociationCacheInfo> h_equip_bin = associationCacheInfoHashMap.get("h_equip_bin");
            AssociationCacheInfo associationCacheInfo = h_equip_bin.get(0);
            String primaryKey = associationCacheInfo.getPrimaryKey();
            Set<String> cacheKeysSpel = associationCacheInfo.getCacheKeysSpel();
            BeanWrapper beanWrapper=new BeanWrapperImpl(binDTO);
            redisTemplate.opsForValue().set("EquipBinDTO:" + beanWrapper.getPropertyValue(primaryKey), JSONObject.toJSONString(binDTO));
            for (String cacheKey : cacheKeysSpel) {
                redisTemplate.opsForValue().set("EquipBinDTO:" + beanWrapper.getPropertyValue(cacheKey), JSONObject.toJSONString(binDTO));
            }
        }

        List<Equipment> equipments = equipmentMapper.find();
        equipments.forEach(equipment -> {
            redisTemplate.opsForValue().set("h_equipment" + equipment.getSbbh(), JSONObject.toJSONString(equipment));
            redisTemplate.opsForValue().set("h_equipment" + equipment.getEquipmentid(), JSONObject.toJSONString(equipment));
        });


        /*List<EquipDispenser> equipDispensers = equipDispenserMapper.find();
        for (EquipDispenser equipDispenser : equipDispensers) {
            redisTemplate.opsForValue().set("h_equip_dispenser" + equipDispenser.getEquipdispenserid(), JSONObject.toJSONString(equipDispenser));
        }
        List<EquipDispenserDTO> equipDispenserDTO = equipDispenserMapper.findEquipDispenserDTO();
        for (EquipDispenserDTO dispenserDTO : equipDispenserDTO) {
            redisTemplate.opsForValue().set("EquipDispenserDTO" + dispenserDTO.getEquipment().getSbbh(), JSONObject.toJSONString(dispenserDTO));
        }*/
    }
}
