/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model.som;

import java.io.Serializable;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author me
 */
public class SomOutput extends ArrayRealVector implements Serializable {

    public SomOutput(RealVector v) throws NullArgumentException {
        super(v);
    }
    
    
}
