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
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.TokenType;
import java.util.LinkedList;
import ast.typesystem.types.IntType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.VarType;

/**
 * This node represents a binary operation.
 * 
 * @author Zach Kissel
 */
public final class BinOpNode extends SyntaxNode {
    private TokenType op;
    private SyntaxNode leftTerm;
    private SyntaxNode rightTerm;

    /**
     * Constructs a new binary operation syntax node.
     * 
     * @param lterm the left operand.
     * @param op    the binary operation to perform.
     * @param rterm the right operand.
     * @param line  the line of code the node is associated with.
     */
    public BinOpNode(SyntaxNode lterm, TokenType op, SyntaxNode rterm,
            long line) {
        super(line);
        this.op = op;
        this.leftTerm = lterm;
        this.rightTerm = rterm;
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
    public void displaySubtree(int indentAmt) {
        printIndented("BinOp[" + op + "](", indentAmt);
        leftTerm.displaySubtree(indentAmt + 2);
        rightTerm.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Evaluate the node.
     * 
     * @param env the executional environment we should evaluate the node under.
     * @return the object representing the result of the evaluation.
     * @throws EvaluationException if the evaluation fails.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object lval;
        Object rval;
        boolean useDouble = false;

        lval = leftTerm.evaluate(env);
        rval = rightTerm.evaluate(env);

        // ---------- List concatenation (++) ----------
        if (op == TokenType.CONCAT) {
            if (!(lval instanceof LinkedList<?>) || !(rval instanceof LinkedList<?>))
                throw new EvaluationException();

            LinkedList<?> leftList = (LinkedList<?>) lval;
            LinkedList<?> rightList = (LinkedList<?>) rval;

            // First non-null element class on each side (homogeneity check)
            Class<?> leftElemType = null;
            for (Object o : leftList) {
                if (o != null) {
                    leftElemType = o.getClass();
                    break;
                }
            }
            Class<?> rightElemType = null;
            for (Object o : rightList) {
                if (o != null) {
                    rightElemType = o.getClass();
                    break;
                }
            }

            if (leftElemType != null && rightElemType != null && !leftElemType.equals(rightElemType))
                throw new EvaluationException();

            LinkedList<Object> out = new LinkedList<>();
            out.addAll((LinkedList<?>) leftList);
            out.addAll((LinkedList<?>) rightList);
            return out;
        }

        // Make sure the type is sound.
        if (!(lval instanceof Integer || lval instanceof Double
                || lval instanceof Boolean)
                && !(rval instanceof Double || rval instanceof Integer
                        || lval instanceof Boolean))
            throw new EvaluationException();

        if (lval.getClass() != rval.getClass()) {
            logError("mixed type expression.");
            throw new EvaluationException();
        }
        if (lval instanceof Double)
            useDouble = true;

        // Perform the operation base on the type.
        switch (op) {
            case ADD:
                if (useDouble)
                    return (Double) lval + (Double) rval;
                return (Integer) lval + (Integer) rval;
            case SUB:
                if (useDouble)
                    return (Double) lval - (Double) rval;
                return (Integer) lval - (Integer) rval;
            case MULT:
                if (useDouble)
                    return (Double) lval * (Double) rval;
                return (Integer) lval * (Integer) rval;
            case DIV:
                if (useDouble)
                    return (Double) lval / (Double) rval;
                return (Integer) lval / (Integer) rval;
            case MOD:
                if (useDouble) {
                    logError("Error: Mod requires integer arguments.");
                    throw new EvaluationException();
                }
                return (Integer) lval % (Integer) rval;
            case AND:
                return (Boolean) lval && (Boolean) rval;
            case OR:
                return (Boolean) lval || (Boolean) rval;
            default:
                throw new EvaluationException();
        }

    }

    /**
     * Determine the type of the syntax node. In particluar bool, int, real,
     * generic, or function.
     * 
     * @param tenv       the type environment.
     * @param inferencer the type inferencer
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    @Override
public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException
{
    // Determine the type of each operand.
    Type leftType  = leftTerm.typeOf(tenv, inferencer);
    Type rightType = rightTerm.typeOf(tenv, inferencer);

    // Unify left and right sides first.
    inferencer.unify(leftType, rightType, "Binary operand type mismatch");

    // Apply substitutions to get the most specific (resolved) types.
    leftType  = inferencer.getSubstitutions().apply(leftType);
    rightType = inferencer.getSubstitutions().apply(rightType);

    // Dispatch based on the operator type.
    switch (op)
    {
        // ---------- Arithmetic operators ----------
        case ADD:
        case SUB:
        case MULT:
        case DIV:
            if (leftType instanceof IntType || leftType instanceof RealType)
                return leftType;
            throw new TypeException("Arithmetic operations require numeric types.");

        // ---------- Modulus ----------
        case MOD:
            if (leftType instanceof IntType)
                return new IntType();
            throw new TypeException("mod requires integer operands.");

        // ---------- Logical operators ----------
        case AND:
        case OR:
            if (leftType instanceof BoolType)
                return new BoolType();
            throw new TypeException("Logical operations require boolean operands.");

        // ---------- List concatenation (++) ----------
        case CONCAT:
        {
            VarType elem = tenv.getTypeVariable(); // fresh type variable
            inferencer.unify(leftType,  new ListType(elem), "Left operand must be a list");
            inferencer.unify(rightType, new ListType(elem), "Right operand must be a list");
            return inferencer.getSubstitutions().apply(leftType);
        }

        // ---------- Relational operators (<, >, <=, >=, =, !=) ----------
        case LT:
        case GT:
        case LTE:
        case GTE:
        case EQ:
        case NEQ:
            if (leftType instanceof IntType || leftType instanceof RealType)
                return new BoolType();
            throw new TypeException("Relational operations require numeric operands.");

        default:
            throw new TypeException("Unknown binary operator: " + op);
    }
}

}
