package pvs;

import mnkgame.MNKCell;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MnkGameSearcher {

    public record Task(MnkGameSearcher searcher, MNKCell[] FC, int depth, int alpha, int beta) implements Callable<Result> {

        @Override
        public Result call() {
            System.out.println(depth);
            return searcher.search(depth, FC, alpha, beta);
        }
    }

    public record Result(int score, List<MNKCell> pv, boolean proof) {

        public int getScore() {
            return score;
        }

        public List<MNKCell> getPrincipleVariation() {
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

        boolean maxi = getGame().getCurrentPlayer() == 1;

        List<MNKCell> pv = new ArrayList<>(depth);
        //System.out.println(depth);
        boolean proof = false;
        for (MNKCell move : FC) {
            //System.out.println(move);
            getGame().doMove(move);
            Result result = search(depth - 1, FC, alpha, beta);
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
}
