package weka.core;

import freechart.HistogramPanel;
import io.Status;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.ListUtil;
import util.MinMaxAvg;
import util.SwingUtil;
import weka.core.neighboursearch.PerformanceStats;

public abstract class AbstractStringSetDistanceFunction extends NormalizableDistance
{
	public static double MISSING_VALUE = Double.MAX_VALUE;

	@Override
	public String globalInfo()
	{
		return "Implementing " + this.getClass().getSimpleName() + ".\n\n";
	}

	@Override
	public String getRevision()
	{
		return RevisionUtils.extract("$Revision: 0.1 $");
	}

	@Override
	protected double updateDistance(double currDist, double diff)
	{
		return (currDist + (diff * diff));
	}

	private HashMap<Instance, BitSet> bitSetCache;

	private int cacheCount = 0;
	private int missCount = 0;

	private void put(Instance i, BitSet b)
	{
		if (bitSetCache == null)
			bitSetCache = new HashMap<Instance, BitSet>();
		bitSetCache.put(i, b);
	}

	private BitSet get(Instance i)
	{
		if (bitSetCache == null)
			return null;
		return bitSetCache.get(i);
	}

	public void clearCache()
	{
		bitSetCache = null;
		cacheCount = 0;
		missCount = 0;
	}

	public void printCacheInfo()
	{
		Status.INFO.println("cached " + cacheCount + "/" + (missCount + cacheCount));
	}

