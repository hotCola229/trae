package com.example.vibecoding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.vibecoding.common.ApiResponse;
import com.example.vibecoding.model.request.ProjectCreateRequest;
import com.example.vibecoding.model.request.ProjectUpdateRequest;
import com.example.vibecoding.model.vo.ProjectVO;
import com.example.vibecoding.service.ProjectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 创建项目
     */
    @PostMapping
    public ApiResponse<ProjectVO> createProject(@Validated @RequestBody ProjectCreateRequest request) {
        ProjectVO createdProject = projectService.createProject(request);
        return ApiResponse.success(createdProject);
    }

    /**
     * 查询单个项目
     */
    @GetMapping("/{id}")
    public ApiResponse<ProjectVO> getProjectById(@PathVariable Long id) {
        ProjectVO project = projectService.getProjectById(id);
        return ApiResponse.success(project);
    }

    /**
     * 分页查询项目列表
     */
    @GetMapping
    public ApiResponse<IPage<ProjectVO>> getProjectList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        IPage<ProjectVO> projectPage = projectService.getProjectList(page, size, keyword);
        return ApiResponse.success(projectPage);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ApiResponse<ProjectVO> updateProject(
            @PathVariable Long id,
            @Validated @RequestBody ProjectUpdateRequest request) {
        // 确保请求中的 id 与路径参数一致
        request.setId(id);
        
        ProjectVO updatedProject = projectService.updateProject(request);
        return ApiResponse.success(updatedProject);
    }

    /**
     * 删除项目（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ApiResponse.success();
    }
}