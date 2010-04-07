package data;

import hep.aida.util.comparison.ComparisonData;
import hep.aida.util.comparison.KolmogorovSmirnovComparisonAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.math.stat.inference.TTestImpl;
import org.apache.commons.math.stat.inference.TestUtils;

import util.MinMaxAvg;
import util.StringUtil;
import datamining.ResultSet;

public class DistancePairDataImpl extends FragmentDataImpl implements DistancePairData
{
	public static final String SMILES_SEPARATOR_PREFIX = "<-";
	public static final String SMILES_SEPARATOR_SUFFIX = "->";

	private List<int[]> distancePairs;
	private HashMap<Integer, HashMap<Integer, List<Double>>> distancePairToMoleculeDistances;

	// private HashMap<Integer, List<Double>> distancePairToInMoleculeDistance;

	private HashMap<Integer, Integer> numDistancePairsForMolecule;

	// private int numDistancesAllZero = 0;

	public DistancePairDataImpl(String fragmentInfo, List<String> fragments, List<int[]> distancePairs,
			HashMap<Integer, HashMap<Integer, List<Double>>> distancePairToMoleculeDistances)
	{
		super(fragmentInfo, fragments);
		this.distancePairs = distancePairs;
		this.distancePairToMoleculeDistances = distancePairToMoleculeDistances;
		// this.distancePairToMolecules = distancePairToMolecules;
		// this.distancePairToInMoleculeDistance = distancePairToInMoleculeDistance;

		numDistancePairsForMolecule = new HashMap<Integer, Integer>();
		for (HashMap<Integer, List<Double>> distances : distancePairToMoleculeDistances.values())
		{
			if (distances != null)
			{
				// boolean zero = true;
				for (Integer mol : distances.keySet())
				{
					Integer count = numDistancePairsForMolecule.get(mol);
					if (count == null)
						numDistancePairsForMolecule.put(mol, 1);
					else
						numDistancePairsForMolecule.put(mol, count + 1);

					// if (zero)
					// {
					// for (Double d : distances.get(mol))
					// {
					// if (d != 0.0)
					// {
					// zero = false;
					// break;
					// }
					// }
					// }
				}
				// if (zero)
				// numDistancesAllZero++;
			}
		}
	}

	@Override
	public int[] getDistancePair(int index)
	{
		return distancePairs.get(index);
	}

	@Override
	public String getDistancePairName(int index)
	{
		return getDistancePairName(index, "");
	}

	private String getDistancePairName(int index, String splitPoint)
	{
		return getFragmentSmiles(getDistancePair(index)[0]) + SMILES_SEPARATOR_PREFIX + splitPoint + SMILES_SEPARATOR_SUFFIX
				+ getFragmentSmiles(getDistancePair(index)[1]);
	}

	@Override
	public List<Double> getDistances(int pairIndex, int moleculeIndex)
	{
		HashMap<Integer, List<Double>> distances = distancePairToMoleculeDistances.get(pairIndex);
		if (distances == null)
			return null;
		else
			return distances.get(moleculeIndex);
	}

	@Override
	public int getNumDistancePairs()
	{
		return distancePairs.size();
	}

	// @Override
	// public List<Integer> getMoleculesForDistancePair(int index)
	// {
	// return distancePairToMolecules.get(index);
	// }

	public String toString()
	{
		return "DistancePairData '" + fragmentName + "' (#fragments: " + fragments.size() + ", #distance-pairs: "
				+ distancePairs.size() /* +", #only-zero-distances " + numDistancesAllZero */+ ")";
	}

	@Override
	public int getNumDistancePairsForMolecule(int index)
	{
		if (!numDistancePairsForMolecule.containsKey(index))
			return 0;
		else
			return numDistancePairsForMolecule.get(index);
	}

	@Override
	public List<int[]> getDistancePairs()
	{
		return distancePairs;
	}

	@Override
	public Set<Integer> getMoleculesForDistancePair(int index)
	{
		HashMap<Integer, List<Double>> distances = distancePairToMoleculeDistances.get(index);
		if (distances == null)
			return null;
		return distances.keySet();
	}

	@Override
	public String getAdditionalInfo(MoleculeData d, int indent)
	{
		List<Integer> moleculesPerDistancePairCount = new ArrayList<Integer>();
		int moleculesPerPairZeroCount = 0;

		List<Integer> distancesPerDistancePairCount = new ArrayList<Integer>();
		int distancesPerPairZeroCount = 0;

		for (int i = 0; i < distancePairs.size(); i++)
		{
			HashMap<Integer, List<Double>> moleculeToDistances = distancePairToMoleculeDistances.get(i);

			if (moleculeToDistances == null)
			{
				moleculesPerPairZeroCount++;
				distancesPerPairZeroCount += d.getNumMolecules();
			}
			else
			{
				moleculesPerDistancePairCount.add(moleculeToDistances.size());

				for (int j = 0; j < d.getNumMolecules(); j++)
				{
					List<Double> distances = moleculeToDistances.get(j);
					int m = 0;
					if (distances != null)
						m = distances.size();

					if (m == 0)
						distancesPerPairZeroCount++;
					else
						distancesPerDistancePairCount.add(m);
				}
			}
		}

		List<Integer> distancePairPerMoleculeCount = new ArrayList<Integer>();
		int distancesPerMoleculeZeroCount = 0;

		for (int j = 0; j < d.getNumMolecules(); j++)
		{
			Integer n = numDistancePairsForMolecule.get(j);
			if (n != null)
				distancePairPerMoleculeCount.add(n);
			else
				distancesPerMoleculeZeroCount++;
		}

		ResultSet set = new ResultSet();
		MinMaxAvg moleculesPerDistancePair = MinMaxAvg.minMaxAvg(moleculesPerDistancePairCount, moleculesPerPairZeroCount);
		moleculesPerDistancePair.addToResult(set, "mol-per-pair");

		MinMaxAvg distancePairsPerMolecule = MinMaxAvg
				.minMaxAvg(distancePairPerMoleculeCount, distancesPerMoleculeZeroCount);
		distancePairsPerMolecule.addToResult(set, "pair-per-mol");

		MinMaxAvg distancesPerDistancePair = MinMaxAvg.minMaxAvg(distancesPerDistancePairCount, distancesPerPairZeroCount);
		distancesPerDistancePair.addToResult(set, "dist-per-pair-and-mol");

		return set.toNiceString(indent, false);

		// return "(molecules-per-pair: " + StringUtil.formatDouble(avgMpp) + ", #pairs-without-mol: " + numZeroMpp
		// + ", pair-per-molecules: " + StringUtil.formatDouble(pairsPerMolecule) + ", #molecules-without-pair: "
		// + moleculesWithoutPairs + ", distances-per-molecule: " + StringUtil.formatDouble(distancesPerMolecule)
		// + " )";
	}

