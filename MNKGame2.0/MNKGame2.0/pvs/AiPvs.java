package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;

public class AiPvs implements MNKPlayer {

    private Game game;
    private AISearcher searcher;

    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        game = new Game(M, N, K);
        searcher = new AISearcher(game, timeout_in_secs, first);
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

        return new MNKCell(game.getRow(move), game.getCol(move));
    }
}
