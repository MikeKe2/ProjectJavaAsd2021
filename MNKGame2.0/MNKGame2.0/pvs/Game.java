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

        playMove(getMove(move.i, move.j));
        //System.out.println();
        //check();
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

    /**
     * Get numbers of diagonals
     */
    public int getDiagonals() {
        int[][] board = getBoard();
        return board.length + board.length - 1;
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
        int rowIndex;
        int columnIndex;
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

    public void check() {
        int[][] B = getBoard();
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println(B[0][0] + "\t" + B[0][1] + "\t" + B[0][2]);
        System.out.println(B[1][0] + "\t" + B[1][1] + "\t" + B[1][2]);
        System.out.println(B[2][0] + "\t" + B[2][1] + "\t" + B[2][2]);
        System.out.println("Turn" + turn);
    }

    /*public static void printRightToLeftDiagonal(int[][] matrix) {
    int n = matrix.length;

    int j = n - 1;
    for (int i = 0; i < n; ++i) {
        printReverseDiagonal(matrix, i, j);
    }

    int i = n - 1;
    for(j = n - 2; j >= 0; --j) {
       printReverseDiagonal(matrix, i, j);
    }
}*/

    public int[] getAntiDiagonalSquares(int diag) {
        int[][] board = getBoard();
        int i = 0;
        int[] list = new int[getDiagonalSize(diag)];
        System.out.print("List length: " + list.length + "\t Diag: " + diag + "\t i:" + i + "\t ");
        for (int row = diag - 1, column = diag - 1; row >= 0 && column >= 0; --row, --column) {
            System.out.print(board[row][column] + " ");
            list[i] = board[row][column];
            i++;
        }
        System.out.println();

        /*int startRow = Math.max(diag - columns, 0);
        int startCol = Math.min(diag, columns - 1);
        int move = getMove(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            //System.out.println("Diagonal: "+ diag + "\t diagonale size: " + list.length +"\t "+ i + ": element\t Move: " + move);
            list[i] = move;
            move += columns - 1;
        }*/
        return list;
    }

    //example for matrix[2, 2] it will print 9 5 1
    private static void printReverseDiagonal(int[][] matrix, int i, int j) {
        System.out.print(matrix[i][j]);
        for (int row = i - 1, column = j - 1; row >= 0 && column >= 0; --row, --column) {
            System.out.print(" " + matrix[row][column]);
        }
        System.out.println();
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
