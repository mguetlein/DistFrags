package weka.core;

import java.util.ArrayList;
import java.util.Vector;

import launch.Settings;
import util.ArrayUtil;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.old.KnnDebug;

public class LinearNNSearchCombineDistances extends LinearNNSearch
{
	Vector<DistanceFunction> m_distanceFunctions = new Vector<DistanceFunction>();

	public LinearNNSearchCombineDistances(DistanceFunction... functions)
	{
		super();

		for (DistanceFunction d : functions)
			m_distanceFunctions.add(d);
		m_DistanceFunction = null;
	}

	public LinearNNSearchCombineDistances(Instances inst)
	{
		throw new Error("not implemented");
	}

	public void setInstances(Instances insts) throws Exception
	{
		m_Instances = insts;

		for (DistanceFunction d : m_distanceFunctions)
			d.setInstances(insts);
	}

	public void update(Instance ins) throws Exception
	{
		if (m_Instances == null)
			throw new Exception("No instances supplied yet. Cannot update without" + "supplying a set of instances first.");

		for (DistanceFunction d : m_distanceFunctions)
		{
			if (d instanceof SetDistanceFunction)
				((SetDistanceFunction) d).clearWeight();
			d.update(ins);
		}
	}

	private String getDistanceFunctionName(int index)
	{
		if (m_distanceFunctions.get(index) instanceof SetDistanceFunction)
			return ((SetDistanceFunction) m_distanceFunctions.get(index)).getSetDistance().getClass().getSimpleName();
		else
			return m_distanceFunctions.get(index).getClass().getSimpleName();
	}

