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

public class TailNode extends SyntaxNode {
    private final SyntaxNode expr;

    public TailNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list))
            throw new EvaluationException();

        if (list.isEmpty())
            throw new EvaluationException();

        LinkedList<Object> out = new LinkedList<>();
        for (int i = 1; i < list.size(); i++)
            out.add(list.get(i));
        return out;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type exprType = expr.typeOf(tenv, inferencer);
        VarType elemType = tenv.getTypeVariable();

        inferencer.unify(exprType, new ListType(elemType), "tl expects a list");

        // The tail is itself a list of the same element type.
        return inferencer.getSubstitutions().apply(exprType);
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("tl", indentAmt);
        expr.displaySubtree(indentAmt + 2);
    }
}
