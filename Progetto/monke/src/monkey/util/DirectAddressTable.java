package monkey.util;

import java.util.Iterator;
import java.util.function.ToIntFunction;

/**
 * A generic direct address table indexed using [0 .. length - 1] integers as
 * keys. See T. H. Cormen, C. E. Leiserson, R. L. Rivest, C. Stein,
 * <i>Introduction to Algorithms</i>, 3rd ed., Mcgraw-Hill Book Company, p.
 * 254f.
 *
 * @param <T> The type of the values to be stored.
 * @author Stefano Volpe
 * @version 1.0
 * @since 1.0
 */
public class DirectAddressTable<T> implements Cloneable, Iterable<T> {

	/**
	 * A function associating each value to an integer key in the range [0 .. length
	 * - 1].
	 */
	final public ToIntFunction<T> toKey;

	/**
	 * Constructs a new {@link DirectAddressTable}. Takes Θ({@link #length}) time.
	 *
	 * @param type        Type token to allocate the underlying
	 *                    {@link java.lang.reflect.Array Array}.
	 * @param keyFunction Initializer for {@link #toKey}.
	 * @param length      Number of possible keys.
	 * @throws IllegalArgumentException length is negative
	 * @throws NullPointerException     type, or keyFunction, or both are null.
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public DirectAddressTable(Class<T> type, ToIntFunction<T> keyFunction, int length) {
		// if (length < 0)
		// throw new IllegalArgumentException("length is negative");
		// if (type == null)
		// throw new NullPointerException("type is null");
		// if (keyFunction == null)
		// throw new NullPointerException("keyFunction is null");
		table = (T[]) java.lang.reflect.Array.newInstance(type, length);
		toKey = keyFunction;
	}

	/**
	 * Creates a clone of this {@link DirectAddressTable}. Takes Θ({@link #length})
	 * time.
	 *
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public DirectAddressTable<T> clone() {
		try {
			DirectAddressTable<T> copy = (DirectAddressTable<T>) super.clone();
			copy.table = table.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			// Should never happen: we support clone
			throw new InternalError(e.toString());
		}
	}

	/**
	 * The number of possible keys.
	 *
	 * @return Length of this {@link DirectAddressTable}.
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	public int length() {
		return table.length;
	}

	/**
	 * Given a key, returns the value associated to it, or <code>null</code> if such
	 * a value does not exist. If <code>null</code> is returned, there is no way to
	 * tell whether it represents a missing element or an actual <code>null</code>
	 * element previously inserted.
	 *
	 * @param key Key of the value to be returned. It must be in the interval [0 ..
	 *            length - 1].
	 * @throws IndexOutOfBoundsException key is out of bounds
	 * @return The value associated to the key, or <code>null</code> if such a value
	 *         does not exist.
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	public T search(int key) {
		// try {
		return table[key];
		// } catch (IndexOutOfBoundsException e) {
		// throw new IndexOutOfBoundsException(key + " is out of direct address table
		// bounds [0;" + table.length + "[");
		// }
	}

	/**
	 * Inserts an element in the table, updating it if already present.
	 *
	 * @param element The element to be inserted. It may be <code>null</code>.
	 * @throws IndexOutOfBoundsException The element's key is out of bounds.
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	public void insert(T element) {
		final int key = toKey.applyAsInt(element);
		// try {
		table[key] = element;
		// } catch (IndexOutOfBoundsException e) {
		// throw new IndexOutOfBoundsException(
		// element + "'s key (" + key + ") is out of direct address table bounds [0;" +
		// table.length + "[");
		// }
	}

	@Override // inherit doc comment
	public Iterator<T> iterator() {
		return new DirectAddressTableIterator<T>(table);
	}

	/**
	 * Returns a string representation of the object. <br>
	 * Takes Θ({@link #length}) time.
	 *
	 * @return A string representation of this object.
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	@Override
	public String toString() {
		String res = "[";
		Iterator<T> it = iterator();
		while (it.hasNext())
			res = res + it.next() + (it.hasNext() ? "|" : "]");
		return res;
	}

	/** Array used to store the data. */
	private T[] table;

}

/**
 * An <code>Iterator</code> class for {@link DirectAddressTable}. It does not
 * implement <code>remove</code>.
 *
 * @author Stefano Volpe
 * @version 1.0
 * @since 1.0
 */
class DirectAddressTableIterator<T> implements Iterator<T> {

	/**
	 * Constructs a new {@link DirectAddressTableIterator} from the underlying
	 * array.
	 * 
	 * @param t Table to iterate through.
	 * @throws NullPointerException t is null.
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 **/
	public DirectAddressTableIterator(T[] t) {
		// if (t == null)
		// throw new NullPointerException("t is null.");
		table = t;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	@Override
	public boolean hasNext() {
		return index < table.length;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NoSuchElementException {@inheritDoc}
	 * @author Stefano Volpe
	 * @version 1.0
	 * @since 1.0
	 */
	@Override
	public T next() {
		// if (hasNext())
		return table[index++];
		// throw new java.util.NoSuchElementException("No next element.");
	}

	/** The table to iterate through. */
	final private T[] table;
	/** The index of the next element, or the length of the table if it is over. */
	private int index = 0;

}
