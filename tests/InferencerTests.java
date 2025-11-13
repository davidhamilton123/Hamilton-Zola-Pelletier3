import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.inferencer.Substitutions;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.VarType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import ast.typesystem.TypeException;

/**
 * Unit tests for the Inferencer and Substitutions classes.
 */
public class InferencerTests {

    @Test
    public void unifyVarToIntUpdatesSubstitution() throws TypeException {
        Inferencer inf = new Inferencer();
        VarType v = new VarType(1);
        inf.unify(v, new IntType(), "bind var to int");

        Substitutions s = inf.getSubstitutions();
        Type applied = s.apply(v);
        assertTrue("Substitution should map var to IntType", applied instanceof IntType);
        assertEquals("int", applied.toString());
    }

    @Test(expected = TypeException.class)
    public void unifyIncompatibleTypesThrows() throws TypeException {
        Inferencer inf = new Inferencer();
        // unify int and real should fail
        inf.unify(new IntType(), new RealType(), "int vs real");
    }

    @Test
    public void propagateListSubstitution() throws TypeException {
        Inferencer inf = new Inferencer();
        VarType v = new VarType(2);
        // unify list of v with list of int -> v := int
        ListType lt1 = new ListType(v);
        ListType lt2 = new ListType(new IntType());
        inf.unify(lt1, lt2, "list unify");

        Type after = inf.getSubstitutions().apply(v);
        assertTrue(after instanceof IntType);
    }
}
