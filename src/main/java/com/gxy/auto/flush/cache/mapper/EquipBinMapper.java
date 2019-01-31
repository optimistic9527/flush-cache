package com.gxy.auto.flush.cache.mapper;
import com.gxy.auto.flush.cache.pojo.EquipBinDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.gxy.auto.flush.cache.domain.EquipBin;

/**
* Created by Mybatis Generator 2019/01/28
*/
public interface EquipBinMapper {
    int deleteByPrimaryKey(String equipbinid);

    int insert(EquipBin record);

    int insertSelective(EquipBin record);

    EquipBin selectByPrimaryKey(String equipbinid);

    int updateByPrimaryKeySelective(EquipBin record);

    int updateByPrimaryKey(EquipBin record);

    List<EquipBin> find();

    List<EquipBinDTO> findEquipBinDTO();
}