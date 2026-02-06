package com.example.vibecoding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vibecoding.common.BusinessException;
import com.example.vibecoding.common.ErrorCode;
import com.example.vibecoding.mapper.ProjectMapper;
import com.example.vibecoding.model.entity.Project;
import com.example.vibecoding.service.ProjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public Project createProject(Project project) {
        // 保存项目
        boolean saved = this.save(project);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "创建项目失败");
        }
        return project;
    }

    @Override
    public Project getProjectById(Long id) {
        Project project = this.getById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    @Override
    public IPage<Project> getProjectList(int page, int size, String keyword) {
        // 创建分页对象
        Page<Project> projectPage = new Page<>(page, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Project::getName, keyword)
                       .or()
                       .like(Project::getOwner, keyword);
        }
        
        // 执行分页查询
        return this.page(projectPage, queryWrapper);
    }

    @Override
    public Project updateProject(Project project) {
        // 检查项目是否存在
        Project existingProject = this.getById(project.getId());
        if (existingProject == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 更新项目
        boolean updated = this.updateById(project);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "更新项目失败");
        }
        
        return this.getById(project.getId());
    }

    @Override
    public boolean deleteProject(Long id) {
        // 检查项目是否存在
        Project existingProject = this.getById(id);
        if (existingProject == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 逻辑删除项目
        return this.removeById(id);
    }
}