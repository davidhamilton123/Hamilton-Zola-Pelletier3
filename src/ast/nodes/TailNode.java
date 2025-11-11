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

public final class TailNode extends SyntaxNode {
    private final SyntaxNode expr;

    public TailNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("tl(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list)) {
            logError("tl expects a list");
            throw new EvaluationException();
        }

        if (list.isEmpty()) {
            logError("tl on empty list");
            throw new EvaluationException();
        }

        LinkedList<Object> out = new LinkedList<>();
        for (int i = 1; i < list.size(); i++) {
            out.add(list.get(i));
        }
        return out;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        // infer the type of the operand
        Type exprType = expr.typeOf(tenv, inferencer);

        // create a fresh element type and assert the operand is a list of that type
        VarType elemType = tenv.getTypeVariable();
        ListType listOfElem = new ListType(elemType);

        // unify operand type with list type
        inferencer.unify(exprType, listOfElem, buildErrorMessage("tl expects a list"));

        // tl returns a list of the same element type
        return inferencer.getSubstitutions().apply(listOfElem);
    }
}
