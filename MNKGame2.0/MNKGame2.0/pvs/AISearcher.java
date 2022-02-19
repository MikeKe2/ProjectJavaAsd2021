package pvs;

import mnkgame.MNKCell;

import java.util.concurrent.TimeoutException;

import static pvs.Game.MAX_SCORE;
import static pvs.Game.MIN_SCORE;

public class AISearcher {

    private Game game;
    private long startTime;
    final private int timeLimit;

    public AISearcher(Game game, int timeLimit) {
        this.game = game;
        this.timeLimit = timeLimit;
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

        Game.IntegerPair partialScore;

        final Game backupGame = game.clone();
        try {
            //iterativeDeepening
            depth = depth > 10 ? depth / 2 : depth;
            for (int i = 0; i < depth; i++) {
                partialScore = findBestMove(i);
                if (partialScore.score() > bestScore) {
                    bestScore = partialScore.score();
                    bestMove = partialScore.move();
                }
            }
        } catch (TimeoutException ex) {
            if (bestMove == -1) bestMove = generateRandomMove();
        }
        if (!game.checkIfEmpty(bestMove)) bestMove = generateRandomMove();

        this.game = backupGame;

        return bestMove;
    }

    private int generateRandomMove() {
        for (int move : game.generateMoves()) {
            return move;
        }
        throw new IllegalStateException("Failed to generate move.");
    }

    private Game.IntegerPair findBestMove(int depth) throws TimeoutException {
        int score = MIN_SCORE - 1, bestMoveDefensive = -1, defensiveScore = MIN_SCORE - 1;
        int partialBestMove = 0;
        boolean isDying = false;

        for (int move : getGame().generateMoves()) {
            timeCheck();

            getGame().playMove(move);
            int searchResult = AlphaBeta(false, depth, Game.MIN_SCORE, Game.MAX_SCORE);
            getGame().unPlayMove();

            timeCheck();

            if (depth % 2 == 0) {
                if (searchResult > score) {
                    partialBestMove = move;
                    score = searchResult;
                }
                if (score == MAX_SCORE && depth == 0)
                    break;
            } else {
                if (searchResult > defensiveScore) {
                    defensiveScore = searchResult;
                    bestMoveDefensive = move;
                } else if (searchResult == MIN_SCORE) {
                    isDying = true;
                }
            }
        }
        if (isDying) {
            partialBestMove = bestMoveDefensive;
            score = MAX_SCORE;
        }
        return new Game.IntegerPair(partialBestMove, score);
    }

    private int AlphaBeta(boolean minimum, int depth, int alpha, int beta) throws TimeoutException {
        int val, a, b;

        timeCheck();

        if (depth == 0) {
            val = evaluate();
        } else if (minimum) {
            val = MIN_SCORE;
            a = alpha;
            for (int move : getGame().generateMoves()) {
                if (val < beta) {
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
                    game.playMove(move);
                    val = Math.min(val, AlphaBeta(true, depth - 1, alpha, b));
                    b = Math.min(b, val);
                    game.unPlayMove();
                }
            }

        }
        return val;
    }

    protected void timeCheck() throws TimeoutException {
        if ((System.currentTimeMillis() - startTime) / 1000 > timeLimit * (90.0 / 100.0))
            throw new TimeoutException();
    }

    public int evaluate() {
        int score = 0;

        int winner = getGame().getWinner();

        if (winner == Game.PLAYER_1) return MAX_SCORE;
        else if (winner == Game.PLAYER_2) return MIN_SCORE;
        else {
            for (int row = 0; row < game.getRows(); row++)
                score += evaluate(game.getCellsForRow(row));

            for (int col = 0; col < game.getCols(); col++)
                score += evaluate(game.getCellsForColumns(col));

            for (int diag = 0; diag < game.getDiagonals(); diag++)
                if (game.getDiagonalSize(diag) >= game.getK()) score += evaluate(game.getDiagonalSquares(diag));

            for (int diag = 0; diag < game.getDiagonals(); diag++)
                if (game.getDiagonalSize(diag) >= game.getK()) score += evaluate(game.getAntiDiagonalSquares(diag));
        }
        return score;
    }

    protected int evaluate(int[] line) {
        int k = game.getK();
        if (line.length < k) return 0;

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
                if (p1MaxScore[i] < p1MaxScore[i + j]) p1MaxScore[i] = p1MaxScore[i + j];
                if (p2MaxScore[i] < p2MaxScore[i + j]) p2MaxScore[i] = p2MaxScore[i + j];
            }
        }
        return p1MaxScore[0] - p2MaxScore[0];
    }
}