package data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface DistancePairData extends FragmentData
{
	public int getNumDistancePairs();

	public int[] getDistancePair(int index);

	public List<int[]> getDistancePairs();

	public String getDistancePairName(int index);

	// public List<Integer> getMoleculesForDistancePair(int index);

	public List<Double> getDistances(int pairIndex, int moleculeIndex);

	public HashMap<Integer, List<Double>> getMoleculesAndDistances(int pairIndex);

	public int getNumDistancePairsForMolecule(int index);

	public Set<Integer> getMoleculesForDistancePair(int index);

	public String getAdditionalInfo(MoleculeData d, int indent);

	/**
	 * @return 0: double array mit active distances<br>
	 *         1: double array mit in-active distances
	 */
	public List<double[]> getActivityDistancesForDistancePair(MoleculeActivityData d, int index);

	public double getFStatistic(MoleculeActivityData d, int index);

	public double getTTest(MoleculeActivityData d, int index);

	public double getKolmogorovSmirnovTest(MoleculeActivityData d, int index);

	public DistancePairData getSubset(String fragmentName, List<Integer> distancePairSubset);

	public String getDistancePairSplitPointName(int index, double splitPoint);
}
