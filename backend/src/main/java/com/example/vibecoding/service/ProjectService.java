package com.example.vibecoding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vibecoding.model.entity.Project;

public interface ProjectService extends IService<Project> {

    /**
     * 创建项目
     */
    Project createProject(Project project);

    /**
     * 查询单个项目
     */
    Project getProjectById(Long id);

    /**
     * 分页查询项目列表
     */
    IPage<Project> getProjectList(int page, int size, String keyword);

    /**
     * 更新项目
     */
    Project updateProject(Project project);

    /**
     * 删除项目（逻辑删除）
     */
    boolean deleteProject(Long id);
}