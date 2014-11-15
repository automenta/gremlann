package syncleus.gremlann.topology.adjacency;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Uses Guava Table to implement a sparse, growable adjacency matrix
 */
public class TableAdjacency<X> implements Adjacency<X> {

    public final Table<Vertex,Vertex,X> table;
    private final HashSet inputs;
    private final HashSet outputs;
    
    public TableAdjacency() {        
        this.table = HashBasedTable.create();
        this.inputs = new HashSet();
        this.outputs = new HashSet();        
    }

    public void addInput(Vertex v) {
        inputs.add(v);
    }
    public void addOutput(Vertex v) {
        outputs.add(v);
    }
    
    @Override
    public Set<Vertex> getInputs() {
        return inputs;
    }

    @Override
    public Set<Vertex> getOutputs() {
        return outputs;
    }

    @Override
    public X get(Vertex source, Vertex target) {
        return table.get(source, target);
    }

    @Override
    public void set(Vertex source, Vertex target, X x) {
        inputs.add(source);
        outputs.add(target);
        table.put(source, target, x);
    }
    
    /** avoids adding source and target to the inputs/outputs list (assuming they are already in them)
     *  for faster updates */
    public void setDirect(Vertex source, Vertex target, X x) {
        table.put(source, target, x);
    }

    @Override
    public EdgeAdjacency toEdges(Graph g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Serializable toCompact() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Table<Vertex, Vertex, X> getTable() {
        return table;
    }

    public void addInputs(List<Vertex> inputs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
