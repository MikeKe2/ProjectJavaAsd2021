package mtd;

import mnkgame.MNKCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class MnkGameSearcher {

    private Game game;
    private MnkGameEvaluator eval;
    private long nodes;
    private int capacity;



    public MnkGameSearcher(Game game, MnkGameEvaluator eval) {
        this.game = game;
        this.eval = eval;

        // FIXME: 30/01/2022 it's possible to have better capacity?
        capacity = Integer.MAX_VALUE;
        transpositionMap = new HashMap<>(capacity);
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

/*
    private Result search(int depth, int alpha, int beta) {
        // FIXME: 31/01/2022 this is pvs (principal variation search) it would be cool to implement it


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
*/
}