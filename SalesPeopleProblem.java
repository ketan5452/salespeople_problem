import java.util.Arrays;
import java.util.Scanner;

/**
 * @author KETAN
 *
 */
public class SalesPeopleProblem {

	private final double[][] initMatrix;
	private final int rowLength, colLength, maxDim;
	private final double[] bySalesPeople, byCities;
	private final int[] minByCities;
	private final double[] minByCities2;
	private final int[] matchBySalesPeople, matchByCities;
	private final int[] salesPeopleAttachedCities;
	private final boolean[] assignedCities;

	/*
	 * Constructor to initialize all the variables
	 */
	public SalesPeopleProblem(double[][] initMatrix) {
		this.maxDim = Math.max(initMatrix.length, initMatrix[0].length);
		this.rowLength = initMatrix.length;
		this.colLength = initMatrix[0].length;
		this.initMatrix = new double[this.maxDim][this.maxDim];
		for (int w = 0; w < this.maxDim; w++) {
			if (w < initMatrix.length) {
				if (initMatrix[w].length != this.colLength) {
					throw new IllegalArgumentException("Irregular cost matrix");
				}
				this.initMatrix[w] = Arrays.copyOf(initMatrix[w], this.maxDim);
			} else {
				this.initMatrix[w] = new double[this.maxDim];
			}
		}
		bySalesPeople = new double[this.maxDim];
		byCities = new double[this.maxDim];
		minByCities = new int[this.maxDim];
		minByCities2 = new double[this.maxDim];
		assignedCities = new boolean[this.maxDim];
		salesPeopleAttachedCities = new int[this.maxDim];
		matchBySalesPeople = new int[this.maxDim];
		Arrays.fill(matchBySalesPeople, -1);
		matchByCities = new int[this.maxDim];
		Arrays.fill(matchByCities, -1);
	}

	/*
	 * Filling of cities matrix
	 */
	protected void checkSol() {
		for (int j = 0; j < maxDim; j++) {
			byCities[j] = Double.POSITIVE_INFINITY;
		}
		for (int w = 0; w < maxDim; w++) {
			for (int j = 0; j < maxDim; j++) {
				if (initMatrix[w][j] < byCities[j]) {
					byCities[j] = initMatrix[w][j];
				}
			}
		}
	}

	/*
	 * Check the row and column by filled with 0 or not
	 */
	public int[] check() {
		minimize();
		checkSol();
		completeMatch();
		int w = getNotMatchedCities();
		while (w < maxDim) {
			init(w);
			checkingAllCosts();
			w = getNotMatchedCities();
		}
		int[] result = Arrays.copyOf(matchBySalesPeople, rowLength);
		for (w = 0; w < result.length; w++) {
			if (result[w] >= colLength) {
				result[w] = -1;
			}
		}
		return result;
	}

	/*
	 * Checking of all costs filled or not
	 */
	protected void checkingAllCosts() {
		while (true) {
			int minSlackWorker = -1, minSlackJob = -1;
			double minSlackValue = Double.POSITIVE_INFINITY;
			for (int j = 0; j < maxDim; j++) {
				if (salesPeopleAttachedCities[j] == -1) {
					if (minByCities2[j] < minSlackValue) {
						minSlackValue = minByCities2[j];
						minSlackWorker = minByCities[j];
						minSlackJob = j;
					}
				}
			}
			if (minSlackValue > 0) {
				updateCosts(minSlackValue);
			}
			salesPeopleAttachedCities[minSlackJob] = minSlackWorker;
			if (matchByCities[minSlackJob] == -1) {
				int committedJob = minSlackJob;
				int parentWorker = salesPeopleAttachedCities[committedJob];
				while (true) {
					int temp = matchBySalesPeople[parentWorker];
					checkMatch(parentWorker, committedJob);
					committedJob = temp;
					if (committedJob == -1) {
						break;
					}
					parentWorker = salesPeopleAttachedCities[committedJob];
				}
				return;
			} else {
				int worker = matchByCities[minSlackJob];
				assignedCities[worker] = true;
				for (int j = 0; j < maxDim; j++) {
					if (salesPeopleAttachedCities[j] == -1) {
						double slack = initMatrix[worker][j] - bySalesPeople[worker] - byCities[j];
						if (minByCities2[j] > slack) {
							minByCities2[j] = slack;
							minByCities[j] = worker;
						}
					}
				}
			}
		}
	}

