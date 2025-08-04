package io.charlie.app.core.modular.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.charlie.app.core.modular.auth.enums.PlatformEnum;
import io.charlie.app.core.modular.auth.param.UsernamePasswordEmailRegisterParam;
import io.charlie.app.core.modular.auth.param.UsernamePasswordLoginParam;
import io.charlie.app.core.modular.auth.result.CaptchaResult;
import io.charlie.app.core.modular.auth.result.LoginUser;
import io.charlie.app.core.modular.auth.service.AuthService;
import io.charlie.app.core.modular.group.enums.SysGroupEnums;
import io.charlie.app.core.modular.user.constant.DefaultUserData;
import io.charlie.app.core.modular.user.entity.SysUser;
import io.charlie.app.core.modular.user.mapper.SysUserMapper;
import io.charlie.galaxy.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;

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

    @Override
    public CaptchaResult captcha() {
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(100, 38, 4, 10);
        CaptchaResult captchaResult = new CaptchaResult();
        String imageBase64Data = circleCaptcha.getImageBase64Data();
        captchaResult.setCaptcha(imageBase64Data);
        String uuid = IdUtil.fastSimpleUUID();
        captchaResult.setUuid(uuid);
        redisTemplate.opsForValue().set("captcha:" + uuid, circleCaptcha.getCode(), Duration.ofSeconds(5 * 60L));
        return captchaResult;
    }

    @Override
    public String doLogin(UsernamePasswordLoginParam usernamePasswordLoginParam) {
        // 校验验证码
        if (ObjectUtil.isEmpty(usernamePasswordLoginParam.getUuid())) {
            throw new BusinessException("验证码标识不能为空");
        }
        if (ObjectUtil.isEmpty(usernamePasswordLoginParam.getCaptchaCode())) {
            throw new BusinessException("验证码不能为空");
        }
        String captchaCode = (String) redisTemplate.opsForValue().get("captcha:" + usernamePasswordLoginParam.getUuid());
        if (ObjectUtil.isEmpty(captchaCode)) {
            throw new BusinessException("验证码已过期");
        }
        if (!usernamePasswordLoginParam.getCaptchaCode().equals(captchaCode)) {
            throw new BusinessException("验证码错误");
        }
        // 到这里验证码校验通过，删除验证码
        redisTemplate.delete("captcha:" + usernamePasswordLoginParam.getUuid());

        // 平台校验
        if (!PlatformEnum.isValid(usernamePasswordLoginParam.getPlatform().toUpperCase())) {
            throw new BusinessException("平台参数错误");
        }

        // 登录逻辑
        String username = usernamePasswordLoginParam.getUsername();
        String password = usernamePasswordLoginParam.getPassword();

        // 数据库用户名是否存在
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (ObjectUtil.isEmpty(sysUser)) {
            throw new BusinessException("用户不存在");
        }
        if (!BCrypt.checkpw(password, sysUser.getPassword())) {
            throw new BusinessException("密码错误");
        }
        StpUtil.login(sysUser.getId(), usernamePasswordLoginParam.getPlatform().toUpperCase());
        return StpUtil.getTokenValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String doRegister(UsernamePasswordEmailRegisterParam usernamePasswordEmailRegisterParam) {
        // 校验验证码
        if (ObjectUtil.isEmpty(usernamePasswordEmailRegisterParam.getUuid())) {
            throw new BusinessException("验证码标识不能为空");
        }
        if (ObjectUtil.isEmpty(usernamePasswordEmailRegisterParam.getCaptchaCode())) {
            throw new BusinessException("验证码不能为空");
        }
        String captchaCode = (String) redisTemplate.opsForValue().get("captcha:" + usernamePasswordEmailRegisterParam.getUuid());
        if (ObjectUtil.isEmpty(captchaCode)) {
            throw new BusinessException("验证码已过期");
        }
        if (!captchaCode.equals(usernamePasswordEmailRegisterParam.getCaptchaCode())) {
            throw new BusinessException("验证码错误");
        }
        // 到这里验证码校验通过，删除验证码
        redisTemplate.delete("captcha:" + usernamePasswordEmailRegisterParam.getUuid());

        // 注册逻辑
        String username = usernamePasswordEmailRegisterParam.getUsername();
        String password = usernamePasswordEmailRegisterParam.getPassword();
        String email = usernamePasswordEmailRegisterParam.getEmail();

        // 用户名 邮箱 存在校验
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username))) {
            throw new BusinessException("用户名已存在");
        }
        if (sysUserMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email))) {
            throw new BusinessException("邮箱已存在");
        }

        // 邮箱格式校验
        if (!Validator.isEmail(email)) {
            throw new BusinessException("邮箱格式错误");
        }

        // 加密密码
        String encodePassword = BCrypt.hashpw(password);

        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPassword(encodePassword);
        sysUser.setEmail(email);
        // 默认用户组
        sysUser.setGroupId(SysGroupEnums.DEFAULT_GROUP.getValue());
        // 默认昵称
        sysUser.setNickname(DefaultUserData.USER_DEFAULT_NICKNAME);
        // 默认头像
        sysUser.setAvatar(DefaultUserData.USER_DEFAULT_AVATAR);
        // 默认背景图片
        sysUser.setBackground(DefaultUserData.USER_DEFAULT_BACKGROUND);
        // 默认 性别
        sysUser.setGender(DefaultUserData.USER_DEFAULT_GENDER);
        // 默认 签名
        sysUser.setQuote(DefaultUserData.USER_DEFAULT_QUOTE);
        // 默认数据
        sysUser.setDeleted(false);
        sysUserMapper.insert(sysUser);

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
        String loginIdAsString = StpUtil.getLoginIdAsString();
        // 获取用户信息
        SysUser sysUser = sysUserMapper.selectById(loginIdAsString);
        LoginUser loginUser = new LoginUser();
        BeanUtils.copyProperties(sysUser, loginUser);
        // 手机号，邮箱脱敏
        loginUser.setTelephone(DesensitizedUtil.mobilePhone(sysUser.getTelephone()));
        loginUser.setEmail(DesensitizedUtil.email(sysUser.getEmail()));
        return loginUser;
    }
}
