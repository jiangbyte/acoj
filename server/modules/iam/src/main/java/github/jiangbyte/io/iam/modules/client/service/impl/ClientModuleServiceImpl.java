package github.jiangbyte.io.iam.modules.client.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.enums.AccountType;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.iam.modules.client.convert.SysClientConvert;
import github.jiangbyte.io.iam.modules.client.entity.SysClientModule;
import github.jiangbyte.io.iam.modules.client.mapper.SysClientModuleMapper;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleAddParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModuleEditParam;
import github.jiangbyte.io.iam.modules.client.param.SysClientModulePageParam;
import github.jiangbyte.io.iam.modules.client.service.ClientModuleService;
import github.jiangbyte.io.iam.modules.relation.constants.IamRelationTypes;
import lombok.RequiredArgsConstructor;
import org.dromara.trans.service.impl.TransService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 客户端模块服务实现：模块增删改查与选择器列表。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class ClientModuleServiceImpl extends ServiceImpl<SysClientModuleMapper, SysClientModule>
        implements ClientModuleService {

    private final SysClientConvert clientConvert;
    private final TransService transService;

    @Override
    @Transactional
    public void create(SysClientModuleAddParam param) {
        SysClientModule module = clientConvert.toEntity(param);
        if (!StringUtils.hasText(module.getAccountType())) {
            module.setAccountType(AccountType.ADMIN.name());
        } else {
            module.setAccountType(module.getAccountType().trim().toUpperCase());
        }
        this.save(module);
        AuditSnapshots.created(module);
    }

    @Override
    @Transactional
    public void update(SysClientModuleEditParam param) {
        SysClientModule module = this.getById(param.getId());
        if (module == null) {
            throw new BizException(404, "Client module not found");
        }
        AuditSnapshots.before(module);
        clientConvert.update(param, module);
        if (!StringUtils.hasText(module.getAccountType())) {
            module.setAccountType(AccountType.ADMIN.name());
        } else {
            module.setAccountType(module.getAccountType().trim().toUpperCase());
        }
        this.updateById(module);
        AuditSnapshots.after(module);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        List<String> ids = param.getIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<SysClientModule> modules = this.listByIds(ids);
        AuditSnapshots.deletedAll(modules);
        this.removeByIds(ids);
    }

    @Override
    @ReadDataSource
    public SysClientModule detail(String id) {
        SysClientModule module = this.getById(id);
        if (module == null) {
            throw new BizException(404, "Client module not found");
        }
        return module;
    }

    @Override
    @ReadDataSource
    public Page<SysClientModule> page(SysClientModulePageParam param) {
        Page<SysClientModule> page = this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<SysClientModule>lambdaQuery()
                        .like(StringUtils.hasText(param.getCode()), SysClientModule::getCode, param.getCode())
                        .like(StringUtils.hasText(param.getName()), SysClientModule::getName, param.getName())
                        .eq(StringUtils.hasText(param.getAccountType()), SysClientModule::getAccountType, param.getAccountType())
                        .eq(StringUtils.hasText(param.getStatus()), SysClientModule::getStatus, param.getStatus())
                        .orderByAsc(SysClientModule::getSort));
        transService.transBatch(page.getRecords());
        return page;
    }

    @Override
    @ReadDataSource
    public List<SysClientModule> selector(String accountType) {
        return this.getBaseMapper().selectList(Wrappers.<SysClientModule>lambdaQuery()
                .eq(SysClientModule::getStatus, IamRelationTypes.STATUS_ENABLED)
                .eq(StringUtils.hasText(accountType), SysClientModule::getAccountType, accountType)
                .orderByAsc(SysClientModule::getSort));
    }
}
