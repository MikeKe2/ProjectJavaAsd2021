package pvs;

import mnkgame.MNKCell;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Game {

    public static final int MAX_SCORE = 1 << 30;
    public static final int MIN_SCORE = -MAX_SCORE;

    // Constant representing empty spaces, i.e. "no" player.
    public static final int PLAYER_NONE = 0;
    // Constant representing the first player.
    public static final int PLAYER_1 = 1;
    // Constant representing the second player.
    public static final int PLAYER_2 = -PLAYER_1;


    // Instance variables
    private final int columns, rows, K, size; // columns = cols, rows = rows, K = win-in a row, size = rows * columns
    private final int[] board; // columns x rows grid as 2D array
    private final int[] history; // past piece placements

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

    public int maxDepth() {
        return size - ply;
    }

    public int getSquare(int row, int col) {
        return row * rows + col;
    }

    public int getSquares() {
        return size;
    }

    public int getRow(int square) {
        return square / columns;
    }

    public int getCol(int square) {
        return square % columns;
    }

    public void doMove(MNKCell move) {
        doMove(getSquare(move.i, move.j));
    }

    public void doMove(int square) {
        board[square] = turn;
        history[ply++] = square;
        winner = calculateWinner(square);
        turn = -turn;
    }

    public void undoMove() {
        int index = history[ply - 1];
        turn = -turn;
        winner = PLAYER_NONE;
        ply--;
        board[index] = PLAYER_NONE;
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

    public int getDiagonals() {
        return columns + rows - 1;
    }

    public int getAntiDiagonals() {
        return getDiagonals();
    }

    public int[] getRowSquares(int row) {
        int[] list = new int[columns];
        for (int col = 0; col < columns; col++)
            list[col] = board[getSquare(row, col)];
        return list;
    }

    public int[] getColSquares(int col) {
        int[] list = new int[rows];
        for (int row = 0; row < rows; row++)
            list[row] = board[getSquare(row, col)];
        return list;
    }

    //TODO:little bug inside here, it needs to be fixed asap
    public int[] getDiagonalSquares(int diag) {
        int[] list = new int[getDiagonalSize(diag)];
        int startRow = Math.max(rows - 1 - diag, 0);
        int startCol = Math.max(diag - rows, 0);
        int square = getSquare(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = square;
            square += columns + 1;
        }
        return list;
    }
    //TODO:same as top one
    public int[] getAntiDiagonalSquares(int diag) {
        int[] list = new int[getAntiDiagonalSize(diag)];
        int startRow = Math.max(diag - columns, 0);
        int startCol = Math.min(diag, columns - 1);
        int square = getSquare(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = square;
            square += columns - 1;
        }
        return list;
    }

    public int getDiagonalSize(int diag) {
        return getAntiDiagonalSize(diag);
    }

    public int getAntiDiagonalSize(int diag) {
        return min(rows + columns - 1 - diag, diag + 1, rows, columns);
    }

    private int min(int i0, int i1, int i2, int i3) {
        return Math.min(Math.min(i0, i1), Math.min(i2, i3));
    }

    public boolean hasWinner() {
        return winner != PLAYER_NONE;
    }

    public boolean isGameOver() {
        return hasWinner() || ply == size;
    }

    public int getElapsedPly() {
        return ply;
    }

    public int getHistory(int ply) {
        if (ply < 0 || ply >= history.length)
            throw new IllegalArgumentException("Illegal history move access.");
        return history[ply];
    }

    public int getPseudoLegalMoves() {
        return size - ply;
    }

    public int getCurrentPlayer() {
        return turn;
    }

    // Private methods
    /* Checks if there is K-in-a-row through the square. */
    private int calculateWinner(int square) {
        int row = getRow(square);
        int col = getCol(square);
        int[][] dirs = {{-1, 1}, {-columns, columns}, {-columns - 1, columns + 1}, {-columns + 1, columns - 1}};
        int[][] lens = {{col, columns - 1 - col}, {row, rows - 1 - row},
                {Math.min(col, row), Math.min(columns - 1 - col, rows - 1 - row)},
                {Math.min(columns - 1 - col, row), Math.min(col, rows - 1 - row)}};
        for (int i0 = 0; i0 < 4; i0++) {
            int consecutive = 1;
            for (int i1 = 0; i1 < 2; i1++) {
                for (int index = square, j = 0; j < lens[i0][i1]; j++) {
                    if (board[(index += dirs[i0][i1])] != turn)
                        break;
                    if (++consecutive >= K)
                        return turn;
                }
            }
        }
        return PLAYER_NONE;
    }

    public Iterable<Integer> generateMoves() {
        return () -> new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                while (index < rows * columns) {
                    if (board[index] == PLAYER_NONE)
                        return true;
                    index++;
                }
                return false;
            }

            @Override
            public Integer next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return index++;
            }
        };
    }
}
