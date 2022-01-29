package monkey;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;
import monkey.ai.AI;
import monkey.ai.Player;
import monkey.mnk.Board;
import monkey.mnk.Position;

/**
 * A <code>MoNKey</code> offers a possible implementation of
 * <code>MNKPlayer</code> using an instance of {@link monkey.ai.AI}.
 */
public class MoNKey implements MNKPlayer {
	/** Artificial intelligence used by <code>MoNKey</code>. */
	private AI<Board, Position> ai = null;
	/** Number of rows. */
	private int m;
	/** Number of columns. */
	private int n;
	/**
	 * Maximum number of cells of a configuration commonly considered small enough
	 * to be explored.
	 */
	final static private int BIGGAME = 100;
	/** Conversion factor from seconds to milliseconds. */
	final static private int S_TO_MS = 1000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		ai = new AI<>(first ? Player.P1 : Player.P2, new Board(M, N, K), (long) timeout_in_secs * S_TO_MS);
		m = M;
		n = N;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		// final long startTime = System.currentTimeMillis();
		if (MC.length > 1)
			ai.update(new Position(m, n, MC[MC.length - 2]));
		if (MC.length > 0)
			ai.update(new Position(m, n, MC[MC.length - 1]));
		final Position p = m * n > BIGGAME ? ai.immediateSearch() : ai.iterativeDeepeningSearch();
		return new MNKCell(p.getRow(), p.getColumn());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String playerName() {
		return "🅼🐵🅽🅺ey";
	}
}
