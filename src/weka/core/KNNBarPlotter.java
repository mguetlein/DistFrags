package weka.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freechart.BarPlot;

public class KNNBarPlotter
{
	String categories[];
	Map<String, List<Double>> data;

	public KNNBarPlotter()
	{

	}

	public void init(String... categories)
	{
		data = new HashMap<String, List<Double>>();
		this.categories = categories;
	}

	public void addValue(double... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			List<Double> list = data.get(categories[i]);
			if (list == null)
			{
				list = new ArrayList<Double>();
				data.put(categories[i], list);
			}
			list.add(values[i]);
		}
	}

	public void plot()
	{
		BarPlot.plot(data);
	}

	public static void main(String args[])
	{
		KNNBarPlotter plot = new KNNBarPlotter();
		plot.init("uno", "due", "tre");
		plot.addValue(1, 3, 4);
		plot.addValue(1, 2, 4);
		plot.addValue(5, 3, 4);
		plot.addValue(1, 3, 2);

		plot.plot();
	}
}
