package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.beans.RestUtils;
import cn.itcast.hotel.config.HotelConfig;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.lucene.search.SortField;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {
        SearchRequest request = new SearchRequest(HotelConfig.INDEX_NAME);

        basicQuery(params, request);

        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return RestUtils.handleResponse(response, !StringUtils.isEmpty(params.getLocation()));
    }

    private void basicQuery(RequestParams params, SearchRequest request) {
        BoolQueryBuilder builder = new BoolQueryBuilder();
        if (params.getKey() == null || params.getKey().isEmpty()) {
            builder.must(QueryBuilders.matchAllQuery());
        } else {
            builder.must(QueryBuilders.matchQuery("all", params.getKey()));
        }

        if (!StringUtils.isEmpty(params.getBrand())) {
            System.out.println("-------------brand------------" + params.getBrand() + "----------------");
            builder.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        if (!StringUtils.isEmpty(params.getStarName())) {
            System.out.println("-------------starName------------" + params.getStarName() + "----------------");
            builder.filter(QueryBuilders.termQuery("starName", params.getStarName()));
        }
        if (!StringUtils.isEmpty(params.getCity())) {
            System.out.println("-------------city------------" + params.getCity() + "----------------");
            builder.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        if (params.getMinPrice() != null) {
            System.out.println("-------------minPrice------------" + params.getMinPrice() + "----------------");
            builder.filter(QueryBuilders.rangeQuery("price").gte(params.getMinPrice()));
        }
        if (params.getMaxPrice() != null) {
            System.out.println("-------------maxPrice------------" + params.getMaxPrice() + "----------------");
            builder.filter(QueryBuilders.rangeQuery("price").lte(params.getMaxPrice()));
        }
        if (!StringUtils.isEmpty(params.getLocation())) {
            System.out.println("-------------location------------" + params.getLocation() + "----------------");
            request.source().sort(
                    SortBuilders.geoDistanceSort(
                            "location", new GeoPoint(params.getLocation())).order(SortOrder.ASC).unit(DistanceUnit.KILOMETERS)
            );
        }

        // 提高AD的权重
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                builder,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true),
                                ScoreFunctionBuilders.weightFactorFunction(10))
                });
        request.source().query(functionScoreQueryBuilder);

        request.source()
                .from((params.getPage() - 1) * params.getSize())
                .size(params.getSize());
    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        Map<String, List<String>> map = new HashMap<>();

        String[] names = {"brand", "city", "starName"};

        List<String> nameList = Arrays.asList(names);

        SearchRequest request = new SearchRequest("hotel");

        basicQuery(params, request);

        // exec
        nameList.forEach(e -> {
            TermsAggregationBuilder builder = AggregationBuilders.terms(e + "Aggs").field(e).size(100);
            request.source().aggregation(builder);
        });

        // analyze
        nameList.forEach(e -> {
            try {
                SearchResponse response = client.search(request, RequestOptions.DEFAULT);
                List<String> list = RestUtils.handleResponseAggs(response, e + "Aggs");
//                if("brand".equals(e)){
//                    map.put("品牌", list);
//                }
//                else if("city".equals(e)){
//                    map.put("城市", list);
//                }
//                else if("starName".equals(e)){
//                    map.put("星级", list);
//                }
                map.put(e, list);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return map;
    }

    @Override
    public List<String> getSuggestion(String key) {
        SearchRequest request = new SearchRequest("hotel");

        request.source().suggest(
                new SuggestBuilder().addSuggestion(
                        "searchSuggetion",
                        SuggestBuilders.completionSuggestion("suggestion")
                                .prefix(key)
                                .skipDuplicates(true)
                                .size(10)
                )
        );

        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return RestUtils.handleResponseSugg(response, "searchSuggetion");
    }

    public void insertById(Long id){
        IndexRequest request = new IndexRequest("hotel").id(id.toString());
        Hotel hotel = getById(id);
        HotelDoc hotelDoc = new HotelDoc(hotel);
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteById(Long id){
        DeleteRequest request = new DeleteRequest("hotel", "" + id);
        try {
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
