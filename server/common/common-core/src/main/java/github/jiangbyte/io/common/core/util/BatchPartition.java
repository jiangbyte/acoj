package github.jiangbyte.io.common.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 集合分批工具：去重后按固定大小切分为子列表，便于批量 SQL / 外部调用。
 *
 * Author: Charlie
 */
public final class BatchPartition {

    public static final int DEFAULT_SIZE = 500;

    private BatchPartition() {
    }

    /** 按默认批次大小（500）分片。 */
    public static <T> List<List<T>> partition(Collection<T> items) {
        return partition(items, DEFAULT_SIZE);
    }

    /** 按指定批次大小去重并分片。 */
    public static <T> List<List<T>> partition(Collection<T> items, int batchSize) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<T> distinct = new ArrayList<>(new LinkedHashSet<>(items));
        List<List<T>> batches = new ArrayList<>((distinct.size() + batchSize - 1) / batchSize);
        for (int i = 0; i < distinct.size(); i += batchSize) {
            batches.add(List.copyOf(distinct.subList(i, Math.min(i + batchSize, distinct.size()))));
        }
        return batches;
    }
}
