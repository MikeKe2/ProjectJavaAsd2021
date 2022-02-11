package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;

import java.util.concurrent.ExecutionException;

public class AiPvs implements MNKPlayer {

    private long time;
    private Game game;

    private MnkGameSearcher searcher;
    private MnkGameEvaluator evaluator;


    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        game = new Game(M, N, K);

        this.time = (long) timeout_in_secs * 1000;

        evaluator = new MnkGameEvaluator(game);
        searcher = new MnkGameSearcher(game, evaluator, time);
    }

    @Override
    public String playerName() {
        return "hi :)";
    }

    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        if (MC.length > 1)
            searcher.update(MC[MC.length - 2]);
        if (MC.length > 0)
            searcher.update(MC[MC.length - 1]);

        int move = searcher.iterativeDeepening();

        System.out.println("Move selected: " + move + "\t that plays:" + game.getRow(move) + " " + game.getCol(move));
        return new MNKCell(game.getRow(move), game.getCol(move));
    }
}
