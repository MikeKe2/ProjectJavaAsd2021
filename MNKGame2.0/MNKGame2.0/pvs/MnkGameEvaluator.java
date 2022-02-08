package pvs;

import mnkgame.MNKCellState;

public class MnkGameEvaluator {

    private Game game;

    public MnkGameEvaluator(Game game) {
        this.game = game;
    }

    public int evaluate() {
        int score = 0;
        for (int row = 0; row < game.getRows(); row++)
            score += evaluate(game.getRowSquares(row));
        for (int col = 0; col < game.getCols(); col++)
            score += evaluate(game.getColSquares(col));
        for (int diag = 0; diag < game.getDiagonals(); diag++)
            score += evaluate(game.getDiagonalSquares(diag));
        for (int diag = 0; diag < game.getAntiDiagonals(); diag++)
            score += evaluate(game.getAntiDiagonalSquares(diag));
        return score;
    }

    protected int evaluate(MNKCellState[] line) {
        int k = game.getK();
        if (line.length < k)
            return 0;

        int p1 = 0;
        int p2 = 0;

        for (int i = 0; i < k - 1; i++) {
            if (line[i] == MNKCellState.P1) {
                p1++;
            } else if (line[i] == MNKCellState.P2) {
                p2++;
            }
        }

        int[] p1Score = new int[line.length - k + 1];
        int[] p2Score = new int[line.length - k + 1];
        for (int i = 0; i < p1Score.length; i++) {
            if (line[i + k - 1] == MNKCellState.P1) {
                p1++;
            } else if (line[i + k - 1] == MNKCellState.P2) {
                p2++;
            }
            p1Score[i] = (p2 <= 0) ? (1 << p1) - 1 : 0;
            p2Score[i] = (p1 <= 0) ? (1 << p2) - 1 : 0;
            if (line[i] == MNKCellState.P1) {
                p1--;
            } else if (line[i] == MNKCellState.P2) {
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
