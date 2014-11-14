/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model.som;

import java.io.Serializable;

/**
 *
 * @author me
 */
public interface SomModel extends Serializable {

    /**
     * Determines the neighborhood function based on the neurons distance from
     * the BMU.
     *
     * @param distanceFromBest The neuron's distance from the BMU.
     * @return the decay effecting the learning of the specified neuron due to
     * its distance from the BMU.
     * @since 2.0
     */
    public double neighborhoodFunction(final SomBrain som, final double distanceFromBest);

    /**
     * Determine the current radius of the neighborhood which will be centered
     * around the Best Matching Unit (BMU).
     *
     * @return the current radius of the neighborhood.
     * @since 2.0
     */
    public double neighborhoodRadiusFunction(SomBrain som);

    /**
     * Determines the current learning rate for the network.
     *
     * @return the current learning rate for the network.
     * @since 2.0
     */
    public double learningRateFunction(SomBrain som);
    
}
