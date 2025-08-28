package io.charlie.app.core.modular.sys.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.app.core.modular.problem.ranking.param.UserActivityRank;
import io.charlie.app.core.modular.problem.ranking.service.ProblemUserRankingService;
import io.charlie.app.core.modular.problem.solved.entity.ProSolved;
import io.charlie.app.core.modular.problem.solved.mapper.ProSolvedMapper;
import io.charlie.app.core.modular.set.solved.entity.ProSetSolved;
import io.charlie.app.core.modular.set.solved.mapper.ProSetSolvedMapper;
import io.charlie.app.core.modular.sys.group.entity.SysGroup;
import io.charlie.app.core.modular.sys.user.entity.SysUser;
import io.charlie.app.core.modular.sys.user.param.*;
import io.charlie.app.core.modular.sys.user.mapper.SysUserMapper;
import io.charlie.app.core.modular.sys.user.service.SysUserService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import org.dromara.core.trans.anno.RpcTrans;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-06-23
 * @description 用户表 服务实现类
 */
@Slf4j
@Service
@RpcTrans
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private final ProSolvedMapper proSolvedMapper;
    private final ProSetSolvedMapper proSetSolvedMapper;
    private final RedisTemplate<String, Object> redisTemplate;

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
        // 加密密码
        bean.setPassword(BCrypt.hashpw(sysUserAddParam.getPassword()));
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
        // 获得活跃指数
        Double score = redisTemplate.opsForZSet().score("user:activity:global:rank", sysUser.getId());
        sysUser.setActiveScore(String.valueOf(score));
        // 获得解决题目数
        Long l = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getUserId, sysUser.getId()));
        sysUser.setSolvedProblem(String.valueOf(l));
        // 获得参与题集数
        Long l1 = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getUserId, sysUser.getId()));
        sysUser.setParticipatedSet(String.valueOf(l1));
        return sysUser;
    }

    @Override
    public List<LabelOption<String>> options(SysUserOptionParam sysUserOptionParam) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().checkSqlInjection();
        // 关键字
        if (ObjectUtil.isNotEmpty(sysUserOptionParam.getKeyword())) {
            queryWrapper.lambda().like(SysUser::getNickname, sysUserOptionParam.getKeyword());
            queryWrapper.lambda().or().like(SysUser::getUsername, sysUserOptionParam.getKeyword());
        }
        return this.list(queryWrapper).stream().map(sysUser -> new LabelOption<>(sysUser.getId(), sysUser.getNickname())).toList();
    }

    @Override
    public SysUser appDetail(SysUserIdParam sysUserIdParam) {
        SysUser sysUser = this.getById(sysUserIdParam.getId());
        if (ObjectUtil.isEmpty(sysUser)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        sysUser.setPassword(null);
        sysUser.setTelephone(null);
        // 获得活跃指数
        Double score = redisTemplate.opsForZSet().score("user:activity:global:rank", sysUser.getId());
        sysUser.setActiveScore(String.valueOf(score));
        // 获得解决题目数
        Long l = proSolvedMapper.selectCount(new LambdaQueryWrapper<ProSolved>().eq(ProSolved::getUserId, sysUser.getId()).eq(ProSolved::getSolved, true));
        sysUser.setSolvedProblem(String.valueOf(l));
        // 获得参与题集数
        Long l1 = proSetSolvedMapper.selectCount(new LambdaQueryWrapper<ProSetSolved>().eq(ProSetSolved::getUserId, sysUser.getId()));
        sysUser.setParticipatedSet(String.valueOf(l1));
        return sysUser;
    }

    @Override
    public void updateApp(SysUserUpdateAppParam sysUserUpdateAppParam) {
        if (!this.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, sysUserUpdateAppParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        SysUser bean = BeanUtil.toBean(sysUserUpdateAppParam, SysUser.class);
        BeanUtil.copyProperties(sysUserUpdateAppParam, bean);
        this.updateById(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateAvatar(SysUserUpdateImageParam sysUserUpdateImageParam) {
        if (!this.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, sysUserUpdateImageParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, sysUserUpdateImageParam.getId())
                .set(SysUser::getAvatar, sysUserUpdateImageParam.getImg())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateBackground(SysUserUpdateImageParam sysUserUpdateImageParam) {
        if (!this.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, sysUserUpdateImageParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, sysUserUpdateImageParam.getId())
                .set(SysUser::getBackground, sysUserUpdateImageParam.getImg())
        );
    }

    @Override
    public Boolean updatePassword(SysUserPasswordUpdateParam sysUserPasswordUpdateParam) {
        // 新密码和确认密码是否一致
        if (!sysUserPasswordUpdateParam.getNewPassword().equals(sysUserPasswordUpdateParam.getConfirmPassword())) {
            throw new BusinessException("新密码不一致");
        }
        SysUser sysUser = this.getById(sysUserPasswordUpdateParam.getId());
        if (!BCrypt.checkpw(sysUserPasswordUpdateParam.getOldPassword(), sysUser.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        sysUser.setPassword(BCrypt.hashpw(sysUserPasswordUpdateParam.getNewPassword()));
        return this.updateById(sysUser);
    }

}