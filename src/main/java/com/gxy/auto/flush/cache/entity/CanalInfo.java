package com.gxy.auto.flush.cache.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guoxingyong
 */
@Data
public class CanalInfo {

    /**
     * 数据库名称
     */
    private String database;
    /**
     * 数据库里面的执行时间(event time)
     */
    private Long es;
    /**
     * 记录id
     */
    private Integer id;
    /**
     * 是否是DDL语句
     */
    private Boolean isDdl;
    /**
     * 例子:
     * cashierId : varchar(15)
     * accumulateCashier : int(255)
     * merchantId : varchar(20)
     */
    private Map<String, String> mysqlType;
    /**
     * sql语句
     */
    private String sql;
    /**
     * cashierId : 12
     * accumulateCashier : 4
     * merchantId : 12
     */
    private Map<String, Integer> sqlType;
    /**
     * 表名
     */
    private String table;
    /**
     * 解析时间(process time)
     */
    private Long ts;
    /**
     * 操作类型
     */
    private String type;

    /**
     * bean修改后的数据
     */
    private List<JSONObject> data;
    /**
     * old : [{"accumulateCashier":"59","merchantId":"009011"}]
     */
    private List<Map<String, ?>> old;

}
