package cn.itcast.mq.spring;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage2SimpleQueue() throws InterruptedException {
        String routingKey = "simple";
        String message = "hello, spring amqp!";
        rabbitTemplate.convertAndSend("amq.topic", routingKey, message);
    }

    @Test
    public void testSendMessage2SimpleQueue2() throws InterruptedException {
        String routingKey = "simple";
        String message = "hello, spring amqp!";

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        correlationData.getFuture().addCallback(result -> {
                    assert result != null;
                    if (result.isAck()) {
                        log.debug("消息成功发送到交换机：" + correlationData.getId());
                    } else {
                        log.error("消息发送到交换机失败：" + correlationData.getId());
                    }
                },
                ex -> {
                    log.error("消息发送到队列失败：" + ex);
                });
        rabbitTemplate.convertAndSend("amq.topic", "simple.test", message, correlationData);
    }

    @Test
    public void testDurableMsg() {
        Message message = MessageBuilder.withBody("hello, world".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        rabbitTemplate.convertAndSend("simple.queue", message);
    }

    @Test
    public void testTTLQueueMsg() {
        Message message = MessageBuilder.withBody("hello, world".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        rabbitTemplate.convertAndSend("ttl.direct", "ttl", message);
        log.info("TTLQueue消息发送成功");
    }

    @Test
    public void testTTLMsg() {
        Message message = MessageBuilder.withBody("hello, world".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setExpiration("5000") // 设置过期时间为5000毫秒
                .build();

        rabbitTemplate.convertAndSend("ttl.direct", "ttl", message);
        log.info("TTL消息发送成功");
    }

    @Test
    public void testTTLMsg2() {
        Message message = MessageBuilder.withBody("hello, world".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setExpiration("15000") // 设置过期时间为15000毫秒，此时dl.queue的过期时间为10000毫秒，测试过期时间
                .build();

        rabbitTemplate.convertAndSend("ttl.direct", "ttl", message);
        log.info("TTL消息发送成功");
    }

    @Test
    public void testToLazyQueueMsg() {
        for (int i = 0; i < 1000000; i++) {
            Message message = MessageBuilder.withBody("hello, world".getBytes(StandardCharsets.UTF_8))
                    .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                    .build();

            rabbitTemplate.convertAndSend("lazy.queue", message);
        }
    }

    @Test
    public void testToNormalQueueMsg() {
        for (int i = 0; i < 1000000; i++) {
            Message message = MessageBuilder.withBody("hello, world".getBytes(StandardCharsets.UTF_8))
                    .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                    .build();

            rabbitTemplate.convertAndSend("normal.queue", message);
        }
    }
}