	/*
	 * Check not matching cities and return the int value of it wrt array index
	 */
	protected int getNotMatchedCities() {
		int w;
		for (w = 0; w < maxDim; w++) {
			if (matchBySalesPeople[w] == -1) {
				break;
			}
		}
		return w;
	}

	/*
	 * Check from unique row and unique column 0 available or not
	 */
	protected void completeMatch() {
		for (int w = 0; w < maxDim; w++) {
			for (int j = 0; j < maxDim; j++) {
				if (matchBySalesPeople[w] == -1 && matchByCities[j] == -1
						&& initMatrix[w][j] - bySalesPeople[w] - byCities[j] == 0) {
					checkMatch(w, j);
				}
			}
		}
	}

	/*
	 * Initialization of the required matrix
	 */
	protected void init(int w) {
		Arrays.fill(assignedCities, false);
		Arrays.fill(salesPeopleAttachedCities, -1);
		assignedCities[w] = true;
		for (int j = 0; j < maxDim; j++) {
			minByCities2[j] = initMatrix[w][j] - bySalesPeople[w] - byCities[j];
			minByCities[j] = w;
		}
	}

	/*
	 * Check salespeople and cities matrix
	 */
	protected void checkMatch(int w, int j) {
		matchBySalesPeople[w] = j;
		matchByCities[j] = w;
	}

	/*
	 * Minimize the matrix by computing 0 by subtracting each minimum value of
	 * row and column
	 */
	protected void minimize() {
		for (int w = 0; w < maxDim; w++) {
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < maxDim; j++) {
				if (initMatrix[w][j] < min) {
					min = initMatrix[w][j];
				}
			}
			for (int j = 0; j < maxDim; j++) {
				initMatrix[w][j] -= min;
			}
		}
		double[] min = new double[maxDim];
		for (int j = 0; j < maxDim; j++) {
			min[j] = Double.POSITIVE_INFINITY;
		}
		for (int w = 0; w < maxDim; w++) {
			for (int j = 0; j < maxDim; j++) {
				if (initMatrix[w][j] < min[j]) {
					min[j] = initMatrix[w][j];
				}
			}
		}
		for (int w = 0; w < maxDim; w++) {
			for (int j = 0; j < maxDim; j++) {
				initMatrix[w][j] -= min[j];
			}
		}
	}

	/*
	 * Update the cost of salespeople
	 */
	protected void updateCosts(double slack) {
		for (int w = 0; w < maxDim; w++) {
			if (assignedCities[w]) {
				bySalesPeople[w] += slack;
			}
		}
		for (int j = 0; j < maxDim; j++) {
			if (salesPeopleAttachedCities[j] != -1) {
				byCities[j] -= slack;
			} else {
				minByCities2[j] -= slack;
			}
		}
	}

	/*
	 * Java Main Method
	 */
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the cost of each city: ");
		System.out.println("rows length: ");
		int r = sc.nextInt();
		System.out.println("column length: ");
		int c = sc.nextInt();
		System.out.println("Enter the cost matrix: (e.g. 2500 4000 3500)");
		double[][] cost = new double[r][c];
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++) {
				cost[i][j] = sc.nextDouble();
			}
		}
		SalesPeopleProblem salesPeopleProblem = new SalesPeopleProblem(cost);
		int[] result = salesPeopleProblem.check();
		System.out.println(
				"Assiged Cities row wise ([1st SalesPerson city index in 1st row, 2nd SalesPerson city index in 2nd row and so on]): ");
		System.out.println(Arrays.toString(result));
		sc.close();
	}
}
