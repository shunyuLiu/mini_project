package bms.exceptions;

/**
 * Exception thrown when a floor is added to a building but the area of the
 * floor below is smaller than the area of the new floor.
 * <p>
 * In other words, the floor does not have the required supporting structure
 * underneath (note that no overhanging floors are allowed).
 *
 * @ass1
 */
public class FloorTooSmallException extends Exception {
    /**
     * Constructs a normal FloorBelowTooSmallException with no error message or
     * cause.
     *
     * @see Exception#Exception()
     * @ass1
     */
    public FloorTooSmallException() {
        super();
    }

    /**
     * Constructs a FloorTooSmallException that contains a helpful message
     * detailing why the exception occurred.
     * <p>
     * <b>Note:</b> implementing this constructor is <b>optional</b>.
     * It has only been included in the Javadoc to ensure your code will compile
     * if you give your exception a message when throwing it. This practice
     * can be useful for debugging purposes.
     * <p>
     * <b>Important:</b> do not write JUnit tests that expect a valid
     * implementation of the assignment to have a certain error message, as the
     * official solution will use different messages to those you are expecting,
     * if any at all.
     *
     * @param message detail message
     * @see Exception#Exception(String)
     * @ass1
     */
    public FloorTooSmallException(String message) {
        super(message);
    }
}
