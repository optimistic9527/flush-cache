package com.gxy.auto.flush.cache.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.gxy.auto.flush.cache.mapper.EquipmentMapper;
import com.gxy.auto.flush.cache.domain.Equipment;

@Service
public class EquipmentService{

    @Resource
    private EquipmentMapper equipmentMapper;

    public int deleteByPrimaryKey(String equipmentid){
        return equipmentMapper.deleteByPrimaryKey(equipmentid);
    }

    public int insert(Equipment record){
        return equipmentMapper.insert(record);
    }

    public int insertSelective(Equipment record){
        return equipmentMapper.insertSelective(record);
    }

    public Equipment selectByPrimaryKey(String equipmentid){
        return equipmentMapper.selectByPrimaryKey(equipmentid);
    }

    public int updateByPrimaryKeySelective(Equipment record){
        return equipmentMapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(Equipment record){
        return equipmentMapper.updateByPrimaryKey(record);
    }

}
