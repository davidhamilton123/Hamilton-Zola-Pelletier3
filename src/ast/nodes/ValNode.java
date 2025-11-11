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
import lexer.Token;

/**
 * Global value declaration
 *
 * Syntax:
 *   val x := expr;
 */
public final class ValNode extends SyntaxNode {
    private final Token name;
    private final SyntaxNode expr;

    public ValNode(Token name, SyntaxNode expr, long line) {
        super(line);
        this.name = name;
        this.expr = expr;
    }

    /** Expose the name token so ProgNode can bind globals in the type env. */
    public Token getNameToken() {
        return name;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Val[" + name.getValue() + "](", indentAmt);
        expr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }

    /**
     * Runtime semantics:
     * evaluate expr, bind it to x if x is not already defined.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object val = expr.evaluate(env);
        if (env.lookup(name) == null) {
            env.updateEnvironment(name, val);
        } else {
            logError(name.getValue() + " already defined.");
            throw new EvaluationException();
        }
        return name.getValue();
    }

    /**
     * Type checking:
     * ProgNode inserts globals into the type environment.
     * Here we only compute and return the expression type.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        return expr.typeOf(tenv, inferencer);
    }
}
