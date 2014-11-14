/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import java.util.Random;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author me
 */
public class Graphs {
    
    public static Random random = new Random(0);
    
    public static void printVertex(Vertex v) {
        System.out.println(v.label() + ':' + v.toString() + "=>" + v.keys() );
            //System.out.println(v.label() + ":" + v.toString() + "=" + m);
        
    }
    

    public static Vertex set(Vertex e, Object... keyValues) {
        ElementHelper.attachSingleProperties(e, keyValues);
        return e;
    }

    public static Edge set(Edge e, Object... keyValues) {
        for (String k : ElementHelper.getKeys(keyValues)) {
            e.property(k).remove();
        }
        ElementHelper.attachProperties(e, keyValues);
        return e;
    }    
    
    public static double real(Element e, String key) {
        return ((Number)e.value(key)).doubleValue();
    }
    public static double real(Element e, String key, double defaultValue) {        
        return ((Number)e.value(key, defaultValue)).doubleValue();
    }
    
    public static boolean isTrue(Element e, String key) {
        if (e.keys().contains(key))
            return ((Boolean)e.value(key)).booleanValue();
        return false;
    }
    
    public static double unipolar(boolean b) { return b ? 1.0 : 0.0; }
    public static double bipolar(boolean b) { return b ? 1.0 : -1.0; }
    
    
    public static double[] doubles(RealVector v) {
        if (v instanceof ArrayRealVector)
            return ((ArrayRealVector)v).getDataRef();
        return v.toArray();
    }    
}
