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
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * A leaf node that holds a single token.
 * Used for identifiers and literals.
 */
public final class TokenNode extends SyntaxNode {
    private final Token tok;

    public TokenNode(Token tok, long line) {
        super(line);
        this.tok = tok;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("Token[" + tok.getValue() + "]", indentAmt);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        // First try identifier lookup
        Object bound = env.lookup(tok);
        if (bound != null) return bound;

        // Otherwise interpret as a literal at runtime
        String v = tok.getValue();

        if ("true".equals(v))  return Boolean.TRUE;
        if ("false".equals(v)) return Boolean.FALSE;

        // integer literal
        if (v.matches("[0-9]+")) {
            try { return Integer.parseInt(v); }
            catch (NumberFormatException e) { /* fall through to real */ }
        }

        // real literal
        if (v.matches("([0-9]+\\.[0-9]+)([eE][+-]?[0-9]+)?")) {
            try { return Double.parseDouble(v); }
            catch (NumberFormatException e) { /* fall through */ }
        }

        // If we reach here it is an unbound identifier
        logError(tok.getValue() + " is not defined.");
        throw new EvaluationException();
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        // Identifier in the type environment
        Type t = tenv.lookup(tok);
        if (t != null) return t;

        // Otherwise treat as a literal and return its concrete type
        String v = tok.getValue();

        if ("true".equals(v) || "false".equals(v)) {
            return new BoolType();
        }

        if (v.matches("[0-9]+")) {
            return new IntType();
        }

        if (v.matches("([0-9]+\\.[0-9]+)([eE][+-]?[0-9]+)?")) {
            return new RealType();
        }

        // Not in env and not a literal means unbound variable
        throw new TypeException(buildErrorMessage(tok.getValue() + " is not defined."));
    }
}
