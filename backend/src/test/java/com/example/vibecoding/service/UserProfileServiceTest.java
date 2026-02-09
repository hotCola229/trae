package com.example.vibecoding.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class UserProfileServiceTest {
    private UserProfileService userProfileService;
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService();
        requestContext = new RequestContext();
    }

    @Test
    void testLoadDisplayNameSuccess() {
        String displayName = userProfileService.loadDisplayName("1001");
        assertEquals("Alice", displayName);
        // 验证上下文已被重置
        assertNull(requestContext.read("traceId"));
        assertNull(requestContext.read("userId"));
    }

    @Test
    void testLoadDisplayNameNotFound() {
        // 测试当用户不存在时，是否会抛出异常
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileService.loadDisplayName("9999");
        });
        assertEquals("user not found: 9999", exception.getMessage());
        // 验证异常情况下上下文是否也被重置
        assertNull(requestContext.read("traceId"));
        assertNull(requestContext.read("userId"));
    }

    @Test
    void testMemoryLeakInConcurrentEnvironment() throws InterruptedException {
        // 模拟线程池环境下的并发请求
        int threadCount = 10;
        int requestPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestPerThread; j++) {
                        try {
                            // 交替请求存在和不存在的用户
                            if (j % 2 == 0) {
                                userProfileService.loadDisplayName("1001");
                            } else {
                                userProfileService.loadDisplayName("9999");
                            }
                        } catch (IllegalArgumentException e) {
                            // 捕获预期的异常
                        }
                        // 验证上下文已被重置
                        assertNull(requestContext.read("traceId"));
                        assertNull(requestContext.read("userId"));
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 验证没有错误发生
        assertEquals(0, errorCount.get());
    }
}