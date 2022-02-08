package mtd;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

import java.util.*;

public class Game implements Cloneable {

    private MNKGameState state;
    private ZobristHashing zobristHashing;

    // Player constants
    //Constant representing empty spaces, i.e. "no" player.
    public static final int PLAYER_NONE = 0;
    //Constant representing the first player.
    public static final int PLAYER_1 = 1;
    //Constant representing the second player.
    public static final int PLAYER_2 = -PLAYER_1;


    // Instance variables
    private final int m, n, k, size; // m = cols, n = rows, k = win-in a row
    private final MNKCellState[][] board; // m x n grid as 2D array
    private final Stack<MNKCell> history = new Stack<>(); // past piece placements
    private final MNKCell[] listOfMoves;

    private int ply; // number of past piece placements
    private int turn, winner; // current player, winning player (if any)

    public Game(int turn, int m, int n, int k) {

        this.m = m;
        this.n = n;
        this.k = k;
        this.turn = turn;
        this.size = m * n;

        MnkGameEvaluator evaluator = new MnkGameEvaluator(this);

        board = resetBoardState();
        listOfMoves = generateMoves();
        state = MNKGameState.OPEN;

        ply = 0;
        winner = PLAYER_NONE;

        this.zobristHashing = new ZobristHashing(m, n);

    }


    public Game clone() {
        try {
            Game clone = (Game) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen: we support clone
            throw new InternalError(e.toString());
        }
    }

    private MNKCell[] generateMoves() {
        // FIXME: 31/01/2022 It's basically copied from monke
        MNKCell[] res = new MNKCell[size];
        int firstRow = 0,
                lastRow = m - 1,
                firstColumn = 0,
                lastColumn = n - 1,
                i = size - 1,
                row = firstRow,
                column = firstColumn;

        // Escargot
        while (i >= 0) {
            // Top left to top right
            while (column < lastColumn)
                res[i--] = new MNKCell(row, column++);
            res[i--] = new MNKCell(row++, column);
            if (i < 0)
                break;
            ++firstRow;
            // Top right to bottom right
            while (row < lastRow)
                res[i--] = new MNKCell(row++, column);
            res[i--] = new MNKCell(row, column--);
            if (i < 0)
                break;
            --lastColumn;
            // Bottom right to bottom left
            while (column > firstColumn)
                res[i--] = new MNKCell(row, column--);
            res[i--] = new MNKCell(row--, column);
            if (i < 0)
                break;
            --lastRow;
            // Bottom left to top left
            while (row > firstRow)
                res[i--] = new MNKCell(row--, column);
            res[i--] = new MNKCell(row, column++);
            ++firstColumn;
        }
        return res;
    }

    private MNKCellState[][] resetBoardState() {
        MNKCellState[][] res = new MNKCellState[m][n];
        for (MNKCellState[] row : res)
            Arrays.fill(row, MNKCellState.FREE);
        return res;
    }

    public Game update(MNKCell mnkCell) {
        final int row = mnkCell.i;
        final int column = mnkCell.j;
        final int player = getCurrentPlayer();

        board[row][column] = player == 0 ? MNKCellState.P1 : MNKCellState.P2;
        history.push(mnkCell);

        if (state == MNKGameState.OPEN && history.size() == size)
            state = MNKGameState.DRAW;
        zobristHashing.makeAMove(a, p);
        return this;
    }

    public int maxDepth() {
        return size - history.size();
    }

    // FIXME: 30/01/2022 

    /**
     * public void doMove(int square, boolean checkLegal) {
     * if (checkLegal && !canDoMove(square))
     * throw new IllegalArgumentException("Illegal move.");
     * board[square] = turn;
     * history[ply++] = square;
     * winner = calculateWinner(square);
     * if (ply >= 1)
     * turn = -turn;
     * }
     * <p>
     * // FIXME: 30/01/2022
     * public void undoMove(boolean checkLegal) {
     * if (checkLegal && !canUndoMove())
     * throw new IllegalArgumentException("Cannot undo any moves.");
     * int index = history[ply - 1];
     * turn = -turn;
     * winner = PLAYER_NONE;
     * ply--;
     * board[index] = PLAYER_NONE;
     * }
     */

    public int getLegalMoves() {
        if (winner != PLAYER_NONE)
            return 0;
        return getSquares() - getOccupiedSquares();
    }

