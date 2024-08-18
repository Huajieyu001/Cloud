package cn.itcast.hotel.config;

import cn.itcast.hotel.constants.RabbitMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {
    @Bean
    public TopicExchange getTopicExchange(){
        return new TopicExchange(RabbitMqConstants.HOTEL_EXCHANGE, true, false);
    }

    @Bean
    public Queue hotelInsertQueue(){
        return new Queue(RabbitMqConstants.HOTEL_INSERT_QUEUE, true);
    }

    @Bean
    public Queue hotelDeleteQueue(){
        return new Queue(RabbitMqConstants.HOTEL_DELETE_QUEUE, true);
    }

    @Bean
    public Binding hotelInsertBinding(){
        return BindingBuilder.bind(hotelInsertQueue()).to(getTopicExchange()).with(RabbitMqConstants.HOTEL_INSERT_ROUTING_KEY);
    }

    @Bean
    public Binding hotelDeleteBinding(){
        return BindingBuilder.bind(hotelDeleteQueue()).to(getTopicExchange()).with(RabbitMqConstants.HOTEL_DELETE_ROUTING_KEY);
    }
}
