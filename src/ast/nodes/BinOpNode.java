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

/** Binary operations with proper Phase 3 typing and numeric promotion. */
public final class BinOpNode extends SyntaxNode {
    private final TokenType op;
    private final SyntaxNode leftTerm;
    private final SyntaxNode rightTerm;

    public BinOpNode(SyntaxNode lterm, TokenType op, SyntaxNode rterm, long line) {
        super(line);
        this.op = op;
        this.leftTerm = lterm;
        this.rightTerm = rterm;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("BinOp[" + op + "](", indentAmt);
        leftTerm.displaySubtree(indentAmt + 2);
        rightTerm.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    // ---------- evaluation with numeric promotion ----------
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object lval = leftTerm.evaluate(env);
        Object rval = rightTerm.evaluate(env);

        // list concatenation
        if (op == TokenType.CONCAT) {
            if (!(lval instanceof LinkedList<?>) || !(rval instanceof LinkedList<?>)) {
                logError("++ expects two lists");
                throw new EvaluationException();
            }
            LinkedList<Object> out = new LinkedList<>((LinkedList<?>) lval);
            out.addAll((LinkedList<?>) rval);
            return out;
        }

        // logical
        if (op == TokenType.AND || op == TokenType.OR) {
            if (!(lval instanceof Boolean) || !(rval instanceof Boolean)) {
                logError("logical ops expect booleans");
                throw new EvaluationException();
            }
            return (op == TokenType.AND)
                    ? ((Boolean) lval && (Boolean) rval)
                    : ((Boolean) lval || (Boolean) rval);
        }

        // numeric promotion int -> real when mixed
        boolean leftNum = lval instanceof Integer || lval instanceof Double;
        boolean rightNum = rval instanceof Integer || rval instanceof Double;
        if (!leftNum || !rightNum) {
            logError(op + " expects numbers");
            throw new EvaluationException();
        }
        double ld = (lval instanceof Integer) ? ((Integer) lval).doubleValue() : (Double) lval;
        double rd = (rval instanceof Integer) ? ((Integer) rval).doubleValue() : (Double) rval;

        switch (op) {
            case ADD:  return (isInt(lval) && isInt(rval)) ? ((Integer) lval + (Integer) rval) : ld + rd;
            case SUB:  return (isInt(lval) && isInt(rval)) ? ((Integer) lval - (Integer) rval) : ld - rd;
            case MULT: return (isInt(lval) && isInt(rval)) ? ((Integer) lval * (Integer) rval) : ld * rd;
            case DIV:  return (isInt(lval) && isInt(rval)) ? ((Integer) lval / (Integer) rval) : ld / rd;
            case MOD:
                if (isInt(lval) && isInt(rval)) return (Integer) lval % (Integer) rval;
                logError("mod requires integer operands");
                throw new EvaluationException();
            case LT:   return ld <  rd;
            case GT:   return ld >  rd;
            case LTE:  return ld <= rd;
            case GTE:  return ld >= rd;
            case EQ:   return ld == rd;
            case NEQ:  return ld != rd;
            default:
                logError("unknown binary operator " + op);
                throw new EvaluationException();
        }
    }

    private static boolean isInt(Object o) { return o instanceof Integer; }

    // ---------- typing with numeric promotion rules ----------
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type lt = leftTerm.typeOf(tenv, inferencer);
        Type rt = rightTerm.typeOf(tenv, inferencer);

        switch (op) {
            // arithmetic: both numeric, result real if either real else int
            case ADD: case SUB: case MULT: case DIV: {
                if (!(isNumeric(lt) && isNumeric(rt)))
                    throw new TypeException(buildErrorMessage(op + " expects numbers"));
                if (lt instanceof RealType || rt instanceof RealType) return new RealType();
                return new IntType();
            }
            case MOD: {
                if (lt instanceof IntType && rt instanceof IntType) return new IntType();
                throw new TypeException(buildErrorMessage("mod requires integer operands"));
            }
            // logical
            case AND: case OR: {
                if (lt instanceof BoolType && rt instanceof BoolType) return new BoolType();
                throw new TypeException(buildErrorMessage("logical ops expect booleans"));
            }
            // list concat: both lists of same element type
            case CONCAT: {
                VarType elem = tenv.getTypeVariable();
                inferencer.unify(lt, new ListType(elem), buildErrorMessage("++ left expects list"));
                inferencer.unify(rt, new ListType(elem), buildErrorMessage("++ right expects list"));
                return new ListType(elem);
            }
            // relations: numeric -> bool
            case LT: case GT: case LTE: case GTE: case EQ: case NEQ: {
                if (isNumeric(lt) && isNumeric(rt)) return new BoolType();
                throw new TypeException(buildErrorMessage("relational ops expect numbers"));
            }
            default:
                throw new TypeException(buildErrorMessage("unknown binary operator " + op));
        }
    }

    private static boolean isNumeric(Type t) {
        return t instanceof IntType || t instanceof RealType;
    }
}
