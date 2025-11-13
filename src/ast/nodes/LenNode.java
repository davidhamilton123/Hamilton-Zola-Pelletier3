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

/**
 * Represents the {@code len(<list>)} operation in MFL.
 * Computes the length of a list expression.
 */
public final class LenNode extends SyntaxNode {
    private final SyntaxNode expr;

    /**
     * Constructs a length operation node.
     *
     * @param expr the list expression whose length is being computed
     * @param lineNumber the source line number of this node
     */
    public LenNode(SyntaxNode expr, long lineNumber) {
        super(lineNumber);
        this.expr = expr;
    }

    /**
     * Displays the subtree for debugging purposes.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("len(", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Evaluates {@code len(<list>)}.
     *
     * @param env the runtime environment
     * @return the integer length of the evaluated list
     * @throws EvaluationException if {@code expr} is not a list
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (!(v instanceof LinkedList<?> list)) {
            logError("len expects a list");
            throw new EvaluationException();
        }

        return list.size();
    }

    /**
     * Performs type inference for {@code len(<list>)}.
     * Ensures operand is a list and returns {@code int}.
     *
     * @param tenv the type environment
     * @param inferencer the type inferencer
     * @return {@link IntType}
     * @throws TypeException if the operand is not a list
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type exprType = expr.typeOf(tenv, inferencer);

        VarType elemType = tenv.getTypeVariable();
        ListType listOfElem = new ListType(elemType);

        inferencer.unify(exprType, listOfElem, buildErrorMessage("len expects a list"));

        return new IntType();
    }
}