package com.heima.item.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.item.pojo.Item;
import com.heima.item.pojo.ItemStock;
import com.heima.item.service.IItemService;
import com.heima.item.service.IItemStockService;
import com.heima.item.service.impl.ItemService;
import com.heima.item.service.impl.ItemStockService;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisHandler implements InitializingBean {

    @Autowired
    private StringRedisTemplate template;
    @Autowired
    private IItemService itemService;
    @Autowired
    private IItemStockService stockService;

    private static ObjectMapper TO_JSON_MAPPER = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<Item> itemList = itemService.list();

        itemList.forEach(e -> {
            try {
                String string = TO_JSON_MAPPER.writeValueAsString(e);
                template.opsForValue().set("item:id:" + e.getId().toString(), string);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });

        List<ItemStock> stockList = stockService.list();

        stockList.forEach(e -> {
            String string = null;
            try {
                string = TO_JSON_MAPPER.writeValueAsString(e);
                template.opsForValue().set("item:stock:id:" + e.getId().toString(), string);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    public void saveItem(Item item){
        String string = null;
        try {
            string = TO_JSON_MAPPER.writeValueAsString(item);
            template.opsForValue().set("item:stock:id:" + item.getId().toString(), string);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void delItemById(Long id){
        template.delete(id.toString());
    }
}
