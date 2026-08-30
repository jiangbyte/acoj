package github.jiangbyte.io.biz.modules.cg_test_activity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.biz.modules.cg_test_activity.convert.CgTestActivityConvert;
import github.jiangbyte.io.biz.modules.cg_test_activity.entity.CgTestActivity;
import github.jiangbyte.io.biz.modules.cg_test_activity.mapper.CgTestActivityMapper;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityAddParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityEditParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.param.CgTestActivityPageParam;
import github.jiangbyte.io.biz.modules.cg_test_activity.service.CgTestActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Activity服务实现：维护与查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class CgTestActivityServiceImpl extends ServiceImpl<CgTestActivityMapper, CgTestActivity> implements CgTestActivityService {

    private final CgTestActivityConvert cgTestActivityConvert;

    @Override
    @Transactional
    public void create(CgTestActivityAddParam param) {
        // 入参转实体并持久化
        CgTestActivity entity = cgTestActivityConvert.toEntity(param);
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(CgTestActivityEditParam param) {
        // 按主键加载
        CgTestActivity entity = this.getById(param.getId());
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestActivity not found");
        }
        // 合并编辑入参并更新
        AuditSnapshots.before(entity);
        cgTestActivityConvert.update(param, entity);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<CgTestActivity> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        // 批量删除
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public CgTestActivity detail(String id) {
        // 按主键加载
        CgTestActivity entity = this.getById(id);
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestActivity not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<CgTestActivity> page(CgTestActivityPageParam param) {
        // 组装条件并分页查询
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<CgTestActivity>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), CgTestActivity::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), CgTestActivity::getName, param.getName())
                        .like(StringUtils.hasText(param.getCategory()), CgTestActivity::getCategory, param.getCategory())
                        .like(StringUtils.hasText(param.getType()), CgTestActivity::getType, param.getType())
                        .eq(param.getStatus() != null && StringUtils.hasText(param.getStatus()), CgTestActivity::getStatus, param.getStatus())
                        .orderByDesc(CgTestActivity::getCreatedAt));
    }
}
