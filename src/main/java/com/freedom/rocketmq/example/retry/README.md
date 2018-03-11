# 消费端重试

## 1、（主动）消费端异常，并反馈reconsumer_later

**代码：** consumer_retry.reconsume_later 

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

**场景1：** 
先启动Consumer1、Consumer2，再启动Producer生产一条消息，假设消息正好由Consumer1消费，Consumer1会sleep 60秒才反馈成功，在Consumer1未反馈期间，停掉Consumer1，模拟Consumer1宕机，此时Consumer2会收到此条消息

**场景2：** 
先启动Consumer1，再启动Producer生产一条消息，Consumer1会sleep 60秒才反馈成功，Consumer1未反馈期间，启动Consumer2，Consumer2也可以成功消费这条消息

这种情况就是不合法的了，即同一条消息被Consumer1、Consumer2重复消费。按照正常程序来说，应该先启动消费者，生产者再生产消息，但实际业务中不可能完全符合，比如程序升级期间可能产生类似上述的情况，Consumer2停机升级，再启动后导致重复消费，故消费端需要增加 **幂等处理**


