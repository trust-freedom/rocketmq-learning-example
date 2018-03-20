package com.freedom.rocketmq.example.retry.consumer_retry.consumer_start_late;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 消费者1
 * 一开始只启动Consumer1，并故意sleep 60s模拟业务处理，之后再ACK
 * 测试中需要在sleep 60s期间将Consumer2启动，RocketMQ会将消息重发到Consumer2，导致重复消费
 * 这是一种非法操作,实际业务中需要保证消费者先订阅Topic，生产者才生产消息
 * 但这是很难做到的，故消费端需要实现“幂等性”
 */
public class Consumer1 {

    public Consumer1() {
        try{
            String prefix = "consumer_start_late";
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(prefix + "_cgroup");

            //指定nameserver
            consumer.setNamesrvAddr("192.168.65.200:9876;192.168.65.201:9876");

            consumer.subscribe(prefix + "_topic", "*");

            //注册MessageListener
            consumer.registerMessageListener(new Listener());
            consumer.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * MessageListener
     */
    class Listener implements MessageListenerConcurrently {
        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            try {
                for (MessageExt msg : msgs) {
                    String topic = msg.getTopic();
                    String tags = msg.getTags();
                    String msgBody = new String(msg.getBody(), "utf-8");
                    System.out.println("收到消息，还未反馈消费状态：" + " topic：" + topic + " ,tags：" + tags + " ,msg：" + msgBody);

                    System.out.println("Consumer1 开始休眠");
                    Thread.sleep(60 * 1000);
                    System.out.println("Consumer1 休眠结束，反馈消费状态");
                }
            } catch (Exception e) {
                e.printStackTrace();

                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }


    public static void main(String[] args) throws InterruptedException, MQClientException {
        Consumer1 c1 = new Consumer1();
        System.out.println("Consumer1 started");
    }
}
