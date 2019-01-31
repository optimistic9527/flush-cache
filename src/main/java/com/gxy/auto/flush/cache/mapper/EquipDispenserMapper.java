package com.gxy.auto.flush.cache.mapper;
import com.gxy.auto.flush.cache.pojo.EquipDispenserDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.gxy.auto.flush.cache.domain.EquipDispenser;

/**
* Created by Mybatis Generator 2019/01/28
*/
public interface EquipDispenserMapper {
    int deleteByPrimaryKey(String equipdispenserid);

    int insert(EquipDispenser record);

    int insertSelective(EquipDispenser record);

    EquipDispenser selectByPrimaryKey(String equipdispenserid);

    int updateByPrimaryKeySelective(EquipDispenser record);

    int updateByPrimaryKey(EquipDispenser record);

    List<EquipDispenser> find();

    List<EquipDispenserDTO> findEquipDispenserDTO();
}