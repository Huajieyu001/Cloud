package cn.itcast.hotel;

import cn.itcast.hotel.constants.HotelConstants;
import cn.itcast.hotel.constants.MappingsUtils;
import cn.itcast.hotel.mapper.HotelMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class HotelIndexTest {

    private RestHighLevelClient client;

    @Autowired
    private MappingsUtils mappings;

    @BeforeEach
    private void setUp(){
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.179.128:9200")));
    }

    @AfterEach
    private void clear() throws IOException {
        this.client.close();
    }

    @Test
    public void test1(){
        System.out.println(client);
    }

    @Test
    void create() throws IOException {
        // create request
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("hotel");

        // set up JSON
        createIndexRequest.source(mappings.getHotelJson(), XContentType.JSON);

        // send to ES
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
    }

    @Test
    void delete() throws IOException{
        // create request
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("hotel");

        // send to ES
        client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
    }

    @Test
    void exist() throws IOException {
        // create request
        GetIndexRequest getIndexRequest = new GetIndexRequest("hotel");

        // send to ES
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

        // print
        System.out.println(exists);
    }

    @Test
    void test() throws Exception{
        System.out.println(mappings.getHotelJson());
    }
}
