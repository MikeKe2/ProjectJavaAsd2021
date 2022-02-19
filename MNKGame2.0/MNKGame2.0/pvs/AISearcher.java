package pvs;

import mnkgame.MNKCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static pvs.Game.*;

public class AISearcher {

    public static class EntryTT {

        public int depth;
        public int lowerbound;
        public int upperbound;
        int chosen = -1;

        void update(int g, int alpha, int beta) {
            /* Traditional transposition table storing of bounds */
            /* Fail low result implies an upper bound */
            if (g <= alpha) {
                upperbound = g;
            }
            /* Found an accurate minimax value - will not occur if called with zero window */
            if (g > alpha && g < beta) {
                lowerbound = g;
                upperbound = g;
            }
            /* Fail high result implies a lower bound */
            if (g >= beta) {
                lowerbound = g;
            }
        }

    }

    private Game game;
    private long startTime;
    final private int timeLimit;
    private final HashMap<Long, EntryTT> transpositionTable;
    private boolean first;

    public AISearcher(Game game, int timeLimit, boolean first) {
        this.game = game;
        this.timeLimit = timeLimit;
        this.first = first;
        transpositionTable = new HashMap<>(MAX_SCORE);
    }

    public void update(MNKCell move) {
        game.playMove(move);
    }

    public final Game getGame() {
        return game;
    }

    public int iterativeDeepening() {
        final Game backupGame = game.clone();
        int depth = game.maxDepth();

        startTime = System.currentTimeMillis();
        int bestScore = MIN_SCORE - 1, bestMove = -1;
        int alpha = Game.MIN_SCORE, beta = Game.MAX_SCORE;

        Game.IntegerPair partialScore;

        int firstGuess = 0;
        EntryTT result = null;
        final List<Integer> moves = new ArrayList<>();
        getGame().generateMoves().iterator().forEachRemaining(moves::add);

        try {
            depth = depth > 10 ? depth / 2 : depth;
            //iterativeDeepening
            for (int i = 1; i <= depth; i++) {

                firstGuess = Mtdf(i, firstGuess);
                result = transpositionTable.get(getGame().computeKey());

                //System.out.println("Depth:" + i);
                /*if (partialScore.score() > bestScore) {
                    bestScore = partialScore.score();
                    bestMove = partialScore.move();
                }*/

                //Aspiration
                /*if (((partialScore.score() <= alpha || partialScore.score() >= beta)) && i > 1) {
                    System.out.println("alpha: " + alpha + "\tbeta: " + beta);
                    alpha = Game.MIN_SCORE;
                    beta = Game.MAX_SCORE;
                    i--;
                } else {
                    System.out.println("alpha: " + alpha + "\tbeta: " + beta);
                    alpha =  alpha + partialScore.score();
                    beta = partialScore.score() + (beta - 100);
                    System.out.println("alpha: " + alpha + "\tbeta: " + beta);
                }*/
                /*
                if ((IamP1 && partialScore.score() >= bestScore) || (!IamP1 && partialScore.score() <= bestScore)) {
                    bestScore = partialScore.score();
                    bestMove = partialScore.move();
                }*/
            }
            bestMove = moves.get(result.chosen);

        } catch (TimeoutException ex) {
            if (result != null)
                bestMove = moves.get(result.chosen);
            else
                bestMove = generateRandomMove();
        }


        this.game = backupGame;

        long endTime = System.currentTimeMillis();
        System.out.println("Move: " + bestMove);
        System.out.println("Timer: " + (endTime - startTime));

        return bestMove;
    }

    private int generateRandomMove() {
        for (int move : game.generateMoves()) {
            return move;
        }
        throw new IllegalStateException("Failed to generate move.");
    }

    private int Mtdf(int depth, int firstGuess) throws TimeoutException {
        int score = firstGuess;
        int lowerbound = Integer.MIN_VALUE;
        int upperbound = Integer.MAX_VALUE;
        int partialBestMove = 0;
        transpositionTable.clear();

        while (lowerbound < upperbound) {
            timeCheck();

            int beta = (score == lowerbound) ? score + 1 : score;
            //System.out.println("Depth: " + depth + "\t beta: " + beta);
            //getGame().playMove(move);
            score = AlphaBeta(depth, beta - 1, beta);
            System.out.println("Depth: " + depth + "\t Score: " + score);
            //getGame().unPlayMove();

            timeCheck();
            if (score < beta) {
                upperbound = score;
            } else {
                lowerbound = score;
            }
            /*if (searchResult > score) {
                score = searchResult;
                partialBestMove = move;
            }*/
        }
        //System.out.println("LB:" + lowerbound + "\t UP: " + upperbound);
        return score;
    }

    private boolean checkIfAiTurn() {
        return first == (game.getCurrentPlayer() == PLAYER_1);
    }

