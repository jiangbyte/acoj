package io.charlie.web.oj.utils.similarity.algorithm;

public class MatchTile {
    public int start1;
    public int start2;
    public int length;

    public MatchTile(int start1, int start2, int length) {
        this.start1 = start1;
        this.start2 = start2;
        this.length = length;
    }
}