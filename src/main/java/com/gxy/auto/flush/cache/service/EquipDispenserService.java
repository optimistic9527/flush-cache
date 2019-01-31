package com.gxy.auto.flush.cache.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.gxy.auto.flush.cache.mapper.EquipDispenserMapper;
import com.gxy.auto.flush.cache.domain.EquipDispenser;

@Service
public class EquipDispenserService{

    @Resource
    private EquipDispenserMapper equipDispenserMapper;

    public int deleteByPrimaryKey(String equipdispenserid){
        return equipDispenserMapper.deleteByPrimaryKey(equipdispenserid);
    }

    public int insert(EquipDispenser record){
        return equipDispenserMapper.insert(record);
    }

    public int insertSelective(EquipDispenser record){
        return equipDispenserMapper.insertSelective(record);
    }

    public EquipDispenser selectByPrimaryKey(String equipdispenserid){
        return equipDispenserMapper.selectByPrimaryKey(equipdispenserid);
    }

    public int updateByPrimaryKeySelective(EquipDispenser record){
        return equipDispenserMapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(EquipDispenser record){
        return equipDispenserMapper.updateByPrimaryKey(record);
    }

}
