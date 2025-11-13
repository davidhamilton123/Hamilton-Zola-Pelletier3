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
 * Represents the {@code hd(<list>)} operation in MFL.
 * Extracts the first element of a list.
 */
public final class HeadNode extends SyntaxNode {
    private final SyntaxNode expr;

    /**
     * Constructs a head operation node.
     *
     * @param expr the list expression whose head is being extracted
     * @param lineNumber the source line number of this node
     */
    public HeadNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    /**
     * Displays the subtree for debugging purposes.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("hd(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Evaluates {@code hd(<list>)}.
     *
     * @param env the runtime environment
     * @return the first element of the evaluated list
     * @throws EvaluationException if the operand is not a list or is empty
     */
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

    /**
     * Performs type inference for {@code hd(<list>)}.
     * Ensures operand is a list and returns the element type.
     *
     * @param tenv the type environment
     * @param inferencer the type inferencer
     * @return the element type of the list
     * @throws TypeException if operand is not a list
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type exprType = expr.typeOf(tenv, inferencer);

        VarType elemType = tenv.getTypeVariable();
        ListType listOfElem = new ListType(elemType);

        inferencer.unify(exprType, listOfElem, buildErrorMessage("hd expects a list"));

        return inferencer.getSubstitutions().apply(elemType);
    }
}
