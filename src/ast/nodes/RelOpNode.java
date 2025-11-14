/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.TokenType;

/**
 * Relational operator node.
 * Supports: <  >  <=  >=  =  <>
 */
public final class RelOpNode extends SyntaxNode {
    private final TokenType op;
    private final SyntaxNode leftTerm;
    private final SyntaxNode rightTerm;

    public RelOpNode(SyntaxNode lterm, TokenType op, SyntaxNode rterm, long line) {
        super(line);
        this.op = op;
        this.leftTerm = lterm;
        this.rightTerm = rterm;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("RelOp[" + op + "](", indentAmt);
        leftTerm.displaySubtree(indentAmt + 2);
        rightTerm.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object lv = leftTerm.evaluate(env);
        Object rv = rightTerm.evaluate(env);

        switch (op) {
            // numeric comparisons
            case LT:
            case GT:
            case LTE:
            case GTE: {
                if (!isNumber(lv) || !isNumber(rv)) {
                    logError(op + " expects numeric operands");
                    throw new EvaluationException();
                }
                double a = toDouble(lv);
                double b = toDouble(rv);
                switch (op) {
                    case LT:  return a <  b;
                    case GT:  return a >  b;
                    case LTE: return a <= b;
                    case GTE: return a >= b;
                    default:  throw new EvaluationException();
                }
            }

            // equality
            case EQ:
                if (isNumber(lv) && isNumber(rv)) {
                    return toDouble(lv) == toDouble(rv);
                }
                return lv != null ? lv.equals(rv) : rv == null;

            case NEQ:
                if (isNumber(lv) && isNumber(rv)) {
                    return toDouble(lv) != toDouble(rv);
                }
                return !(lv != null ? lv.equals(rv) : rv == null);

            default:
                logError("unknown relational operator " + op);
                throw new EvaluationException();
        }
    }

    private static boolean isNumber(Object o) {
        return o instanceof Integer || o instanceof Double;
    }

    private static double toDouble(Object v) {
        return (v instanceof Integer) ? ((Integer) v).doubleValue() : (Double) v;
    }
    /**
     * Performs type inference for the relational operation.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type lt = leftTerm.typeOf(tenv, inferencer);
        Type rt = rightTerm.typeOf(tenv, inferencer);

        switch (op) {
            // numeric relational operators
            case LT:
            case GT:
            case LTE:
            case GTE: {
                // promote to real if either side is real, otherwise int
                if (lt instanceof RealType || rt instanceof RealType) {
                    RealType r = new RealType();
                    inferencer.unify(lt, r, buildErrorMessage(op + " expects numbers"));
                    inferencer.unify(rt, r, buildErrorMessage(op + " expects numbers"));
                } else {
                    IntType i = new IntType();
                    inferencer.unify(lt, i, buildErrorMessage(op + " expects numbers"));
                    inferencer.unify(rt, i, buildErrorMessage(op + " expects numbers"));
                }
                return new BoolType();
            }

            // equality and inequality
            case EQ:
            case NEQ: {
                // allow numeric mixing by promoting to real
                if ((lt instanceof IntType && rt instanceof RealType)
                        || (lt instanceof RealType && rt instanceof IntType)
                        || (lt instanceof RealType && rt instanceof RealType)) {
                    RealType r = new RealType();
                    inferencer.unify(lt, r, buildErrorMessage(op + " numeric comparison"));
                    inferencer.unify(rt, r, buildErrorMessage(op + " numeric comparison"));
                } else {
                    // otherwise require same type via a fresh type variable
                    VarType a = tenv.getTypeVariable();
                    inferencer.unify(lt, a, buildErrorMessage(op + " requires compatible types"));
                    inferencer.unify(rt, a, buildErrorMessage(op + " requires compatible types"));
                }
                return new BoolType();
            }

            default:
                throw new TypeException(buildErrorMessage("unknown relational operator " + op));
        }
    }
}
