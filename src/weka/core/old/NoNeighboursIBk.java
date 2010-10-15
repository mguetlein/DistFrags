/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    IBk.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.old;

import io.Status;
import launch.Settings;

import org.apache.commons.collections.iterators.ArrayIterator;

import util.MinMaxAvg;
import util.StringUtil;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Capabilities.Capability;

public class NoNeighboursIBk extends IBk
{

	private ZeroR zeroR;

	public Capabilities getCapabilities()
	{
		Capabilities result = super.getCapabilities();
		result.enable(Capability.STRING_ATTRIBUTES);
		return result;
	}

	public void buildClassifier(Instances instances) throws Exception
	{
		super.buildClassifier(instances);
		zeroR.buildClassifier(instances);

		instance_count = 0;
		lastMessage = -1;

		if (m_NNSearch.getDistanceFunction() instanceof OldStringSetDistanceFunction)
			((OldStringSetDistanceFunction) m_NNSearch.getDistanceFunction()).clearCache();
	}

	public static long start = -1;
	public static long lastMessage = -1;
	public static int instance_count = 0;

	@SuppressWarnings("unchecked")
	public double[] distributionForInstance(Instance instance) throws Exception
	{
		long now = System.currentTimeMillis();
		if (instance_count == 0)
			start = now;
		instance_count++;

		if (lastMessage == -1)
			lastMessage = now;
		if (now - lastMessage > 5000)
		{
			Status.INFO.println("IBk " + StringUtil.formatTime(now - start) + " > " + instance_count + " instances done");
			lastMessage = now;
		}

		if (m_Train.numInstances() == 0)
		{
			throw new Exception("No training instances!");
		}
		if ((m_WindowSize > 0) && (m_Train.numInstances() > m_WindowSize))
		{
			m_kNNValid = false;
			boolean deletedInstance = false;
			while (m_Train.numInstances() > m_WindowSize)
			{
				m_Train.delete(0);
			}
			// rebuild datastructure KDTree currently can't delete
			if (deletedInstance == true)
				m_NNSearch.setInstances(m_Train);
		}

		// Select k by cross validation
		if (!m_kNNValid && (m_CrossValidate) && (m_kNNUpper >= 1))
		{
			crossValidate();
		}

		m_NNSearch.addInstanceInfo(instance);

		if (Settings.DEBUG_KNN_PRINT_TESTSET_PREDICT)
		{
			KnnDebug.TestPredictions.add();
			KnnDebug.TestPredictions.set("i", KnnDebug.TestPredictions.currentIndex);
		}

		Instances neighbours = m_NNSearch.kNearestNeighbours(instance, m_kNN);

		double[] distribution;

		double minDist = Double.NaN;
		double maxDist = Double.NaN;
		double avgDist = Double.NaN;

		if (neighbours.numInstances() == 0)
			distribution = zeroR.distributionForInstance(instance);
		else
		{
			double[] distances = m_NNSearch.getDistances();
			MinMaxAvg mma = MinMaxAvg.minMaxAvg(new ArrayIterator(distances));
			minDist = mma.getMin();
			maxDist = mma.getMax();
			avgDist = mma.getMean();

			distribution = makeDistribution(neighbours, distances);
		}

		if (Settings.DEBUG_KNN_PRINT_TESTSET_PREDICT)
		{
			KnnDebug.TestPredictions.set("#neigb", neighbours.numInstances());
			KnnDebug.TestPredictions.set("dist-min", minDist);
			KnnDebug.TestPredictions.set("dist-max", maxDist);
			KnnDebug.TestPredictions.set("dist-avg", avgDist);
			KnnDebug.TestPredictions.set("distr[0]", distribution[0]);
			KnnDebug.TestPredictions.set("distr[1]", distribution[1]);
		}

		return distribution;
	}

	protected void init()
	{
		super.init();
		m_NNSearch = new NoNeighboursLinearNNSearch();
		zeroR = new ZeroR();
	}

}
