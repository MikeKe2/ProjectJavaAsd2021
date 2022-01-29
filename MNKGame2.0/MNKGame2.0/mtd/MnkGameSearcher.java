package mtd;

import mnkgame.MNKCell;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MnkGameSearcher {

    private Game game;
    private MnkGameEvaluator eval;
    private long nodes;

    public MnkGameSearcher(Game game, MnkGameEvaluator eval) {
        this.game = game;
        this.eval = eval;
    }

    public void update(MNKCell mnkCell) {
        game.update(mnkCell);
    }

    public record Task(MnkGameSearcher searcher, int depth) implements Callable<Result> {
        @Override
        public Result call() {
            return searcher.search(depth, MnkGameEvaluator.MIN_SCORE - 1, MnkGameEvaluator.MAX_SCORE + 1);
        }
    }

    public record Result(int score, List<Integer> pv, boolean proof) {

        public List<Integer> getPrincipleVariation() {
            return pv;
        }

        public int getScore() {
            return score;
        }

        public int getPrincipleVariationLength() {
            return pv.size();
        }

        public int getPrincipleVariationMove() {
            return pv.get(0);
        }

        public boolean isProvenResult() {
            return proof;
        }
    }

    protected final void incrementNodeCount() {
        nodes++;
    }

    protected Iterable<Integer> generateMoves() {
        return getGame().generatePseudoLegalMoves();
    }

    protected int numMoves() {
        return getGame().getPseudoLegalMoves();
    }

    public final Game getGame() {
        return game;
    }

    public final MnkGameEvaluator getEvaluator() {
        return eval;
    }

    public final long getNodeCount() {
        return nodes;
    }

    private Result search(int depth, int alpha, int beta) {

        incrementNodeCount();

        if (getGame().isGameOver())
            return new Result(getEvaluator().evaluate(), null, true);
        if (depth <= 0)
            return new Result(getEvaluator().evaluate(), null, false);

        boolean maxi = getGame().getCurrentPlayer() == MnkGameEvaluator.PLAYER_MAX;

        List<Integer> pv = new ArrayList<>(depth);
        boolean proof = false;
        for (int move : generateMoves()) {
            getGame().doMove(move, false);
            Result result = search(depth - 1, alpha, beta);
            getGame().undoMove(true);
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
        if (score == MnkGameEvaluator.MIN_SCORE + depth - 1) {
            score++;
        } else if (score == MnkGameEvaluator.MAX_SCORE - depth + 1) {
            score--;
        }

        return new Result(score, pv, proof);

    }

}