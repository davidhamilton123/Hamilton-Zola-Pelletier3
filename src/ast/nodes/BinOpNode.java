package ast.nodes;

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.TokenType;

/**
 * Represents a binary operation node.
 * 
 * @author Zach Kissel
 */
public final class BinOpNode extends SyntaxNode {
    private final SyntaxNode leftTerm;
    private final SyntaxNode rightTerm;
    private final TokenType op;

    /**
     * Creates a new binary operation node.
     * 
     * @param leftTerm the left-hand side operand.
     * @param op the operator token.
     * @param rightTerm the right-hand side operand.
     * @param line the line number.
     */
    public BinOpNode(SyntaxNode leftTerm, TokenType op, SyntaxNode rightTerm, long line) {
        super(line);
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
        this.op = op;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("BinOp[" + op + "](", indentAmt);
        leftTerm.displaySubtree(indentAmt + 2);
        rightTerm.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Evaluates the binary operation.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object lval = leftTerm.evaluate(env);
        Object rval = rightTerm.evaluate(env);

        // Handle list concatenation.
        if (op == TokenType.CONCAT) {
            if (!(lval instanceof LinkedList<?>) || !(rval instanceof LinkedList<?>)) {
                logError("++ expects two lists.");
                throw new EvaluationException();
            }
            LinkedList<Object> newList = new LinkedList<>((LinkedList<?>) lval);
            newList.addAll((LinkedList<?>) rval);
            return newList;
        }

        // Handle logical operators.
        if (op == TokenType.AND || op == TokenType.OR) {
            if (!(lval instanceof Boolean) || !(rval instanceof Boolean)) {
                logError("Logical operators expect boolean operands.");
                throw new EvaluationException();
            }
            if (op == TokenType.AND)
                return (Boolean) lval && (Boolean) rval;
            else
                return (Boolean) lval || (Boolean) rval;
        }

        // Handle arithmetic and relational operators.
        boolean leftNum = lval instanceof Integer || lval instanceof Double;
        boolean rightNum = rval instanceof Integer || rval instanceof Double;

        if (!leftNum || !rightNum) {
            logError("Operator " + op + " expects numeric operands.");
            throw new EvaluationException();
        }

        boolean isIntOp = (lval instanceof Integer) && (rval instanceof Integer);
        double ld = (lval instanceof Integer) ? ((Integer) lval).doubleValue() : (Double) lval;
        double rd = (rval instanceof Integer) ? ((Integer) rval).doubleValue() : (Double) rval;

        Object result;

        switch (op) {
            case ADD:
                result = isIntOp ? ((Integer) lval + (Integer) rval) : ld + rd;
                break;
            case SUB:
                result = isIntOp ? ((Integer) lval - (Integer) rval) : ld - rd;
                break;
            case MULT:
                result = isIntOp ? ((Integer) lval * (Integer) rval) : ld * rd;
                break;
            case DIV:
                // If both are ints, perform integer division, otherwise real division.
                result = isIntOp ? ((Integer) lval / (Integer) rval) : ld / rd;
                break;
            case MOD:
                if (isIntOp)
                    result = ((Integer) lval % (Integer) rval);
                else {
                    logError("mod requires integer operands.");
                    throw new EvaluationException();
                }
                break;
            case LT:
                result = ld < rd;
                break;
            case GT:
                result = ld > rd;
                break;
            case LTE:
                result = ld <= rd;
                break;
            case GTE:
                result = ld >= rd;
                break;
            case EQ:
                result = ld == rd;
                break;
            case NEQ:
                result = ld != rd;
                break;
            default:
                logError("Unknown binary operator: " + op);
                throw new EvaluationException();
        }

        // Ensure integer results remain integers.
        if (result instanceof Double && isIntOp) {
            double d = (Double) result;
            if (d == Math.rint(d)) return (int) d;
        }

        return result;
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type lt = leftTerm.typeOf(tenv, inferencer);
        Type rt = rightTerm.typeOf(tenv, inferencer);

        switch (op) {
            case ADD:
            case SUB:
            case MULT:
            case DIV:
                if (!(isNumeric(lt) && isNumeric(rt)))
                    throw new TypeException(buildErrorMessage(op + " expects numeric operands."));
                if (lt instanceof RealType || rt instanceof RealType)
                    return new RealType();
                return new IntType();
            case MOD:
                if (lt instanceof IntType && rt instanceof IntType)
                    return new IntType();
                throw new TypeException(buildErrorMessage("mod requires integer operands."));
            case AND:
            case OR:
                if (lt instanceof BoolType && rt instanceof BoolType)
                    return new BoolType();
                throw new TypeException(buildErrorMessage("Logical operators expect boolean operands."));
            case CONCAT:
                VarType elemType = tenv.getTypeVariable();
                inferencer.unify(lt, new ListType(elemType), buildErrorMessage("++ expects two lists."));
                inferencer.unify(rt, new ListType(elemType), buildErrorMessage("++ expects two lists."));
                return new ListType(elemType);
            case LT:
            case LTE:
            case GT:
            case GTE:
            case EQ:
            case NEQ:
                if (isNumeric(lt) && isNumeric(rt))
                    return new BoolType();
                throw new TypeException(buildErrorMessage("Relational operators expect numeric operands."));
            default:
                throw new TypeException(buildErrorMessage("Unknown binary operator: " + op));
        }
    }

    private boolean isNumeric(Type t) {
        return (t instanceof IntType) || (t instanceof RealType);
    }
}