    // FIXME: 30/01/2022 
    public boolean canDoMove(int square) {
        return (0 <= square && square < n * m) && (board[square] == PLAYER_NONE)
                && (winner == PLAYER_NONE)
                && (getRow(square) == 0 || board[square - m] != PLAYER_NONE);
    }

    public boolean canUndoMove() {
        return ply > 0;
    }

    public int getCols() {
        return m;
    }

    public int getRows() {
        return n;
    }

    public int getK() {
        return k;
    }

    public int getDiagonals() {
        return m + n - 1;
    }

    public int getAntiDiagonals() {
        return getDiagonals();
    }

    private int getSquares() {
        return m * n;
    }

    public int getOccupiedSquares() {
        return ply;
    }

    public int getSquare(int row, int col) {
        return row * m + col;
    }

    public int getRow(int square) {
        return square / m;
    }

    public int[] getRowSquares(int row) {
        int[] list = new int[m];
        for (int col = 0; col < m; col++)
            list[col] = board[getSquare(row, col)];
        return list;
    }

    public int getCol(int square) {
        return square % m;
    }

    public int[] getColSquares(int col) {
        int[] list = new int[n];
        for (int row = 0; row < n; row++)
            list[row] = board[getSquare(row, col)];
        return list;
    }


    public int getDiagonal(int square) {
        return getCol(square) - getRow(square) + getRows() - 1;
    }

    /**
     * Gets the squares corresponding to the specified diagonal.
     * <p>
     * See {@link #getDiagonal(int)} for details about diagonal indexing,
     * which is what the <code>diag</code> parameter is based on.
     *
     * @param diag Diagonal index
     * @return Array of squares in the specified diagonal
     */
    public int[] getDiagonalSquares(int diag) {
        int[] list = new int[getDiagonalSize(diag)];
        int startRow = Math.max(n - 1 - diag, 0);
        int startCol = Math.max(diag - n, 0);
        int square = getSquare(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = square;
            square += m + 1;
        }
        return list;
    }

    /**
     * Gets the number of squares in the specified diagonal.
     * <p>
     * This is more efficient than calling <code>getDiagonal(int).length</code>
     * if the length is the only attribute desired.
     * <p>
     * See {@link #getDiagonal(int)} for details about diagonal indexing,
     * which is what the <code>diag</code> parameter is based on.
     *
     * @param diag Diagonal index
     * @return Number of squares in the diagonal.
     */
    public int getDiagonalSize(int diag) {
        return getAntiDiagonalSize(diag);
    }

    /**
     * Gets the anti-diagonal corresponding to the specified square.
     * <p>
     * See {@link #getSquare(int, int)} for obtaining the square parameter
     * from a row and column.
     * <p>
     * Anti-diagonals have inversely correlated row and column values.
     * Specifically, the sum of the column and row of squares on a given
     * diagonal is constant.
     * <p>
     * Anti-diagonal indexing assigns the lowest index to the diagonal at the
     * corner with minimum row and minimum column; the highest at the
     * corner with maximum row and maximum column. For example, on the 3x3
     * board, the diagonal indexes are as follows:
     * <pre>
     *     0 1 2 column
     * 0   0 1 2
     * 1   1 2 3
     * 2   2 3 4
     * row</pre>
     * For a given anti-diagonal <i>i</i>, either increasing the column
     * or row by 1 goes to the anti-diagonal <i>i + 1</i>; either decreasing the
     * column or row by 1 goes to the anti-diagonal <i>i - 1</i>.
     *
     * @param square Square index
     * @return Anti-diagonal the square occupies
     */
    public int getAntiDiagonal(int square) {
        return getCol(square) + getRow(square);
    }

    /**
     * Gets the squares corresponding to the specified anti-diagonal.
     * <p>
     * See {@link #getAntiDiagonal(int)} for details about anti-diagonal
     * indexing, which is what the <code>diag</code> parameter is based on.
     *
     * @param diag Anti-diagonal index
     * @return Array of squares in the specified anti-diagonal
     */
    public int[] getAntiDiagonalSquares(int diag) {
        int[] list = new int[getAntiDiagonalSize(diag)];
        int startRow = Math.max(diag - m, 0);
        int startCol = Math.min(diag, m - 1);
        int square = getSquare(startRow, startCol);
        for (int i = 0; i < list.length; i++) {
            list[i] = square;
            square += m - 1;
        }
        return list;
    }

