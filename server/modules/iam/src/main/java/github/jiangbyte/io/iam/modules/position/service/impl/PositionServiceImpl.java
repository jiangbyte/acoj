package github.jiangbyte.io.iam.modules.position.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.common.security.datascope.DataScopeConstraint;
import github.jiangbyte.io.iam.modules.dept.support.DataScopeResolver;
import github.jiangbyte.io.iam.modules.position.convert.SysPositionConvert;
import github.jiangbyte.io.iam.modules.position.entity.SysPosition;
import github.jiangbyte.io.iam.modules.position.mapper.SysPositionMapper;
import github.jiangbyte.io.iam.modules.position.param.SysPositionAddParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionEditParam;
import github.jiangbyte.io.iam.modules.position.param.SysPositionPageParam;
import github.jiangbyte.io.iam.modules.position.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 岗位服务实现：岗位增删改查与数据权限过滤。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition> implements PositionService {

    private final TransService transService;
    private final DataScopeResolver dataScopeResolver;
    private final SysPositionConvert positionConvert;

    @Override
    @Transactional
    public void create(SysPositionAddParam param) {
        SysPosition position = positionConvert.toEntity(param);
        this.save(position);
        AuditSnapshots.created(position);
    }

    @Override
    @Transactional
    public void update(SysPositionEditParam param) {
        SysPosition position = this.getById(param.getId());
        if (position == null) {
            throw new BizException(404, "Position not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                position.getCreatedBy(), position.getOwnerDeptId(), "iam:position:page");
        AuditSnapshots.before(position);
        positionConvert.update(param, position);
        this.updateById(position);
        AuditSnapshots.after(position);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysPosition> positions = this.listByIds(ids);
        DataScopeConstraint scope = dataScopeResolver.resolve("iam:position:page");
        for (SysPosition position : positions) {
            dataScopeResolver.assertOwnerOrDeptAccessible(
                    position.getCreatedBy(), position.getOwnerDeptId(), scope);
        }
        AuditSnapshots.deletedAll(positions);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysPosition detail(String id) {
        SysPosition position = this.getById(id);
        if (position == null) {
            throw new BizException(404, "Position not found");
        }
        dataScopeResolver.assertOwnerOrDeptAccessible(
                position.getCreatedBy(), position.getOwnerDeptId(), "iam:position:page");
        return position;
    }

    @Override
    @ReadDataSource
    public Page<SysPosition> page(SysPositionPageParam param) {
        LambdaQueryWrapper<SysPosition> wrapper = Wrappers.<SysPosition>lambdaQuery()
                .like(StringUtils.hasText(param.getName()), SysPosition::getName, param.getName())
                .eq(StringUtils.hasText(param.getCategory()), SysPosition::getCategory, param.getCategory())
                .eq(StringUtils.hasText(param.getStatus()), SysPosition::getStatus, param.getStatus())
                .orderByAsc(SysPosition::getSort);
        dataScopeResolver.applyOwnerOrDept(
                wrapper, "iam:position:page", SysPosition::getCreatedBy, SysPosition::getOwnerDeptId);
        Page<SysPosition> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()), wrapper);
        transService.transBatch(page.getRecords());
        return page;
    }
}
