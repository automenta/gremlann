/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import syncleus.gremlann.train.Backprop;

/**
 *
 * @author me
 */
public class LayerBrainTest {
    
    @Test
    public void testXOR_3_1() {
        TinkerGraph g = TinkerGraph.open();
                
        LayerBrain b = new LayerBrain(g.addVertex(), 3,2,1);

        assertEquals("output=0 on new unpropagated network", b.outputSignals().getL1Norm(), 0.0, 0.01);
        
        b.input(true, 1,1,1);
        
        assertTrue("propagating some values will make output vector non-zero", b.outputSignals().getL1Norm() != 0.0);        

        Backprop training = new Backprop(b);
        double e = training.associate(new double[] { 1,0,0 }, new double[] { 1 }, true);
        
        assertTrue("random network should not give exact expected value without training", e!=0d); 
        

        //
        // Graph is constructed, just need to train and test our network now.
        //
        final int maxCycles = 10000;
        final int completionPeriod = 50;
        final double maxError = 0.75;
        for (int cycle = maxCycles; cycle >= 0; cycle--) {
            int finished = 0;
            for (int in1 = -1; in1 <= 1; in1 += 2) {
                for (int in2 = -1; in2 <= 1; in2 += 2) {
                    for (int in3 = -1; in3 <= 1; in3 += 2) {
                        boolean bi = in1 >= 0;
                        boolean bj = in2 >= 0;
                        boolean bk = in3 >= 0;
                        boolean expect = bi ^ bj ^ bk;
                        double expectD = expect ? 1.0 : -1.0;

                        double error = training.train(new double[] { in1, in2, in3 }, new double[] { expectD } );

                        if (cycle % completionPeriod == 0) {
                            System.out.println("error=" + error);
                                    
                            if (error < maxError)
                                finished++;
                        }
                    }
                }
            }
            if (finished == 8)
                break;
        }
        
    }
}
