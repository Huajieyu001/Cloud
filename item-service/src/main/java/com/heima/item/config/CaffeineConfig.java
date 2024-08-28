package com.heima.item.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.heima.item.pojo.Item;
import com.heima.item.pojo.ItemStock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<Long, Item> getItemCache(){
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10000)
                .build();
    }

    @Bean
    public Cache<Long, ItemStock> getItemStockCache(){
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10000)
                .build();
    }
}