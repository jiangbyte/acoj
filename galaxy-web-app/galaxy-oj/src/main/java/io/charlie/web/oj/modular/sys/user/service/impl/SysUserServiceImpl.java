package io.charlie.web.oj.modular.sys.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.web.oj.modular.data.ranking.service.UserCacheService;
import io.charlie.web.oj.modular.data.solved.entity.DataSolved;
import io.charlie.web.oj.modular.data.solved.mapper.DataSolvedMapper;
import io.charlie.web.oj.modular.data.submit.entity.DataSubmit;
import io.charlie.web.oj.modular.data.submit.mapper.DataSubmitMapper;
import io.charlie.web.oj.modular.sys.auth.utils.UserValidationUtil;
import io.charlie.web.oj.modular.sys.notice.entity.SysNotice;
import io.charlie.web.oj.modular.sys.relation.entity.SysUserRole;
import io.charlie.web.oj.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.oj.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.oj.modular.sys.user.constant.DefaultUserData;
import io.charlie.web.oj.modular.sys.user.entity.ACRecord;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.param.*;
import io.charlie.web.oj.modular.sys.user.mapper.SysUserMapper;
import io.charlie.web.oj.modular.sys.user.service.SysUserService;
import io.charlie.galaxy.enums.ISortOrderEnum;
import io.charlie.galaxy.exception.BusinessException;
import io.charlie.galaxy.pojo.CommonPageRequest;
import io.charlie.galaxy.result.ResultCode;
import io.charlie.web.oj.modular.task.judge.enums.JudgeStatus;
import org.dromara.trans.service.impl.TransService;
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
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private final UserCacheService userCacheService;
    private final DataSubmitMapper dataSubmitMapper;

    private final DataSolvedMapper dataSolvedMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    private final TransService transService;

    @Override
    public Page<SysUser> page(SysUserPageParam sysUserPageParam) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<SysUser>().checkSqlInjection();

        String type = sysUserPageParam.getType();
        if (type.equals("username")) {
            queryWrapper.lambda()
                    .like(SysUser::getUsername, sysUserPageParam.getKeyword());
        } else if (type.equals("nickname")) {
            queryWrapper.lambda()
                    .like(SysUser::getNickname, sysUserPageParam.getKeyword());
        } else if (type.equals("email")) {
            queryWrapper.lambda()
                    .like(SysUser::getEmail, sysUserPageParam.getKeyword());
        } else if (type.equals("telephone")) {
            queryWrapper.lambda()
                    .like(SysUser::getTelephone, sysUserPageParam.getKeyword());
        } else if (type.equals("studentNumber")) {
            queryWrapper.lambda()
                    .like(SysUser::getStudentNumber, sysUserPageParam.getKeyword());
        }

        if (ObjectUtil.isNotEmpty(sysUserPageParam.getGroupId())) {
            queryWrapper.lambda()
                    .eq(SysUser::getGroupId, sysUserPageParam.getGroupId());
        }
//        // 关键字
//        if (ObjectUtil.isNotEmpty(sysUserPageParam.getKeyword())) {
//            queryWrapper.lambda()
//                    .like(SysUser::getNickname, sysUserPageParam.getKeyword());
//        }
        if (ObjectUtil.isNotEmpty(sysUserPageParam.getGroupId())) {
            queryWrapper.lambda()
                    .like(SysUser::getGroupId, sysUserPageParam.getGroupId());
        }
        if (ObjectUtil.isAllNotEmpty(sysUserPageParam.getSortField(), sysUserPageParam.getSortOrder()) && ISortOrderEnum.isValid(sysUserPageParam.getSortOrder())) {
            queryWrapper.orderBy(
                    true,
                    sysUserPageParam.getSortOrder().equals(ISortOrderEnum.ASCEND.getValue()),
                    StrUtil.toUnderlineCase(sysUserPageParam.getSortField()));
        }

        Page<SysUser> page = this.page(CommonPageRequest.Page(
                        Optional.ofNullable(sysUserPageParam.getCurrent()).orElse(1),
                        Optional.ofNullable(sysUserPageParam.getSize()).orElse(20),
                        null
                ),
                queryWrapper);
        page.getRecords().forEach(sysUser -> {
            List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, sysUser.getId())
            );
            List<String> stringList = sysUserRoles.stream().map(SysUserRole::getRoleId).toList();
            sysUser.setAssignRoles(stringList);
        });
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(SysUserAddParam sysUserAddParam) {
//        UserValidationUtil.validatePassword(password).throwIfFailed();
        UserValidationUtil.validateUsername(sysUserAddParam.getUsername()).throwIfFailed();

        UserValidationUtil.validateEmail(sysUserAddParam.getEmail()).throwIfFailed();

        // 加密密码
        String encodePassword = BCrypt.hashpw(DefaultUserData.DEFAULT_PASSWORD);
        SysUser bean = BeanUtil.toBean(sysUserAddParam, SysUser.class);
        bean.setPassword(encodePassword);
        this.save(bean);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(SysUserEditParam sysUserEditParam) {
        if (!this.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, sysUserEditParam.getId()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        UserValidationUtil.validateUsername(sysUserEditParam.getUsername()).throwIfFailed();

        UserValidationUtil.validateEmail(sysUserEditParam.getEmail()).throwIfFailed();

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
        List<SysUserRole> sysUserRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, sysUser.getId())
        );
        List<String> stringList = sysUserRoles.stream().map(SysUserRole::getRoleId).toList();
        sysUser.setAssignRoles(stringList);
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

        Long solvedProblemCount = dataSolvedMapper.selectCount(new LambdaQueryWrapper<DataSolved>()
                .eq(DataSolved::getUserId, sysUser.getId())
                .eq(DataSolved::getIsSet, false)
                .eq(DataSolved::getSolved, true)
        );
        sysUser.setSolvedProblem(solvedProblemCount);
        Long tryProblemCount = dataSolvedMapper.selectCount(new LambdaQueryWrapper<DataSolved>()
                .eq(DataSolved::getUserId, sysUser.getId())
                .eq(DataSolved::getIsSet, false)
                .eq(DataSolved::getSolved, false)
        );
        sysUser.setTryProblem(tryProblemCount);
        long participatedSetCount = dataSolvedMapper.selectCount(
                new QueryWrapper<DataSolved>()
                        .select("DISTINCT set_id")
                        .lambda()
                        .eq(DataSolved::getUserId, sysUser.getId())
                        .eq(DataSolved::getIsSet, true)
        );
        sysUser.setParticipatedSet(participatedSetCount);
        sysUser.setActiveScore(userCacheService.getUserActivity(sysUser.getId()));

        List<DataSubmit> dataSubmits = dataSubmitMapper.selectList(new LambdaQueryWrapper<DataSubmit>()
                .eq(DataSubmit::getUserId, sysUser.getId())
                .eq(DataSubmit::getIsSet, false)
                .eq(DataSubmit::getStatus, JudgeStatus.ACCEPTED.getValue())
                .eq(DataSubmit::getSubmitType, true)
                // 近两年的
                .ge(DataSubmit::getCreateTime, DateUtil.offsetMonth(new Date(), -24))
        );
        // 构建 ACRecord 列表
        List<ACRecord> acRecords = dataSubmits.stream()
                .collect(Collectors.groupingBy(
                        dataSubmit -> DateUtil.format(dataSubmit.getCreateTime(), "yyyy-MM-dd"),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> new ACRecord(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ACRecord::getDate))
                .toList();
        sysUser.setAcRecord(acRecords);
        transService.transOne(sysUser);
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