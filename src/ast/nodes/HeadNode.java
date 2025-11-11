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

public final class HeadNode extends SyntaxNode {
    private final SyntaxNode expr;

    public HeadNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("hd(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list)) {
            logError("hd expects a list");
            throw new EvaluationException();
        }
        if (list.isEmpty()) {
            logError("hd on empty list");
            throw new EvaluationException();
        }

        return list.getFirst();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        // infer type of the list expression
        Type exprType = expr.typeOf(tenv, inferencer);

        // fresh element type and list-of-element type
        VarType elemType = tenv.getTypeVariable();
        ListType listOfElem = new ListType(elemType);

        // unify operand with a list type
        inferencer.unify(exprType, listOfElem, buildErrorMessage("hd expects a list"));

        // head returns the element type after applying substitutions
        return inferencer.getSubstitutions().apply(elemType);
    }
}
