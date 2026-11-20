package io.charlie.web.modular.sys.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.web.constant.EGroupConstant;
import io.charlie.web.context.DataScopeUtil;
import io.charlie.web.modular.data.ranking.utils.ActivityScoreCalculator;
import io.charlie.web.modular.data.ranking.service.UserActivityService;
import io.charlie.web.modular.sys.auth.enums.PlatformEnum;
import io.charlie.web.modular.sys.auth.param.PasswordChangeParam;
import io.charlie.web.modular.sys.auth.param.UsernamePasswordLoginParam;
import io.charlie.web.modular.sys.auth.service.AuthService;
import io.charlie.web.modular.sys.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.web.modular.sys.auth.result.CaptchaResult;
import io.charlie.web.modular.sys.auth.result.LoginUser;
import io.charlie.web.modular.sys.auth.utils.UserValidationUtil;
import io.charlie.web.modular.sys.config.service.SysConfigService;
import io.charlie.web.modular.sys.group.mapper.SysGroupMapper;
import io.charlie.web.modular.sys.menu.mapper.SysMenuMapper;
import io.charlie.web.modular.sys.relation.mapper.SysRoleMenuMapper;
import io.charlie.web.modular.sys.relation.mapper.SysUserRoleMapper;
import io.charlie.web.modular.sys.relation.service.SysUserRoleService;
import io.charlie.web.constant.ERoleConstant;
import io.charlie.web.modular.sys.role.mapper.SysRoleMapper;
import io.charlie.web.modular.sys.role.service.SysRoleService;
import io.charlie.web.constant.EUserConstant;
import io.charlie.web.modular.sys.user.entity.SysUser;
import io.charlie.web.modular.sys.user.mapper.SysUserMapper;
import io.charlie.cores.exception.BusinessException;
import io.charlie.cores.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 23/07/2025
 * @description 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserRoleService sysUserRoleService;

    private final UserActivityService userActivityService;

    private final SysRoleService sysRoleService;
    private final SysGroupMapper sysGroupMapper;

    private final SysConfigService sysConfigService;

    private final TransService transService;

    private final DataScopeUtil dataScopeUtil;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysMenuMapper sysMenuMapper;

    private final Environment environment;

    @Override
    public CaptchaResult captcha() {
        CaptchaResult captchaResult = new CaptchaResult();

//        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(100, 38, 4, 10);
//        String imageBase64Data = circleCaptcha.getImageBase64Data();

        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 5);
        lineCaptcha.setGenerator(randomGenerator);
        String imageBase64Data = lineCaptcha.getImageBase64Data();

        captchaResult.setCaptcha(imageBase64Data);
        String uuid = IdUtil.fastSimpleUUID();
        captchaResult.setUuid(uuid);
        redisTemplate.opsForValue().set("captcha:" + uuid, lineCaptcha.getCode(), Duration.ofSeconds(5 * 60L));
        return captchaResult;
    }

    /**
     * 用户名密码登录：校验验证码与凭据后签发 Sa-Token。
     */
    @Override
    public String doLogin(UsernamePasswordLoginParam usernamePasswordLoginParam) {
        // 1. 校验图形验证码（dev 下允许测试码 9926）
        assertCaptchaValid(usernamePasswordLoginParam.getUuid(), usernamePasswordLoginParam.getCaptchaCode(), "9926");

        // 2. 校验登录平台
        if (ObjectUtil.isEmpty(usernamePasswordLoginParam.getPlatform())
                || !PlatformEnum.isValid(usernamePasswordLoginParam.getPlatform().toUpperCase())) {
            throw new BusinessException(ResultCode.PLATFORM_ERROR);
        }

        // 3. 校验用户名/密码格式
        String username = usernamePasswordLoginParam.getUsername().toLowerCase();
        UserValidationUtil.validateUsername(username).throwIfFailed();
        String password = usernamePasswordLoginParam.getPassword();
        UserValidationUtil.validatePassword(password).throwIfFailed();

        // 4. 查库校验账号与密码
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (ObjectUtil.isEmpty(sysUser)) {
            throw new BusinessException(ResultCode.USERNAME_NOT_EXISTS);
        }
        if (!BCrypt.checkpw(password, sysUser.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 5. ADMIN 需管理权限；CLIENT 记活跃分
        if ("ADMIN".equalsIgnoreCase(usernamePasswordLoginParam.getPlatform())) {
            if (!dataScopeUtil.canManageData(sysUser.getId())) {
                throw new BusinessException(ResultCode.UNAUTHORIZED);
            }
        } else {
            userActivityService.addActivity(sysUser.getId(), ActivityScoreCalculator.LOGIN, false);
        }

        // 6. 更新登录时间并签发 token
        sysUser.setLoginTime(new Date());
        sysUserMapper.updateById(sysUser);
        StpUtil.login(sysUser.getId(), usernamePasswordLoginParam.getPlatform().toUpperCase());
        return StpUtil.getTokenValue();
    }

    /**
     * 用户注册：校验验证码与唯一性后落库并自动登录 CLIENT。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doRegister(UsernamePasswordEmailRegisterParam usernamePasswordEmailRegisterParam) {
        // 1. 校验图形验证码（dev 下允许测试码 9927）
        assertCaptchaValid(usernamePasswordEmailRegisterParam.getUuid(),
                usernamePasswordEmailRegisterParam.getCaptchaCode(), "9927");

        // 2. 校验用户名/密码/邮箱格式
        String username = usernamePasswordEmailRegisterParam.getUsername().toLowerCase();
        UserValidationUtil.validateUsername(username).throwIfFailed();
        String password = usernamePasswordEmailRegisterParam.getPassword();
        UserValidationUtil.validatePassword(password).throwIfFailed();
        String email = usernamePasswordEmailRegisterParam.getEmail().toLowerCase();
        UserValidationUtil.validateEmail(email).throwIfFailed();

        // 3. 唯一性校验
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username))) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email))) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 加密密码
        String encodePassword = BCrypt.hashpw(password);

        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(encodePassword);
        sysUser.setEmail(email);
        // 默认用户组
        sysUser.setGroupId(EGroupConstant.DEFAULT_USER_GROUP_ID);
        // 默认昵称
        long timestamp = System.currentTimeMillis(); // 获取当前时间戳（毫秒）
        String last5Digits = String.format("%05d", timestamp % 100000); // 取模保留后5位，不足补0
        sysUser.setNickname("用户_" + last5Digits);
        // 默认头像
        sysUser.setAvatar(sysConfigService.getValueByCode("APP_DEFAULT_AVATAR"));
        // 默认背景图片
        sysUser.setBackground(sysConfigService.getValueByCode("APP_DEFAULT_USER_BACKGROUND"));
        // 默认 性别
        sysUser.setGender(EUserConstant.USER_DEFAULT_GENDER);
        // 默认 签名
        sysUser.setQuote(EUserConstant.USER_DEFAULT_QUOTE);
        // 默认数据
        sysUser.setDeleted(false);
        sysUser.setLoginTime(new Date());
        sysUserMapper.insert(sysUser);

        // 分配角色
        sysUserRoleService.assignRoles(sysUser.getId(), List.of(ERoleConstant.DEFAULT_USER_ROLE_ID));

        // 登录
        StpUtil.login(sysUser.getId(), PlatformEnum.CLIENT.getValue());
        return StpUtil.getTokenValue();
    }

    @Override
    public void doLogout() {
        StpUtil.logout();
    }

    @Override
    public LoginUser getLoginUser() {
        String userId = StpUtil.getLoginIdAsString();
        // 获取用户信息
        SysUser sysUser = sysUserMapper.selectById(userId);
        transService.transOne(sysUser);
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        // 手机号，邮箱脱敏
//        loginUser.setTelephone(DesensitizedUtil.mobilePhone(sysUser.getTelephone()));
//        loginUser.setEmail(DesensitizedUtil.email(sysUser.getEmail()));
        // 查询这个用户的角色名
        loginUser.setRoleNames(sysRoleService.getRoleNamesByUserId(sysUser.getId()));

        List<String> list = sysMenuMapper.selectPermissionsByUserId(userId);
        if (CollectionUtil.isNotEmpty(list)) {
            List<String> permissions = new ArrayList<>();
            for (String s : list) {
                if (StrUtil.isBlank(s) || "null".equals(s)) {
                    continue;
                }

                // 判断是否是JSON数组格式
                if (JSONUtil.isTypeJSONArray(s)) {
                    // 解析JSON数组
                    JSONArray jsonArray = JSONUtil.parseArray(s);
                    for (Object item : jsonArray) {
                        if (item != null) {
                            permissions.add(item.toString());
                        }
                    }
                } else {
                    // 单个权限字符串
                    permissions.add(s);
                }
            }

            loginUser.setPermissions(permissions);
        }

        return loginUser;
    }

    @Override
    public void changePassword(PasswordChangeParam passwordChangeParam) {
        String loginIdAsString = StpUtil.getLoginIdAsString();
        // 获取用户信息
        SysUser sysUser = sysUserMapper.selectById(loginIdAsString);


        // 验证旧密码
        if (!BCrypt.checkpw(passwordChangeParam.getOldPassword(), sysUser.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 新密码是否两次一样
        if (!passwordChangeParam.getNewPassword().equals(passwordChangeParam.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        UserValidationUtil.validatePassword(passwordChangeParam.getNewPassword()).throwIfFailed();
        sysUser.setPassword(BCrypt.hashpw(passwordChangeParam.getNewPassword()));

        sysUserMapper.updateById(sysUser);
    }

    /**
     * 校验图形验证码；仅 dev 环境允许测试旁路码。
     */
    private void assertCaptchaValid(String uuid, String captchaCode, String devBypassCode) {
        // 1. dev 旁路：仅当激活了 dev 且输入了指定测试码时跳过 Redis 校验
        if (isDevCaptchaBypass(captchaCode, devBypassCode)) {
            return;
        }
        // 2. 必填校验（避免 captchaCode 为 null 时 NPE）
        if (ObjectUtil.isEmpty(uuid)) {
            throw new BusinessException(ResultCode.PARAM_NOT_NULL);
        }
        if (ObjectUtil.isEmpty(captchaCode)) {
            throw new BusinessException(ResultCode.PARAM_NOT_NULL);
        }
        // 3. Redis 比对并消费
        String cached = (String) redisTemplate.opsForValue().get("captcha:" + uuid);
        if (ObjectUtil.isEmpty(cached)) {
            throw new BusinessException(ResultCode.CAPTCHA_EXPIRED);
        }
        if (!captchaCode.equals(cached)) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        redisTemplate.delete("captcha:" + uuid);
    }

    /**
     * 是否允许使用开发环境验证码旁路。
     */
    private boolean isDevCaptchaBypass(String captchaCode, String bypassCode) {
        return ObjectUtil.isNotEmpty(captchaCode)
                && captchaCode.equals(bypassCode)
                && environment.acceptsProfiles(Profiles.of("dev"));
    }
}
