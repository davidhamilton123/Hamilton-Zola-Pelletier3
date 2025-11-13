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
 * Represents the {@code tl} (tail) operation node in the abstract syntax tree.
 * <p>
 * The {@code tl} operator returns all but the first element of a list. It is
 * undefined on empty lists and will raise an {@link EvaluationException} if applied
 * to a non-list or an empty list.
 * </p>
 * 
 * @author Zach Kissel
 */
public final class TailNode extends SyntaxNode {
    private final SyntaxNode expr;

    /**
     * Constructs a new {@code TailNode}.
     *
     * @param expr       the expression representing the list operand.
     * @param lineNumber the source code line number of this node.
     */
    public TailNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    /**
     * Displays the subtree for this {@code TailNode}.
     * <p>
     * This method is used for debugging and visualization of the AST. It prints the
     * node and recursively displays its child with indentation.
     * </p>
     *
     * @param indentAmt the indentation level for pretty-printing.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("tl(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Evaluates the {@code tl} operation at runtime.
     * <p>
     * The method retrieves the value of the operand expression, verifies that it is
     * a non-empty {@link LinkedList}, and returns a new list containing all but the
     * first element. If the operand is not a list or is empty, an
     * {@link EvaluationException} is thrown.
     * </p>
     *
     * @param env the runtime environment containing variable bindings.
     * @return a new {@link LinkedList} representing the tail of the evaluated list.
     * @throws EvaluationException if the operand is not a list or is empty.
     */
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

    /**
     * Performs type inference for the {@code tl} operation.
     * <p>
     * This method ensures that the operand expression is of type
     * {@code [T]} (a list of some element type {@code T}) and infers that
     * {@code tl([T])} returns another list of the same element type {@code [T]}.
     * </p>
     *
     * @param tenv        the type environment containing type variable bindings.
     * @param inferencer  the type inferencer used to unify types and track substitutions.
     * @return a {@link ListType} representing a list of the same element type as the operand.
     * @throws TypeException if the operand is not a list type.
     */
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
