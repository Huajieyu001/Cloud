package cn.itcast.mq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class LazyConfig {

    @Bean
    public Queue getLazyQueue(){
        return QueueBuilder.durable("lazy.queue")
                .lazy()
                .build();
    }

    @Bean
    public Queue getNormalQueue(){
        return QueueBuilder.durable("normal.queue")
                .build();
    }
}