    /**
     * Gets the number of squares in the specified anti-diagonal.
     * <p>
     * This is more efficient than calling
     * <code>getAntiDiagonal(int).length</code>
     * if the length is the only attribute desired.
     * <p>
     * See {@link #getAntiDiagonal(int)} for details about anti-diagonal
     * indexing, which is what the <code>diag</code> parameter is based on.
     *
     * @param diag Diagonal index
     * @return Number of squares in the diagonal.
     */
    public int getAntiDiagonalSize(int diag) {
        return min(n + m - 1 - diag, diag + 1, n, m);
    }

    private int min(int i0, int i1, int i2, int i3) {
        return Math.min(Math.min(i0, i1), Math.min(i2, i3));
    }

    /**
     * Gets the piece at the specified square.
     * <p>
     * See {@link #getSquare(int, int)} for details on how the square
     * parameter is related to row and column.
     *
     * @param square Square index
     * @return Constant representing the the player whose piece is on the
     * square, or {@link #PLAYER_NONE} otherwise.
     */
    public int getPiece(int square) {
        return board[square];
    }

    /**
     * Gets the moves played in the game.
     * <p>
     * Note the returned array is a copy. Modifying it does not changes the
     * game state.
     *
     * @return An array containing the squares of all the moves played, in
     * order from first to latest.
     */
    public int[] getHistory() {
        int[] historyTrimmed = new int[ply];
        System.arraycopy(history, 0, historyTrimmed, 0, ply);
        return historyTrimmed;
    }

    /**
     * Gets the move played in the game on after <code>ply</code> plies.
     * <p>
     * See {@link #getElapsedPly()} for a description of a ply.
     *
     * @param ply Number of plies before the desired move
     * @return Square index of the move played after <code>ply</code>
     * moves into the game
     */
    public int getHistory(int ply) {
        if (ply < 0 || ply >= history.length)
            throw new IllegalArgumentException("Illegal history move access.");
        return history[ply];
    }

    /**
     * Gets a two-dimensional array representation of the board.
     *
     * @return two-dimension array in row major order representing the board
     */
    public int[][] getBoard() {
        int[][] board2d = new int[n][m];
        for (int row = 0; row < n; row++)
            for (int col = 0; col < m; col++)
                board2d[row][col] = board[getSquare(row, col)];
        return board2d;
    }

    /**
     * Gets the number of ply that have elapsed since the start of the game.
     * <p>
     * A ply is an atomic "move" by a player involving the placement of
     * exactly one piece. This is the same as a player's entire move if
     * they place only one piece per turn.
     * <p>
     * In other words, the number of ply elapsed is the same as the number
     * of pieces placed on the board.
     *
     * @return Number of ply since start of game
     */
    public int getElapsedPly() {
        return ply;
    }

    /**
     * Gets the number of turns that elapse after <code>totalPly</code> ply.
     * <p>
     * A turn consists of all the pieces a player plays in a row before their
     * opponent gets the move. This is different the a ply if player get
     * to place multiple pieces on their turns. See {@link #getElapsedPly()}
     * for a description of ply.
     *
     * @param totalPly Total number of elapsed ply
     * @return Number of elapsed turns after <code>totalPly</code>
     * elapsed ply from beginning of a game
     */
    public int getElapsedTurns(int totalPly) {
        if (totalPly < q)
            return 0;
        return 1 + (totalPly - q) % p;
    }

    /**
     * Gets the number of turns that have elapsed in the game.
     *
     * @return Number of elapsed turns since the beginning of the game
     * @see #getElapsedTurns(int)
     */
    public int getElapsedTurns() {
        return getElapsedTurns(ply);
    }

    /**
     * Gets the number of pieces left to place for the turn of the current
     * player, after <code>totalPly</code> ply.
     * <p>
     * See {@link #getElapsedTurns(int)} for a description of turns, which
     * are not necessarily the same as ply. See {@link #getElapsedPly()}
     * for a description of ply.
     *
     * @param totalPly Total number of elapsed ply
     * @return Number of "moves" (pieces to place) left for the
     * current player (after <code>totalPly</code> ply has
     * elapsed) to play
     */
    public int getTurnRemainingMoves(int totalPly) {
        if (totalPly < q)
            return q - totalPly;
        return p - (totalPly - q) % p;
    }

