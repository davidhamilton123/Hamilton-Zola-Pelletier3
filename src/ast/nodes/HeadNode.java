package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

public class HeadNode extends SyntaxNode
{
    private final SyntaxNode expr;

    public HeadNode(SyntaxNode expr, long lineNumber)
    {
        super(lineNumber);
        this.expr = expr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list))
            throw new EvaluationException();

        if (list.isEmpty())
            throw new EvaluationException();

        return list.getFirst();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        // Conservative: return the type of the element expression statically.
        // Runtime enforces that v is a list and has at least one element.
        return expr.typeOf(tenv, inferencer);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("hd", indentAmt);
        expr.displaySubtree(indentAmt + 2);
    }
}