	/**
	 * @return 0: double array mit active distances<br>
	 *         1: double array mit in-active distances
	 */
	@Override
	public List<double[]> getActivityDistancesForDistancePair(MoleculeActivityData d, int index)
	{
		HashMap<Integer, List<Double>> distances = distancePairToMoleculeDistances.get(index);

		if (distances == null || distances.size() == 0)
			return null;

		Vector<Double> actives = new Vector<Double>();
		Vector<Double> inactives = new Vector<Double>();

		for (Integer mol : distances.keySet())
		{
			List<Double> dist = distances.get(mol);
			if (dist == null || dist.size() == 0)
				continue;
			boolean active = d.getMoleculeActivity(mol) == 1;
			for (Double di : dist)
			{
				if (active)
					actives.add(di);
				else
					inactives.add(di);
			}
		}

		double[] activeDistance = new double[actives.size()];
		for (int i = 0; i < actives.size(); i++)
			activeDistance[i] = actives.get(i);
		double[] inActiveDistance = new double[inactives.size()];
		for (int i = 0; i < inactives.size(); i++)
			inActiveDistance[i] = inactives.get(i);

		List<double[]> res = new ArrayList<double[]>();
		res.add(activeDistance);
		res.add(inActiveDistance);
		return res;
	}

	@Override
	public double getFStatistic(MoleculeActivityData d, int index)
	{
		try
		{
			List<double[]> acts = getActivityDistancesForDistancePair(d, index);

			if (acts == null || acts.get(0).length < 2 || acts.get(1).length < 2)
				return 1;

			double p = TestUtils.oneWayAnovaPValue(acts);

			if (Double.isNaN(p))
				return 1;

			return p;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 1;
		}
	}

	@Override
	public double getTTest(MoleculeActivityData d, int index)
	{
		try
		{
			List<double[]> acts = getActivityDistancesForDistancePair(d, index);

			if (acts == null || acts.get(0).length < 2 || acts.get(1).length < 2)
				return 1;

			TTestImpl ttest = new TTestImpl();
			double p = ttest.tTest(acts.get(0), acts.get(1));

			if (Double.isNaN(p))
				return 1;

			return p;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 1;
		}
	}

	@Override
	public double getKolmogorovSmirnovTest(MoleculeActivityData d, int index)
	{
		try
		{
			List<double[]> acts = getActivityDistancesForDistancePair(d, index);

			if (acts == null || acts.get(0).length < 2 || acts.get(1).length < 2)
				return 1;

			KolmogorovSmirnovComparisonAlgorithm test = new KolmogorovSmirnovComparisonAlgorithm();
			double wa[] = new double[acts.get(0).length];
			Arrays.fill(wa, 1);
			ComparisonData a = new ComparisonData(acts.get(0), wa, ComparisonData.UNBINNED_DATA);
			double wi[] = new double[acts.get(1).length];
			Arrays.fill(wi, 1);
			ComparisonData i = new ComparisonData(acts.get(1), wi, ComparisonData.UNBINNED_DATA);
			double p = test.quality(a, i);

			// SmirnovTest test = new SmirnovTest(acts.get(0), acts.get(1));
			// double p = test.getTestStatistic();

			if (Double.isNaN(p))
				return 1;

			return p;
		}
		catch (Exception e)
		{
			// List<double[]> acts = getActivityDistancesForDistancePair(d, index);
			// double d0[] = acts.get(0);
			// double d1[] = acts.get(1);
			e.printStackTrace();
			return 1;
		}
	}

	@Override
	public DistancePairDataImpl getSubset(String fragmentName, List<Integer> pairSubset)
	{
		List<int[]> distancePairs = new ArrayList<int[]>();
		HashMap<Integer, HashMap<Integer, List<Double>>> distancePairToMoleculeDistances = new HashMap<Integer, HashMap<Integer, List<Double>>>();

		int count = 0;
		for (Integer index : pairSubset)
		{
			distancePairs.add(this.distancePairs.get(index));
			distancePairToMoleculeDistances.put(count, this.distancePairToMoleculeDistances.get(index));

			count++;
		}

		return new DistancePairDataImpl(fragmentName, this.fragments, distancePairs, distancePairToMoleculeDistances);
	}

	@Override
	public HashMap<Integer, List<Double>> getMoleculesAndDistances(int pairIndex)
	{
		return distancePairToMoleculeDistances.get(pairIndex);
	}

	@Override
	public String getDistancePairSplitPointName(int index, double splitPoint)
	{
		return getDistancePairName(index, StringUtil.formatDouble(splitPoint));
	}
}
