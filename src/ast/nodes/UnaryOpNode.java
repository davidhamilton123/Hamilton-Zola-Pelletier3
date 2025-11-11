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

public final class UnaryOpNode extends SyntaxNode {
    private final TokenType op;
    private final SyntaxNode expr;

    public UnaryOpNode(SyntaxNode expr, TokenType op, long line) {
        super(line);
        this.op = op;
        this.expr = expr;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Unary[" + op + "](", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object v = expr.evaluate(env);

        if (op == TokenType.NOT) {
            if (v instanceof Boolean) return !((Boolean) v);
            logError("not expects a boolean");
            throw new EvaluationException();
        }

        if (op == TokenType.SUB) {
            if (v instanceof Integer) return -((Integer) v);
            if (v instanceof Double)  return -((Double) v);
            logError("negation expects a number");
            throw new EvaluationException();
        }

        logError("unknown unary operator " + op);
        throw new EvaluationException();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type t = expr.typeOf(tenv, inferencer);

        if (op == TokenType.NOT) {
            if (t instanceof BoolType) return new BoolType();
            throw new TypeException(buildErrorMessage("not expects a boolean"));
        }

        if (op == TokenType.SUB) {
            if (t instanceof IntType)  return new IntType();
            if (t instanceof RealType) return new RealType();
            throw new TypeException(buildErrorMessage("negation expects a number"));
        }

        throw new TypeException(buildErrorMessage("unknown unary operator " + op));
    }
}
