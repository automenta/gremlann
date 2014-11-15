/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syncleus.gremlann.model;

import com.tinkerpop.gremlin.structure.Vertex;
import java.util.List;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import syncleus.gremlann.Graphs;
import static syncleus.gremlann.Graphs.random;
import syncleus.gremlann.Neuron;
import syncleus.gremlann.topology.BipartiteBrain;

/**
 * TODO subclass implementation that uses synapses; currently this uses a hardcoded weight matrix
 */
public class Autoencoder extends BipartiteBrain {

    public double[][] W;
    public double[] hbias;
    public double[] vbias;    
    private double[] tilde_x;
    //private double[] y;
    //private double[] z;
    private double[] L_vbias;
    private double[] L_hbias;

    public Autoencoder(Vertex v, int inputs, int outputs) {
        this(v, newSignalArray("input", v.graph(), inputs), newNeuronArray("output", v.graph(), outputs));        
    }
    
    public Autoencoder(Vertex v, List<Vertex> inputs, List<Vertex> outputs) {
        super(v, inputs, outputs);
        

        this.W = new double[this.getOutputCount()][this.getInputCount()];
        double a = 1.0 / this.getInputCount();

        for (int i = 0; i < this.getOutputCount(); i++) {
            for (int j = 0; j < this.getInputCount(); j++) {
                weight(i,j, uniform(-a, a));
            }
        }


        this.hbias = new double[this.getOutputCount()];
        for (int i = 0; i < this.getOutputCount(); i++) {
            this.hbias[i] = 0;
        }


        this.vbias = new double[this.getInputCount()];
        for (int i = 0; i < this.getInputCount(); i++) {
            this.vbias[i] = 0;
        }

        
    }

    public double weight(int input, int hidden) {
        return W[input][hidden];
    }
    public void weight(int input, int hidden, double value) {
        W[input][hidden] = value;
    }

    public double uniform(final double min, final double max) {
        return random.nextDouble() * (max - min) + min;
    }

    public double binomial(final int n, final double p) {
        if (p < 0 || p > 1) {
            return 0;
        }

        int c = 0;
        double r;

        for (int i = 0; i < n; i++) {
            r = Graphs.random.nextDouble();
            if (r < p) {
                c++;
            }
        }

        return c;
    }

    final public static double sigmoid(final double x) {
        return 1.0 / (1.0 + Math.pow(Math.E, -x));
    }
    

    public void get_corrupted_input(double[] x, double[] tilde_x, double p) {
        for (int i = 0; i < getInputCount(); i++) {
            if (x[i] == 0) {
                tilde_x[i] = 0;
            } else {
                tilde_x[i] = binomial(1, p);
            }
        }
    }

    // Encode
    public void encode(double[] x, List<Vertex> outputs, boolean sigmoid, boolean normalize) {
        
        for (int i = 0; i < getOutputCount(); i++) {
            double yi = 0;
                        
            for (int j = 0; j < getInputCount(); j++) {
                yi += weight(i, j) * x[j];
            }
            yi += hbias[i];
            
            if (sigmoid)
                yi = sigmoid(yi);
            
            Neuron.signal(outputs.get(i), yi);                
        }
        
        if (normalize) {
            normalize(outputs);
        }
        
    }

    // Decode
    public void get_reconstructed_input(List<Vertex> y, List<Vertex> z) {
        ArrayRealVector vy = signals(y);
        
        for (int i = 0; i < getInputCount(); i++) {
            double zi = 0;

            for (int j = 0; j < vy.getDimension(); j++) {
                zi += weight(j,i) * vy.getEntry(j);
            }
            
            zi += vbias[i];
            Neuron.signal(z.get(i), sigmoid(zi));
        }
    }

    public double train(double[] v, double lr, double corruption_level) {
        return train(new ArrayRealVector(v), lr, corruption_level);
    }
    public double train(RealVector v, double lr, double corruption_level) {
        double[] x = Graphs.doubles(v);
        
        if ((tilde_x == null) || (tilde_x.length!=getInputCount())) {
            tilde_x = new double[getInputCount()];
            L_vbias = new double[getInputCount()];
            L_hbias = new double[getOutputCount()];            
        }

        if (corruption_level > 0) {        
            get_corrupted_input(x, tilde_x, 1 - corruption_level);
        }
        else {
            tilde_x = x;
        }
        encode(tilde_x, getOutputs(), true, false);
        get_reconstructed_input(getOutputs(), getInputs());

        // vbias
        for (int i = 0; i < getInputCount(); i++) {
            L_vbias[i] = x[i] - Neuron.signal(inputs.get(i));
            vbias[i] += lr * L_vbias[i];
        }

        // hbias
        for (int i = 0; i < getOutputCount(); i++) {
            L_hbias[i] = 0;
            for (int j = 0; j < getInputCount(); j++) {
                L_hbias[i] += W[i][j] * L_vbias[j];
            }
            double yi = outputSignal(i);
            L_hbias[i] *= yi * (1 - yi);
            hbias[i] += lr * L_hbias[i];
        }

        // W
        for (int i = 0; i < getOutputCount(); i++) {
            for (int j = 0; j < getInputCount(); j++) {
                W[i][j] += lr * (L_hbias[i] * tilde_x[j] + L_vbias[j] * Neuron.signal(outputs.get(i)));
            }
        }
                
        return v.getDistance( signals(getInputs() ) );
    }

    public ArrayRealVector reconstruct(double[] x) {
        encode(x, getOutputs(), true, false);
        get_reconstructed_input(getOutputs(), getInputs());
        return signals(getInputs());
    }

    
    
}
