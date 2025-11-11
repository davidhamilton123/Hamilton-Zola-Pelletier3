package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Base class for all AST nodes.
 * Phase 3 uses the typeOf method with a TypeEnvironment and an Inferencer.
 */
public abstract class SyntaxNode {
    // Source line number for diagnostics
    private final long lineNumber;

    /** Construct a node occurring on the given source line. */
    public SyntaxNode(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    /** Allow subclasses to read the line number for error reporting. */
    protected long lineNumber() {
        return lineNumber;
    }

    /** Print a formatted error tied to this node’s line. */
    protected void logError(String msg) {
        System.out.println("Error (line " + lineNumber + "): " + msg);
    }

    /** Build a standard error message with the line number. */
    protected String buildErrorMessage(String msg) {
        return "(line " + lineNumber + ") " + msg;
    }

    /** Print an indented message (debug tree printing helper). */
    protected void printIndented(String msg, int indentAmt) {
        for (int i = 0; i < indentAmt; i++) System.out.print(" ");
        System.out.println(msg);
    }

    /** Evaluate the node in the execution environment. */
    public abstract Object evaluate(Environment env) throws EvaluationException;

    /**
     * Phase 3 typing entry point.
     * Return this node’s static type using the type environment and inferencer.
     */
    public abstract Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException;

    /** Pretty print this node’s subtree with the given indentation. */
    public abstract void displaySubtree(int indentAmt);
}
