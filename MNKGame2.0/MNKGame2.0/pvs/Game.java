package pvs;

import mnkgame.MNKCell;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Game {

    public static final int MAX_SCORE = 1 << 20;
    public static final int MIN_SCORE = -MAX_SCORE;

    // Constant representing empty spaces, i.e. "no" player.
    public static final int PLAYER_NONE = 0;
    // Constant representing the first player.
    public static final int PLAYER_1 = 1;
    // Constant representing the second player.
    public static final int PLAYER_2 = -PLAYER_1;


    // Instance variables
    private final int columns, rows, K, size;
    private final int[] board;
    private final int[] history;

    private int ply; // number of past piece placements
    private int turn; // current player, winning player (if any)
    private int winner;

    public Game(int row, int column, int K) {
        this.rows = row;
        this.columns = column;
        this.K = K;

        this.turn = PLAYER_1;
        this.size = columns * rows;

        history = new int[size];
        board = new int[size];

        winner = PLAYER_NONE;
        ply = 0;
    }

    public int getCols() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public int getK() {
        return K;
    }

    public int getNumberOfMovesPlayed() {
        return ply;
    }

    public int maxDepth() {
        return size - ply;
    }

    public int getMove(int row, int col) {
        return row * rows + col;
    }

    public int getSize() {
        return size;
    }

    public int getRow(int move) {
        return move / columns;
    }

    public int getCol(int move) {
        return move % columns;
    }

    public void playMove(MNKCell move) {
        int play = getMove(move.i, move.j);
        //playMove(getMove(move.i, move.j));
        board[play] = turn;
        history[ply++] = play;
    }

    public void playMove(int move) {
        board[move] = turn;
        history[ply++] = move;
        winner = isWinningCell(getRow(move), getCol(move));
        turn = -turn;
    }

    public void unplayMove() {
        int index = history[ply - 1];
        turn = -turn;
        winner = PLAYER_NONE;
        ply--;
        board[index] = PLAYER_NONE;
    }

    public int[] getCellsForRow(int row) {
        int[] list = new int[columns];
        for (int col = 0; col < columns; col++)
            list[col] = board[getMove(row, col)];
        return list;
    }

    public int[] getCellsForColumns(int col) {
        int[] list = new int[rows];
        for (int row = 0; row < rows; row++)
            list[row] = board[getMove(row, col)];
        return list;
    }

    public int getDiagonals() {
        return columns + rows - 1;
    }

    public int getDiagonalSize(int diag) {
        return min(rows + columns - 1 - diag, diag + 1, rows, columns);
    }

    private int min(int i0, int i1, int i2, int i3) {
        return Math.min(Math.min(i0, i1), Math.min(i2, i3));
    }

    public int[] getDiagonalSquares(int diag) {
        int[] list = new int[getDiagonalSize(diag)];
        int startCol = Math.max(rows - 1 - diag, 0);
        int startRow = Math.max(diag - rows, 0);
        int move = getMove(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = move;
            //System.out.println("Diagonal: " + diag + "\t Move: " + move + "\t Cells: " + getRow(move) + "-" + getCol(move));
            move += columns + 1;
        }
        return list;
    }

    public int[] getAntiDiagonalSquares(int diag) {
        int[] list = new int[getDiagonalSize(diag)];
        int startRow = Math.max(diag - columns, 0);
        int startCol = Math.min(diag, columns - 1);
        int move = getMove(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = move;
            move += columns - 1;
        }
        return list;
    }

    public int getWinner() {
        return winner;
    }

    public boolean hasWinner() {
        return winner != PLAYER_NONE;
    }

    public boolean isGameOver() {
        return hasWinner() || ply == size;
    }

    public int getHistory(int ply) {
        if (ply < 0 || ply >= history.length) throw new IllegalArgumentException("Illegal history move access.");
        return history[ply];
    }

    public int[][] getBoard() {
        int[][] board2d = new int[rows][columns];
        for (int row = 0; row < rows; row++)
            for (int col = 0; col < columns; col++)
                board2d[row][col] = board[getMove(row, col)];
        return board2d;
    }

    private int isWinningCell(int i, int j) {
        int[][] B = getBoard();
        int s = B[i][j];
        int n;

        // Useless pedantic check
        if (s == PLAYER_NONE) return PLAYER_NONE;

        // Horizontal check
        n = 1;
        for (int k = 1; j - k >= 0 && B[i][j - k] == s; k++) n++; // backward check
        for (int k = 1; j + k < rows && B[i][j + k] == s; k++) n++; // forward check
        if (n >= K) return turn;

        // Vertical check
        n = 1;
        for (int k = 1; i - k >= 0 && B[i - k][j] == s; k++) n++; // backward check
        for (int k = 1; i + k < columns && B[i + k][j] == s; k++) n++; // forward check
        if (n >= K) return turn;


        // Diagonal check
        n = 1;
        for (int k = 1; i - k >= 0 && j - k >= 0 && B[i - k][j - k] == s; k++) n++; // backward check
        for (int k = 1; i + k < columns && j + k < rows && B[i + k][j + k] == s; k++) n++; // forward check
        if (n >= K) return turn;

        // Anti-diagonal check
        n = 1;
        for (int k = 1; i - k >= 0 && j + k < rows && B[i - k][j + k] == s; k++) n++; // backward check
        for (int k = 1; i + k < columns && j - k >= 0 && B[i + k][j - k] == s; k++) n++; // backward check
        if (n >= K) return turn;

        return PLAYER_NONE;
    }


    public int getRemainingMoves() {
        return size - ply;
    }

    public int getCurrentPlayer() {
        return turn;
    }

    public Iterable<Integer> generateMoves() {
        return () -> new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                while (index < size) {
                    if (board[index] == PLAYER_NONE) return true;
                    index++;
                }
                return false;
            }

            @Override
            public Integer next() {
                if (!hasNext()) throw new NoSuchElementException();
                return index++;
            }
        };
    }
}
