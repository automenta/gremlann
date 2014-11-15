/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model.som;

import java.io.Serializable;
import org.apache.commons.math3.linear.RealVector;
import static syncleus.gremlann.Graphs.doubles;

/**
 *
 * @author me
 */
public class SomOutput implements Serializable {
    
    private static final double[] empty = new double[0];

    protected double[] position = empty;

    
    public void setPosition(RealVector position) {
        this.position = doubles(position);
    }
    
    public void setPosition(double[] position) {
        this.position = position;
    }

    public double[] getPosition() {        
        return position;
    }
    
    
}
