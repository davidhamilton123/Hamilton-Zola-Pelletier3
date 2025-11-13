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

/**
 * Represents a list literal, e.g. {@code [1, 2, 3]}.
 */
public final class ListNode extends SyntaxNode {
    private final LinkedList<SyntaxNode> elems;

    /**
     * Constructs a list literal node.
     *
     * @param elems the list of element expression nodes
     * @param lineNumber the source line number
     */
    public ListNode(LinkedList<SyntaxNode> elems, long lineNumber) {
        super(lineNumber);
        this.elems = elems;
    }

    /**
     * Displays the subtree for debugging.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("List[", indentAmt);
        for (SyntaxNode e : elems) e.displaySubtree(indentAmt + 2);
        printIndented("]", indentAmt);
    }

    /**
     * Evaluates the list literal by evaluating each element.
     *
     * @param env the runtime environment
     * @return a new LinkedList of evaluated element values
     * @throws EvaluationException if any element evaluation fails
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        LinkedList<Object> out = new LinkedList<>();
        for (SyntaxNode e : elems) out.add(e.evaluate(env));
        return out;
    }

    /**
     * Performs type inference for the list literal.
     * All elements must unify to the same type.
     *
     * @param tenv the type environment
     * @param inferencer the type inferencer
     * @return a {@link ListType} representing a list of element type
     * @throws TypeException if elements do not match in type
     */
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
