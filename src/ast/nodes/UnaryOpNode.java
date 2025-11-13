package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.TokenType;

/**
 * Represents a unary operation node.
 * 
 * @author Zach Kissel
 */
public final class UnaryOpNode extends SyntaxNode {
    private final SyntaxNode expr;
    private final TokenType op;

    /**
     * Creates a new unary operation node.
     * 
     * @param expr the operand.
     * @param op the unary operator.
     * @param line the line number.
     */
    public UnaryOpNode(SyntaxNode expr, TokenType op, long line) {
        super(line);
        this.expr = expr;
        this.op = op;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("UnaryOp[" + op + "](", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Evaluates the unary operation.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object val = expr.evaluate(env);

        if (op == TokenType.NOT) {
            if (val instanceof Boolean)
                return !((Boolean) val);
            logError("not expects a boolean.");
            throw new EvaluationException();
        } else if (op == TokenType.SUB) {
            if (val instanceof Integer)
                return -((Integer) val);
            else if (val instanceof Double)
                return -((Double) val);
            else {
                logError("Unary - expects numeric operands.");
                throw new EvaluationException();
            }
        } else {
            logError("Unknown unary operator: " + op);
            throw new EvaluationException();
        }
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type t = expr.typeOf(tenv, inferencer);

        if (op == TokenType.NOT) {
            if (t instanceof BoolType)
                return new BoolType();
            throw new TypeException(buildErrorMessage("not expects a boolean."));
        } else if (op == TokenType.SUB) {
            if (t instanceof IntType)
                return new IntType();
            else if (t instanceof RealType)
                return new RealType();
            throw new TypeException(buildErrorMessage("Unary - expects numeric operands."));
        }

        throw new TypeException(buildErrorMessage("Unknown unary operator: " + op));
    }
}
