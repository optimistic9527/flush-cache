package com.gxy.auto.flush.cache.pojo;



import com.gxy.auto.flush.cache.annotation.Association;
import com.gxy.auto.flush.cache.annotation.AssociationCache;
import com.gxy.auto.flush.cache.domain.Equipment;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AssociationCache(tableName = "h_equip_bin", cachePrefix = "EquipBinDTO:", expireTime = 20, cacheKey = {"equipment.sbbh"})
public class EquipBinDTO {
    private String equipbinid;

    @Association(id = "equipmentid", tableName = "h_equipment")
    private Equipment equipment;

    private Integer flxlx;

    private Integer czlx;

    private String zdsb;

    private BigDecimal dtjd;

    private BigDecimal dtwd;

    private Integer dwly;
}

