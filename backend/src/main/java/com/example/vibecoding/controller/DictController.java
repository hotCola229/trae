package com.example.vibecoding.controller;

import com.example.vibecoding.common.ApiResponse;
import com.example.vibecoding.service.DictService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;

@RestController
@RequestMapping("/api/dict")
@Validated
public class DictController {

    private static final Logger logger = LoggerFactory.getLogger(DictController.class);

    @Autowired
    private DictService dictService;

    @GetMapping("/query")
    public ApiResponse<Map<String, Object>> queryDict(
            @RequestParam Long pageNum,
            @RequestParam Long pageSize,
            @RequestParam String dictType,
            HttpServletRequest request) {
        
        // 参数验证
        Assert.notNull(pageNum, "pageNum cannot be null");
        Assert.isTrue(pageNum >= 1, "pageNum must be greater than or equal to 1");
        
        Assert.notNull(pageSize, "pageSize cannot be null");
        Assert.isTrue(pageSize >= 1 && pageSize <= 100, "pageSize must be between 1 and 100");
        
        Assert.hasText(dictType, "dictType cannot be empty");
        Assert.isTrue(dictType.length() <= 50, "dictType length must be less than or equal to 50");
        
        // 获取或生成traceId
        String traceId = request.getHeader("X-Trace-Id");
        if (StringUtils.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        
        // 将traceId放入MDC
        MDC.put("traceId", traceId);
        
        try {
            logger.info("Dict query request received, pageNum: {}, pageSize: {}, dictType: {}, traceId: {}", 
                    pageNum, pageSize, dictType, traceId);
            
            Map<String, Object> result = dictService.queryDict(pageNum, pageSize, dictType, traceId);
            return ApiResponse.success(result);
        } finally {
            // 清除MDC中的traceId
            MDC.remove("traceId");
        }
    }
}