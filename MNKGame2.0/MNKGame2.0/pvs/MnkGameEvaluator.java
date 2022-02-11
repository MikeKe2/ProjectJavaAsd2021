package pvs;

public class MnkGameEvaluator {

    private Game game;

    public MnkGameEvaluator(Game game) {
        this.game = game;
    }

    public int evaluate() {
        int score = 0;

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
                //System.out.println("Line " + line.length + "\t Player 1 move " + game.getRow(line[i]) + " - " + game.getCol(line[i]));
                p1++;
            } else if (line[i] == Game.PLAYER_2) {
                //System.out.println("Line " + line.length + "\t Player 2 move " + game.getRow(line[i]) + " - " + game.getCol(line[i]));
                p2++;
            }
        }
        /**Check how many points are inside that line for a player to win
         * for example 3x3 with k=3 equals 3 - 3 + 1
         * */
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
