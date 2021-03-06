# 生产者重试

**代码：** producer_retry.producer_send_fail

可以在发送消息时设置发送失败的重试次数，默认值是2

```
同步发送 - producer.send(msg)，使用producer.setRetryTimesWhenSendFailed()设置失败重试次数，默认2次
异步发送 - producer.send(msg, sendCallback)，使用producer.setRetryTimesWhenSendAsyncFailed()设置失败重试次数，默认2次
```
<br>

# 消费端重试

## 1、（主动）消费端异常，并反馈reconsumer_later

**代码：** consumer_retry.reconsume_later 

**场景：** 
当消费端发生异常，并反馈RECONSUME_LATER后，RocketMQ会在一定时间后重发消息给Consumer Group中的消费者，重发的时间间隔为：

|第几次重试|每次重试时间间隔|
| :--------: | :--------: |
| 1 | 10 秒 | 
| 2 | 30 秒 | 
| 3 | 1 分钟 | 
| 4 | 2 分钟 | 
| 5 | 3 分钟 | 
| 6 | 4 分钟 | 
| 7 | 5 分钟 | 
| 8 | 6 分钟 | 
| 9 | 7 分钟 | 
| 10 | 8 分钟 | 
| 11 | 9 分钟 | 
| 12 | 10 分钟 | 
| 13 | 20 分钟 | 
| 14 | 30 分钟 | 
| 15 | 1 小时 | 
| 16 | 2 小时 | 

在实际业务中，没必要重试那么多次

在失败一定次数后，可将消息持久化并告警，待后续处理


## 2、（被动）消费过程中宕机，RocketMQ主动负载给Consumer Group中的其它消费者

**代码：** consumer_retry.consumer_down

**场景：** 
先启动Consumer1、Consumer2，再启动Producer生产一条消息，假设消息正好由Consumer1消费，Consumer1会sleep 60秒模拟业务处理才反馈成功，在Consumer1未反馈期间，停掉Consumer1，模拟Consumer1宕机，此时RocketMQ会重发消息给Consumer2

## 3、（被动）部分Consumer订阅晚于Producer生产的非法情况，重试机制导致消息重复消费

**代码：** consumer_retry.start_late

**场景：** 
先启动Consumer1，再启动Producer生产一条消息，Consumer1会sleep 60秒才反馈成功，Consumer1未反馈期间，启动Consumer2，Consumer2也可以成功消费这条消息

这种情况就是不合法的了，即同一条消息被Consumer1、Consumer2重复消费。按照正常程序来说，应该先启动消费者，生产者再生产消息，但实际业务中不可能完全符合，比如程序升级期间可能产生类似上述的情况，Consumer2停机升级，再启动后导致重复消费，故消费端需要增加 **幂等处理**

>RocketMQ 无法避免消息重复，所以如果业务对消费重复非常敏感，务必要在业务层面去重，有以下几种去重方式：
>1. 将消息的唯一键，可以是 msgId，也可以是消息内容中的唯一标识字段，例如订单Id等，消费之前判断是否在Db 或 Tair(全局KV存储)中存在，如果不存在则插入，并消费，否则跳过。（实际过程要考虑原子性问题，判断是否存在可以使用尝试插入，如果报主键冲突，则插入失败，直接跳过）
>msgId 一定是全局唯一标识符，但是可能会存在同样的消息有两个不同 msgId 的情况（有多种原因），这种情况可能会使业务上重复消费，建议最好使用消息内容中的唯一标识字段去重
>2. 使用业务层面的状态机去重

RocketMQ消息可以在构造时指定业务keys：

`Message(String topic, String tags, String keys, byte[] body)`

在消费时可以获取keys，用于消息去重

