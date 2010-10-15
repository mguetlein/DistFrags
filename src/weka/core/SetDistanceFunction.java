package weka.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import launch.Settings;

import org.apache.commons.lang.NotImplementedException;

import weka.core.neighboursearch.PerformanceStats;
import weka.core.setdistance.SetDistance;
import weka.core.setdistance.SetDistanceCache;

public class SetDistanceFunction extends NamedDistanceFunction
{
	static boolean DEBUG = false;

	public static void debug(String msg)
	{
		if (DEBUG)
			System.out.println("--- " + msg);
	}

	SetDistance setDistance;
	public static double MISSING_VALUE = Double.MAX_VALUE;
	Instances m_Data;
	double[][] m_Ranges;
	int rangeInitCount;
	private int stringSetCount;
	public static final int R_MIN = 0;
	public static final int R_MAX = 1;
	public static final int R_WIDTH = 2;
	public static final double R_UNDEF = -1.0;

	int maxCommonAttributes = -Integer.MAX_VALUE;

	// int commonAttributesTrain[][];

	// int commonInitCount = 0;

	public SetDistanceFunction(SetDistance setDistance)
	{
		this.setDistance = setDistance;
	}

	public SetDistance getSetDistance()
	{
		return setDistance;
	}

	@Override
	public double distance(Instance first, Instance second)
	{
		return distance(first, second, Double.POSITIVE_INFINITY, null);
	}

	@Override
	public double distance(Instance first, Instance second, PerformanceStats stats) throws Exception
	{
		return distance(first, second, Double.POSITIVE_INFINITY, stats);
	}

	@Override
	public double distance(Instance first, Instance second, double cutOffValue)
	{
		return distance(first, second, cutOffValue, null);
	}

