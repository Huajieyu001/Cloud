package cn.itcast.hotel;

import cn.itcast.hotel.constants.MappingsUtils;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class HotelDocTest {

    private RestHighLevelClient client;

    @Autowired
    private IHotelService iHotelService;

    @BeforeEach
    private void setUp(){
        this.client = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.179.128:9200")));
    }

    @AfterEach
    private void clear() throws IOException {
        this.client.close();
    }

    @Test
    void testDocAdd() throws IOException {
        Hotel hotel = iHotelService.getById(56214L);
        HotelDoc hotelDoc = new HotelDoc(hotel);

        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());

        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        System.out.println("--------------------------------------------------");
        System.out.println(response);
    }

    @Test
    void testDocGet() throws IOException {
        GetRequest request = new GetRequest("hotel", "56214");

        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        String responseSourceAsString = response.getSourceAsString();

        HotelDoc hotelDoc = JSON.parseObject(responseSourceAsString, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    void testDocUpdate() throws IOException{
        Hotel hotel = iHotelService.getById(56214L);
        HotelDoc hotelDoc = new HotelDoc(hotel);

        hotelDoc.setName("XXXXX");
        hotelDoc.setCity("YYYYY");
        hotelDoc.setAddress("ZZZZZ");

        UpdateRequest request = new UpdateRequest("hotel", hotelDoc.getId().toString());

        request.doc(
                "city","XXXXX","age","99"
        );

        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDocDelete() throws IOException{
        DeleteRequest request  = new DeleteRequest("hotel", "56214");

        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDocBulkAdd() throws IOException {
        BulkRequest request = new BulkRequest();

        List<Hotel> list = iHotelService.list();
        List<HotelDoc> docList = new ArrayList<>();
        list.forEach(e -> {
            docList.add(new HotelDoc(e));
        });

        docList.forEach(e -> {
            request.add(new IndexRequest("hotel").id(e.getId().toString()).source(JSON.toJSONString(e), XContentType.JSON));
        });

        client.bulk(request, RequestOptions.DEFAULT);
    }
}
