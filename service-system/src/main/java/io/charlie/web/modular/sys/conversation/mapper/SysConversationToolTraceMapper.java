package io.charlie.web.modular.sys.conversation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.charlie.web.modular.sys.conversation.entity.SysConversationToolTrace;
import org.apache.ibatis.annotations.Mapper;

/**
 * LLM 工具调用轨迹 Mapper。
 */
@Mapper
public interface SysConversationToolTraceMapper extends BaseMapper<SysConversationToolTrace> {
}