	@Override
	public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats)
	{
		double squaredSetValueDistance = 0;

		int validSetValuesCount = 0;

		List<Double> diffs = new ArrayList<Double>();

		for (int i = 0; i < m_Data.numAttributes(); i++)
		{
			if (m_Data.classIndex() != i)
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
				default:
					// ignore
				}
			}
		}

		if (validSetValuesCount > 0)
		{
			double setValueDistance = Math.sqrt(squaredSetValueDistance) / (double) validSetValuesCount;
			assert (!Double.isNaN(setValueDistance));
			return setValueDistance;
		}
		else
			return Double.MAX_VALUE;
	}

	public int getMaxPairs()
	{
		return maxCommonAttributes;
	}

	public double getWeight(Instance inst1, Instance inst2)
	{
		int commonAttributes = 0;
		int numStringAttributes = 0;

		for (int i = 0; i < m_Data.numAttributes(); i++)
		{
			if (m_Data.attribute(i).type() != Attribute.STRING)
				continue;
			numStringAttributes++;
			if (inst1.stringValue(i).length() <= 2 || inst2.stringValue(i).length() <= 2)
				continue;
			commonAttributes++;
		}

		if (commonAttributes > 0 && commonAttributes > maxCommonAttributes)
			throw new Error(commonAttributes + " / " + maxCommonAttributes);

		// if (commonAttributes == maxCommonAttributes)
		// {
		// System.err.println("weight is 1");
		// System.err.println(inst1);
		// System.err.println(inst2);
		// }

		double w = commonAttributes / (double) maxCommonAttributes;

		// w *= maxCommonAttributes / (double) numStringAttributes;

		return w;
	}

	protected double updateDistance(double currDist, double diff)
	{
		return (currDist + (diff * diff));
	}

	protected double difference(int index, Instance first, Instance second)
	{
		return difference(index, first, second, true);
	}

	protected double difference(int index, Instance first, Instance second, boolean normalize)
	{
		// StopWatchUtil.start("difference");
		// try
		// {
		switch (m_Data.attribute(index).type())
		{
		case Attribute.STRING:

			String string1 = first.stringValue(index);
			if (string1.length() <= 2)
				return MISSING_VALUE;
			String string2 = second.stringValue(index);
			if (string2.length() <= 2)
				return MISSING_VALUE;
			double diff = setDistance.distance(SetDistanceCache.getMatrix(string1, string2));

			if (DEBUG)
			{
				debug("train: " + second);
				debug("diff: " + diff);
				System.exit(0);
			}

			// double[][] matrix = SetDistanceCache.getMatrix(first, second,
			// index);
			// if (matrix == null)
			// return MISSING_VALUE;
			// double diff = setDistance.distance(matrix);

			assert (diff != MISSING_VALUE && diff != Double.MAX_VALUE && diff >= 0) : "illegal difference: " + diff;

			double u_diff = diff;

			if (normalize)
				diff = normalize(diff, index);

			if (Settings.DEBUG_KNN_ANALYZE && IBkDebbuger.DISTANCE_INSTANCE != null)
				IBkDebbuger.DISTANCE_INSTANCE.addAttribute(index, m_Data.attribute(index).name(), u_diff, diff, string1, string2);

			return diff;

		default:
			throw new NotImplementedException();
		}
		// } finally
		// {
		// StopWatchUtil.stop("difference");
		// }
	}

	protected double normalize(double distance, int i)
	{
		if (m_Data.attribute(i).type() != Attribute.STRING)
			throw new IllegalStateException();

		if (m_Ranges[i][R_WIDTH] == R_UNDEF)
			throw new Error("undef");
		else if (m_Ranges[i][R_WIDTH] == 0 && distance == m_Ranges[i][R_MIN] && distance == m_Ranges[i][R_MAX])
			return 0;
		else if (Double.isNaN(m_Ranges[i][R_MIN]) || m_Ranges[i][R_WIDTH] == 0 || m_Ranges[i][R_WIDTH] == Double.MAX_VALUE)
			throw new Error("analyze me! min:" + m_Ranges[i][R_MIN] + " max:" + m_Ranges[i][R_MAX] + " width:" + m_Ranges[i][R_WIDTH]);
		// return 0;
		else
			return distance / m_Ranges[i][R_WIDTH];
	}

	protected void updateNormalization(Instance ins)
	{
		// int debug_total = 0;
		// int debug_skip = 0;
		// int debug_update = 0;

		int numAtt = m_Data.numAttributes();

		int commonAttribs[] = new int[m_Data.numInstances()];

		for (int i = 0; i < numAtt; i++)
		{
			if (m_Data.attribute(i).type() != Attribute.STRING)
				continue;

			if (ins.stringValue(i).length() <= 2)
			{
				// debug_skip++;
				continue;
			}

			for (int j = 0; j < m_Data.numInstances(); j++)
			{
				// debug_total++;
				double distance = difference(i, m_Data.get(j), ins, false);
				if (distance == MISSING_VALUE)
				{
					// debug_skip++;
					continue;
				}

				commonAttribs[j]++;
				if (commonAttribs[j] > maxCommonAttributes)
					maxCommonAttributes = commonAttribs[j];

				if (m_Ranges[i][R_WIDTH] == R_UNDEF)
				{
					// debug_update++;
					initNormalization(i);
				}
				if (distance < m_Ranges[i][R_MIN])
					m_Ranges[i][R_MIN] = distance;
				if (distance > m_Ranges[i][R_MAX])
					m_Ranges[i][R_MAX] = distance;
				if (m_Ranges[i][R_MIN] != Double.MAX_VALUE && m_Ranges[i][R_MAX] != -Double.MAX_VALUE)
					m_Ranges[i][R_WIDTH] = m_Ranges[i][R_MAX] - m_Ranges[i][R_MIN];
			}
		}

		// commonInitCount++;

		// if (DEBUG)
		// debug("upated for test instance" + /*
		// * ", total: " + debug_total + ", skipped: " + debug_skip +
		// ", init-attribs: "
		// * +debug_update + ","+
		// */" total initialized sets: " + rangeInitCount + "/" +
		// stringSetCount);

		// if (DEBUG)// && rangeInitCount % 100 == 0)
		// debug(rangeInitCount + "/" + m_Data.numAttributes() +
		// " attribute normalization computed");
	}

	protected void initNormalization(int attribute)
	{
		if (m_Data.attribute(attribute).type() != Attribute.STRING)
			throw new Error("no string");

		// if (commonAttributesTrain == null)
		// commonAttributesTrain = new int[m_Data.numInstances()][m_Data.numInstances()];

		m_Ranges[attribute][R_MIN] = Double.MAX_VALUE;
		m_Ranges[attribute][R_MAX] = -Double.MAX_VALUE;
		m_Ranges[attribute][R_WIDTH] = Double.MAX_VALUE;

		for (int j = 0; j < m_Data.numInstances(); j++)
		{
			if (m_Data.get(j).stringValue(attribute).length() <= 2)
			{
				// debug_skip++;
				continue;
			}

			for (int k = 0; k < m_Data.numInstances(); k++)
			{
				if (j == k)
					continue;
				double distance = difference(attribute, m_Data.get(j), m_Data.get(k), false);
				if (distance == MISSING_VALUE)
					continue;

				// commonAttributesTrain[j][k]++;
				// if (commonAttributesTrain[j][k] > maxCommonAttributes)
				// maxCommonAttributes = commonAttributesTrain[j][k];

				if (distance < m_Ranges[attribute][R_MIN])
					m_Ranges[attribute][R_MIN] = distance;
				if (distance > m_Ranges[attribute][R_MAX])
					m_Ranges[attribute][R_MAX] = distance;
			}
		}
		if (m_Ranges[attribute][R_MIN] != Double.MAX_VALUE && m_Ranges[attribute][R_MAX] != -Double.MAX_VALUE)
			m_Ranges[attribute][R_WIDTH] = m_Ranges[attribute][R_MAX] - m_Ranges[attribute][R_MIN];

		rangeInitCount++;
	}

	protected void initNormalization()
	{
		int numAtt = m_Data.numAttributes();
		m_Ranges = new double[numAtt][3];
		rangeInitCount = 0;
		stringSetCount = 0;

		for (int i = 0; i < numAtt; i++)
		{
			if (m_Data.attribute(i).type() != Attribute.STRING)
				continue;
			stringSetCount++;

			m_Ranges[i][R_MIN] = R_UNDEF;
			m_Ranges[i][R_MAX] = R_UNDEF;
			m_Ranges[i][R_WIDTH] = R_UNDEF;
		}

		if (DEBUG)
			debug(stringSetCount + "/" + m_Data.numAttributes() + " attributes are string set attributes");
	}

	@Override
	public void update(Instance ins)
	{
		updateNormalization(ins);
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
		initNormalization();
		// SetDistanceCache.clearInstanceCache();
	}

	@Override
	public void setInvertSelection(boolean value)
	{
		throw new Error("not yet implemented");

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

	public void clearWeight()
	{
		maxCommonAttributes = -Integer.MAX_VALUE;
	}

}
