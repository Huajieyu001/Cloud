package cn.itcast.hotel;

import cn.itcast.hotel.beans.RestUtils;
import cn.itcast.hotel.service.IHotelService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class HotelAggsTest {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private IHotelService service;

    @Test
    void testAggs() throws IOException {
        SearchRequest request = new SearchRequest("hotel");

        request.source().size(0);
        TermsAggregationBuilder terms = AggregationBuilders.terms("brandAggs").field("brand").size(20);
        request.source().aggregation(terms);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        RestUtils.handleResponseAggs(response, "brandAggs");
        System.out.println(response);
    }

    @Test
    void testFilters(){
        Map<String, List<String>> map = service.filters(null);
        map.forEach((k, v) -> {
            System.out.println("Key:[" + k + "], Value:" + v);
        });
    }
}
