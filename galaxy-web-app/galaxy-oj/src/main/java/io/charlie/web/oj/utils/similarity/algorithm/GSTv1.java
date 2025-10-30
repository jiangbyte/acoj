package io.charlie.web.oj.utils.similarity.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangJiangHu
 * @version v1.0
 * @date 30/10/2025
 */
public class GSTv1 {
    /**
     * 贪婪字符串匹配算法实现
     *
     * @param token1         第一个Token序列
     * @param token2         第二个Token序列
     * @param minMatchLength 控制匹配敏感度
     * @return 匹配的Token数量
     */
    public static int greedyStringTiling(List<Integer> token1, List<Integer> token2, int minMatchLength) {
        List<MatchTile> tiles = new ArrayList<>();
        boolean[] matched1 = new boolean[token1.size()];
        boolean[] matched2 = new boolean[token2.size()];

        int maxMatch;
        do {
            maxMatch = minMatchLength;
            List<MatchTile> maxTiles = new ArrayList<>();

            // 寻找所有最大匹配块
            for (int i = 0; i < token1.size(); i++) {
                if (matched1[i]) continue;

                for (int j = 0; j < token2.size(); j++) {
                    if (matched2[j]) continue;

                    int k = 0;
                    while (i + k < token1.size() &&
                            j + k < token2.size() &&
                            !matched1[i + k] &&
                            !matched2[j + k] &&
                            token1.get(i + k).equals(token2.get(j + k))) {
                        k++;
                    }

                    if (k > maxMatch) {
                        maxMatch = k;
                        maxTiles.clear();
                        maxTiles.add(new MatchTile(i, j, k));
                    } else if (k == maxMatch) {
                        maxTiles.add(new MatchTile(i, j, k));
                    }
                }
            }

            // 标记已匹配的块
            for (MatchTile tile : maxTiles) {
                for (int k = 0; k < tile.length; k++) {
                    matched1[tile.start1 + k] = true;
                    matched2[tile.start2 + k] = true;
                }
                tiles.add(tile);
            }
        } while (maxMatch > minMatchLength);

        // 计算总匹配数
        int totalMatches = 0;
        for (MatchTile tile : tiles) {
            totalMatches += tile.length;
        }

        return totalMatches;
    }

}
