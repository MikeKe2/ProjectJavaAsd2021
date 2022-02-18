package pvs;

import mnkgame.MNKCell;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static pvs.Game.*;

public class AISearcher {

    public static class EntryTT {

        public int depth;
        public int flag;
        public int value;

        public EntryTT(int depth, int flag, int value) {
            this.depth = depth;
            this.flag = flag;
            this.value = value;
        }

    }

    private Game game;
    private long startTime;
    final private long timeLimit;
    private final HashMap<Long, EntryTT> transpositionTable;
    private boolean IamP1;

    public AISearcher(Game game, long timeLimit, boolean first) {
        this.game = game;
        this.timeLimit = timeLimit;
        this.IamP1 = first;
        transpositionTable = new HashMap<>(MAX_SCORE);
    }

    public void update(MNKCell move) {
        game.playMove(move);
    }

    public final Game getGame() {
        return game;
    }

    public int iterativeDeepening() {
        startTime = System.currentTimeMillis();
        int depth = game.maxDepth();
        int bestScore = MIN_SCORE - 1, bestMove = -1;
        int alpha = Game.MIN_SCORE, beta = Game.MAX_SCORE;
        Game.IntegerPair partialScore;

        final Game backupGame = game.clone();
        try {
            //iterativeDeepening
            for (int i = 1; i <= depth; i++) {
                partialScore = findBestMove(i, alpha, beta);
                timeCheck();
                //Aspiration
                //System.out.println("Depth:" + depth);
                //System.out.println("Score:" + partialScore.second() + "\t move" + partialScore.first());
                if (partialScore.second() > bestScore) {
                    bestScore = partialScore.second();
                    bestMove = partialScore.first();
                }
                /*if (((partialScore.second() <= alpha && alpha != Game.MIN_SCORE) || (beta != MAX_SCORE && partialScore.second() >= beta)) && i > 1) {
                    System.out.println("alpha: " + alpha + "\tbeta: " + beta);
                    alpha = Game.MIN_SCORE;
                    beta = Game.MAX_SCORE;
                    i--;
                } else {
                    alpha = partialScore.second() - alpha / 4;
                    beta = partialScore.second() + beta / 4;
                }*/
                /*
                if ((IamP1 && partialScore.second() >= bestScore) || (!IamP1 && partialScore.second() <= bestScore)) {
                    bestScore = partialScore.second();
                    bestMove = partialScore.first();
                }*/
            }
        } catch (TimeoutException ex) {
            //System.out.println("Error, move not found on time.");
            bestMove = generateRandomMove();
        }
        this.game = backupGame;
        System.out.println("Move: " + bestMove);
        return bestMove;
    }

    private int generateRandomMove() {

        for (int move : game.generateMoves()) {
            return move;
        }
        throw new IllegalStateException("Failed to generate move.");
    }

    private Game.IntegerPair findBestMove(int depth, int alpha, int beta) throws TimeoutException {
        int score = checkIfAiTurn() ? MIN_SCORE - 1 : MAX_SCORE + 1;
        int partialBestMove = 0;

        for (int move : getGame().generateMoves()) {
            timeCheck();

            getGame().playMove(move);
            int searchResult = AlphaBeta(depth, alpha, beta);
            getGame().unPlayMove();

            timeCheck();

            if (checkIfAiTurn() ? searchResult > score : searchResult < score) {
                System.out.println("move: " + move + "\tscore: " + score + "\tsearch: " + searchResult);
                score = searchResult;
                partialBestMove = move;
            }
        }

        return new Game.IntegerPair(partialBestMove, score);
    }

    private boolean checkIfAiTurn() {
        return IamP1 == (game.getCurrentPlayer() == PLAYER_1);
    }

    private int AlphaBeta(int depth, int alpha, int beta) throws TimeoutException {
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
            for (int row = 0; row < game.getRows(); row++)
                score += evaluate(game.getCellsForRow(row));

            for (int col = 0; col < game.getCols(); col++)
                score += evaluate(game.getCellsForColumns(col));

            int midpoint = (game.getDiagonals() / 2) + 1;
            game.resetItemsInDiagonal();
            for (int diag = 1; diag <= game.getDiagonals(); diag++)
                score += evaluate(game.getDiagonalSquares(diag, midpoint));

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