	public Instances kNearestNeighbours(Instance target, int kNN) throws Exception
	{
		// StopWatchUtil.start("knn " + this);
		// try
		// {
		if (m_distanceFunctions.size() < 1)
			throw new Error("no distances functions specified");
		if (m_SkipIdentical)
			throw new Error("identical skip not supported");

		if (m_Stats != null)
			m_Stats.searchStart();

		double[] totalDistance = new double[m_Instances.numInstances()];

		// Vector<MinMaxAvg> minMax = new Vector<MinMaxAvg>();

		KNNLinePlotter plot = null;
		if (Settings.DEBUG_KNN_LINE_PLOT)
		{
			plot = new KNNLinePlotter();
			plot.init("instance", KnnDebug.SinglePredictionActualClassValue == 1.0);
		}

		double[][] dbg_distances = null;
		double[][] dbg_orig_distances = null;
		double[][] dbg_weights = null;
		int[] dbg_max_pairs = null;

		if (Settings.DEBUG_KNN_ANALYZE && m_distanceFunctions.size() > 1)
		{
			dbg_distances = new double[m_distanceFunctions.size()][m_Instances.size()];
			dbg_orig_distances = new double[m_distanceFunctions.size()][m_Instances.size()];
			dbg_weights = new double[m_distanceFunctions.size()][m_Instances.size()];
			IBkDebbuger.INSTANCE.distanceFunction = (SetDistanceFunction) m_distanceFunctions.get(1);
			dbg_max_pairs = new int[m_distanceFunctions.size()];
		}

		KNNBarPlotter barPlot = null;
		double barPlotDistances[][] = null;
		if (Settings.DEBUG_KNN_BAR_PLOT)
		{
			barPlot = new KNNBarPlotter();
			String categories[] = new String[m_distanceFunctions.size()];
			for (int i = 0; i < categories.length; i++)
				categories[i] = getDistanceFunctionName(i);
			barPlot.init(categories);
			barPlotDistances = new double[m_Instances.size()][m_distanceFunctions.size()];
		}

		for (int j = 0; j < m_distanceFunctions.size(); j++)
		{
			DistanceFunction distanceFunction = m_distanceFunctions.get(j);
			Double[] distances = new Double[m_Instances.numInstances()];

			for (int i = 0; i < m_Instances.numInstances(); i++)
			{
				if (target == m_Instances.instance(i)) // for hold-one-out
					// cross-validation
					continue;

				// StopWatchUtil.start(distanceFunction + " distance");
				double d = distanceFunction.distance(target, m_Instances.instance(i), Double.POSITIVE_INFINITY, m_Stats);
				// StopWatchUtil.stop(distanceFunction + " distance");

				// if (new Random().nextDouble() > 0.95)
				// d = Double.MAX_VALUE;
				if (d != Double.MAX_VALUE)
					distances[i] = d;

			}

			// normalizes distance to 0-1, null values are replaced with 0.5
			distances = ArrayUtil.normalize(distances);

			if (distanceFunction instanceof SetDistanceFunction)
			{
				for (int i = 0; i < m_Instances.numInstances(); i++)
				{
					if (distances[i] != 0.5)
					{
						double delta = distances[i] - 0.5;
						double w = ((SetDistanceFunction) distanceFunction).getWeight(target, m_Instances.instance(i));
						// if (delta < 0)
						// w *= 0.1;
						// w *= w;
						double new_dist = 0.5 + delta * w;

						if (Settings.DEBUG_KNN_ANALYZE && m_distanceFunctions.size() > 1)
						{
							dbg_orig_distances[j][i] = distances[i];
							dbg_weights[j][i] = w;
							dbg_max_pairs[j] = ((SetDistanceFunction) distanceFunction).getMaxPairs();
						}

						// if (new_dist == 0)
						// throw new Error(i + ": before: " + distances[i] + ", weight: " + w + ", after: " + new_dist);

						// System.err.println(i + ": before: " + distances[i] + ", weight: " + w + ", after: " + new_dist);
						distances[i] = new_dist;

					}
				}
			}

			if (Settings.DEBUG_KNN_LINE_PLOT)
			{
				int ordering[] = ArrayUtil.getOrdering(ArrayUtil.toPrimitiveDoubleArray(distances), true);
				double d[] = new double[ordering.length];
				boolean c[] = new boolean[ordering.length];
				for (int i = 0; i < c.length; i++)
				{
					d[i] = distances[ordering[i]];
					c[i] = m_Instances.get(ordering[i]).classValue() == 1.0;
				}
				plot.addSeries(getDistanceFunctionName(j), d, c);
			}
			if (Settings.DEBUG_KNN_BAR_PLOT)
				for (int i = 0; i < m_Instances.numInstances(); i++)
					barPlotDistances[i][j] = distances[i]
							* ((m_Instances.get(i).classValue() == KnnDebug.SinglePredictionActualClassValue) ? 1 : -1);
			if (Settings.DEBUG_KNN_ANALYZE && m_distanceFunctions.size() > 1)
				for (int i = 0; i < m_Instances.numInstances(); i++)
					dbg_distances[j][i] = distances[i];

			for (int i = 0; i < distances.length; i++)
				totalDistance[i] += distances[i];

			// minMax.add(MinMaxAvg.minMaxAvg(distances));
			// System.out.println(ArrayUtils.toString(distances));
			// System.out.println(minMax.lastElement());
			// System.out.println(ArrayUtils.toString(ArrayUtil.normalize(distances)));
		}

		int ordering[] = ArrayUtil.getOrdering(totalDistance, true);

		if (Settings.DEBUG_KNN_LINE_PLOT)
		{
			double d[] = new double[ordering.length];
			boolean c[] = new boolean[ordering.length];
			for (int i = 0; i < c.length; i++)
			{
				d[i] = totalDistance[ordering[i]];
				c[i] = m_Instances.get(ordering[i]).classValue() == 1.0;
			}
			plot.addSeries("total", d, c);
			plot.plot("Instance value: " + (KnnDebug.SinglePredictionActualClassValue == 1.0), "training instances sorted by distance",
					"ratio correct classified");
		}
		// if (Settings.DEBUG_KNN_ANALYZE)
		// {
		// if (dbg.analyze())
		// {
		// for (int j = 0; j < m_distanceFunctions.size(); j++)
		// {
		// DistanceFunction distanceFunction = m_distanceFunctions.get(j);
		//
		// if (distanceFunction instanceof SetDistanceFunction)
		// {
		// SetDistanceFunction.DEBUG = true;
		// for (int i = 0; i < m_Instances.numInstances(); i++)
		// {
		// if (target == m_Instances.instance(i))
		// continue;
		// distanceFunction.distance(target, m_Instances.instance(i), Double.POSITIVE_INFINITY, m_Stats);
		// }
		// SetDistanceFunction.DEBUG = false;
		// }
		// }
		// }
		//
		// }
		if (Settings.DEBUG_KNN_BAR_PLOT)
		{
			for (int i = 0; i < ordering.length; i++)
				barPlot.addValue(barPlotDistances[ordering[i]]);
			barPlot.plot();
		}

		// if (DEBUG)
		// debug("total Distances: " + ArrayUtil.toString(totalDistance));

		if (m_Stats != null)
			m_Stats.searchFinish();

		if (totalDistance[ordering[0]] == totalDistance[ordering[totalDistance.length - 1]])
		{
			// no neighbors!!
			System.err.println("no neighbors");
			return null;
		}
		else
		{
			Instances neighbours = new Instances(m_Instances, kNN);
			ArrayList<Integer> selectedAttributes = new ArrayList<Integer>(kNN);
			ArrayList<Double> finalDistances = new ArrayList<Double>(kNN);

			int i = 0;
			while (neighbours.size() < kNN || totalDistance[ordering[i]] == finalDistances.get(i - 1))
			{
				// System.err.println(">> neighbor " + i + ", distance: " + totalDistance[ordering[i]]);
				selectedAttributes.add(ordering[i]);
				finalDistances.add(totalDistance[ordering[i]]);
				neighbours.add(m_Instances.get(ordering[i]));
				i++;
			}
			if (neighbours.size() < kNN)
				throw new Error("WTF");
			m_Distances = ArrayUtil.toPrimitiveDoubleArray(finalDistances);

			if (Settings.DEBUG_KNN_ANALYZE && m_distanceFunctions.size() > 1)
				IBkDebbuger.INSTANCE.addTestInstance(target, (KnnDebug.SinglePredictionActualClassValue == 1.0), totalDistance,
						dbg_distances, dbg_orig_distances, dbg_weights, dbg_max_pairs);

			return neighbours;
		}

		// } finally
		// {
		// StopWatchUtil.stop("knn " + this);
		// }

	}
}
