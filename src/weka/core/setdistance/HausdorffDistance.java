package weka.core.setdistance;

import weka.core.AbstractStringSetDistanceFunction;

public class HausdorffDistance extends AbstractStringSetDistanceFunction
{
	@Override
	public double distance(double matrix[][])
	{
		double maxMinA = -1;
		for (int i = 0; i < matrix.length; i++)
		{
			double minA = Double.MAX_VALUE;
			for (int j = 0; j < matrix[0].length; j++)
				if (matrix[i][j] < minA)
					minA = matrix[i][j];

			if (minA > maxMinA)
				maxMinA = minA;
		}
		double res = maxMinA;

		double maxMinB = -1;
		for (int j = 0; j < matrix[0].length; j++)
		{
			double minB = Double.MAX_VALUE;
			for (int i = 0; i < matrix.length; i++)
				if (matrix[i][j] < minB)
					minB = matrix[i][j];

			if (minB > maxMinB)
				maxMinB = minB;
		}
		if (maxMinB > res)
			res = maxMinB;

		assert res == (int) res;
		return res;
	}

}
