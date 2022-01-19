package monkey.mnk;

/**
 * A <code>Position</code> refers to a single cell of the {@link Board}. It
 * features bounds checking and helper operations.
 *
 * @author Gaia Clerici
 * @version 1.0
 * @since 1.0
 */
public class Position implements Cloneable {

	/** Number of rows of the board. */
	public final int ROWSNUMBER;
	/** Number of columns of the board. */
	public final int COLUMNSNUMBER;

	/**
	 * Constructs a new {@link Position} given the numbers of rows and columns and
	 * its coordinates.
	 *
	 * @param rowsNumber    Number of rows in the grid.
	 * @param columnsNumber Number of columns in the grid.
	 * @param row           Row index (starting from zero).
	 * @param column        Column index (starting from zero).
	 * @throws IllegalArgumentException  rowsNumber or columnsNumber is negative.
	 * @throws IndexOutOfBoundsException Referring to a {@link Position} outside of
	 *                                   <code>b</code>'s bounds.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public Position(int rowsNumber, int columnsNumber, int row, int column) {
		// if (rowsNumber < 0 || columnsNumber < 0)
		// throw new IllegalArgumentException("rowsNumber or columnsNumber aren't
		// valid");
		ROWSNUMBER = rowsNumber;
		COLUMNSNUMBER = columnsNumber;
		// validate(row, column);
		this.row = row;
		this.column = column;
	}

	/**
	 * Constructs a new {@link Position} given its parent {@link Board} and its
	 * coordinates.
	 *
	 * @param b      Parent {@link Board} whose extents are used for bounds
	 *               checking.
	 * @param row    Row index (starting from zero).
	 * @param column Column index (starting from zero).
	 * @throws NullPointerException      Null {@link Board}.
	 * @throws IndexOutOfBoundsException Referring to a {@link Position} outside of
	 *                                   <code>b</code>'s bounds.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public Position(Board b, int row, int column) {
		this(b.M, b.N, row, column);
	}

	/**
	 * Constructs a new {@link Position} given an {@link mnkgame.MNKCell [MNKCell]}.
	 *
	 * @param rowsNumber    Number of rows in the grid.
	 * @param columnsNumber Number of columns in the grid.
	 * @param cell          {@link mnkgame.MNKCell [MNKCell]} to be used.
	 * @throws IllegalArgumentException  rowsNumber or columnsNumber is negative.
	 * @throws IndexOutOfBoundsException Referring to a {@link Position} outside of
	 *                                   <code>b</code>'s bounds.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public Position(int rowsNumber, int columnsNumber, mnkgame.MNKCell cell) {
		this(rowsNumber, columnsNumber, cell.i, cell.j);
	}

	/**
	 * Creates a clone of this {@link Position}.
	 *
	 * @return The desired clone.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public Position clone() {
		try {
			return (Position) super.clone();
		} catch (CloneNotSupportedException e) {
			// Should never happen: we support clone
			throw new InternalError(e.toString());
		}
	}

	/**
	 * Getter for row index.
	 *
	 * @see #getColumn
	 * @return Row index.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Getter for column index.
	 *
	 * @see #getRow
	 * @return Column index.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Indicates whether some other object is "equal to" (memberwise) this one.
	 *
	 * @param o The reference object with which to compare.
	 * @return <code>true</code> if this object is the same as the obj argument;
	 *         <code>false</code> otherwise.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	@Override // inherit doc comment
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Position))
			return false;
		Position p = (Position) o;
		return ROWSNUMBER == p.ROWSNUMBER && COLUMNSNUMBER == p.COLUMNSNUMBER && row == p.row && column == p.column;
	}

	/**
	 * Implements bounds checking for rows.
	 *
	 * @see #validateColumn
	 * @param row Row index.
	 * @throws IndexOutOfBoundsException Row out of bounds.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	protected void validateRow(int row) {
		if (row >= ROWSNUMBER || row < 0)
			throw new IndexOutOfBoundsException("This row isn't valid");
	}

	/**
	 * Implements bounds checking for columns.
	 *
	 * @see #validateRow
	 * @param column Column index.
	 * @throws IndexOutOfBoundsException Column out of bounds.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	protected void validateColumn(int column) {
		if (column >= COLUMNSNUMBER || column < 0)
			throw new IndexOutOfBoundsException("This row isn't valid");
	}

	/**
	 * Implements bounds checking for both rows and columns.
	 *
	 * @param row    Row index.
	 * @param column Column index.
	 * @throws IndexOutOfBoundsException Row and/or column out of bounds.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	protected void validate(int row, int column) {
		validateRow(row);
		validateColumn(column);
	}

	/**
	 * Returns a string representation of the object.
	 *
	 * @return A string representation of this object.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	@Override
	public String toString() {
		return "(" + row + ", " + column + ")";
	}

	/** Current row index. */
	private int row;
	/** Current column index */
	private int column;

}
