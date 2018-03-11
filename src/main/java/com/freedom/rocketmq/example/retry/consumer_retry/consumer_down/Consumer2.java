package com.freedom.rocketmq.example.retry.consumer_retry.consumer_down;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 消费者2
 */
public class Consumer2 {

    public Consumer2() {
        try{
            String prefix = "consumer_down";
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
                    System.out.println("收到消息：" + " topic：" + topic + " ,tags：" + tags + " ,msg：" + msgBody);
                }
            } catch (Exception e) {
                e.printStackTrace();

                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }


    public static void main(String[] args) throws InterruptedException, MQClientException {
        Consumer2 c2 = new Consumer2();
        System.out.println("Consumer2 started");
    }
}
