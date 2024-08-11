package cn.itcast.hotel.beans;

import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RestUtils {
    public static List<HotelDoc> parseHotelDoc(SearchResponse response) {
        if (response == null) {
            throw new RuntimeException("response is null");
        }

        SearchHits hits = response.getHits();
        List<HotelDoc> docList = new ArrayList<>();
        if (hits != null) {
            hits.forEach(e -> {
                HotelDoc hotelDoc = JSON.parseObject(e.getSourceAsString(), HotelDoc.class);
                docList.add(hotelDoc);
            });
        }
        return docList;
    }

    public static PageResult handleResponse(SearchResponse response, boolean sortByLocation) {
        if (response == null) {
            throw new RuntimeException("response is null");
        }

        PageResult result = new PageResult();

        SearchHits hits = response.getHits();
        result.setTotal(hits.getTotalHits().value);
        List<HotelDoc> docList = new ArrayList<>();
        if (hits != null) {
            hits.forEach(e -> {
                HotelDoc hotelDoc = JSON.parseObject(e.getSourceAsString(), HotelDoc.class);

                if (sortByLocation) {
                    Object[] sortValues = e.getSortValues();
                    if (sortValues != null) {
                        System.out.println("sortValues[0] = " + sortValues[0]);
                        hotelDoc.setDistance(sortValues[0]);
                    }
                }
                docList.add(hotelDoc);
            });
        }
        result.setHotels(docList);
        return result;
    }

    public static PageResult handleResponse(SearchResponse response) {
        return handleResponse(response, false);
    }

    public static List<String> handleResponseAggs(SearchResponse response, String aggsName) {
        if (response == null) {
            throw new RuntimeException("response is null!");
        }

        Aggregations aggregations = response.getAggregations();
        Terms terms = aggregations.get(aggsName);

        List<String> list = new ArrayList<>();
        terms.getBuckets().forEach(e -> {
            String key = e.getKeyAsString();
            list.add(key);
        });

        return list;
    }
}
