package cn.itcast.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class TTLMessageConfig {

    @Bean
    public DirectExchange ttlExchange(){
        return new DirectExchange("ttl.direct");
    }

    @Bean
    public Queue ttlQueue(){
         return QueueBuilder.durable("ttl.queue")
                 .ttl(10000)
                 .deadLetterExchange("dl.direct")
                 .deadLetterRoutingKey("dl")
                 .build();
    }

    @Bean
    public Binding ttlBinding(){
        return BindingBuilder.bind(ttlQueue()).to(ttlExchange()).with("ttl");
    }

//    @Bean
//    public DirectExchange dlExchange(){
//        return new DirectExchange("dl.direct");
//    }
//
//    @Bean
//    public Queue dlQueue(){
//        return QueueBuilder
//                .durable("dl.queue").build();
//    }
//
//    @Bean
//    public Binding dlBinding(){
//        return BindingBuilder.bind(dlQueue()).to(dlExchange()).with("dl");
//    }
}
