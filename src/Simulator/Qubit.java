package Simulator;

import mathLib.Complex;
import mathLib.Vector;

import java.util.ArrayList;
import java.util.Random;

public class Qubit extends Vector<Complex> {

    public Qubit(Complex a, Complex b) {
        super(a,b);
    }

    public Qubit(Vector<Complex> v) {
        super(v);
    }

    public static Qubit ONE() {
        return new Qubit(Complex.ZERO(),Complex.ONE());
    }

    public static Qubit ZERO() {
        return new Qubit(Complex.ONE(),Complex.ZERO());
    }

    public static int measure(Vector<Complex> qubits){
        //Check that qubit vector is properly normalized
        ArrayList<Complex> amps = qubits.toArrayList();
        ArrayList<Double> probs = new ArrayList<>();
        double sum = 0;
        for(Complex c : amps) {
            double mag = c.abs();
            double prob = mag*mag;
            sum += prob;
            probs.add(prob);
        }
        if(sum-1 > 0.05) { //error tolerance
            return -1;
        }
        int measured = 0;
        double rand = (new Random()).nextDouble();
        for(int i = 0; i < probs.size(); ++i) {
            rand -= probs.get(i);
            if(rand <= 0) {
                measured = i;
                break;
            }
        }
        //for(int i = probs.size()-1; i >= 0; --i) {
        //    measured += (new Random()).nextDouble() < probs.get(i) ? 1 << i : 0;
        //}
        return measured; //possible bug, may need to bitshift down 1, as 0001 should be zero.
    }

    public static Qubit getInputState(int registers) {
        Qubit start = Qubit.ZERO();
        for(int i = 1; i < registers; ++i) {
            start = new Qubit(start.kronecker(Qubit.ZERO()).toVector());
        }
        return start;
    }

}
