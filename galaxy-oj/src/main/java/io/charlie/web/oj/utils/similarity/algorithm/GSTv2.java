package io.charlie.web.oj.utils.similarity.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 */
public class GSTv2 {
    public static int greedyStringTilingFinal(List<Integer> token1, List<Integer> token2, int minMatchLength) {
        if (minMatchLength < 1) minMatchLength = 1;

        if (token1 == null || token2 == null) {
            return 0;
        }
        if (token1.isEmpty() || token2.isEmpty()) {
            return 0;
        }

        List<MatchTile> tiles = new ArrayList<>();
        boolean[] matched1 = new boolean[token1.size()];
        boolean[] matched2 = new boolean[token2.size()];

        // 构建token2的索引，但只对未匹配的token建立索引
        Map<Integer, List<Integer>> token2Index = createInitialIndex(token2);

        int maxMatch;
        int iterations = 0;
        do {
            maxMatch = minMatchLength;
            List<MatchTile> maxTiles = new ArrayList<>();

            // 只遍历token1中未匹配的区域
            for (int i = 0; i < token1.size(); i++) {
                if (matched1[i]) continue;

                Integer token = token1.get(i);
                List<Integer> positions = token2Index.get(token);
                if (positions == null || positions.isEmpty()) continue;

                // 对每个可能的位置进行快速检查
                for (int j : new ArrayList<>(positions)) { // 复制避免并发修改
                    if (matched2[j]) {
                        // 清理已匹配的位置
                        positions.remove((Integer) j);
                        continue;
                    }

                    // 快速长度检查：如果剩余长度不足，跳过
                    if (Math.min(token1.size() - i, token2.size() - j) < maxMatch &&
                            Math.min(token1.size() - i, token2.size() - j) < minMatchLength) {
                        continue;
                    }

                    int matchLen = calculateMatchLength(token1, token2, i, j, matched1, matched2);

                    if (matchLen > maxMatch) {
                        maxMatch = matchLen;
                        maxTiles.clear();
                        maxTiles.add(new MatchTile(i, j, matchLen));
                    } else if (matchLen == maxMatch && matchLen >= minMatchLength) {
                        maxTiles.add(new MatchTile(i, j, matchLen));
                    }
                }
            }

            // 处理找到的最大匹配块
            for (MatchTile tile : maxTiles) {
                markMatched(matched1, tile.start1, tile.length);
                markMatched(matched2, tile.start2, tile.length);
                updateIndex(token2Index, token2, tile.start2, tile.length);
                tiles.add(tile);
            }

            iterations++;
            // 安全限制，避免无限循环
            if (iterations > 1000) break;

        } while (maxMatch > minMatchLength);

        return tiles.stream().mapToInt(tile -> tile.length).sum();
    }

    private static Map<Integer, List<Integer>> createInitialIndex(List<Integer> tokens) {
        Map<Integer, List<Integer>> index = new HashMap<>();
        for (int i = 0; i < tokens.size(); i++) {
            index.computeIfAbsent(tokens.get(i), k -> new ArrayList<>()).add(i);
        }
        return index;
    }

    private static void updateIndex(Map<Integer, List<Integer>> index, List<Integer> tokens, int start, int length) {
        for (int i = 0; i < length; i++) {
            int pos = start + i;
            Integer token = tokens.get(pos);
            List<Integer> positions = index.get(token);
            if (positions != null) {
                positions.remove((Integer) pos);
                if (positions.isEmpty()) {
                    index.remove(token);
                }
            }
        }
    }

    private static int calculateMatchLength(List<Integer> token1, List<Integer> token2,
                                            int i, int j, boolean[] matched1, boolean[] matched2) {
        int k = 0;
        int maxK = Math.min(token1.size() - i, token2.size() - j);

        // 快速失败：如果第一个token就不匹配，直接返回0
        if (!token1.get(i).equals(token2.get(j))) {
            return 0;
        }

        while (k < maxK &&
                !matched1[i + k] &&
                !matched2[j + k] &&
                token1.get(i + k).equals(token2.get(j + k))) {
            k++;
        }
        return k;
    }

    private static void markMatched(boolean[] matched, int start, int length) {
        for (int i = 0; i < length; i++) {
            matched[start + i] = true;
        }
    }
}
