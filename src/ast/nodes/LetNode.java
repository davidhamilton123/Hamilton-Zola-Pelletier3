package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * let x = bound in body
 * No scope object available, so we:
 *  - save any prior binding for x,
 *  - bind x to the new value,
 *  - evaluate the body,
 *  - restore the prior binding (or clear by setting null if none existed).
 */
public final class LetNode extends SyntaxNode {
    private final Token name;
    private final SyntaxNode bound;
    private final SyntaxNode body;

    public LetNode(Token name, SyntaxNode bound, SyntaxNode body, long lineNumber) {
        super(lineNumber);
        this.name = name;
        this.bound = bound;
        this.body = body;
    }

    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("let " + name.getValue() + " =", indentAmt);
        bound.displaySubtree(indentAmt + 2);
        printIndented("in", indentAmt);
        body.displaySubtree(indentAmt + 2);
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        // save previous binding if any
        Object old = env.lookup(name);
        boolean had = (old != null);

        // bind to new value
        Object val = bound.evaluate(env);
        env.updateEnvironment(name, val);

        try {
            return body.evaluate(env);
        } finally {
            // restore the prior binding
            if (had) {
                env.updateEnvironment(name, old);
            } else {
                // no remove available in your Environment, so clear by setting null
                // your env.lookup returns null for "not bound", so this mimics unbinding
                env.updateEnvironment(name, null);
            }
        }
    }
    /**
     * Type checking:
     *  - type the bound expression,
     *  - extend the type environment by shadowing the name for the body,
     *  - type the body,
     *  - restore the prior binding (or clear by setting null if none existed).
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        Type boundType = bound.typeOf(tenv, inferencer);

        // extend type environment by shadowing the name for the body
        Type oldT = tenv.lookup(name);
        boolean had = (oldT != null);
        tenv.updateEnvironment(name, boundType);

        try {
            return body.typeOf(tenv, inferencer);
        } finally {
            if (had) {
                tenv.updateEnvironment(name, oldT);
            } else {
                // mirror the runtime trick, clear the temporary binding
                tenv.updateEnvironment(name, null);
            }
        }
    }
}
