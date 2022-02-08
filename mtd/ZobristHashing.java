package mtd;

import mnkgame.MNKCell;

import java.util.Random;

public class ZobristHashing {

    private int[][][] zobristArray;
    private int[] hashCodeCandidates;

    /**
     * Seed to be used for the generated pseudo-random sequence.
     */
    final public static long SEED = 0l;

    public ZobristHashing(int m, int n) {
        zobristArray = zobristFillArray(m, n);
    }

    protected int[][][] zobristFillArray(int m, int n) {
        int[][][] board = new int[m][n][2];
        Random r = new Random(SEED);
        for (int[][] grid : board)
            for (int[] row : grid)
                for (int i = 0; i < 2; ++i)
                    row[i] = r.nextInt();
        return board;
    }

    public int hash(MNKCell cell, int player){
        int h = 0;
        for (int[][] grid : zobristArray)
            for (int[] row : grid)
                if(row[player] != 0){
                    h ^= row[player];
                }
        return h;
    }

}
