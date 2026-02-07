package com.example.vibecoding.service.impl;

import com.example.vibecoding.mapper.ExternalCallLogMapper;
import com.example.vibecoding.model.entity.ExternalCallLog;
import com.example.vibecoding.service.DictService;
import com.example.vibecoding.util.ThirdPartySignatureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DictServiceImpl implements DictService {

    private static final Logger logger = LoggerFactory.getLogger(DictServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExternalCallLogMapper externalCallLogMapper;

    @Value("${third-party.dict.base-url}")
    private String baseUrl;

    @Value("${third-party.dict.app-key}")
    private String appKey;

    @Value("${third-party.dict.app-secret}")
    private String appSecret;

    private static final String SERVICE_NAME = "DICT_QUERY";
    private static final String API_PATH = "/api/v1/dataapi/execute/dict/query";

    @Override
    public Map<String, Object> queryDict(Long pageNum, Long pageSize, String dictType, String traceId) {
        // 参数验证已在Controller层完成
        
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("pageNum", pageNum);
        queryParams.put("pageSize", pageSize);
        queryParams.put("dictType", dictType);

        // 调用第三方API
        try {
            long startTime = System.currentTimeMillis();
            Map<String, Object> result = callThirdPartyApi(queryParams, traceId);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Dict query success, traceId: {}, duration: {}ms", traceId, duration);
            return result;
        } catch (Exception e) {
            logger.error("Dict query failed, traceId: {}", traceId, e);
            throw e;
        }
    }

    /**
     * 调用第三方API（带重试机制）
     */
    @Retryable(
            value = {HttpServerErrorException.class, RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 5000
            )
    )
    protected Map<String, Object> callThirdPartyApi(Map<String, Object> queryParams, String traceId) {
        int attempt = 1;
        if (RetrySynchronizationManager.getContext() != null) {
            attempt = RetrySynchronizationManager.getContext().getRetryCount() + 1;
        }
        long startTime = System.currentTimeMillis();
        String url = baseUrl + API_PATH;
        String queryString = buildQueryString(queryParams);
        
        logger.info("Calling third-party API, attempt: {}, url: {}, traceId: {}", attempt, url, traceId);

        try {
            // 生成请求头
            Map<String, String> headersMap = ThirdPartySignatureUtil.generateHeaders(
                    appKey, appSecret, HttpMethod.GET.name(), API_PATH, queryParams);
            
            HttpHeaders headers = new HttpHeaders();
            headersMap.forEach(headers::add);
            
            // 创建请求实体
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    url + "?" + queryString, 
                    HttpMethod.GET, 
                    entity, 
                    Map.class);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            logExternalCall(traceId, null, API_PATH, HttpMethod.GET.name(), 
                    queryString, response.getStatusCodeValue(), true, attempt, duration, 
                    null, null);
            
            return response.getBody();
        } catch (HttpServerErrorException e) {
            // 服务器错误，重试
            long duration = System.currentTimeMillis() - startTime;
            logExternalCall(traceId, null, API_PATH, HttpMethod.GET.name(), 
                    queryString, e.getRawStatusCode(), false, attempt, duration, 
                    e.getClass().getSimpleName(), e.getMessage());
            logger.warn("Third-party API server error, attempt: {}, status: {}, traceId: {}", 
                    attempt, e.getRawStatusCode(), traceId);
            throw e;
        } catch (HttpClientErrorException e) {
            // 客户端错误，不重试
            long duration = System.currentTimeMillis() - startTime;
            logExternalCall(traceId, null, API_PATH, HttpMethod.GET.name(), 
                    queryString, e.getRawStatusCode(), false, attempt, duration, 
                    e.getClass().getSimpleName(), e.getMessage());
            logger.error("Third-party API client error, status: {}, traceId: {}", 
                    e.getRawStatusCode(), traceId);
            throw new RuntimeException("Third-party API client error: " + e.getMessage());
        } catch (RestClientException e) {
            // 网络错误等，重试
            long duration = System.currentTimeMillis() - startTime;
            logExternalCall(traceId, null, API_PATH, HttpMethod.GET.name(), 
                    queryString, null, false, attempt, duration, 
                    e.getClass().getSimpleName(), e.getMessage());
            logger.warn("Third-party API connection error, attempt: {}, traceId: {}", 
                    attempt, traceId);
            throw e;
        } catch (Exception e) {
            // 其他错误，不重试
            long duration = System.currentTimeMillis() - startTime;
            logExternalCall(traceId, null, API_PATH, HttpMethod.GET.name(), 
                    queryString, null, false, attempt, duration, 
                    e.getClass().getSimpleName(), e.getMessage());
            logger.error("Third-party API call failed, traceId: {}", traceId, e);
            throw new RuntimeException("Third-party API call failed: " + e.getMessage());
        }
    }

    /**
     * 构建查询字符串
     */
    private String buildQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return StringUtils.EMPTY;
        }
        
        StringBuilder queryString = new StringBuilder();
        params.forEach((key, value) -> {
            if (value != null) {
                if (queryString.length() > 0) {
                    queryString.append("&");
                }
                queryString.append(key).append("=").append(value);
            }
        });
        
        return queryString.toString();
    }

    /**
     * 记录调用日志
     */
    private void logExternalCall(String traceId, String requestId, String targetUrl, String httpMethod,
                                String queryString, Integer httpStatus, boolean success, int attempt,
                                long durationMs, String exceptionType, String exceptionMessage) {
        try {
            ExternalCallLog log = new ExternalCallLog();
            log.setTraceId(traceId);
            log.setRequestId(requestId);
            log.setService("DICT_QUERY");
            log.setTargetUrl(baseUrl + targetUrl);
            log.setHttpMethod(httpMethod);
            log.setQueryString(queryString);
            log.setHttpStatus(httpStatus);
            log.setSuccess(success ? 1 : 0);
            log.setAttempt(attempt);
            log.setDurationMs(durationMs);
            log.setExceptionType(exceptionType);
            log.setExceptionMessage(exceptionMessage);
            log.setCreatedAt(new Date());
            
            externalCallLogMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不影响主流程
            logger.error("Failed to record external call log, traceId: {}", traceId, e);
        }
    }
}