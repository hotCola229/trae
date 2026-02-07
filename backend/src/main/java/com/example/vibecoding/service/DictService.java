package com.example.vibecoding.service;

import java.util.Map;

public interface DictService {
    /**
     * 查询字典数据
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param dictType 字典类型
     * @param traceId 请求追踪ID
     * @return 第三方API响应结果
     */
    Map<String, Object> queryDict(Long pageNum, Long pageSize, String dictType, String traceId);
}