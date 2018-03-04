package com.freedom.rocketmq.example.quickstart;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

/**
 * 生产者
 */
public class Producer {

    public static void main(String[] args) throws MQClientException, InterruptedException {
        /**
         * DefaultMQProducer
         * 参数：producerGroup，必须指定，一类Producer的集合名，这类Producer通常发送一类消息，且发送逻辑一致
         */
        DefaultMQProducer producer = new DefaultMQProducer("quickstart_producer_group");

        //指定nameserver
        producer.setNamesrvAddr("192.168.65.200:9876;192.168.65.201:9876");
        producer.start();

        //循环发送消息
        for (int i = 0; i < 100; i++) {
            try {
                Message msg = new Message("quickstart_topic",// topic
                                          "tagA",// tag
                                          ("Hello RocketMQ " + i).getBytes()// body
                );

                /**
                 SendResult 发送结果
                 [
                    sendStatus = SEND_OK,                        //发送状态
                    msgId = C0A841C800002A9F0000000000001D06,    //可以记录一下返回的msgId，之后可以通过msgId到MQ查询消息
                    messageQueue = MessageQueue [
                                                topic = quickstart_topic,
                                                brokerName = broker-a,    //双主master模式，有的发到broker-a，有的发到broker-b，自动负载均衡
                                                queueId = 0               //进入到哪个队列，依次递增（一个topic下默认配置了4个queue）
                                                ],
                    queueOffset = 13                            //消息在queue队列中的偏移量
                 ]
                 */
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
