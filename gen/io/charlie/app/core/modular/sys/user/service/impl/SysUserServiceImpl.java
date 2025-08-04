package io.charlie.problem.service.base.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.param.SysUserAddParam;
import io.charlie.app.core.modular.sys.user.param.SysUserEditParam;
import io.charlie.app.core.modular.sys.user.param.SysUserIdParam;
import io.charlie.app.core.modular.sys.user.param.SysUserPageParam;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import io.charlie.app.core.modular.sys.user.service.SysUserService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-06-23
* @description 用户表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public Page<SysUser> page(SysUserPageParam sysUserPageParam) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().checkSqlInjection();
        if (ObjectUtil.isAllNotEmpty(sysUserPageParam.getSortField(), sysUserPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysUserPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysUserPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysUserPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysUserPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysUserPageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysUserAddParam sysUserAddParam) {
        SysUser bean = BeanUtil.toBean(sysUserAddParam, SysUser.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysUserEditParam sysUserEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, sysUserEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysUser bean = BeanUtil.toBean(sysUserEditParam, SysUser.class);
        BeanUtil.copyProperties(sysUserEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysUserIdParam> sysUserIdParamList) {
        if (ObjectUtil.isEmpty(sysUserIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysUserIdParamList, SysUserIdParam::getId));
    }

    @Override
    public SysUser detail(SysUserIdParam sysUserIdParam) {
        SysUser sysUser = this.getById(sysUserIdParam.getId());
        if (ObjectUtil.isEmpty(sysUser)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysUser;
    }

}