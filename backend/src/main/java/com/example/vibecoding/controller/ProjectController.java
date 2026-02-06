package com.example.vibecoding.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.vibecoding.common.ApiResponse;
import com.example.vibecoding.model.entity.Project;
import com.example.vibecoding.model.request.ProjectCreateRequest;
import com.example.vibecoding.model.request.ProjectUpdateRequest;
import com.example.vibecoding.service.ProjectService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 创建项目
     */
    @PostMapping
    public ApiResponse<Project> createProject(@Validated @RequestBody ProjectCreateRequest request) {
        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        Project createdProject = projectService.createProject(project);
        return ApiResponse.success(createdProject);
    }

    /**
     * 查询单个项目
     */
    @GetMapping("/{id}")
    public ApiResponse<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ApiResponse.success(project);
    }

    /**
     * 分页查询项目列表
     */
    @GetMapping
    public ApiResponse<IPage<Project>> getProjectList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 1;
        } else if (size > 100) {
            size = 100;
        }
        
        IPage<Project> projectPage = projectService.getProjectList(page, size, keyword);
        return ApiResponse.success(projectPage);
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ApiResponse<Project> updateProject(
            @PathVariable Long id,
            @Validated @RequestBody ProjectUpdateRequest request) {
        // 确保请求中的 id 与路径参数一致
        request.setId(id);
        
        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        Project updatedProject = projectService.updateProject(project);
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