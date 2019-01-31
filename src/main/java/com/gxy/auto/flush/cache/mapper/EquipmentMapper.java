package com.gxy.auto.flush.cache.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.gxy.auto.flush.cache.domain.Equipment;

/**
* Created by Mybatis Generator 2019/01/28
*/
public interface EquipmentMapper {
    int deleteByPrimaryKey(String equipmentid);

    int insert(Equipment record);

    int insertSelective(Equipment record);

    Equipment selectByPrimaryKey(String equipmentid);

    int updateByPrimaryKeySelective(Equipment record);

    int updateByPrimaryKey(Equipment record);

    List<Equipment> find();


}