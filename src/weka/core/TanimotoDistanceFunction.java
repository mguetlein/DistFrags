package weka.core;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashMap;

import weka.core.neighboursearch.PerformanceStats;

public class TanimotoDistanceFunction extends NamedDistanceFunction
{
	Instances m_Data;

	@Override
	public double distance(Instance first, Instance second)
	{
		return distance(first, second, Double.POSITIVE_INFINITY, null);
	}

	@Override
	public double distance(Instance first, Instance second,
			PerformanceStats stats) throws Exception
	{
		return distance(first, second, Double.POSITIVE_INFINITY, stats);
	}

	@Override
	public double distance(Instance first, Instance second, double cutOffValue)
	{
		return distance(first, second, cutOffValue, null);
	}

	private HashMap<Instance, BitSet> bitSetCache;

	private int cacheCount = 0;
	private int missCount = 0;

	private void putSet(Instance i, BitSet b)
	{
		if (bitSetCache == null)
			bitSetCache = new HashMap<Instance, BitSet>();
		bitSetCache.put(i, b);
	}

	private BitSet getSet(Instance i)
	{
		if (bitSetCache == null)
			return null;
		return bitSetCache.get(i);
	}

	private void clearSetCache()
	{
		bitSetCache = null;
		cacheCount = 0;
		missCount = 0;
	}

	@Override
	public double distance(Instance first, Instance second, double cutOffValue,
			PerformanceStats stats)
	{
		int numAttributes = m_Data.numAttributes();

		BitSet firstNominalBitSet = null;
		BitSet secondNominalBitSet = null;

		boolean firstCached = false;
		boolean secondCached = false;

		for (int i = 0; i < numAttributes; i++)
		{
			if (m_Data.classIndex() != i)
			{
				switch (m_Data.attribute(i).type())
				{
				case Attribute.NOMINAL:

					if (firstNominalBitSet == null)
					{
						firstNominalBitSet = getSet(first);
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

						secondNominalBitSet = getSet(second);
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
					// Status.WARN.println(i + ": new vals 1:" + firstOn + " 2:"
					// + secondOn + " size 1:"
					// + firstNominalBitSet.size() + " 2:" +
					// secondNominalBitSet.size());
					// if (firstNominalBitSet.size() !=
					// secondNominalBitSet.size())
					// throw new Error("WTF");
					// bitSetCount++;
					break;
				default:
					// ignore
				}
			}
		}

		if (!firstCached)
			putSet(first, firstNominalBitSet);
		if (!secondCached)
			putSet(second, secondNominalBitSet);

		return 1 - tanimoto(firstNominalBitSet, secondNominalBitSet);
	}

	public static float tanimoto(BitSet bitset1, BitSet bitset2)// copied from
																// org.openscience.cdk.similarity.Tanimoto;
																// apart
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
		return _common_bit_count
				/ (_bitset1_cardinality + _bitset2_cardinality - _common_bit_count);
	}

	@Override
	public String getAttributeIndices()
	{
		throw new Error("not yet implemented");
		// return null;
	}

	@Override
	public Instances getInstances()
	{
		throw new Error("not yet implemented");
		// return null;
	}

	@Override
	public boolean getInvertSelection()
	{
		throw new Error("not yet implemented");
		// return false;
	}

	@Override
	public void postProcessDistances(double[] distances)
	{
		throw new Error("not yet implemented");

	}

	@Override
	public void setAttributeIndices(String value)
	{
		throw new Error("not yet implemented");

	}

	@Override
	public void setInstances(Instances insts)
	{
		m_Data = insts;
		clearSetCache();
	}

	@Override
	public void setInvertSelection(boolean value)
	{
		throw new Error("not yet implemented");

	}

	@Override
	public void update(Instance ins)
	{
		// no need to update
	}

	@Override
	public String[] getOptions()
	{
		throw new Error("not yet implemented");
		// return null;
	}

	@Override
	public Enumeration listOptions()
	{
		throw new Error("not yet implemented");
		// return null;
	}

	@Override
	public void setOptions(String[] options) throws Exception
	{
		throw new Error("not yet implemented");
	}

	// @Override
	// public String globalInfo()
	// {
	// return "tanimoto for nominal attibutes";
	// }
	//
	// @Override
	// public String getRevision()
	// {
	// return RevisionUtils.extract("$Revision: 0.1 $");
	// }
	//
	// @Override
	// protected double updateDistance(double currDist, double diff)
	// {
	// // TODO Auto-generated method stub
	// return 0;
	// }

}
