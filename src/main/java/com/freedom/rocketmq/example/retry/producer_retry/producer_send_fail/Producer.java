package com.freedom.rocketmq.example.retry.producer_retry.producer_send_fail;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

/**
 * 生产者
 * 同步发送 - producer.send(msg)，使用producer.setRetryTimesWhenSendFailed()设置失败重试次数，默认2次
 * 异步发送 - producer.send(msg, sendCallback)，使用producer.setRetryTimesWhenSendAsyncFailed()设置失败重试次数，默认2次
 */
public class Producer {

    public static void main(String[] args) throws MQClientException, InterruptedException {
        String prefix = "producer_send_fail";
        DefaultMQProducer producer = new DefaultMQProducer(prefix + "_pgroup");

        //指定nameserver
        producer.setNamesrvAddr("192.168.65.200:9876;192.168.65.201:9876");
        producer.setRetryTimesWhenSendFailed(3);  //设置发送失败重试3次，默认值为2
        producer.start();

        for (int i = 0; i < 100; i++) {
            try {
                Message msg = new Message(prefix + "_topic",// topic
                                          "tagA",// tag
                                          ("Hello RocketMQ " + i).getBytes()// body
                );

                //设置发送超时时间为1秒，发送超时导致的失败，也会重试
                SendResult sendResult = producer.send(msg, 1000);
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
