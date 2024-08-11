package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.beans.RestUtils;
import cn.itcast.hotel.config.HotelConfig;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.lucene.search.SortField;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {
        SearchRequest request = new SearchRequest(HotelConfig.INDEX_NAME);

        SearchResponse response = basicQuery(params, request);

        return RestUtils.handleResponse(response, !StringUtils.isEmpty(params.getLocation()));
    }

    private SearchResponse basicQuery(RequestParams params, SearchRequest request) {
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
        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
