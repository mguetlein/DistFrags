package weka.core.setdistance;


public class SingleLinkageDistance implements SetDistance
{
	@Override
	public double distance(double matrix[][])
	{
		double res = Double.MAX_VALUE;
		for (int i = 0; i < matrix.length; i++)
		{
			for (int j = 0; j < matrix[0].length; j++)
			{
				if (matrix[i][j] < res)
					res = matrix[i][j];
			}
		}
		assert res == (int) res;
		return res;
	}

}
