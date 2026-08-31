package github.jiangbyte.io.oj.modules.submission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.oj.modules.submission.entity.OjSubmission;
import org.apache.ibatis.annotations.Mapper;

/**
 * OJ 提交 Mapper。
 *
 * Author: Charlie
 */
@Mapper
public interface OjSubmissionMapper extends BaseMapper<OjSubmission> {
}
