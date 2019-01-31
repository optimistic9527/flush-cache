package com.gxy.auto.flush.cache.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.gxy.auto.flush.cache.mapper.EquipBinMapper;
import com.gxy.auto.flush.cache.domain.EquipBin;

@Service
public class EquipBinService{

    @Resource
    private EquipBinMapper equipBinMapper;

    public int deleteByPrimaryKey(String equipbinid){
        return equipBinMapper.deleteByPrimaryKey(equipbinid);
    }

    public int insert(EquipBin record){
        return equipBinMapper.insert(record);
    }

    public int insertSelective(EquipBin record){
        return equipBinMapper.insertSelective(record);
    }

    public EquipBin selectByPrimaryKey(String equipbinid){
        return equipBinMapper.selectByPrimaryKey(equipbinid);
    }

    public int updateByPrimaryKeySelective(EquipBin record){
        return equipBinMapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(EquipBin record){
        return equipBinMapper.updateByPrimaryKey(record);
    }

}
