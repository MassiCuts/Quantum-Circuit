package mathLib;
import java.io.Serializable;

public class Eigenspace implements Serializable {		
	private static final long serialVersionUID = -1331930154279103218L;
	
	private int dimension;		// dimension of underlying space
	private Complex eigenvalue;	// eigenvalue of a matrix
	private int multiplicity;	// dimension of the eigenspace
	private Matrix<Complex> eigenprojector;	// sum of the outer product of the eigenvectors
	private Matrix<Complex> eigenvectors;	// matrix whose columns are the eigenvectors
	
	/**
	 * Eigenspace
	 *  stores the eigenvalue and computes the orthogonal projection onto the corresponding eigenspace
	 * @param eigval: the eigenvalue
	 * @param v: a matrix whose columns are the relevant eigenvectors
	 */
	public Eigenspace( Complex eigval, Matrix<Complex> v) {
		boolean debugMode = false;
		
		this.dimension = v.getRows();
		this.eigenvalue = eigval;
		this.multiplicity = v.getColumns();
		this.eigenvectors = v.copy();
		
		// build the sum of the outer products of the eigenvectors which span the eigenspace
		Matrix<Complex> eigp = new Matrix<Complex>( Complex.ZERO(), dimension, dimension );
		for (int i = 0; i < this.multiplicity; ++i) {
			Matrix<Complex> evec = eigenvectors.getSlice(0, v.getRows()-1, i, i);
			eigp = eigp.add( evec.mult( conjugateTranspose(evec)) );
		}
		this.eigenprojector = eigp;

		if ( debugMode ) {
			System.err.println("Eigenvalue = " + eigval);
			System.err.println("Eigenprojector = \n" + eigp);
		}
	}
	
	public Eigenspace copy() {
		return new Eigenspace( this.eigenvalue, this.eigenvectors );
	}
	
	public int getDimension() {
		return dimension;
	}

	public Complex getEigenvalue() {
		return eigenvalue;
	}
	
	public Matrix<Complex> getEigenprojector() {
		return eigenprojector;
	}
	
	public Matrix<Complex> getEigenvectors() {
		return eigenvectors;
	}
	
	public int getMultiplicity() {
		return multiplicity;
	}
	
	public void setEigenvalue( Complex val ) {
		this.eigenvalue = val;
	}
	
	@Override
	public String toString() {
		return "[" + String.valueOf(eigenvalue) + "\n," + eigenprojector.toString() + "]\n";
	}
	
	/**
	 * conjugateTranspose
	 *  computes the conjugate (hermitian) transpose of a matrix
	 * @param mat: a square matrix
	 * @return: the matrix conjugate(mat.transpose)
	 */
	public static Matrix<Complex> conjugateTranspose( Matrix<Complex> mat ) {
		return Matrix.map( Complex.ONE(), mat.transpose(), x -> x.conjugate() );
	}

	/**
	 * gramMatrix
	 *  computes the Gram matrix of the set of columns of a matrix
	 * @param mat: a square matrix
	 * @return: the matrix conjugateTranspose(mat) * mat
	 */
	public Matrix<Complex> gramMatrix( Matrix<Complex> mat ) {
		return conjugateTranspose(mat).mult(mat);
	}
	
}
