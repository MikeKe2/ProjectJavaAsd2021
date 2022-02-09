package pvs;

import java.util.*;
import java.util.concurrent.Callable;

public class MnkGameSearcher {

    public record Task(MnkGameSearcher searcher, int depth) implements Callable<Result> {
        @Override
        public Result call() {
            return searcher.search(depth, Game.MIN_SCORE, Game.MAX_SCORE);
        }
    }

    public record Result(int score, List<Integer> pv, boolean proof) {

        public int getScore() {
            return score;
        }

        public List<Integer> getPrincipleVariation() {
            return pv;
        }

        public int getPrincipleVariationMove() {
            return pv.get(0);
        }

        public boolean isProvenResult() {
            return proof;
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
    private MnkGameEvaluator eval;
    protected int[] weights;
    private int lastPly;
    private long nodes;

    public MnkGameSearcher(Game game, MnkGameEvaluator eval) {
        this.game = game;
        this.eval = eval;
        this.weights = new int[game.getSquares()];
        for (int i = 0; i < weights.length; i++) {
            int top = game.getRow(i);
            int bottom = game.getRows() - 1 - top;
            int left = game.getCol(i);
            int right = game.getCols() - 1 - left;
            weights[i] = Math.min(Math.min(top, bottom), Math.min(left, right));
        }
        lastPly = 0;
    }

    public final Game getGame() {
        return game;
    }

    public final MnkGameEvaluator getEvaluator() {
        return eval;
    }

    protected final void incrementNodeCount() {
        nodes++;
    }

    public final long getNodeCount() {
        return nodes;
    }

    protected int numMoves() {
        return getGame().getPseudoLegalMoves();
    }

    public Result search(int depth, int alpha, int beta) {
        incrementNodeCount();

        if (getGame().isGameOver())
            return new Result(getEvaluator().evaluate(), null, true);
        if (depth <= 0)
            return new Result(getEvaluator().evaluate(), null, false);

        boolean maxi = getGame().getCurrentPlayer() == Game.PLAYER_1;

        List<Integer> pv = new ArrayList<>(depth);
        boolean proof = false;

        for (int move : generateMoves()) {

            getGame().doMove(move);
            Result result = search(depth - 1, alpha, beta);
            getGame().undoMove();

            if (Thread.currentThread().isInterrupted())
                return null;
            int score = result.getScore();
            if (maxi ? score > alpha : score < beta) {
                if (maxi) alpha = score;
                else beta = score;
                proof = result.isProvenResult();
                if (alpha >= beta)
                    break;
                pv.clear();
                pv.add(move);
                if (result.getPrincipleVariation() != null)
                    pv.addAll(result.getPrincipleVariation());
            }
        }

        int score = maxi ? alpha : beta;
        if (score == Game.MIN_SCORE + depth - 1) {
            score++;
        } else if (score == Game.MAX_SCORE - depth + 1) {
            score--;
        }

        return new Result(score, pv, proof);
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
        int currentPly = getGame().getElapsedPly();
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

}
