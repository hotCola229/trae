package com.example.vibecoding;

import com.example.vibecoding.model.enums.ProjectStatusEnum;
import com.example.vibecoding.model.request.ProjectCreateRequest;
import com.example.vibecoding.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectService projectService;

    // 测试前清理表数据
    @BeforeEach
    void setUp() {
        projectService.remove(null);
    }

    /**
     * 测试创建项目成功
     */
    @Test
    void testCreateProjectSuccess() throws Exception {
        // 创建测试数据
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("测试项目1");
        request.setOwner("测试用户1");
        request.setStatus(ProjectStatusEnum.ACTIVE);

        // 执行请求
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.name").value("测试项目1"))
                .andReturn();
    }

    /**
     * 测试参数校验失败
     */
    @Test
    void testCreateProjectParamValidationFailed() throws Exception {
        // 创建测试数据（name为空）
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("");
        request.setOwner("测试用户1");
        request.setStatus(ProjectStatusEnum.ACTIVE);

        // 执行请求
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value(Matchers.containsString("项目名称不能为空")))
                .andReturn();
    }

    /**
     * 测试查询不存在的项目返回40401
     */
    @Test
    void testGetProjectNotFound() throws Exception {
        // 执行请求（查询不存在的ID）
        MvcResult result = mockMvc.perform(get("/api/projects/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").value("项目不存在"))
                .andReturn();
    }

    /**
     * 测试逻辑删除项目后查询返回40401
     */
    @Test
    void testDeleteProjectAndGetNotFound() throws Exception {
        // 先创建一个项目，获取返回的项目信息
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("测试项目2");
        request.setOwner("测试用户2");
        request.setStatus(ProjectStatusEnum.ACTIVE);
        
        // 通过API创建项目并获取ID
        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        // 解析返回的项目ID
        String responseContent = createResult.getResponse().getContentAsString();
        Long projectId = objectMapper.readTree(responseContent).path("data").path("id").asLong();

        // 删除项目
        mockMvc.perform(delete("/api/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 再次查询该项目，应该返回40401
        mockMvc.perform(get("/api/projects/" + projectId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.message").value("项目不存在"));
    }
}