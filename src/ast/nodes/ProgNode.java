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

import java.util.LinkedList;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Program root node
 */
public final class ProgNode extends SyntaxNode {
    private final LinkedList<SyntaxNode> exprs;

    public ProgNode(LinkedList<SyntaxNode> exprs, long line) {
        super(line);
        this.exprs = exprs;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Prog(", indentAmt);
        for (SyntaxNode expr : exprs)
            expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object res = null;
        for (SyntaxNode expr : exprs)
            res = expr.evaluate(env);
        return res;
    }

    /**
     * Type pass for the whole program
     * Binds global vals into the type environment as they appear
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        if (exprs.isEmpty())
            throw new TypeException("Invalid expression.");

        Type lastType = null;

        for (SyntaxNode ex : exprs) {
            // type the node
            Type t = ex.typeOf(tenv, inferencer);

            // if it is a global val, insert into the type environment now
            if (ex instanceof ValNode) {
                ValNode v = (ValNode) ex;
                if (tenv.lookup(v.getNameToken()) == null) {
                    tenv.updateEnvironment(v.getNameToken(), t);
                } else {
                    throw new TypeException(buildErrorMessage(v.getNameToken().getValue() + " already defined."));
                }
            }

            lastType = t;
        }

        return inferencer.getSubstitutions().apply(lastType);
    }

    /**
     * Boolean style type check used by file mode
     */
    public boolean typeCheck(TypeEnvironment tenv, Inferencer inferencer) {
        try {
            typeOf(tenv, inferencer);
            return true;
        } catch (TypeException ex) {
            System.out.println("Type Error: " + ex.getMessage());
            return false;
        }
    }
}
