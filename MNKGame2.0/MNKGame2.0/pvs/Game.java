package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

import java.util.Arrays;
import java.util.Stack;

public class Game {

    public static final int MAX_SCORE = Integer.MAX_VALUE;
    public static final int DRAW_SCORE = 0;
    public static final int MIN_SCORE = Integer.MIN_VALUE;

    final private int ALPHAP1;
    final private int BETAP1;
    final private int ALPHAP2;
    final private int BETAP2;

    /**
     * Constant representing empty spaces, i.e. "no" player.
     */
    public static final int PLAYER_NONE = 0;
    /**
     * Constant representing the first player.
     */
    public static final int PLAYER_1 = 1;
    /**
     * Constant representing the second player.
     */
    public static final int PLAYER_2 = -PLAYER_1;

    // Instance variables
    private final int M, N, K, size; // M = cols, N = rows, K = win-in a row, size = N * M
    private final MNKCellState[][] board; // M x N grid as 2D array
    private final Stack<MNKCell> history = new Stack<>(); // past piece placements

    private int ply; // number of past piece placements
    private int turn; // current player, winning player (if any)
    private MNKGameState winner;

    public Game(int M, int N, int K) {

        this.M = M;
        this.N = N;
        this.K = K;
        this.turn = PLAYER_1;
        this.size = M * N;

        ALPHAP1 = MIN_SCORE;
        BETAP2 = MAX_SCORE;
        Integer tgv = theoreticalGameValue();
        BETAP1 = tgv == null ? MAX_SCORE : tgv;
        // no m,n,k-game has theoreticalGameValue() == LOSSUTILITY anyway
        ALPHAP2 = BETAP1 == MAX_SCORE ? MIN_SCORE : DRAW_SCORE;

        board = resetBoardState();
        winner = MNKGameState.OPEN;
        ply = 0;
    }

    private MNKCellState[][] resetBoardState() {
        MNKCellState[][] res = new MNKCellState[M][N];
        for (MNKCellState[] row : res)
            Arrays.fill(row, MNKCellState.FREE);
        return res;
    }

    public Game update(MNKCell mnkCell) {
        final int row = mnkCell.i;
        final int column = mnkCell.j;

        board[row][column] = turn == 0 ? MNKCellState.P1 : MNKCellState.P2;
        history.push(mnkCell);

        if (winner == MNKGameState.OPEN && history.size() == size)
            winner = MNKGameState.DRAW;

        return this;
    }

    protected Integer theoreticalGameValue() {
        if (K == 1)
            return MAX_SCORE;
        if (K == 2)
            return size > 2 ? MAX_SCORE : DRAW_SCORE;
        if (K == 3)
            return M >= 4 && N >= 3 || M >= 3 && N >= 4 ? MAX_SCORE : DRAW_SCORE;
        if (K == 4) {
            if (M <= 8 && N == 4 || M == 4 && N <= 8 || M == 5 && N == 5)
                return DRAW_SCORE;
            if (M >= 6 && N >= 5 || M >= 5 && N >= 6 || M == 4 && N >= 30 || M >= 30 && N == 4)
                return MAX_SCORE;
        }
        if (K == 5) {
            if (M <= 6 && N <= 6)
                return DRAW_SCORE;
            if (M == 19 && N == 19)
                return MAX_SCORE;
        }
        if (K >= 8)
            return DRAW_SCORE;
        return null;
    }

    public int initialAlpha(boolean p) {
        return history.empty() ? p ? ALPHAP1 : ALPHAP2 : MIN_SCORE;
    }

    public int initialBeta(boolean p) {
        return history.empty() ? p ? BETAP1 : BETAP2 : MAX_SCORE;
    }

    public int maxDepth() {
        return size - history.size();
    }

    public void doMove(MNKCell move) {
        board[move.i][move.j] = turn == PLAYER_1 ? MNKCellState.P1 : MNKCellState.P2;
        history.push(move);
        ply++;
        winner = calculateWinner(move);
        turn = -turn;
    }

