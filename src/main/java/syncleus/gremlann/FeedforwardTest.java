/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann;

import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import static syncleus.gremlann.Graphs.printVertex;

/**
 *
 * @author me
 */
public class FeedforwardTest {

    
    
    static public void main(String[] args) {
        TinkerGraph g = TinkerGraph.open();
                
        LayerBrain b = new LayerBrain(g.addVertex(), 3,2,1);
        
        b.input(true, 1,1,1);
        
        System.out.println(g);        
        b.printAllNeurons();
        System.out.println();
        
        //b.printLayers();
        System.out.println(g);
        b.traverseLayersIncreasing().sideEffect(v -> printVertex(v.get()) ).iterate();
        System.out.println();
        
        //b.printNeurons()
        System.out.println(g);
        b.traverseNeuronsByIncreasingLayer().sideEffect(v -> printVertex(v.get()) ).iterate();
        System.out.println();
        
        b.traverseNeuronsBackward().sideEffect(v -> printVertex(v.get()) ).iterate();
        
    }
    
//        //Example from Tinkerpop3 unit test
//        g.compute().program(LambdaVertexProgram.build().
//                setup(memory -> {
//                }).
//                execute((vertex, messenger, memory) -> {
//                    // TODO: Implement wrapper for GiraphGraph internal TinkerVertex
//
//                    vertex.property("blah", "blah");
//
//                    memory.incr("a", 1);
//
//                    if (memory.isInitialIteration()) {
//                        vertex.property("nameLengthCounter", vertex.<String>value("name").length());
//                        memory.incr("b", vertex.<String>value("name").length());
//                    } else {
//                        vertex.singleProperty("nameLengthCounter", vertex.<String>value("name").length() + vertex.<Integer>value("nameLengthCounter"));
//                    }
//                }).
//                terminate(memory -> memory.getIteration() == 1).
//                elementComputeKeys("nameLengthCounter").
//                memoryComputeKeys("a", "b").create());    }
    
}
