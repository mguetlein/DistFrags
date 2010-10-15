package weka.core.old;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.Utils;

public class EuclideanDistanceMissingValues extends EuclideanDistance
{
	@Override
	protected double difference(int index, double val1, double val2)
	{
		switch (m_Data.attribute(index).type())
		{
			case Attribute.NOMINAL:
				if (Utils.isMissingValue(val1) || Utils.isMissingValue(val2))
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
