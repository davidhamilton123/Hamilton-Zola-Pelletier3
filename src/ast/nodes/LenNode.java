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

public final class LenNode extends SyntaxNode {
    private final SyntaxNode expr;

    public LenNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("len(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list)) {
            logError("len expects a list");
            throw new EvaluationException();
        }

        return list.size();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type exprType = expr.typeOf(tenv, inferencer);

        VarType elemType = tenv.getTypeVariable();
        ListType listOfElem = new ListType(elemType);

        inferencer.unify(exprType, listOfElem, buildErrorMessage("len expects a list"));

        return new IntType();
    }
}