    private int AlphaBeta(int depth, int alpha, int beta) throws TimeoutException {

        timeCheck();
        long zobrist_key = getGame().computeKey();
        EntryTT entry = transpositionTable.get(zobrist_key);
        if (entry != null) {
            if (depth <= entry.depth) {
                if (entry.lowerbound >= beta) {
                    //System.out.println("LB: " + entry.lowerbound);
                    return entry.lowerbound;
                }
                if (entry.upperbound <= alpha) {
                    //System.out.println("UB: " + entry.upperbound);
                    return entry.upperbound;
                }
                alpha = Math.max(alpha, entry.lowerbound);
                beta = Math.min(beta, entry.upperbound);
            }
        } else {
            entry = new EntryTT();
            transpositionTable.put(zobrist_key, entry);
        }

        if (depth == 0) {
            int val = evaluate();
            entry.update(val, alpha, beta);
            //System.out.println("DEPTH: " + val);
            return val;
        }

        final boolean isMax = game.getCurrentPlayer() == (first ? 1 : -1);
        final boolean isMin = !isMax;

        int val = (isMax) ? MIN_SCORE : MAX_SCORE;
        int a = alpha;
        int b = beta;
        int idx = -1;

        for (int move : getGame().generateMoves()) {
            if ((isMax && val >= beta) || (isMin && val <= alpha)) break;

            game.playMove(move);
            final int child_val = AlphaBeta(depth - 1, a, b);
            game.unPlayMove();

            idx++;

            if ((isMax && child_val > val) || (isMin && child_val < val)) {
                val = child_val;
                entry.chosen = idx;
            }

            if (isMax) {
                a = Math.max(a, val);
            } else {
                b = Math.min(b, val);
            }

        }

        final long zobrist_check = game.computeKey();
        if (zobrist_key != zobrist_check) {
            transpositionTable.put(zobrist_check, entry);
            transpositionTable.remove(zobrist_key);
        }

        entry.update(val, alpha, beta);
        //System.out.println("EXIT: " + val);
        return val;

        /*if (minimum) {
            val = MIN_SCORE;
            a = alpha;
            for (int move : getGame().generateMoves()) {
                if (val < beta) {
                    //System.out.println("MIN_MOVE: " + move);
                    game.playMove(move);
                    val = Math.max(val, AlphaBeta(false, depth - 1, a, beta));
                    a = Math.max(a, val);
                    game.unPlayMove();
                }
            }
        } else {
            val = MAX_SCORE;
            b = beta;
            for (int move : getGame().generateMoves()) {
                if (val > alpha) {
                    //System.out.println("MAX_MOVE: " + move);
                    game.playMove(move);
                    val = Math.min(val, AlphaBeta(true, depth - 1, alpha, b));
                    b = Math.min(b, val);
                    game.unPlayMove();
                }
            }

        }
        if (val <= alpha)
            transpositionTable.put(zobristKey, new EntryTT(depth, Integer.MIN_VALUE, val));
        if (val > alpha && val < beta)
            transpositionTable.put(zobristKey, new EntryTT(depth, val, val));
        if (val >= beta)
            transpositionTable.put(zobristKey, new EntryTT(depth, val, Integer.MAX_VALUE));

        if (getGame().getWinner() == PLAYER_2)
            System.out.println("WNR2: " + val + "\t depth:" + depth);
        else if (getGame().getWinner() == PLAYER_1)
            System.out.println("WNR1: " + val + "\t depth:" + depth);
        return val;*/
    }


    /*private int AlphaBeta(int depth, int alpha, int beta) throws TimeoutException {
        int val, hashf = hashfALPHA;
        long zobristKey = getGame().computeKey();
        timeCheck();

        EntryTT entry = transpositionTable.get(zobristKey);
        if (entry != null) {
            if (entry.flag == hashfEXACT) {
                return entry.value;
            } else if (entry.flag == hashfBETA) {
                beta = Math.min(beta, entry.value);
            } else if (entry.flag == hashfALPHA) {
                alpha = Math.max(alpha, entry.value);
            }
            if (alpha >= beta) {
                return entry.value;
            }
        }

        if (depth == 0) {
            val = evaluate();
            //Save val in transposition table
            transpositionTable.put(zobristKey, new EntryTT(depth, val, hashfEXACT));
            return val;
        }

        timeCheck();
        for (int move : getGame().generateMoves()) {

            getGame().playMove(move);
            val = -AlphaBeta(depth - 1, -beta, -alpha);
            getGame().unPlayMove();

            if (val >= beta) {
                //RecordHash(depth, beta, hashfBETA);
                transpositionTable.put(zobristKey, new EntryTT(depth, beta, hashfBETA));
                return beta;
            }
            if (val > alpha) {
                hashf = hashfEXACT;
                alpha = val;
            }
        }
        timeCheck();
        //RecordHash(depth, alpha, hashf);
        transpositionTable.put(zobristKey, new EntryTT(depth, alpha, hashf));
        return alpha;
    }*/

    protected void timeCheck() throws TimeoutException {
       /* if ((System.currentTimeMillis() - startTime) / 1000 > timeLimit * (90.0 / 100.0))
            throw new TimeoutException();*/
    }

    public int evaluate() {
        int score = 0;

        int winner = getGame().getWinner();

       /* if (winner == Game.PLAYER_1)
            return MAX_SCORE;
        else if (winner == Game.PLAYER_2)
            return MIN_SCORE;
        else {*/
        for (int row = 0; row < game.getRows(); row++)
            score += evaluate(game.getCellsForRow(row));

        for (int col = 0; col < game.getCols(); col++)
            score += evaluate(game.getCellsForColumns(col));

        for (int diag = 0; diag < game.getDiagonals(); diag++)
            if (game.getDiagonalSize(diag) >= game.getK())
                score += evaluate(game.getDiagonalSquares(diag));

        for (int diag = 0; diag < game.getDiagonals(); diag++)
            if (game.getDiagonalSize(diag) >= game.getK())
                score += evaluate(game.getAntiDiagonalSquares(diag));
        //}
        return score;
    }

    protected int evaluate(int[] line) {
        int k = game.getK();
        if (line.length < k)
            return 0;

        int p1 = 0;
        int p2 = 0;

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
