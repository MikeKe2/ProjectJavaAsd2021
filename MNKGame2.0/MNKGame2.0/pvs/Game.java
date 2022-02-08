package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

import java.util.Arrays;
import java.util.Stack;

public class Game {

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

    public Game(int turn, int M, int N, int K) {

        this.M = M;
        this.N = N;
        this.K = K;
        this.turn = turn;
        this.size = M * N;

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

    public int maxDepth() {
        return size - history.size();
    }

    public boolean canDoMove(MNKCell square) {
        return board[square.i][square.j] == MNKCellState.FREE;
    }

    // FIXME: 05/02/2022 They need to be fixed
    public void doMove(MNKCell move, boolean checkLegal) {
        if (checkLegal && !canDoMove(move))
            throw new IllegalArgumentException("Illegal move.");

        board[move.i][move.j] = MNKCellState.P1;
        history.push(move);
        ply++;

        winner = calculateWinner(move);
        if (ply >= 1)
            turn = -turn;
    }

    public void undoMove(boolean checkLegal) {
        if (checkLegal && !canUndoMove())
            throw new IllegalArgumentException("Cannot undo any moves.");

        MNKCell move = history.pop();

        if (ply >= 1)
            turn = -turn;

        winner = MNKGameState.OPEN;
        ply--;
        board[move.i][move.j] = MNKCellState.FREE;
    }

    public boolean canUndoMove() {
        return ply > 0;
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

    public int getSquare(int row, int col) {
        return row * M + col;
    }

    public int getRow(int square) {
        return square / M;
    }

    public int[] getRowSquares(int row) {
        int[] list = new int[M];
        for (int col = 0; col < M; col++)
            list[col] = board[row][col] == MNKCellState.P1 ? 1 : -1;
        return list;
    }

    public int getCol(int square) {
        return square % M;
    }

    public int[] getColSquares(int col) {
        int[] list = new int[N];
        for (int row = 0; row < N; row++)
            list[row] = board[row][col] == MNKCellState.P1 ? 1 : -1;
        return list;
    }

    public int[] getDiagonalSquares(int diag) {
        int[] list = new int[getDiagonalSize(diag)];
        int startRow = Math.max(N - 1 - diag, 0);
        int startCol = Math.max(diag - N, 0);
        int square = getSquare(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = square;
            square += M + 1;
        }
        return list;
    }

    public int getDiagonalSize(int diag) {
        return getAntiDiagonalSize(diag);
    }

    public int[] getAntiDiagonalSquares(int diag) {
        int[] list = new int[getAntiDiagonalSize(diag)];
        int startRow = Math.max(diag - M, 0);
        int startCol = Math.min(diag, M - 1);
        int square = getSquare(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = square;
            square += M - 1;
        }
        return list;
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
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;;

        // Vertical check
        n = 1;
        for (int k = 1; i - k >= 0 && board[i - k][j] == s; k++) n++; // backward check
        for (int k = 1; i + k < M && board[i + k][j] == s; k++) n++; // forward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;;


        // Diagonal check
        n = 1;
        for (int k = 1; i - k >= 0 && j - k >= 0 && board[i - k][j - k] == s; k++) n++; // backward check
        for (int k = 1; i + k < M && j + k < N && board[i + k][j + k] == s; k++) n++; // forward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;;

        // Anti-diagonal check
        n = 1;
        for (int k = 1; i - k >= 0 && j + k < N && board[i - k][j + k] == s; k++) n++; // backward check
        for (int k = 1; i + k < M && j - k >= 0 && board[i + k][j - k] == s; k++) n++; // backward check
        if (n >= K) return turn == PLAYER_1 ? MNKGameState.WINP1 : MNKGameState.WINP2;;

        return MNKGameState.OPEN;
    }
}
