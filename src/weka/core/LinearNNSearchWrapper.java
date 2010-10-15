package weka.core;

import java.util.ArrayList;
import java.util.Arrays;

public class LinearNNSearchWrapper extends LinearNNSearchCombineDistances
{
	static boolean DEBUG = false;
	int count = 0;

	public static void debug(String msg)
	{
		if (DEBUG)
			System.out.println(">>> " + msg);
	}

	public Instances kNearestNeighbours(Instance target, int kNN) throws Exception
	{
		// DEBUG = count == 9;
		count++;

		if (DEBUG)
		{
			debug("");
			debug("performing " + kNN + "NN Search, #instances: " + m_Instances.numInstances());
			debug(target.toString());
		}

		Instances res = super.kNearestNeighbours(target, kNN);

		if (DEBUG)
		{
			debug("num neighbors: " + res.size());
			debug("distances: " + Arrays.toString(m_Distances));

			ArrayList<Integer> selectedAttributes = new ArrayList<Integer>();
			for (Instance i : res)
			{
				int index = -1;
				String s = i.toString();
				for (int j = 0; j < m_Instances.size(); j++)
					if (s.equals(m_Instances.get(j).toString()))
					{
						index = j;
						break;
					}
				selectedAttributes.add(index);// m_Instances.indexOf(i));
				debug(i.toString());
			}
			debug("selected: " + Arrays.toString(selectedAttributes.toArray()));
		}

		return res;
	}
}
