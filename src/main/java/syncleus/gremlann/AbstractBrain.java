/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.ArrayRealVector;
import static syncleus.gremlann.Graphs.printVertex;
import static syncleus.gremlann.Graphs.real;
import static syncleus.gremlann.Graphs.set;
import static syncleus.gremlann.topology.LayerBrain.newSignalVertex;

abstract public class AbstractBrain {

    abstract public GraphTraversal<Vertex,Vertex> traverseNeurons();
    
    public static ArrayRealVector signals(List<Vertex> ll) {
        ArrayRealVector r = new ArrayRealVector(ll.size());
        double[] d = r.getDataRef();
        
        int j = 0;
        for (Vertex o : ll) {
            d[j++] = signal(o);
        }
        return r;        
    } 
    
    public static Edge newSynapse(Vertex source, Vertex target) {
        Edge s = source.addEdge("synapse", target);
        //TODO multiple synapse initial weight methods
        //    private static final Random RANDOM = new Random();
        final double RANGE = 2.0;
        final double OFFSET = -1.0;
        final double SCALE = 0.1;
        set(s, "weight", ((Math.random() * RANGE) + OFFSET) * SCALE);
        return s;
    }

    public static List<Vertex> newNeuronArray(String prefix, Graph g, int size) {
        List<Vertex> l = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            l.add(newNeuronVertex(prefix + "." + i, g));
        }
        return l;
    }

    public static List<Vertex> newSignalArray(String label, Graph g, int size) {
        List<Vertex> l = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            l.add(newSignalVertex(label + "." + i, g));
        }
        return l;
    }

    public static Vertex newSignalVertex(String label, Graph g) {
        return newSignalVertex(label, g, 0);
    }
    
        
    public static Vertex newSignalVertex(String label, Graph g, double initialValue) {
        Vertex v = g.addVertex(label);
        v.property("signal", initialValue);
        v.property("input", true);
        return v;
    }


    public static Vertex newNeuronVertex(String label, Graph g) {
        Vertex v = g.addVertex(label);
        v.property("activity", 0);
        v.property("signal", 0);
        return v;
    }
    
    public void printAllNeurons() {
        traverseNeurons().sideEffect(n -> printVertex(n.get())).properties().sideEffect(prop -> System.out.println("  " + prop)).iterate();
    }
    
    
    public static double weight(Edge e) {  return real(e, "weight");    }        
    public static void weight(Edge e, double newWeight) {  set(e, "weight", newWeight);    }
    
    public static double signal(Element v) {  return real(v, "signal");    }
    public static void signal(Element v, double newValue) {  set(v, "signal", newValue);    }
    
    public static double activity(Vertex v) {  return real(v, "activity",0);    }
    public static void activity(Vertex v, double newValue) {  set(v, "activity", newValue);    }

    public static double normalize(List<Vertex> y) {
        double max=0, min=0;
        for (int i = 0; i < y.size(); i++) {
            double yi = signal(y.get(i));
            if (i == 0)
                max = min = yi;
            else {
                if (yi > max) max = yi;
                if (yi < min) min = yi;
            }    
        }
        if (max!=min) {
            for (int i = 0; i < y.size(); i++) {
                double yi = signal(y.get(i));
                signal(y.get(i), (yi-min)/(max-min));
            }
        }      
        return max-min;
    }
    
}
