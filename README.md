# flush-cache
级联更新redis缓存
现在依赖于Canal组件与Kafka，由Canal伪装成mysql，把binlog的变更信息推送到kafka，应用程序拿到kafka的消息进行级联更新缓存，欠缺一个缓存预热，与协助级联更新缓存的Cache组件

![avatar](https://www.processon.com/view/link/5c47d655e4b0641c83e6487b)