	public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats)
	{
		// StopWatchUtil.start("distance string-set");
		// try
		// {
		double squaredSetValueDistance = 0;
		int numAttributes = m_Data.numAttributes();

		validate();
		int validSetValuesCount = 0;

		List<Double> diffs = new ArrayList<Double>();

		if (KnnDebug.PrintSingleNeighbour)
			KnnDebug.SingleNeighbour.clear();

		BitSet firstNominalBitSet = null;
		BitSet secondNominalBitSet = null;
		// int bitSetCount = 0;

		boolean nominalAttributes = false;

		boolean firstCached = false;
		boolean secondCached = false;

		// StopWatchUtil.start("set-bit-set");
		for (int i = 0; i < numAttributes; i++)
		{
			if (m_ActiveIndices[i] && m_Data.classIndex() != i)
			{
				switch (m_Data.attribute(i).type())
				{
					case Attribute.STRING:
						double setValueDiff = difference(i, first, second);
						if (setValueDiff != MISSING_VALUE)
						{
							diffs.add(setValueDiff);
							squaredSetValueDistance = updateDistance(squaredSetValueDistance, setValueDiff);
							validSetValuesCount++;
						}
						break;
					case Attribute.NOMINAL:

						if (firstNominalBitSet == null)
						{
							nominalAttributes = true;

							firstNominalBitSet = get(first);
							if (firstNominalBitSet != null)
							{
								cacheCount++;
								firstCached = true;
							}
							else
							{
								missCount++;
								firstNominalBitSet = new BitSet(numAttributes);
							}

							secondNominalBitSet = get(second);
							if (secondNominalBitSet != null)
							{
								cacheCount++;
								secondCached = true;
							}
							else
							{
								missCount++;
								secondNominalBitSet = new BitSet(numAttributes);
							}
						}

						if (!firstCached)
						{
							boolean firstOn = (int) first.value(i) == 0;
							firstNominalBitSet.set(i, firstOn);
						}
						if (!secondCached)
						{
							boolean secondOn = (int) second.value(i) == 0;
							secondNominalBitSet.set(i, secondOn);
						}
						// Status.WARN.println(i + ": new vals 1:" + firstOn + " 2:" + secondOn + " size 1:"
						// + firstNominalBitSet.size() + " 2:" + secondNominalBitSet.size());
						// if (firstNominalBitSet.size() != secondNominalBitSet.size())
						// throw new Error("WTF");
						// bitSetCount++;
						break;
				}
			}
		}

		if (!firstCached)
			put(first, firstNominalBitSet);
		if (!secondCached)
			put(second, secondNominalBitSet);

		// StopWatchUtil.stop("set-bit-set");

		double setValueDistance = 0;

		if (validSetValuesCount > 0)
		{
			setValueDistance = Math.sqrt(squaredSetValueDistance) / (double) validSetValuesCount;
			assert (!Double.isNaN(setValueDistance));
		}
		else
			setValueDistance = 1;

		// if (nominalAttributes && validSetValuesCount == 0)
		// throw new Error("not working");

		double invTanimotoDistance = 0;
		if (nominalAttributes && firstNominalBitSet.size() > 0)
		{
			invTanimotoDistance = 1 - tanimoto(firstNominalBitSet, secondNominalBitSet);
		}

		if (KnnDebug.PrintSingleNeighbour)
		{
			KnnDebug.SingleNeighbour.numSort("diff-norm");
			KnnDebug.SingleNeighbour.print();
		}

		if (KnnDebug.PrintSinglePrediction)
		{
			MinMaxAvg mma = MinMaxAvg.minMaxAvg(diffs);

			KnnDebug.SinglePrediction.set("#diffs", validSetValuesCount);
			if (validSetValuesCount > 0)
			{
				KnnDebug.SinglePrediction.set("min-diff", mma.getMin());
				KnnDebug.SinglePrediction.set("max-diff", mma.getMax());
				KnnDebug.SinglePrediction.set("avg-diff", mma.getMean());
			}
			else
			{
				KnnDebug.SinglePrediction.set("min-diff", Double.NaN);
				KnnDebug.SinglePrediction.set("max-diff", Double.NaN);
				KnnDebug.SinglePrediction.set("avg-diff", Double.NaN);
			}
			KnnDebug.SinglePrediction.set("eucl-dist", setValueDistance);

			if (nominalAttributes)
				KnnDebug.SinglePrediction.set("inv-tanimoto", invTanimotoDistance);
			else
				KnnDebug.SinglePrediction.set("inv-tanimoto", Double.NaN);
		}

		if (nominalAttributes)
		{
			return invTanimotoDistance + setValueDistance;
		}
		else
		{
			if (validSetValuesCount > 0)
				return setValueDistance;
			else
				return Double.MAX_VALUE;
		}
		// }
		// finally
		// {
		// StopWatchUtil.stop("distance string-set");
		// }
	}

	public static float tanimoto(BitSet bitset1, BitSet bitset2)// copied from org.openscience.cdk.similarity.Tanimoto; apart
	// from size check, which is not working for very large bitsets
	{
		float _bitset1_cardinality = bitset1.cardinality();
		float _bitset2_cardinality = bitset2.cardinality();
		// if (bitset1.size() != bitset2.size())
		// {
		// throw new CDKException("Bisets must have the same bit length");
		// }
		BitSet one_and_two = (BitSet) bitset1.clone();
		one_and_two.and(bitset2);
		float _common_bit_count = one_and_two.cardinality();
		return _common_bit_count / (_bitset1_cardinality + _bitset2_cardinality - _common_bit_count);
	}

	public static List<Double> normedDistances = new ArrayList<Double>();

	protected void initialize()
	{
		super.initialize();
		normedDistances.clear();
	}

	public static void showNormedDistance(String info)
	{
		double distances[] = new double[normedDistances.size()];
		for (int i = 0; i < distances.length; i++)
			distances[i] = normedDistances.get(i);

		HistogramPanel p = new HistogramPanel(info, null, "distance", "num", "Normalized Distances", distances, 20,
				new double[] { 0, 1 });

		JFrame f = new JFrame(info);
		f.getContentPane().add(p);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		SwingUtil.waitWhileVisible(f);
	}

	protected double difference(int index, Instance first, Instance second)
	{
		double diff;
		double normedDiff;
		Object niceVal1;
		Object niceVal2;

		switch (m_Data.attribute(index).type())
		{
			case Attribute.STRING:

				String string1 = first.stringValue(index);
				String string2 = second.stringValue(index);

				if (string1.length() <= 2 || string2.length() <= 2)
					return MISSING_VALUE;

				diff = distance(SetDistanceCache.getMatrix(string1, string2));
				assert (diff != MISSING_VALUE && diff != Double.MAX_VALUE && diff >= 0) : "illegal difference: " + diff;

				if (m_DontNormalize == true)
				{
					normedDiff = diff;
				}
				else
				{
					normedDiff = norm(diff, index);
					normedDistances.add(normedDiff);
				}
				niceVal1 = string1;
				niceVal2 = string2;
				break;

			case Attribute.NOMINAL:
				double val1 = first.value(index);
				double val2 = second.value(index);

				if (Instance.isMissingValue(val1) || Instance.isMissingValue(val2))
					throw new NotImplementedException();

				if ((int) val1 != (int) val2)
					diff = 1;
				else
					diff = 0;
				normedDiff = diff;

				niceVal1 = val1;
				niceVal2 = val2;
				break;

			default:
				throw new NotImplementedException();
		}

		if (KnnDebug.PrintSingleNeighbour)
		{
			KnnDebug.SingleNeighbour.add();
			KnnDebug.SingleNeighbour.set("test-inst", KnnDebug.TestPredictions.currentIndex);
			KnnDebug.SingleNeighbour.set("train-inst", KnnDebug.SinglePrediction.currentIndex);
			KnnDebug.SingleNeighbour.set("attribute", index);
			KnnDebug.SingleNeighbour.set("attribute-name", m_Data.attribute(index).name());
			KnnDebug.SingleNeighbour.set("diff", diff);
			KnnDebug.SingleNeighbour.set("diff-norm", normedDiff);
			KnnDebug.SingleNeighbour.set("range-min", m_Ranges[index][R_MIN]);
			KnnDebug.SingleNeighbour.set("range-max", m_Ranges[index][R_MAX]);
			KnnDebug.SingleNeighbour.set("test-val", niceVal1);
			KnnDebug.SingleNeighbour.set("train-val", niceVal2);
		}

		return normedDiff;
	}

	public abstract double distance(double matrix[][]);

	public void updateRangesFirst(Instance instance, int numAtt, double[][] ranges)
	{
		for (int j = 0; j < numAtt; j++)
		{
			switch (m_Data.attribute(j).type())
			{
				case Attribute.STRING:
					String s = instance.stringValue(j);
					if (s.length() > 2)
					{
						List<Double> set = SetDistanceCache.getSet(s);
						ranges[j][R_MIN] = ListUtil.getMin(set);
						ranges[j][R_MAX] = ListUtil.getMax(set);
						ranges[j][R_WIDTH] = ranges[j][R_MAX] - ranges[j][R_MIN];
					}
					else
					{ // if value was missing
						ranges[j][R_MIN] = Double.POSITIVE_INFINITY;
						ranges[j][R_MAX] = -Double.POSITIVE_INFINITY;
						ranges[j][R_WIDTH] = Double.POSITIVE_INFINITY;
					}
					break;
				case Attribute.NOMINAL:
					// do noting so far
					break;
				default:
					throw new IllegalArgumentException("attribute neither string(sub-set) nor nominal");
			}
		}
	}

	@Override
	protected double norm(double x, int i)
	{
		if (Double.isNaN(m_Ranges[i][R_MIN]) || m_Ranges[i][R_WIDTH] == 0)
			return 0;
		else
			return x / m_Ranges[i][R_WIDTH];
	}

	@Override
	public double[][] updateRanges(Instance instance, double[][] ranges)
	{
		// updateRangesFirst must have been called on ranges
		for (int j = 0; j < ranges.length; j++)
		{
			switch (m_Data.attribute(j).type())
			{
				case Attribute.STRING:
					String s = instance.stringValue(j);
					if (s.length() > 2)
					{
						List<Double> set = SetDistanceCache.getSet(s);
						double min = ListUtil.getMin(set);
						double max = ListUtil.getMax(set);

						if (min < ranges[j][R_MIN])
						{
							ranges[j][R_MIN] = min;
							ranges[j][R_WIDTH] = ranges[j][R_MAX] - ranges[j][R_MIN];
						}
						if (max > ranges[j][R_MAX])
						{
							ranges[j][R_MAX] = max;
							ranges[j][R_WIDTH] = ranges[j][R_MAX] - ranges[j][R_MIN];
						}
					}
					break;
				case Attribute.NOMINAL:
					// do noting so far
					break;
				default:
					throw new IllegalArgumentException("attribute neither string(sub-set) nor nominal");
			}
		}

		return ranges;
	}

	@Override
	public boolean inRanges(Instance instance, double[][] ranges)
	{
		// updateRangesFirst must have been called on ranges
		for (int j = 0; j < ranges.length; j++)
		{
			String s = instance.stringValue(j);
			if (s.length() > 2)
			{
				List<Double> set = SetDistanceCache.getSet(s);

				if (ListUtil.getMin(set) < ranges[j][R_MIN])
					return false;
				if (ListUtil.getMax(set) > ranges[j][R_MAX])
					return false;
			}
		}
		return true;
	}

	@Override
	public void updateRanges(Instance instance, int numAtt, double[][] ranges)
	{
		// updateRangesFirst must have been called on ranges
		for (int j = 0; j < numAtt; j++)
		{
			switch (m_Data.attribute(j).type())
			{
				case Attribute.STRING:

					String s = instance.stringValue(j);
					if (s.length() > 2)
					{
						List<Double> set = SetDistanceCache.getSet(s);
						double min = ListUtil.getMin(set);
						double max = ListUtil.getMax(set);

						if (min < ranges[j][R_MIN])
						{
							ranges[j][R_MIN] = min;
							ranges[j][R_WIDTH] = ranges[j][R_MAX] - ranges[j][R_MIN];
						}
						if (max > ranges[j][R_MAX])
						{
							ranges[j][R_MAX] = max;
							ranges[j][R_WIDTH] = ranges[j][R_MAX] - ranges[j][R_MIN];
						}
					}
					break;

				case Attribute.NOMINAL:

					// nothing to do for nominal attributes (distance is 0 or 1)
					break;

				default:

					throw new NotImplementedException();
			}
		}
	}

}
