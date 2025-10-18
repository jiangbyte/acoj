package io.charlie.web.oj.modular.sys.log.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.oj.modular.sys.log.entity.SysLog;
import io.charlie.web.oj.modular.sys.log.param.SysLogAddParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogEditParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogIdParam;
import io.charlie.web.oj.modular.sys.log.param.SysLogPageParam;
import io.charlie.web.oj.modular.sys.log.mapper.SysLogMapper;
import io.charlie.web.oj.modular.sys.log.service.SysLogService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.sys.menu.entity.SysMenu;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 系统活动/日志记录表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
    private final TransService transService;

    @Override
    public Page<SysLog> page(SysLogPageParam sysLogPageParam) {
        QueryWrapper<SysLog> queryWrapper = new QueryWrapper<SysLog>().checkSqlInjection();
        // 默认倒序
        queryWrapper.lambda().orderByDesc(SysLog::getCreateTime);
        if (ObjectUtil.isNotEmpty(sysLogPageParam.getKeyword())) {
            queryWrapper.lambda().like(SysLog::getOperation, sysLogPageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysLogPageParam.getSortField(), sysLogPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysLogPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysLogPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysLogPageParam.getSortField()));
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysLogPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysLogPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysLogAddParam sysLogAddParam) {
        SysLog bean = BeanUtil.toBean(sysLogAddParam, SysLog.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysLogEditParam sysLogEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysLog>().eq(SysLog::getId, sysLogEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysLog bean = BeanUtil.toBean(sysLogEditParam, SysLog.class);
        BeanUtil.copyProperties(sysLogEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysLogIdParam> sysLogIdParamList) {
        if (ObjectUtil.isEmpty(sysLogIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysLogIdParamList, SysLogIdParam::getId));
    }

    @Override
    public SysLog detail(SysLogIdParam sysLogIdParam) {
        SysLog sysLog = this.getById(sysLogIdParam.getId());
        if (ObjectUtil.isEmpty(sysLog)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysLog;
    }

    @Override
    public List<SysLog> recent(int count) {
        List<SysLog> list = this.list(new QueryWrapper<SysLog>().lambda().orderByDesc(SysLog::getCreateTime).last("limit " + count));
        transService.transBatch(list);
        return list;
    }

}