    /**
     * Gets the number of pieces left to place for the current player's turn.
     *
     * @param totalPly Total number of elapsed ply
     * @return Number of "moves" (pieces to place) left for the
     * current player to play
     * @see #getTurnRemainingMoves(int)
     */
    public int getTurnRemainingMoves() {
        return getTurnRemainingMoves(ply);
    }

    /**
     * Gets the number of psuedo-legal moves the current player can play.
     * <p>
     * A pseudolegal move is a location where the player <i>could</i> play.
     * Whether it's legal depends on whether the game is over already or not.
     *
     * @return Number of moves where the current player could play, assuming
     *            the game is not over.
     */

    /**
     * Gets a generator of all legal moves of the current player.
     * <p>
     *
     * @return An Iterable for iterating through the legal moves of the
     * current player.
     */
    public Iterable<Integer> generateLegalMoves() {
        if (winner != PLAYER_NONE)
            return Collections.emptyList();
        return genFullPseudolegalMoves();
    }

    public Iterable<Integer> generatePseudoLegalMoves() {
        return genFullPseudolegalMoves();
    }

    public int getPseudoLegalMoves() {
        return getSquares() - getOccupiedSquares();
    }

    public boolean hasWinner() {
        return winner != PLAYER_NONE;
    }

    public boolean isGameOver() {
        return hasWinner() || (getOccupiedSquares() == getSquares());
    }

    public int getCurrentPlayer() {
        return turn;
    }

    /**
     * Gets a generator of all legal moves in "inside-out" order.
     * It is considerably slower that {@link #generateLegalMoves()},
     * but the order that the moves are produced may be preferable in some
     * cases.
     *
     * @return An Iterable for iterating through the legal moves
     * of the current player, in an order that roughly starts
     * from the innermost squares and proceeds out
     */
    public Iterable<Integer> generateInOutLegalMoves() {
        if (winner != PLAYER_NONE)
            return Collections.emptyList();
        return genInOutFullPseudolegalMvs();
    }


    // Private methods
    /* Checks if there is k-in-a-row through the square. */
    private int calculateWinner(int square) {
        int row = getRow(square);
        int col = getCol(square);
        int[][] dirs = {{-1, 1}, {-m, m}, {-m - 1, m + 1}, {-m + 1, m - 1}};
        int[][] lens = {{col, m - 1 - col}, {row, n - 1 - row},
                {Math.min(col, row), Math.min(m - 1 - col, n - 1 - row)},
                {Math.min(m - 1 - col, row), Math.min(col, n - 1 - row)}};
        for (int i0 = 0; i0 < 4; i0++) {
            int consecutive = 1;
            for (int i1 = 0; i1 < 2; i1++) {
                for (int index = square, j = 0; j < lens[i0][i1]; j++) {
                    if (board[(index += dirs[i0][i1])] != turn)
                        break;
                    if (++consecutive >= k)
                        return turn;
                }
            }
        }
        return PLAYER_NONE;
    }

    /* Gets a generator for all empty squares. */
    private Iterable<Integer> genFullPseudolegalMoves() {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        while (index < n * m) {
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
        };
    }


    private Iterable<Integer> genInOutFullPseudolegalMvs() {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    int index = (m + 1) * Math.min((n - 1) / 2, (m - 1) / 2);
                    int start = index;

                    boolean shortSideEven = (m > n ? n & 1 : m & 1) == 0;
                    int col = 0, ncol = Math.max(0, m - n) + (shortSideEven ? 1 : 0);
                    int row = 0, nrow = Math.max(0, n - m) + (shortSideEven ? 1 : 0);

                    @Override
                    public boolean hasNext() {
                        while (true) {
                            if (row > nrow) {
                                if (start <= 0)
                                    return false;
                                index = (start -= m + 1);
                                col = 0;
                                row = 0;
                                ncol += 2;
                                nrow += 2;
                                continue;
                            }
                            if (col > ncol) {
                                index += m - 1 - ncol;
                                col = 0;
                                row++;
                                continue;
                            }
                            if (board[index] == PLAYER_NONE)
                                return true;
                            goNext();
                        }
                    }

                    @Override
                    public Integer next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        int ret = index;
                        goNext();
                        return ret;
                    }

                    private void goNext() {
                        if (col == 0 && row != 0 && row != nrow && ncol != 0) {
                            index += ncol;
                            col += ncol;
                        } else {
                            index++;
                            col++;
                        }
                    }
                };
            }
        };
    }
}
