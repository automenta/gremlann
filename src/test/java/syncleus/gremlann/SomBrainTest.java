/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Table;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Random;
import static junit.framework.Assert.assertEquals;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import syncleus.gremlann.model.som.SomExponentialDecay;
import syncleus.gremlann.model.som.SomBrain;

public class SomBrainTest {

    final Random random = new Random(31337 & 0xDEAD);
    private TinkerGraph graph;
    
    @Before
    public void init() {
       graph = TinkerGraph.open();
    }
    
    @Test
    public void testEmptySOM() throws Throwable {
        try {
            SomBrain som = new SomBrain(graph.addVertex(), new SomExponentialDecay(10, 0.1), 3, 4);

            assertNotNull(som);

            assertEquals(4, Iterables.size(som.getInputs()));

            assertEquals(1 /* the SOM "brain" itself */ + 4 /* inputs */, 
                    Iterators.size(graph.V()));

            som.addOutput(0, 0, 0);
            som.addOutput(1, 1, 1);

            assertEquals(1 /* the SOM "brain" itself */ + 4 /* inputs */ + 2 /* outputs */,
                    Iterators.size(graph.V()));

            assertEquals(2, Iterables.size(som.getOutputs()));

            Table w = som.getOutputWeights();
            assertEquals(8, w.size());

            som.setInput(0.75,0.25,0.33,0.10);

            assertEquals(3, som.getBestMatchingUnit(true).getDimension());

            som.addOutput(0.25, 0.25, 0.25);
            assertEquals(3, Iterables.size(som.getOutputs()));
            assertEquals(12, som.getOutputWeights().size());

            som.getBestMatchingUnit(true);

            assertEquals(2, som.getIterationsTrained());
        }
        catch (UndeclaredThrowableException t) {
            t.printStackTrace();
            throw t.getCause();
        }

    }

