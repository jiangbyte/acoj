package io.charlie.web.modular.sys.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charlie.web.modular.sys.conversation.entity.SysConversation;
import io.charlie.web.modular.sys.conversation.param.SysConversationPageParam;
import io.charlie.web.modular.sys.conversation.result.SysConversationSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Charlie Zhang
 * @version v1.0
 * @date 2025-10-20
 * @description 大模型对话表 Mapper 接口
 */
@Mapper
public interface SysConversationMapper extends BaseMapper<SysConversation> {

    /**
     * 按 conversation_id 聚合分页。ORM 无法表达 GROUP BY 会话摘要，故用手写 SQL。
     */
    IPage<SysConversationSessionVO> selectSessionPage(Page<SysConversationSessionVO> page,
                                                      @Param("param") SysConversationPageParam param);
}
