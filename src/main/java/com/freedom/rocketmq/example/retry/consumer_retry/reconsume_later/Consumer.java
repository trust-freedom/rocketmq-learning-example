package com.freedom.rocketmq.example.retry.consumer_retry.reconsume_later;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 消费者
 * 当消费失败，并反馈RECONSUME_LATER后，RocketMQ会有自动重试机制
 * 分别在10s、30s、1min、2min ...2h，最多重试16次
 * 但在实际业务中，没必要重试那么多次
 * 在失败一定次数后，可将消息持久化并告警，待后续处理
 */
public class Consumer {

    public static void main(String[] args) throws Exception {
        String prefix = "reconsume_later";
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(prefix + "_cgroup");

        //指定nameserver
        consumer.setNamesrvAddr("192.168.65.200:9876;192.168.65.201:9876");

        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        consumer.subscribe(prefix + "_topic", "*");

        //注册MessageListener
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                System.out.println(Thread.currentThread().getName() + " Receive New Messages: " + msgs);
                MessageExt msg = null;
                try {
                    msg = msgs.get(0);
                    String topic = msg.getTopic();
                    String tags = msg.getTags();
                    String msgBody = new String(msg.getBody(), "utf-8");
                    System.out.println("收到消息：" + " topic：" + topic + " ,tags：" + tags + " ,msg：" + msgBody);

                    if(true){
                        System.out.println("主动抛异常");
                        throw new Exception();
                    }
                }
                catch (Exception e) {
                    //e.printStackTrace();

                    System.out.println(df.format(new Date()) + " 消费失败，当前是第" + msg.getReconsumeTimes() + "次重试");

                    if(msg.getReconsumeTimes() == 2){
                        System.out.println("第2次重试后，仍然失败，记录失败日志，不再重复消费");

                        //TODO 持久化失败日志

                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }

                    //It is not recommend to throw exception,rather than returning ConsumeConcurrentlyStatus.RECONSUME_LATER if consumption failure
                    //如果消费失败，不建议抛异常，而是返回RECONSUME_LATER，稍后再发送
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });

        consumer.start();
        System.out.println("Consumer Started.");

        Thread.sleep(60 * 1000);

        MessageExt msg = consumer.viewMessage("失败2次后持久化的offsetMsgId");
        System.out.println(msg);
    }
}
