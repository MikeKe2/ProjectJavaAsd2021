package monkey.ai;

/**
 * A <code>Player</code> can refer to one of the two agents partaking in the
 * game.
 *
 * @author Gaia Clerici
 * @version 1.0
 * @since 1.0
 */
public enum Player {

	P1() {public Player not() {return P2;}},
	P2() {public Player not() {return P1;}};

	Player() {
	}

	/**Returns the other {@link Player}.*/
	abstract public Player not();
}
