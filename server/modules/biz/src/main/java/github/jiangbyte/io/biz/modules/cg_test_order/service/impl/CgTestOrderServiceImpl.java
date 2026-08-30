package github.jiangbyte.io.biz.modules.cg_test_order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.jiangbyte.io.common.core.exception.BizException;
import github.jiangbyte.io.common.core.param.IdsParam;
import github.jiangbyte.io.common.log.audit.AuditSnapshots;
import github.jiangbyte.io.common.mybatis.datasource.ReadDataSource;
import github.jiangbyte.io.biz.modules.cg_test_order.convert.CgTestOrderConvert;
import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrder;
import github.jiangbyte.io.biz.modules.cg_test_order.mapper.CgTestOrderMapper;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderEditParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderPageParam;
import github.jiangbyte.io.biz.modules.cg_test_order.service.CgTestOrderService;
import github.jiangbyte.io.biz.modules.cg_test_order.convert.CgTestOrderItemConvert;
import github.jiangbyte.io.biz.modules.cg_test_order.entity.CgTestOrderItem;
import github.jiangbyte.io.biz.modules.cg_test_order.mapper.CgTestOrderItemMapper;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemAddParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemEditParam;
import github.jiangbyte.io.biz.modules.cg_test_order.param.CgTestOrderItemPageParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Order服务实现：维护与查询。
 *
 * Author: Charlie
 */
@Service
@RequiredArgsConstructor
public class CgTestOrderServiceImpl extends ServiceImpl<CgTestOrderMapper, CgTestOrder> implements CgTestOrderService {

    private final CgTestOrderConvert cgTestOrderConvert;
    private final CgTestOrderItemMapper cgTestOrderItemMapper;
    private final CgTestOrderItemConvert cgTestOrderItemConvert;

    @Override
    @Transactional
    public void create(CgTestOrderAddParam param) {
        // 入参转实体并持久化
        CgTestOrder entity = cgTestOrderConvert.toEntity(param);
        this.save(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void update(CgTestOrderEditParam param) {
        // 按主键加载
        CgTestOrder entity = this.getById(param.getId());
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestOrder not found");
        }
        // 合并编辑入参并更新
        AuditSnapshots.before(entity);
        cgTestOrderConvert.update(param, entity);
        this.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void delete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<CgTestOrder> entities = this.listByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        // 批量删除
        this.removeByIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public CgTestOrder detail(String id) {
        // 按主键加载
        CgTestOrder entity = this.getById(id);
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestOrder not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<CgTestOrder> page(CgTestOrderPageParam param) {
        // 组装条件并分页查询
        return this.getBaseMapper().selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<CgTestOrder>lambdaQuery()
                        .like(StringUtils.hasText(param.getOrderNo()), CgTestOrder::getOrderNo, param.getOrderNo())
                        .like(StringUtils.hasText(param.getName()), CgTestOrder::getName, param.getName())
                        .like(StringUtils.hasText(param.getCustomerName()), CgTestOrder::getCustomerName, param.getCustomerName())
                        .eq(param.getStatus() != null && StringUtils.hasText(param.getStatus()), CgTestOrder::getStatus, param.getStatus())
                        .like(StringUtils.hasText(param.getType()), CgTestOrder::getType, param.getType())
                        .orderByDesc(CgTestOrder::getCreatedAt));
    }

    @Override
    @Transactional
    public void childCreate(CgTestOrderItemAddParam param) {
        // 入参转子实体并插入
        CgTestOrderItem entity = cgTestOrderItemConvert.toEntity(param);
        cgTestOrderItemMapper.insert(entity);
        AuditSnapshots.created(entity);
    }

    @Override
    @Transactional
    public void childUpdate(CgTestOrderItemEditParam param) {
        // 按主键加载子实体
        CgTestOrderItem entity = cgTestOrderItemMapper.selectById(param.getId());
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestOrderItem not found");
        }
        // 合并编辑入参并更新
        AuditSnapshots.before(entity);
        cgTestOrderItemConvert.update(param, entity);
        cgTestOrderItemMapper.updateById(entity);
        AuditSnapshots.after(entity);
    }

    @Override
    @Transactional
    public void childDelete(IdsParam param) {
        if (param.getIds() == null || param.getIds().isEmpty()) {
            return;
        }
        List<CgTestOrderItem> entities = cgTestOrderItemMapper.selectByIds(param.getIds());
        AuditSnapshots.deletedAll(entities);
        // 批量删除子实体
        cgTestOrderItemMapper.deleteBatchIds(param.getIds());
    }

    @Override
    @ReadDataSource
    public CgTestOrderItem childDetail(String id) {
        // 按主键加载子实体
        CgTestOrderItem entity = cgTestOrderItemMapper.selectById(id);
        if (entity == null) {
            // 资源不存在
            throw new BizException(404, "CgTestOrderItem not found");
        }
        return entity;
    }

    @Override
    @ReadDataSource
    public Page<CgTestOrderItem> childPage(CgTestOrderItemPageParam param) {
        // 按外键分页查询子实体
        return cgTestOrderItemMapper.selectPage(new Page<>(param.getCurrent(), param.getSize()),
                Wrappers.<CgTestOrderItem>lambdaQuery()
                        .eq(StringUtils.hasText(param.getOrderId()), CgTestOrderItem::getOrderId, param.getOrderId())
                        .orderByDesc(CgTestOrderItem::getCreatedAt));
    }
}
