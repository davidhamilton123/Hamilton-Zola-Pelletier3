package ast.typesystem.types;

/**
 * Represents a list type in the MFL type system.
 * <p>
 * A {@code ListType} wraps an element type, indicating that values of this type
 * are lists whose elements all share the same underlying type.
 * For example, {@code [ int ]} or {@code [ real ]}.
 * </p>
 */
public class ListType extends Type
{
    private Type elementType;

    /**
     * Constructs a new {@code ListType} with the given element type.
     *
     * @param elementType the type of elements stored in the list
     */
    public ListType(Type elementType)
    {
        this.elementType = elementType;
    }

    /**
     * Retrieves the element type contained within this list type.
     *
     * @return the element type
     */
    public Type getElementType()
    {
        return elementType;
    }

    /**
     * Compares this list type with another object for equality.
     * Two list types are equal if:
     * <ul>
     *     <li>The other object is also a {@code ListType}</li>
     *     <li>Their element types are equal</li>
     * </ul>
     *
     * @param obj the object to compare against
     * @return {@code true} if equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ListType))
            return false;
        return elementType.equals(((ListType)obj).elementType);
    }

    /**
     * Returns a string representation of the list type.
     * <p>
     * The format matches test expectations, e.g.:
     * <pre>
     * [ int ]
     * [ bool ]
     * [ real ]
     *
     * @return the formatted list type string
     */
    @Override
    public String toString()
    {
        return "[ " + elementType.toString() + " ]";
    }
}
