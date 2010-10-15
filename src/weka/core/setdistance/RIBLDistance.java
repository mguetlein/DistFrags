package weka.core.setdistance;


public class RIBLDistance implements SetDistance
{
	@Override
	public double distance(double matrix[][])
	{
		double res = 0;
		if (matrix.length <= matrix[0].length)
		{
			for (int i = 0; i < matrix.length; i++)
			{
				double min = Double.MAX_VALUE;
				for (int j = 0; j < matrix[0].length; j++)
					if (matrix[i][j] < min)
						min = matrix[i][j];
				res += min;
			}
			res /= matrix.length;
		}
		else
		{
			for (int j = 0; j < matrix[0].length; j++)
			{
				double min = Double.MAX_VALUE;
				for (int i = 0; i < matrix.length; i++)
					if (matrix[i][j] < min)
						min = matrix[i][j];
				res += min;
			}
			res /= matrix[0].length;
		}
		return res;
	}

}
