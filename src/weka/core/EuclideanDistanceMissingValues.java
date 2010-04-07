package weka.core;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class EuclideanDistanceMissingValues extends EuclideanDistance
{
	@Override
	protected double difference(int index, double val1, double val2)
	{
		switch (m_Data.attribute(index).type())
		{
		case Attribute.NOMINAL:
			if (Instance.isMissingValue(val1) || Instance.isMissingValue(val2))
			{
				return 0;
			}
			else if ((int) val1 != (int) val2)
			{
				return 1;
			}
			else
			{
				return 0;
			}

		default:
			throw new NotImplementedException();
		}
	}
}
