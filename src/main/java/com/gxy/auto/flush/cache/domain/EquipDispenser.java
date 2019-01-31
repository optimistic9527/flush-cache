package com.gxy.auto.flush.cache.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import com.gxy.auto.flush.cache.annotation.CacheKey;
import com.gxy.auto.flush.cache.annotation.CacheTable;
import com.gxy.auto.flush.cache.annotation.PrimaryKey;
import lombok.Data;

/**
* Created by Mybatis Generator 2019/01/28
*/
@Data
@CacheTable(tableName = "h_equip_dispenser")
public class EquipDispenser implements Serializable {
    @PrimaryKey
    private String equipdispenserid;

    private String equipmentid;

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

    private static final long serialVersionUID = 1L;
}