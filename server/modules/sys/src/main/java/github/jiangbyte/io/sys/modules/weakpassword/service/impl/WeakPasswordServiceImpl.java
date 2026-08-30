package github.jiangbyte.io.sys.modules.weakpassword.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.sys.modules.weakpassword.convert.SysWeakPasswordConvert;
import github.jiangbyte.io.sys.modules.weakpassword.entity.SysWeakPassword;
import github.jiangbyte.io.sys.modules.weakpassword.mapper.SysWeakPasswordMapper;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordAddParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordEditParam;
import github.jiangbyte.io.sys.modules.weakpassword.param.SysWeakPasswordPageParam;
import github.jiangbyte.io.sys.modules.weakpassword.service.WeakPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 弱密码服务实现：库维护与匹配校验。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class WeakPasswordServiceImpl extends ServiceImpl<SysWeakPasswordMapper, SysWeakPassword>
        implements WeakPasswordService {

    private final SysWeakPasswordConvert weakPasswordConvert;

    @Override
    @Transactional
    public void create(SysWeakPasswordAddParam param) {
        if (!StringUtils.hasText(param.getPassword())) {
            throw new BizException("Password is required");
        }
        SysWeakPassword existing = getBaseMapper().selectOne(Wrappers.<SysWeakPassword>lambdaQuery()
                .eq(SysWeakPassword::getPassword, param.getPassword().trim())
                .last("limit 1"));
        if (existing != null) {
            throw new BizException("Weak password already exists");
        }
        SysWeakPassword entity = weakPasswordConvert.toEntity(param);
        entity.setPassword(param.getPassword().trim());
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(SysWeakPasswordEditParam param) {
        // 按主键加载
        SysWeakPassword entity = this.getById(param.getId());
        if (entity == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Weak password not found");
        }
        if (!StringUtils.hasText(param.getPassword())) {
            throw new BizException("Password is required");
        }
        SysWeakPassword existing = getBaseMapper().selectOne(Wrappers.<SysWeakPassword>lambdaQuery()
                .eq(SysWeakPassword::getPassword, param.getPassword().trim())
                .last("limit 1"));
        if (existing != null && !entity.getId().equals(existing.getId())) {
            throw new BizException("Weak password already exists");
        }
        AuditSnapshots.before(entity);
        weakPasswordConvert.update(param, entity);
        entity.setPassword(param.getPassword().trim());
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysWeakPassword> entities = this.listByIds(ids);
        AuditSnapshots.deletedAll(entities);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysWeakPassword detail(String id) {
        // 按主键加载
        SysWeakPassword entity = this.getById(id);
        if (entity == null) {
            // 不存在则抛出业务异常
            throw new BizException(404, "Weak password not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<SysWeakPassword> page(SysWeakPasswordPageParam param) {
        String filter = firstText(param.getPassword(), param.getKeyword());
        return this.getBaseMapper().selectPage(
                new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysWeakPassword>lambdaQuery()
                        .like(StringUtils.hasText(filter), SysWeakPassword::getPassword, filter)
                        .orderByDesc(SysWeakPassword::getId));
    }

    @Override
    @ReadDataSource
    public List<SysWeakPassword> list(String password, String keyword) {
        String filter = firstText(password, keyword);
        return getBaseMapper().selectList(Wrappers.<SysWeakPassword>lambdaQuery()
                .like(StringUtils.hasText(filter), SysWeakPassword::getPassword, filter)
                .orderByDesc(SysWeakPassword::getId));
    }

    private static String firstText(String primary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        return StringUtils.hasText(fallback) ? fallback : null;
    }
}