    public void undoMove() {
        MNKCell move = history.pop();
        turn = -turn;
        winner = MNKGameState.OPEN;
        ply--;
        board[move.i][move.j] = MNKCellState.FREE;
    }

    public int getCols() {
        return M;
    }

    public int getRows() {
        return N;
    }

    public int getK() {
        return K;
    }

    public int getDiagonals() {
        return M + N - 1;
    }

    public int getAntiDiagonals() {
        return getDiagonals();
    }

    public MNKCellState[] getRowSquares(int row) {
        MNKCellState[] list = new MNKCellState[M];
        for (int col = 0; col < M; col++)
            list[col] = board[row][col];
        return list;
    }

    public MNKCellState[] getColSquares(int col) {
        MNKCellState[] list = new MNKCellState[N];
        for (int row = 0; row < N; row++)
            list[row] = board[row][col];
        return list;
    }

    // FIXME: 08/02/2022 
    public MNKCellState[] getDiagonalSquares(int diag) {
        MNKCellState[] list = new MNKCellState[getDiagonalSize(diag)];
        int startRow = Math.max(N - 1 - diag, 0);
        int startCol = Math.max(diag - N, 0);
        for (int i = 0; i < list.length; i++) {
            list[i] = board[startRow][startCol];
        }
        return list;
    }

    // FIXME: 08/02/2022
    public MNKCellState[] getAntiDiagonalSquares(int diag) {
        MNKCellState[] list = new MNKCellState[getAntiDiagonalSize(diag)];
        int startRow = Math.max(diag - M, 0);
        int startCol = Math.min(diag, M - 1);
        for (int i = 0; i < list.length; i++) {
            list[i] = board[startRow][startCol];
        }
        return list;
    }

    public int getDiagonalSize(int diag) {
        return getAntiDiagonalSize(diag);
    }

    public int getAntiDiagonalSize(int diag) {
        return min(N + M - 1 - diag, diag + 1, N, M);
    }

    private int min(int i0, int i1, int i2, int i3) {
        return Math.min(Math.min(i0, i1), Math.min(i2, i3));
    }

    public boolean hasWinner() {
        return winner != MNKGameState.OPEN && winner != MNKGameState.DRAW;
    }

    public boolean isGameOver() {
        return hasWinner() || history.size() == size;
    }

    public int getCurrentPlayer() {
        return turn;
    }

    // Private methods
    /* Checks if there is K-in-a-row through the square. */
    private MNKGameState calculateWinner(MNKCell cell) {
        MNKCellState s = cell.state;
        int i = cell.i;
        int j = cell.j;
        int n;

        // Useless pedantic check
        if (s == MNKCellState.FREE) return MNKGameState.OPEN;

        // Horizontal check
        n = 1;
        for (int k = 1; j - k >= 0 && board[i][j - k] == s; k++) n++; // backward check
        for (int k = 1; j + k < N && board[i][j + k] == s; k++) n++; // forward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;


        // Vertical check
        n = 1;
        for (int k = 1; i - k >= 0 && board[i - k][j] == s; k++) n++; // backward check
        for (int k = 1; i + k < M && board[i + k][j] == s; k++) n++; // forward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;


        // Diagonal check
        n = 1;
        for (int k = 1; i - k >= 0 && j - k >= 0 && board[i - k][j - k] == s; k++) n++; // backward check
        for (int k = 1; i + k < M && j + k < N && board[i + k][j + k] == s; k++) n++; // forward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;


        // Anti-diagonal check
        n = 1;
        for (int k = 1; i - k >= 0 && j + k < N && board[i - k][j + k] == s; k++) n++; // backward check
        for (int k = 1; i + k < M && j - k >= 0 && board[i + k][j - k] == s; k++) n++; // backward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;


        return MNKGameState.OPEN;
    }
}
