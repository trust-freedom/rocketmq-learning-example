package com.freedom.rocketmq.example.retry.consumer_retry.consumer_start_late;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

/**
 * 生产者
 * 只生产一条消息，此时只有Consumer1启动，Consumer1接收到消息
 * 在Consumer1业务处理未ACK时，启动Consumer2，Consumer2也会接收到这条消息
 */
public class Producer {

    public static void main(String[] args) throws MQClientException, InterruptedException {
        String prefix = "consumer_start_late";
        DefaultMQProducer producer = new DefaultMQProducer(prefix + "_pgroup");

        //指定nameserver
        producer.setNamesrvAddr("192.168.65.200:9876;192.168.65.201:9876");
        producer.start();

        //只生产一条消息
        for (int i = 0; i < 1; i++) {
            try {
                Message msg = new Message(prefix + "_topic",// topic
                                          "tagA",// tag
                                          ("Hello RocketMQ " + i).getBytes()// body
                );

                SendResult sendResult = producer.send(msg);
                System.out.println(sendResult);
            }
            catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }

        producer.shutdown();
    }
}
