package com.gxy.auto.flush.cache.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.gxy.auto.flush.cache.annotation.CacheKey;
import com.gxy.auto.flush.cache.annotation.CacheTable;
import com.gxy.auto.flush.cache.annotation.PrimaryKey;
import lombok.Data;

/**
* Created by Mybatis Generator 2019/01/28
*/
@Data
@CacheTable(tableName = "h_equipment")
public class Equipment implements Serializable {
    @PrimaryKey
    private String equipmentid;

    private String tenantid;

    private String productid;

    private String orderid;

    private String regionid;

    private String communityid;

    private String sblx;

    private String sbmc;
    @CacheKey
    private String sbbh;

    private String fzbh;

    private String fzwz;

    private String simkh;

    private LocalDateTime simyxq;

    private String kzbb;

    private String txbb;

    private Integer sbzt;

    private String createuser;

    private LocalDateTime createtime;

    private String lastmodifyuser;

    private LocalDateTime lastmodifytime;

    private Integer active;

    private static final long serialVersionUID = 1L;
}