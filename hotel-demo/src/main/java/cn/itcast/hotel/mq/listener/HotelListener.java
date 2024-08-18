package cn.itcast.hotel.mq.listener;

import cn.itcast.hotel.constants.RabbitMqConstants;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HotelListener {

    @Autowired
    IHotelService service;

    @RabbitListener(queues = {RabbitMqConstants.HOTEL_INSERT_QUEUE})
    public void insertListener(Long id){
        service.insertById(id);
    }

    @RabbitListener(queues = {RabbitMqConstants.HOTEL_DELETE_QUEUE})
    public void deleteListener(Long id){
        service.deleteById(id);
    }
}
