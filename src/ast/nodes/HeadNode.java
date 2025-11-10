package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
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
    // Determine the type of the expression.
    Type exprType = expr.typeOf(tenv, inferencer);

    // Create a fresh type variable for the list element.
    VarType elemType = tenv.getTypeVariable();

    // Unify expression with a list type.
    inferencer.unify(exprType, new ListType(elemType), "hd expects a list");

    // Return the element type (most specific form).
    return inferencer.getSubstitutions().apply(elemType);
}


    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("hd", indentAmt);
        expr.displaySubtree(indentAmt + 2);
    }
}
