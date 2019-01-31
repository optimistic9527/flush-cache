package com.gxy.auto.flush.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author guoxingyong
 * @since 2019/1/25 20:08
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface AssociationCache {
    /**
     * @return 当前关联的主表的名称 如EquipDispenserDTO 返回的应该是 h_equip_dispenser
     */
    String tableName();

    String cachePrefix() default "";

    int expireTime() default 0;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String[] cacheKey();
}
