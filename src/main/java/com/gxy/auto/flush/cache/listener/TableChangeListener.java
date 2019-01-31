package com.gxy.auto.flush.cache.listener;

import com.gxy.auto.flush.cache.entity.CanalInfo;

/**
 * @author guoxingyong
 * @since 2019/1/30 10:24
 */
public interface TableChangeListener {

    void onInsert(CanalInfo canalInfo);

    void onUpdate(CanalInfo canalInfo);

    void onDelete(CanalInfo canalInfo);
}
