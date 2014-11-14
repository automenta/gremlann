/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static syncleus.gremlann.AbstractBrain.signal;
import static syncleus.gremlann.Graphs.unipolar;
import syncleus.gremlann.model.Autoencoder;

/**
 *
 * @author me
 */
public class TestAutoencoder {
 
    @Test public void testAE() {
        TinkerGraph g = TinkerGraph.open();
                
        Autoencoder e = new Autoencoder(g.addVertex(), 4, 6);
        
        assertEquals(4, e.getInputCount());
        assertEquals(6, e.getOutputCount());
        
                
        final int maxCycles = 1000;
        double learningRate = 0.05;
        List<Double> totalErrors = new ArrayList();
        
        for (int cycle = maxCycles; cycle >= 0; cycle--) {
            double totalError = 0;
            for (int in1 = -1; in1 <= 1; in1 += 2) {
                for (int in2 = -1; in2 <= 1; in2 += 2) {
                    for (int in3 = -1; in3 <= 1; in3 += 2) {
                        
                        boolean bi = in1 >= 0;
                        boolean bj = in2 >= 0;
                        boolean bk = in3 >= 0;
                        boolean expect = bi ^ bj ^ bk;

                        
                        double error = e.train(new double[] { unipolar(bi), unipolar(bj), unipolar(bk), unipolar(expect) }, learningRate, 0);
                        
                        totalError += error;
                        
                    }
                }
            }            
            totalErrors.add(totalError);
        }
        
        double firstError = totalErrors.get(0);
        double halfError = totalErrors.get(totalErrors.size()/2);
        double finalError = totalErrors.get(totalErrors.size()-1);
        assertTrue(firstError > halfError);
        assertTrue(halfError > finalError);
        

    }
    

}
