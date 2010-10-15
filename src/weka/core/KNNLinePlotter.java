package weka.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freechart.Chart;

public class KNNLinePlotter
{

	private boolean instanceClass;
	private int numValues;
	private List<String> captions;

	private List<Map<Double, Double>> linePlotData;

	// private Map<String, double[]> barPlotData;

	public KNNLinePlotter()
	{

	}

	public void init(String instance, boolean instanceClass)
	{
		this.instanceClass = instanceClass;
		numValues = -1;
		// barPlotData = new HashMap<String, double[]>();
		linePlotData = new ArrayList<Map<Double, Double>>();
		captions = new ArrayList<String>();
	}

	public void addSeries(String name, double distances[], boolean classes[])
	{
		if (numValues == -1)
			numValues = distances.length;
		if (distances.length != numValues || classes.length != numValues)
			throw new Error();

		// bar plot data

		// double d[] = new double[numValues];
		// for (int i = 0; i < d.length; i++)
		// d[i] = distances[i] * ((instanceClass == classes[i]) ? 1 : -1);

		// line plot data

		captions.add(name);
		int series = linePlotData.size();

		int sum = 0;
		int sumCorrect[] = new int[numValues];
		for (int i = 0; i < sumCorrect.length; i++)
		{
			if (instanceClass == classes[i])
				sum++;
			sumCorrect[i] = sum;
		}

		double lastDist = -1;
		int lastIndex = 1;

		for (int i = 0; i < numValues; i++)
		{
			if (lastDist == distances[i] && i < numValues - 1)
				continue;
			lastDist = distances[i];
			double correctRatio = sumCorrect[i] / (double) (i + 1);
			addPlotValue(series, lastIndex, correctRatio);
			lastIndex = i + 1;
		}
		addPlotValue(series, numValues, sumCorrect[numValues - 1] / numValues);
	}

	private void addPlotValue(int series, double x, double y)
	{
		while (series >= linePlotData.size())
			linePlotData.add(new HashMap<Double, Double>());
		linePlotData.get(series).put(x, y);
	}

	public void plot(String title, String x, String y)
	{
		Chart.plotStep(title, x, y, captions, linePlotData, true, new double[] { 1, numValues }, new double[] { 0, 1 });
	}

	public static void main(String args[])
	{
		KNNLinePlotter knn = new KNNLinePlotter();
		knn.init("demo", true);
		knn
				.addSeries("dist1", new double[] { 1, 2.9, 3, 6, 7.5, 8 }, new boolean[] { true, false, true, false, true,
						true });
		knn.plot("test title", "x-label", "y-label");

		System.out.println("done");
	}
}
