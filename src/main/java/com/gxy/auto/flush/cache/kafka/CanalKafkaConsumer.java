package com.gxy.auto.flush.cache.kafka;

import com.alibaba.fastjson.JSONObject;

import com.gxy.auto.flush.cache.contant.TableOperatorConst;
import com.gxy.auto.flush.cache.entity.CanalInfo;
import com.gxy.auto.flush.cache.listener.DefaultTableChangeListener;
import com.gxy.auto.flush.cache.listener.TableChangeListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author guoxingyong
 */
@Configuration
public class CanalKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CanalKafkaConsumer.class);

    @Autowired
    private TableChangeListener tableChangeListener;


    @KafkaListener(topics = {"${kafka.canal.topic}"})
    public void consumerListen(List<ConsumerRecord<String, String>> consumerRecordList) {
        for (ConsumerRecord<String, String> stringStringConsumerRecord : consumerRecordList) {
            logger.info(stringStringConsumerRecord.value());
            CanalInfo canalInfo = JSONObject.parseObject(stringStringConsumerRecord.value(), CanalInfo.class);
            switch (canalInfo.getType()) {
                case TableOperatorConst.INSERT:
                    tableChangeListener.onInsert(canalInfo);
                    break;
                case TableOperatorConst.UPDATE:
                    tableChangeListener.onUpdate(canalInfo);
                    break;
                case TableOperatorConst.DELETE:
                    break;
                default:
                    //logger.warn("can't find this TableOperator:{}", canalEntity.getType());
                    break;
            }
        }
    }
}


