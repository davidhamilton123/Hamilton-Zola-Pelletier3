package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;

public class LenNode extends SyntaxNode {
    private final SyntaxNode expr;

    public LenNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list))
            throw new EvaluationException();

        return Integer.valueOf(list.size());
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type exprType = expr.typeOf(tenv, inferencer);
        VarType elemType = tenv.getTypeVariable();

        inferencer.unify(exprType, new ListType(elemType), "len expects a list");

        // The length of a list is always an integer.
        return new IntType();
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("len", indentAmt);
        expr.displaySubtree(indentAmt + 2);
    }
}
