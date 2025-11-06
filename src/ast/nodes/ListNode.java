package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

public class ListNode extends SyntaxNode
{
    private final LinkedList<SyntaxNode> elements;

    public ListNode(LinkedList<SyntaxNode> elements, long lineNumber)
    {
        super(lineNumber);
        this.elements = (elements == null) ? new LinkedList<>() : elements;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        LinkedList<Object> evaluated = new LinkedList<>();
        Class<?> elemClass = null;

        for (SyntaxNode n : elements)
        {
            Object v = n.evaluate(env);

            if (elemClass == null && v != null)
                elemClass = v.getClass();
            else if (v != null && !v.getClass().equals(elemClass))
                throw new EvaluationException();

            evaluated.add(v);
        }

        return evaluated;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        if (elements.isEmpty())
            throw new TypeException(buildErrorMessage(
                "Empty list has no inferable element type."));

        Type elementType = elements.getFirst().typeOf(tenv, inferencer);
        for (int i = 1; i < elements.size(); i++)
        {
            Type t = elements.get(i).typeOf(tenv, inferencer);
            inferencer.unify(elementType, t, "List elements must have the same type");
        }
        // Without a dedicated list type, we return the element type.
        return elementType;
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("[", indentAmt);
        for (SyntaxNode n : elements)
            n.displaySubtree(indentAmt + 2);
        printIndented("]", indentAmt);
    }
}
