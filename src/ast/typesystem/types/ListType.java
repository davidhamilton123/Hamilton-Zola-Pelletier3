package ast.typesystem.types;

public class ListType extends Type
{
    private Type elementType;

    public ListType(Type elementType)
    {
        this.elementType = elementType;
    }

    public Type getElementType()
    {
        return elementType;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ListType))
            return false;
        return elementType.equals(((ListType)obj).elementType);
    }

    @Override
    public String toString()
    {
        return "[ " + elementType.toString() + " ]";
    }
}
