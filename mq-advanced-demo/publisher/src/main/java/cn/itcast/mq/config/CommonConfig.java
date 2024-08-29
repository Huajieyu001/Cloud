package cn.itcast.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CommonConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate template = applicationContext.getBean(RabbitTemplate.class);

        template.setReturnCallback((msg, replyCode, replyText, exchange, routingkey) -> {
            log.info("Send failed! msg={}, reply={}:{}, exchange={}, routingkey={}", msg, replyCode, replyText, exchange, routingkey);
        });
    }
}
