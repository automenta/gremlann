/******************************************************************************
 *                                                                             *
 *  Copyright: (c) Syncleus, Inc.                                              *
 *                                                                             *
 *  You may redistribute and modify this source code under the terms and       *
 *  conditions of the Open Source Community License - Type C version 1.0       *
 *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
 *  There should be a copy of the license included with this file. If a copy   *
 *  of the license is not included you are granted no right to distribute or   *
 *  otherwise use this file except through a legal and valid license. You      *
 *  should also contact Syncleus, Inc. at the information below if you cannot  *
 *  find a license:                                                            *
 *                                                                             *
 *  Syncleus, Inc.                                                             *
 *  2604 South 12th Street                                                     *
 *  Philadelphia, PA 19148                                                     *
 *                                                                             *
 ******************************************************************************/
package syncleus.gremlann.model.som;

public class SOMExponentialDecay implements SomModel {
    
    final int iterationsToConverge;
    final double initialLearningRate;

    public SOMExponentialDecay(final int iterationsToConverge, final double initialLearningRate) {
        this.iterationsToConverge = iterationsToConverge;
        this.initialLearningRate = initialLearningRate;
    }


    double getInitialRadius(SomBrain som) {
        double maxCrossSection = 0.0;
        
        //TODO create SomBrain.getCrossSection() method
        
        for (int dimensionIndex = 0; dimensionIndex < som.getDimension(); dimensionIndex++) {
            
            final double crossSection = som.getUpperBounds().getEntry(dimensionIndex) - som.getLowerBounds().getEntry(dimensionIndex);
            if (crossSection > maxCrossSection)
                maxCrossSection = crossSection;
        }

        return maxCrossSection / 2.0;
    }

    double getTimeConstant(SomBrain som) {
        return this.iterationsToConverge / Math.log(this.getInitialRadius(som));
    }

    /**
     * Determines the neighborhood function based on the neurons distance from
     * the BMU.
     *
     * @param distanceFromBest The neuron's distance from the BMU.
     * @return the decay effecting the learning of the specified neuron due to
     * its distance from the BMU.
     * @since 2.0
     */
    @Override
    public double neighborhoodFunction(SomBrain som, final double distanceFromBest) {      
        return Math.exp(-1.0 * (Math.pow(distanceFromBest, 2.0)) / (2.0 * Math.pow(this.neighborhoodRadiusFunction(som), 2.0)));
    }

    /**
     * Determine the current radius of the neighborhood which will be centered
     * around the Best Matching Unit (BMU).
     *
     * @return the current radius of the neighborhood.
     * @since 2.0
     */
    @Override
    public double neighborhoodRadiusFunction(SomBrain som) {
        return getInitialRadius(som) * Math.exp(-1.0 * som.getIterationsTrained() / this.getTimeConstant(som));        
    }

    /**
     * Determines the current learning rate for the network.
     *
     * @return the current learning rate for the network.
     * @since 2.0
     */
    @Override
    public double learningRateFunction(SomBrain som) {        
        return this.initialLearningRate * Math.exp(-1.0 * som.getIterationsTrained() / this.getTimeConstant(som));
    }
}
