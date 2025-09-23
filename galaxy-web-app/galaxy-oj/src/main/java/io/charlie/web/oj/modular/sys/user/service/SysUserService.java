package io.charlie.web.oj.modular.sys.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.charlie.galaxy.option.LabelOption;
import io.charlie.web.oj.modular.sys.user.entity.SysUser;
import io.charlie.web.oj.modular.sys.user.param.*;

import java.util.List;

/**
* @author Charlie Zhang
* @version v1.0
* @date 2025-09-20
* @description 用户表 服务类
*/
public interface SysUserService extends IService<SysUser> {
    Page<SysUser> page(SysUserPageParam sysUserPageParam);

    void add(SysUserAddParam sysUserAddParam);

    void edit(SysUserEditParam sysUserEditParam);

    void delete(List<SysUserIdParam> sysUserIdParamList);

    SysUser detail(SysUserIdParam sysUserIdParam);

    List<LabelOption<String>> options(SysUserOptionParam sysUserOptionParam);

    SysUser appDetail(SysUserIdParam sysUserIdParam);

    // 客户端更新个人资料
    void updateApp(SysUserUpdateAppParam sysUserUpdateAppParam);

    // 更新头像
    Boolean updateAvatar(SysUserUpdateImageParam sysUserUpdateImageParam);

    // 更新背景图片
    Boolean updateBackground(SysUserUpdateImageParam sysUserUpdateImageParam);

    // 更新密码
    Boolean updatePassword(SysUserPasswordUpdateParam sysUserPasswordUpdateParam);

}