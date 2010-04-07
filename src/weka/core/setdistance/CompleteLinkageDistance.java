package weka.core.setdistance;

import weka.core.AbstractStringSetDistanceFunction;

public class CompleteLinkageDistance extends AbstractStringSetDistanceFunction
{
	@Override
	public double distance(double matrix[][])
	{
		double res = -1;
		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < matrix[0].length; j++)
			{
				if (matrix[i][j] > res)
					res = matrix[i][j];
			}
		}
		assert res == (int) res;
		return res;
	}

}
