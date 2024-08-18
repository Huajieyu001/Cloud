package cn.itcast.hotel.constants;

public class RabbitMqConstants {
    public static final String HOTEL_EXCHANGE = "hotel.topic";
    public static final String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    public static final String HOTEL_DELETE_QUEUE = "hotel.delete.queue";
    public static final String HOTEL_INSERT_ROUTING_KEY = "hotel.insert";
    public static final String HOTEL_DELETE_ROUTING_KEY = "hotel.delete";
}
