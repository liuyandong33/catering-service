package build.dream.catering.controllers;

import build.dream.catering.constants.Constants;
import build.dream.catering.services.DemoService;
import build.dream.common.utils.ApplicationHandler;
import build.dream.common.utils.MethodCaller;
import build.dream.common.utils.ValidateUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

@Controller
@RequestMapping(value = "/demo")
public class DemoController {
    @Autowired
    private DemoService demoService;
    @Autowired
    private TransportClient transportClient;

    @RequestMapping(value = "/writeSaleFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String writeSaleFlow() {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        MethodCaller methodCaller = () -> {
            String dietOrderId = requestParameters.get("dietOrderId");
            ApplicationHandler.notNull(dietOrderId, "dietOrderId");
            return demoService.writeSaleFlow(BigInteger.valueOf(Long.valueOf(dietOrderId)));
        };
        return ApplicationHandler.callMethod(methodCaller, "写入流水失败", requestParameters);
    }

    @RequestMapping(value = "/createIndex")
    @ResponseBody
    public String createIndex() throws IOException {
        Map<String, String> requestParameters = ApplicationHandler.getRequestParameters();
        String index = requestParameters.get("index");
        ApplicationHandler.notBlank(index, "index");

        AdminClient adminClient = transportClient.admin();
        IndicesAdminClient indicesAdminClient = adminClient.indices();

        boolean isExists = indicesAdminClient.prepareExists(index).execute().actionGet().isExists();
        if (isExists) {

        } else {
            CreateIndexRequestBuilder createIndexRequestBuilder = indicesAdminClient.prepareCreate(index);

            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("id").field("type", "integer").field("index", "not_analyzed").endObject()
                    .startObject("name").field("type", "string").field("index", "not_analyzed").endObject()
                    .endObject();
            createIndexRequestBuilder.addMapping("tenant", builder);

            CreateIndexResponse createIndexResponse = createIndexRequestBuilder.execute().actionGet();
            ValidateUtils.isTrue(createIndexResponse.isAcknowledged(), "创建索引失败");
        }

        return Constants.SUCCESS;
    }
}
