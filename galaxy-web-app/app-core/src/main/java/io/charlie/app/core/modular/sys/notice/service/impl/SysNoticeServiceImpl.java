package io.charlie.app.core.modular.sys.notice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.sys.notice.entity.SysNotice;
import io.charlie.app.core.modular.sys.notice.param.SysNoticeAddParam;
import io.charlie.app.core.modular.sys.notice.param.SysNoticeEditParam;
import io.charlie.app.core.modular.sys.notice.param.SysNoticeIdParam;
import io.charlie.app.core.modular.sys.notice.param.SysNoticePageParam;
import io.charlie.app.core.modular.sys.notice.mapper.SysNoticeMapper;
import io.charlie.app.core.modular.sys.notice.service.SysNoticeService;
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
* @description 公告表 服务实现类
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {

    @Override
    public Page<SysNotice> page(SysNoticePageParam sysNoticePageParam) {
        QueryWrapper<SysNotice> queryWrapper = new QueryWrapper<SysNotice>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysNoticePageParam.getKeyword())) {
            queryWrapper.lambda().like(SysNotice::getTitle, sysNoticePageParam.getKeyword());
        }
        if (ObjectUtil.isAllNotEmpty(sysNoticePageParam.getSortField(), sysNoticePageParam.getSortOrder()) && ISortOrderEnum.isValid(sysNoticePageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysNoticePageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysNoticePageParam.getSortField()));
        } else {
            queryWrapper.lambda().orderByAsc(SysNotice::getSort);
        }

        return this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysNoticePageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysNoticePageParam.getSize()).orElse(20),
                null
                ),
                queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysNoticeAddParam sysNoticeAddParam) {
        SysNotice bean = BeanUtil.toBean(sysNoticeAddParam, SysNotice.class);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysNoticeEditParam sysNoticeEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysNotice>().eq(SysNotice::getId, sysNoticeEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysNotice bean = BeanUtil.toBean(sysNoticeEditParam, SysNotice.class);
        BeanUtil.copyProperties(sysNoticeEditParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<SysNoticeIdParam> sysNoticeIdParamList) {
        if (ObjectUtil.isEmpty(sysNoticeIdParamList)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        this.removeByIds(CollStreamUtil.toList(sysNoticeIdParamList, SysNoticeIdParam::getId));
    }

    @Override
    public SysNotice detail(SysNoticeIdParam sysNoticeIdParam) {
        SysNotice sysNotice = this.getById(sysNoticeIdParam.getId());
        if (ObjectUtil.isEmpty(sysNotice)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return sysNotice;
    }

    @Override
    public List<SysNotice> latestN(int n) {
        return this.list(new QueryWrapper<SysNotice>().checkSqlInjection().lambda().orderByDesc(SysNotice::getCreateTime).last("LIMIT " + n));
    }

}