    @Test
    public void testColor() {
        final int TEST_ITERATIONS = 100;
        final int TRAIN_ITERATIONS = 10000;
        final double DRIFT_FACTOR = 400.0;
        final int OUTPUT_WIDTH = 10;
        final int OUTPUT_HEIGHT = 10;
        final int OUTPUT_DIMENSIONS = 2;
        final double LEARNING_RATE = 0.1;
        final int INPUT_DIMENSIONS = 3;

        //initialize brain with 3d input and 2d output
        SomBrain brain = new SomBrain(graph.addVertex(), new SomExponentialDecay(TRAIN_ITERATIONS, LEARNING_RATE), OUTPUT_DIMENSIONS, INPUT_DIMENSIONS);
        

        //create the output latice
        for (double x = 0; x < OUTPUT_WIDTH; x++) {
            for (double y = 0; y < OUTPUT_HEIGHT; y++) {
                brain.addOutput(x, y);
            }
        }
        
        
        //run through RANDOM training data
        for (int iteration = 0; iteration < TRAIN_ITERATIONS; iteration++) {
            brain.setInput( random.nextDouble(), random.nextDouble(), random.nextDouble() );
            brain.getBestMatchingUnit(true);
        }
        
        //some static varibles for the blocksize
        final double blockSize = 0.0025;
        final double maxOffset = 1.0 - blockSize;
        //test the maximum distance of close colors in the color space
        double farthestDistanceClose = 0.0;
        String closeOutText = "";
        for (int iteration = 0; iteration < TEST_ITERATIONS; iteration++) {
            final StringBuilder outText = new StringBuilder(64);
                //find a mutual offset in the color space (leaving room for the
            //block)
            final double redOffset = random.nextDouble() * maxOffset;
            final double greenOffset = random.nextDouble() * maxOffset;
            final double blueOffset = random.nextDouble() * maxOffset;
            outText.append("close color offsets... red: ").append(redOffset).append(", green: ").append(greenOffset).append(", blue: ").append(blueOffset).append('\n');

            //get the location of a color within the block
            brain.setInput(
                    redOffset + (random.nextDouble() * blockSize),
                    greenOffset + (random.nextDouble() * blockSize),
                    blueOffset + (random.nextDouble() * blockSize)
            );
            
            double[] iRandom = brain.inputSignals().toArray();
            
            outText.append("close color1... red:").append(iRandom[0]).append(", green: ").append(iRandom[1]).append(", blue").append(iRandom[2]).append('\n');
            final RealVector color1 = brain.getBestMatchingUnit(true);

            //get the location of the other color within the block
            brain.setInput(
                    redOffset + (random.nextDouble() * blockSize),
                    greenOffset + (random.nextDouble() * blockSize),
                    blueOffset + (random.nextDouble() * blockSize)
            );
            
            double[] jRandom = brain.inputSignals().toArray();
            
            outText.append("close color2... red:").append(jRandom[0]).append(", green: ").append(jRandom[1]).append(", blue").append(jRandom[2]).append('\n');
            
            final RealVector color2 = brain.getBestMatchingUnit(true);

            //calculate the distance between these two points
            outText.append("close color1 point: ").append(color1).append('\n');
            outText.append("close color2 point: ").append(color2).append('\n');
            
            final double distance = color1.getDistance(color2);
            
            outText.append("close color distance: ").append(distance).append('\n');
            //store the distance if its greater than the current max
            if (farthestDistanceClose < distance) {
                farthestDistanceClose = distance;
                closeOutText = outText.toString();
            }
        }

        //test the maximum distance of far colors in the color space
        final double maxDrift = maxOffset / DRIFT_FACTOR;
        double closestDistanceFar = Double.POSITIVE_INFINITY;
        String farOutText = "";
        for (int iteration = 0; iteration < TEST_ITERATIONS; iteration++) {
            final StringBuilder outText = new StringBuilder(64);
            //get the location of a color within the block
            final boolean isRed1Positive = random.nextBoolean();
            final boolean isGreen1Positive = random.nextBoolean();
            final boolean isBlue1Positive = random.nextBoolean();
            brain.setInput(
                (isRed1Positive ? random.nextDouble() * maxDrift : 1.0 - (random.nextDouble() * maxDrift)),
                (isGreen1Positive ? random.nextDouble()*maxDrift: 1.0 - (random.nextDouble() * maxDrift)),
                (isBlue1Positive ? random.nextDouble() * maxDrift : 1.0 - (random.nextDouble() * maxDrift))
            );            
            
            //outText.append("far color1... red:").append(brain.getInput(0)).append(", green: ").append(brain.getInput(1)).append(", blue").append(brain.getInput(2)).append('\n');
            final RealVector color1 = brain.getBestMatchingUnit(true);

            //get the location of the other color within the block
            brain.setInput(
                (isRed1Positive ? 1.0 - (random.nextDouble() * maxDrift) : random.nextDouble() * maxDrift),
                (isGreen1Positive ? 1.0 - (random.nextDouble()*maxDrift): random.nextDouble() * maxDrift),
                (isBlue1Positive ? 1.0 - (random.nextDouble() * maxDrift) : random.nextDouble() * maxDrift)
            );     
           
            //outText.append("far color2... red:").append(brain.getInput(0)).append(", green: ").append(brain.getInput(1)).append(", blue").append(brain.getInput(2)).append('\n');
            final RealVector color2 = brain.getBestMatchingUnit(true);
            
            //calculate the distance between these two points
            outText.append("far color1 point: ").append(color1).append('\n');
            outText.append("far color2 point: ").append(color2).append('\n');
            final double distance = color1.getDistance(color2);
            outText.append("far color distance: ").append(distance).append('\n');
            //store the distance if its greater than the current max
            if (closestDistanceFar > distance) {
                closestDistanceFar = distance;
                farOutText = outText.toString();
            }
        }

        
            //check that the farthest close is closer than the farthest far,
        //essentially make sure similar colors are always close and
        //dissimilar colors are always far away.
        Assert.assertTrue("colors did not map properly: far: " + closestDistanceFar + " -> close: " + farthestDistanceClose + '\n' + closeOutText + '\n' + farOutText + '\n', closestDistanceFar > farthestDistanceClose);

    }

    //TODO test that reset() removes existing vertices and edges
    
    
    public static void print(Vertex v) {
        System.out.println(v + ": ");        
    }

    
}
