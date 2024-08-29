package cn.itcast.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class ErrorMessageConfig {

//    @Autowired
    private RabbitTemplate template;

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange("error.direct");
    }

    @Bean
    public Queue queue(){
        return new Queue("error.queue");
    }

    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(directExchange()).with("error");
    }

    @Bean
    public MessageRecoverer messageRecoverer(){
        return new RepublishMessageRecoverer(template, "error.direct", "error");
    }
}
