package pvs;

import mnkgame.MNKCell;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Game implements Cloneable {

    public static final int MAX_SCORE = 1 << 14;
    public static final int MIN_SCORE = -MAX_SCORE;

    // Constant representing empty spaces, i.e. "no" player.
    public static final int PLAYER_NONE = 0;
    // Constant representing the first player.
    public static final int PLAYER_1 = 1;
    // Constant representing the second player.
    public static final int PLAYER_2 = -PLAYER_1;

    // Instance variables
    private final int columns, rows, K, size;
    private int[] board;
    private int[] history;

    private int ply; // number of past piece placements
    private int turn; // current player
    private int winner; // winning player
    private int itemsInDiagonal;

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

    @Override
    public Game clone() {
        try {
            Game copy = (Game) super.clone();
            copy.board = board.clone();
            System.arraycopy(board, 0, copy.board, 0, board.length);
            copy.history = history.clone();
            System.arraycopy(history, 0, copy.history, 0, history.length);
            copy.turn = turn;
            copy.ply = ply;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
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

    public int getSize() {
        return size;
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

    public int getRow(int move) {
        return move / columns;
    }

    public int getCol(int move) {
        return move % columns;
    }

    public int getCurrentPlayer() {
        return turn;
    }

    public int getDiagonals() {
        return rows + rows - 1;
    }

    public void playMove(MNKCell move) {
        playMove(getMove(move.i, move.j));
    }

    public void playMove(int move) {
        board[move] = turn;
        history[ply++] = move;
        winner = isWinningCell(getRow(move), getCol(move));
        turn = -turn;
    }

    public void unPlayMove() {
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

    public int getDiagonalSize(int diag) {
        return min(rows + columns - 1 - diag, diag + 1, rows, columns);
    }

    private int min(int i0, int i1, int i2, int i3) {
        return Math.min(Math.min(i0, i1), Math.min(i2, i3));
    }

    public void resetItemsInDiagonal() {
        itemsInDiagonal = 0;
    }

    public int[] getDiagonalSquares(int diag, int midpoint) {
        int[] list = new int[getDiagonalSize(diag - 1)];
        int[][] board = getBoard();
        int rowIndex, columnIndex;
        if (diag <= midpoint) {
            itemsInDiagonal++;
            for (int j = 0; j < itemsInDiagonal; j++) {
                rowIndex = (diag - j) - 1;
                columnIndex = j;
                list[j] = board[rowIndex][columnIndex];
            }
        } else {
            itemsInDiagonal--;
            for (int j = 0; j < itemsInDiagonal; j++) {
                rowIndex = (board.length - 1) - j;
                columnIndex = (diag - board.length) + j;
                list[j] = board[rowIndex][columnIndex];
            }
        }
        return list;
    }

    public int[] getAntiDiagonalSquares(int counter, int i, int j) {
        int[] list = new int[getDiagonalSize(counter - 1)];
        int[][] board = getBoard();
        int k = 0;

        list[k] = board[i][j];
        for (int row = i - 1, column = j - 1; row >= 0 && column >= 0; --row, --column) {
            k++;
            list[k] = board[row][column];
        }
        return list;
    }

    public int getWinner() {
        return winner;
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
