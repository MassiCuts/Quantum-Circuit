package framework;

import appUI.Window;
import mathLib.Complex;
import mathLib.Eigenspace;
import mathLib.Matrix;
import mathLib.HermitianDecomposition;
import java.util.*;

public class Main {
	
	private static Window window;
	
    public static void main(String[] args) {
    	/* toggle flags: debug mode or not */
    	boolean normalMode = true;
    	boolean debugMode = true;

    	if ( debugMode ) {
    		/* TESTING: matrix operators */
    		// P3
    		Matrix<Complex> mat1 = new Matrix<>(Complex.ONE(), 3, 3,
                Complex.ZERO(), Complex.ONE(), Complex.ZERO(),
                Complex.ONE(), Complex.ZERO(), Complex.ONE(),
                Complex.ZERO(), Complex.ONE(), Complex.ZERO());

    		// K3
    		Matrix<Complex> mat2 = new Matrix<>(Complex.ONE(),3,3,
    			Complex.ZERO(), Complex.ONE(), Complex.ONE(),
    			Complex.ONE(), Complex.ZERO(), Complex.ONE(),
    			Complex.ONE(), Complex.ONE(), Complex.ZERO());
 
    		// K4
    		Matrix<Complex> mat3 = new Matrix<>(Complex.ONE(),4,4,
    			Complex.ZERO(), Complex.ONE(), Complex.ONE(), Complex.ONE(),
    			Complex.ONE(), Complex.ZERO(), Complex.ONE(), Complex.ONE(),
    			Complex.ONE(), Complex.ONE(), Complex.ZERO(), Complex.ONE(),
    			Complex.ONE(), Complex.ONE(), Complex.ONE(), Complex.ZERO());
    		
    		Matrix<Complex> mat = mat3;
    		System.out.println("Test matrix = \n" + mat);

    		/* TESTING: matrix map */
    		//Matrix<Complex> m = Matrix.map(Complex.ONE(), mat, c -> c.mult(Complex.ONE()));
    		//System.out.println(m.toString());
    	
    		/* TESTING: spectral decomposition */
    		HermitianDecomposition obj = new HermitianDecomposition();
    	
    		/* testing EigenvalueDecomposition:
    		List<Matrix<Complex>> matrices = new ArrayList<Matrix<Complex>>();
    		matrices = obj.decompose(mat);
    		Matrix<Complex> d = matrices.get(0);
    		Matrix<Complex> v = matrices.get(1);
    	
    		System.out.println("d = \n" + d.toString());
    		System.out.println("v = \n" + v.toString());
    		*/
    		
    		List<Eigenspace> eigspaces = new ArrayList<Eigenspace>();
    		eigspaces = obj.eigh(mat);
    		
    		if ( obj.checkEigh( mat, eigspaces, 0.0001) ) {
    			System.err.println("Spectral decomposition: ok");
    		}
    		else {
    			System.err.println("Spectral decomposition: fail");
    		}

    		/* Testing matrix exponential */
    		Matrix<Complex> newMat = obj.map( x -> x*x, mat3 );
    		System.out.println("Matrix = \n" + mat3.toString());
    		System.out.println("Squaring = \n" + newMat.toString());

    		/* TESTING: user input matrix
    		CustomGateConstructorUI g = new CustomGateConstructorUI(null);
    		g.show();
    		Matrix<Complex> mat = g.getCustomMatrix().stream().reduce((a, m) -> a.kronecker(m)).get();
    		System.out.println(mat);
    		*/
    	}

    	
    	if ( normalMode ) {
        	DefaultGate.loadGates();
    		window = new Window();
    		window.setVisible(true);
    	}
    	if ( debugMode ) {
			ArrayList<ArrayList<SolderedRegister>> gates = Translator.loadProgram(DefaultGate.LangType.QUIL,"C:\\Users\\Josh\\Desktop\\test.quil");
			for(int x = 0; x < gates.size(); ++x) {
				ArrayList<SolderedRegister> srs = gates.get(x);
				for(int y = 0; y < srs.size(); ++y) {
					SolderedRegister sr = srs.get(y);
					System.out.println("X: " + x + "\nY: " + y + "\nGate: " + sr);
				}
			}
			window.getSelectedBoard().setGates(gates);
		}

    }

    public static Window getWindow() {
    	return window;
    }



    /*
     * Note, the following code is needed to run the output program
     * from pyquil.parser import parse_program
     * from pyquil.api import QVMConnection
     * qvm = QVMConnection()
     * p = parse_program("whatever this java code outputs")
     * qvm.wavefunction(p).amplitudes
     */

    /*
     * Alternatively, to use the QASM output, this code will work:
     * import qiskit
     * qp = qiskit.QuantumProgram()
     * name = "test"
     * qp.load_qasm_file("test.qasm",name=name)
     * ret = qp.execute([name])
     * print(ret.get_counts(name))
     */

}
