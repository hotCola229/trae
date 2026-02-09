package com.example.vibecoding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vibecoding.common.BusinessException;
import com.example.vibecoding.common.ErrorCode;
import com.example.vibecoding.mapper.ProjectMapper;
import com.example.vibecoding.model.entity.Project;
import com.example.vibecoding.model.request.ProjectCreateRequest;
import com.example.vibecoding.model.request.ProjectUpdateRequest;
import com.example.vibecoding.model.vo.ProjectVO;
import com.example.vibecoding.service.ProjectService;
import com.example.vibecoding.util.BeanCopyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public ProjectVO createProject(ProjectCreateRequest request) {
        // 将Request转换为Entity
        Project project = BeanCopyUtil.copy(request, Project.class);
        
        // 保存项目
        boolean saved = this.save(project);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "创建项目失败");
        }
        
        // 将Entity转换为VO并返回
        return BeanCopyUtil.copy(project, ProjectVO.class);
    }

    @Override
    public ProjectVO getProjectById(Long id) {
        // 查询项目
        Project project = this.getById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 将Entity转换为VO并返回
        return BeanCopyUtil.copy(project, ProjectVO.class);
    }

    @Override
    public IPage<ProjectVO> getProjectList(int page, int size, String keyword) {
        // 验证参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 1;
        } else if (size > 100) {
            size = 100;
        }
        
        // 创建分页对象
        Page<Project> projectPage = new Page<>(page, size);
        projectPage.addOrder(OrderItem.desc("created_at"));
        
        // 构建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Project::getName, keyword)
                       .or()
                       .like(Project::getOwner, keyword);
        }
        
        // 执行分页查询
        IPage<Project> resultPage = this.page(projectPage, queryWrapper);
        
        // 将Entity Page转换为VO Page并返回
        return resultPage.convert(project -> BeanCopyUtil.copy(project, ProjectVO.class));
    }

    @Override
    public ProjectVO updateProject(ProjectUpdateRequest request) {
        // 检查项目是否存在
        Project existingProject = this.getById(request.getId());
        if (existingProject == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        
        // 将Request转换为Entity
        Project project = BeanCopyUtil.copy(request, Project.class);
        
        // 更新项目
        boolean updated = this.updateById(project);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "更新项目失败");
        }
        
        // 查询更新后的项目
        Project updatedProject = this.getById(request.getId());
        
        // 将Entity转换为VO并返回
        return BeanCopyUtil.copy(updatedProject, ProjectVO.class);
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