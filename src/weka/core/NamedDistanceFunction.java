package weka.core;

public abstract class NamedDistanceFunction implements DistanceFunction
{
	String name = null;

	public String toString()
	{
		if (name == null)
		{
			if (this instanceof SetDistanceFunction)
				name = ((SetDistanceFunction) this).setDistance.getClass()
						.getSimpleName();
			else
				name = this.getClass().getSimpleName();
		}
		return name;
	}
}
