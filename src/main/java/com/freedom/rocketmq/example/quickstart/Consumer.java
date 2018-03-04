package com.freedom.rocketmq.example.quickstart;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 消费者
 */
public class Consumer {

    public static void main(String[] args) throws InterruptedException, MQClientException {
        /**
         * DefaultMQPushConsumer
         * 参数：consumerGroup，一类Consumer的集合名称，这类Consumer通常消费一类消息，且消费逻辑一致
         * 在集群消费中，一个Consumer Group中的Consumer实例平均分摊消费消息
         */
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("quickstart_consumer_group");

        //指定nameserver
        consumer.setNamesrvAddr("192.168.65.200:9876;192.168.65.201:9876");

        /**
         * 设置Consumer第一次启动是从队列头部开始消费，还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.subscribe("quickstart_topic", "*"); //* 说明不对Tag做限制

        //注册MessageListener（类似于ActiveMQ的onMessage()方法）
        //A MessageListenerConcurrently object is used to receive asynchronously delivered messages concurrently
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, //可以批量消费，可以指定批量获得的消息数量
                                                            ConsumeConcurrentlyContext context) {  //context的作用，消息可以自动提交 或 手动提交，Listener形式都是自动提交
                System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);
                try {
                    for (MessageExt msg : msgs) {
                        String topic = msg.getTopic();
                        String tags = msg.getTags();
                        String msgBody = new String(msg.getBody(), "utf-8");
                        System.out.println("收到消息：" + " topic：" + topic + " ,tags：" + tags + " ,msg：" + msgBody);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    //It is not recommend to throw exception,rather than returning ConsumeConcurrentlyStatus.RECONSUME_LATER if consumption failure
                    //如果消费失败，不建议抛异常，而是返回RECONSUME_LATER，稍后再发送
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; //告诉mq消费成功
            }
        });

        consumer.start(); //启动就不用停了，会一直监听有没有消息

        System.out.println("Consumer Started.");
    }
}
