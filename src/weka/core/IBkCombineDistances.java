package weka.core;

import io.Status;
import launch.Settings;
import util.StringUtil;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities.Capability;
import eval.DefaultWekaEvaluation;

public class IBkCombineDistances extends IBk
{
	private ZeroR zeroR;

	private long debug_startTime = -1;
	private long debug_lastMsgTime = -1;
	private long debug_predictCount = -1;
	public static int test_instance_index = -1;

	public IBkCombineDistances(int kNN)
	{
		super(kNN);
	}

	protected void init()
	{
		// m_NNSearch = new LinearNNSearchCombineDistances();
		zeroR = new ZeroR();
		super.init();
	}

	public void buildClassifier(Instances instances) throws Exception
	{
		if (!(m_NNSearch instanceof LinearNNSearchCombineDistances))
			throw new Error("not a combined search");
		super.buildClassifier(instances);
		zeroR.buildClassifier(instances);

		if (Settings.DEBUG_KNN_ANALYZE)
			IBkDebbuger.INSTANCE.zeroR = zeroR;

		debug_startTime = System.currentTimeMillis();
		debug_lastMsgTime = debug_startTime;
		test_instance_index = 0;
	}

	public Capabilities getCapabilities()
	{
		Capabilities result = super.getCapabilities();
		result.enable(Capability.STRING_ATTRIBUTES);
		return result;
	}

	@Override
	public double[] distributionForInstance(Instance instance) throws Exception
	{
		test_instance_index++;
		if (System.currentTimeMillis() - debug_lastMsgTime > 5000)
		{
			debug_lastMsgTime = System.currentTimeMillis();
			Status.INFO.println(Status.INDENT + "IBk " + StringUtil.formatTime(debug_lastMsgTime - debug_startTime)
					+ " predict test instance " + test_instance_index + "/" + DefaultWekaEvaluation.DEBUG_NUM_TEST_INSTANCES);
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

		// StopWatchUtil.start("up " + this.hashCode());
		m_NNSearch.addInstanceInfo(instance);
		// StopWatchUtil.stop("up " + this.hashCode());
		// StopWatchUtil.start("nn " + this.hashCode());
		Instances neighbours = m_NNSearch.kNearestNeighbours(instance, m_kNN);
		// StopWatchUtil.stop("nn " + this.hashCode());

		if (neighbours == null || neighbours.numInstances() == 0)
			return zeroR.distributionForInstance(instance);
		else
		{
			double[] distances = m_NNSearch.getDistances();
			return makeDistribution(neighbours, distances);
		}
	}
}
