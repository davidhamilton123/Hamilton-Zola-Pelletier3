package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.IntType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

public class LenNode extends SyntaxNode
{
    private final SyntaxNode expr;

    public LenNode(SyntaxNode expr, long lineNumber)
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

        return Integer.valueOf(list.size());
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
    {
        return new IntType();
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("len", indentAmt);
        expr.displaySubtree(indentAmt + 2);
    }
}
