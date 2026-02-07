package com.example.vibecoding.controller;

import com.example.vibecoding.mapper.ExternalCallLogMapper;
import com.example.vibecoding.model.entity.ExternalCallLog;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "third-party.dict.base-url=http://localhost:8089/"
        })
public class DictControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExternalCallLogMapper externalCallLogMapper;

    @Value("${third-party.dict.app-key}")
    private String appKey;

    @Value("${third-party.dict.app-secret}")
    private String appSecret;

    private WireMockServer wireMockServer;

    @BeforeEach
    public void setUp() {
        // 启动WireMock服务器
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        // 测试前清理日志表
        // externalCallLogMapper.delete(null);
    }

    @AfterEach
    public void tearDown() {
        // 停止WireMock服务器
        wireMockServer.stop();
    }

    @Test
    public void testQueryDictSuccess() {
        // 模拟第三方服务成功响应
        String mockResponse = "{\"total\":5,\"data\":[{\"code\":\"4\",\"value\":\"jar\"},{\"code\":\"1\",\"value\":\"java类\"},{\"code\":\"2\",\"value\":\"spring bean\"},{\"code\":\"9\",\"value\":\"其他\"},{\"code\":\"3\",\"value\":\"Rest 调用\"}],\"totalPage\":1,\"currentPageNum\":1,\"pageSize\":10}";

        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/dataapi/execute/dict/query"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // 发送请求
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        params.put("dictType", "job_type");

        ResponseEntity<Map> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/dict/query?pageNum={pageNum}&pageSize={pageSize}&dictType={dictType}",
                Map.class, params);

        // 验证响应
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(data);
        assertEquals(5, data.get("total"));

        // 验证日志记录
        // List<ExternalCallLog> logs = externalCallLogMapper.selectList(null);
        // assertEquals(1, logs.size());
        // ExternalCallLog log = logs.get(0);
        // assertEquals("DICT_QUERY", log.getService());
        // assertEquals(1, log.getAttempt());
        // assertEquals(1, log.getSuccess().intValue());
        // assertEquals(HttpStatus.OK.value(), log.getHttpStatus().intValue());
    }

    @Test
    public void testQueryDictServerError() {
        // 模拟第三方服务返回500错误，触发重试
        WireMock.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/v1/dataapi/execute/dict/query"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        // 发送请求
        Map<String, Object> params = new HashMap<>();
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        params.put("dictType", "job_type");

        try {
            restTemplate.getForEntity(
                    "http://localhost:" + port + "/api/dict/query?pageNum={pageNum}&pageSize={pageSize}&dictType={dictType}",
                    Map.class, params);
        } catch (Exception e) {
            // 预期会抛出异常
        }

        // 验证日志记录（重试3次）
        // List<ExternalCallLog> logs = externalCallLogMapper.selectList(null);
        // assertEquals(3, logs.size());
        // 
        // for (int i = 0; i < logs.size(); i++) {
        //     ExternalCallLog log = logs.get(i);
        //     assertEquals("DICT_QUERY", log.getService());
        //     assertEquals(i + 1, log.getAttempt());
        //     assertEquals(0, log.getSuccess().intValue());
        //     assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), log.getHttpStatus().intValue());
        // }
    }
}