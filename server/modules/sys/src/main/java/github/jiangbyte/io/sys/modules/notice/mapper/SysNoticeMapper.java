package github.jiangbyte.io.sys.modules.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.jiangbyte.io.sys.modules.notice.entity.SysNotice;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 公告/通知表 {@code sys_notice} 的 Mapper：基础 CRUD，以及未读统计与可见已发布 ID 查询。
 *
 * Author: Charlie
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    /**
     * 统计对指定账号可见且未读的已发布消息数量。
     */
    @Lang(XMLLanguageDriver.class)
    @SelectProvider(type = SysNoticeSqlProvider.class, method = "countUnread")
    int countUnread(
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("kind") String kind,
            @Param("now") OffsetDateTime now);

    /**
     * 列出对指定账号可见的已发布消息 ID（分页）。
     */
    @Lang(XMLLanguageDriver.class)
    @SelectProvider(type = SysNoticeSqlProvider.class, method = "listVisiblePublishedIdsPage")
    List<String> listVisiblePublishedIdsPage(
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("kind") String kind,
            @Param("now") OffsetDateTime now,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Lang(XMLLanguageDriver.class)
    @SelectProvider(type = SysNoticeSqlProvider.class, method = "listVisiblePublishedIds")
    List<String> listVisiblePublishedIds(
            @Param("accountType") String accountType,
            @Param("accountId") String accountId,
            @Param("kind") String kind,
            @Param("now") OffsetDateTime now);
}
