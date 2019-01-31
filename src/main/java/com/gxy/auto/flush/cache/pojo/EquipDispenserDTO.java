package com.gxy.auto.flush.cache.pojo;




import com.gxy.auto.flush.cache.annotation.Association;
import com.gxy.auto.flush.cache.annotation.AssociationCache;
import com.gxy.auto.flush.cache.domain.Equipment;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author guoxingyong
 * @since 2019/1/25 14:43
 */
@Data
@AssociationCache(tableName = "h_equip_dispenser", cachePrefix = "equipDispenserDTO", expireTime = 20, cacheKey = {"equipment.sbbh"})
public class EquipDispenserDTO {
    private String equipdispenserid;

    @Association(id = "equipmentid", tableName = "h_equipment")
    private Equipment equipment;

    private Integer jfdh;

    private Integer ljdzl;

    private Integer shlx;

    private Integer hwlx;

    private Integer ffjlx;

    private Integer ldpl;

    private Integer ldcs;

    private Integer mfld;

    private BigDecimal dtjd;

    private BigDecimal dtwd;

    private Integer dwly;
}
