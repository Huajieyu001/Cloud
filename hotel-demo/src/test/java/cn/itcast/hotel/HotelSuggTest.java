package cn.itcast.hotel;

import cn.itcast.hotel.beans.RestUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class HotelSuggTest {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void testSugg() throws IOException {
        SearchRequest request = new SearchRequest("hotel");

        request.source().suggest(new SuggestBuilder().addSuggestion("mySugg", SuggestBuilders.completionSuggestion("suggestion")
                        .prefix("s")
                        .skipDuplicates(true)
                        .size(20)));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        Suggest suggest = response.getSuggest();
        CompletionSuggestion mySugg = suggest.getSuggestion("mySugg");

        mySugg.getOptions().forEach(e -> {
            System.out.println(e.getText().string());
        });
    }
}
