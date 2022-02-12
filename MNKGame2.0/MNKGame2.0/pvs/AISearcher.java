package pvs;

import mnkgame.MNKCell;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class AISearcher {

    public record Result(int score, List<Integer> pv) {

        public int getScore() {
            return score;
        }

        public List<Integer> getPrincipleVariation() {
            return pv;
        }

        public int getPrincipleVariationMove() {
            return pv.get(0);
        }

    }

    private static class WeightedMove {
        Integer move;
        int score;

        public WeightedMove(Integer move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    private Game game;
    protected int[] weights;
    private int lastPly;
    private long startTime;
    final private long timeLimit;

    public AISearcher(Game game, long timeLimit) {
        this.game = game;
        this.timeLimit = timeLimit;
        this.weights = new int[game.getSize()];
        for (int i = 0; i < weights.length; i++) {
            int top = game.getRow(i);
            int bottom = game.getRows() - 1 - top;
            int left = game.getCol(i);
            int right = game.getCols() - 1 - left;
            weights[i] = Math.min(Math.min(top, bottom), Math.min(left, right));
        }
        this.lastPly = 0;
    }

    public void update(MNKCell move) {
        game.playMove(move);
    }

    public final Game getGame() {
        return game;
    }

    protected int numMoves() {
        return getGame().maxDepth();
    }

    public int iterativeDeepening() {
        startTime = System.currentTimeMillis();
        AISearcher.Result result;
        int depth = game.maxDepth();
        int move = 0;
        int alpha = Game.MIN_SCORE;
        int beta = Game.MAX_SCORE;

        final Game backupGame = game.clone();
        try {
            //iterativeDeepening
            for (int i = 1; i <= depth; i++) {
                result = search(i, alpha, beta);
                //Aspiration Window
                if (result.score <= alpha || result.score >= beta) {
                    alpha = Game.MIN_SCORE;
                    beta = Game.MAX_SCORE;
                    i--;
                } else {
                    alpha = result.score - 100;
                    beta = result.score + 100;
                }
                move = result.getPrincipleVariationMove();
            }
        } catch (TimeoutException ex) {
            move = generateMoves().iterator().next();
        }
        this.game = backupGame;

        return move;
    }

    public Result search(int depth, int alpha, int beta) throws TimeoutException {
        timeCheck();

        if (depth <= 0)
            return new Result(evaluate(), null);

        boolean isPlayerOne = getGame().getCurrentPlayer() == Game.PLAYER_1;
        List<Integer> pv = new ArrayList<>(depth);

        for (int move : generateMoves()) {
            timeCheck();

            getGame().playMove(move);
            Result result = search(depth - 1, alpha, beta);
            getGame().unPlayMove();

            if (Thread.currentThread().isInterrupted())
                return null;

            int score = result.getScore();
            timeCheck();
            if (isPlayerOne ? score > alpha : score < beta) {
                if (isPlayerOne) alpha = score;
                else beta = score;
                if (alpha >= beta)
                    break;
                pv.clear();
                pv.add(move);
                if (result.getPrincipleVariation() != null)
                    pv.addAll(result.getPrincipleVariation());
            }
        }

        timeCheck();
        int score = isPlayerOne ? alpha : beta;
        if (score == Game.MIN_SCORE + depth - 1) {
            score++;
        } else if (score == Game.MAX_SCORE - depth + 1) {
            score--;
        }

        return new Result(score, pv);
    }

    protected Iterable<Integer> generateMoves() {
        updateWeights();

        Comparator<WeightedMove> comp = (m1, m2) -> {
            return m2.score - m1.score; // reverse
        };

        PriorityQueue<WeightedMove> moves = new PriorityQueue<>(numMoves(), comp);

        for (Integer move : game.generateMoves())
            moves.add(new WeightedMove(move, weights[move]));

        return () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !moves.isEmpty();
            }

            @Override
            public Integer next() {
                return moves.remove().move;
            }
        };
    }

    protected void updateWeights() {
        int currentPly = getGame().getNumberOfMovesPlayed();
        while (lastPly > currentPly)
            updateMove(getGame().getHistory(--lastPly), true);
        while (lastPly < currentPly)
            updateMove(getGame().getHistory(lastPly++), false);
    }

    protected void updateMove(int square, boolean undo) {
        int cols = getGame().getCols();
        int rows = getGame().getRows();
        int k = getGame().getK();
        int row = getGame().getRow(square);
        int col = getGame().getCol(square);
        int[] dirs = {-1, 1, -cols, cols, -cols - 1, cols + 1, -cols + 1, cols - 1};
        int[] lens = {col, cols - 1 - col, row, rows - 1 - row, Math.min(col, row),
                Math.min(cols - 1 - col, rows - 1 - row),
                Math.min(cols - 1 - col, row), Math.min(col, rows - 1 - row)};
        for (int i = 0; i < dirs.length; i++) {
            for (int j = 1; j < Math.min(k, lens[i]); j++) {
                weights[square + dirs[i] * j] += (undo ? -1 : 1) * (k - j);
            }
        }
    }

    protected void timeCheck() throws TimeoutException {
        if (System.currentTimeMillis() - startTime > timeLimit)
            throw new TimeoutException();
    }

    public int evaluate() {
        int score = 0;

        int winner = getGame().getWinner();

        if (winner == Game.PLAYER_1)
            return Game.MAX_SCORE;
        else if (winner == Game.PLAYER_2)
            return Game.MIN_SCORE;
        else {
            /*Row*/
            for (int row = 0; row < game.getRows(); row++)
                score += evaluate(game.getCellsForRow(row));

            /*Column*/
            for (int col = 0; col < game.getCols(); col++)
                score += evaluate(game.getCellsForColumns(col));

            /*Diagonal*/
            int midpoint = (game.getDiagonals() / 2) + 1;
            game.resetItemsInDiagonal();
            for (int diag = 1; diag <= game.getDiagonals(); diag++)
                score += evaluate(game.getDiagonalSquares(diag, midpoint));

            /*Anti diagonal*/
            int n = game.getRows(), j = n - 1, counter = 0;
            for (int i = 0; i < n; ++i) {
                counter++;
                score += evaluate(game.getAntiDiagonalSquares(counter, i, j));
            }
            int i = n - 1;
            for (j = n - 2; j >= 0; --j) {
                counter--;
                score += evaluate(game.getAntiDiagonalSquares(counter, i, j));
            }
            return score;
        }
    }

    /**
     * Evaluate single line scores for every cells
     */
    protected int evaluate(int[] line) {
        int k = game.getK();
        if (line.length < k)
            return 0;

        int p1 = 0;
        int p2 = 0;
        //Check how many squares inside the line are occupied by the players
        for (int i = 0; i < k - 1; i++) {
            if (line[i] == Game.PLAYER_1) {
                p1++;
            } else if (line[i] == Game.PLAYER_2) {
                p2++;
            }
        }

        int[] p1Score = new int[line.length - k + 1];
        int[] p2Score = new int[line.length - k + 1];
        for (int i = 0; i < p1Score.length; i++) {
            if (line[i + k - 1] == Game.PLAYER_1) {
                p1++;
            } else if (line[i + k - 1] == Game.PLAYER_2) {
                p2++;
            }
            p1Score[i] = (p2 <= 0) ? (1 << p1) - 1 : 0;
            p2Score[i] = (p1 <= 0) ? (1 << p2) - 1 : 0;
            if (line[i] == Game.PLAYER_1) {
                p1--;
            } else if (line[i] == Game.PLAYER_2) {
                p2--;
            }
        }

        int[] p1MaxScore = new int[p1Score.length];
        int[] p2MaxScore = new int[p2Score.length];
        for (int i = p1MaxScore.length - 1; i >= 0; i--) {
            p1MaxScore[i] = p1Score[i];
            p2MaxScore[i] = p2Score[i];
            if (i < p1MaxScore.length - 1 - k) {
                p1MaxScore[i] += p1MaxScore[i + k];
                p2MaxScore[i] += p2MaxScore[i + k];
            }
            for (int j = 1; j < Math.min(k, p1MaxScore.length - i); j++) {
                if (p1MaxScore[i] < p1MaxScore[i + j])
                    p1MaxScore[i] = p1MaxScore[i + j];
                if (p2MaxScore[i] < p2MaxScore[i + j])
                    p2MaxScore[i] = p2MaxScore[i + j];
            }
        }
        return p1MaxScore[0] - p2MaxScore[0];
    }
}
