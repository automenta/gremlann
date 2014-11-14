/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.activation;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

/**
 *
 * @author me
 */
public class ActivationFunctions {

    static ClassToInstanceMap<ActivationFunction> activationFuncs = MutableClassToInstanceMap.create();
    
    public static <X extends ActivationFunction> X get(Class<? extends X> c) {
        X x = (X) activationFuncs.get(c);
        if (x == null) {
            try {
                x = c.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Invalid activation function: " + c + "(" + ex.toString() + ")");
            }
            activationFuncs.put(c, x);
        }
        return x;
    }
    
}
