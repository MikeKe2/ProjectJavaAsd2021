package pvs;

import mnkgame.MNKCell;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MnkGameSearcher {

    public record Task(MnkGameSearcher searcher, MNKCell[] FC, int depth) implements Callable<Result> {

        @Override
        public Result call() throws Exception {
            return searcher.search(depth, FC, MnkGameEvaluator.MIN_SCORE - 1, MnkGameEvaluator.MAX_SCORE + 1);
        }
    }

    public record Result(int score, List<MNKCell> pv, boolean proof) {

        public int getScore() {
            return score;
        }

        public List<MNKCell> getPrincipleVaration() {
            return pv;
        }

        public MNKCell getPrincipleVariationMove() {
            return pv.get(0);
        }

        public boolean isProvenResult() {
            return proof;
        }
    }

    private Game game;
    private MnkGameEvaluator eval;

    public MnkGameSearcher(Game game, MnkGameEvaluator eval) {
        this.game = game;
        this.eval = eval;
    }

    public final Game getGame() {
        return game;
    }

    public final MnkGameEvaluator getEvaluator() {
        return eval;
    }

    public Result search(int depth, MNKCell[] FC, int alpha, int beta) {

        if (getGame().isGameOver())
            return new Result(getEvaluator().evaluate(), null, true);
        if (depth <= 0)
            return new Result(getEvaluator().evaluate(), null, false);

        boolean maxi = getGame().getCurrentPlayer() == MnkGameEvaluator.PLAYER_MAX;

        List<MNKCell> pv = new ArrayList<>(depth);

        boolean proof = false;
        for (MNKCell move : FC) {
            getGame().doMove(move, false);
            Result result = search(depth - 1, FC, alpha, beta);
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
                if (result.getPrincipleVaration() != null)
                    pv.addAll(result.getPrincipleVaration());
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
