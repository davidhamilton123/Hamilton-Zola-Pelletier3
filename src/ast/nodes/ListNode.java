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

public final class ListNode extends SyntaxNode {
    private final LinkedList<SyntaxNode> elems;

    public ListNode(LinkedList<SyntaxNode> elems, long lineNumber) {
        super(lineNumber);
        this.elems = elems;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("List[", indentAmt);
        for (SyntaxNode e : elems) e.displaySubtree(indentAmt + 2);
        printIndented("]", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        LinkedList<Object> out = new LinkedList<>();
        for (SyntaxNode e : elems) out.add(e.evaluate(env));
        return out;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        VarType elemType = tenv.getTypeVariable();
        for (SyntaxNode e : elems) {
            Type t = e.typeOf(tenv, inferencer);
            inferencer.unify(t, elemType, buildErrorMessage("list elements must match"));
        }
        return new ListType(elemType);
    }
}
