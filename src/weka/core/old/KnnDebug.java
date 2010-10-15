package weka.core.old;

import io.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.MoleculeActivityData;
import datamining.ResultSet;
import freechart.Chart;

public class KnnDebug
{
	public static KnnDebug TestPredictions = new KnnDebug();
	public static KnnDebug SinglePrediction = new KnnDebug();
	public static KnnDebug SingleNeighbour = new KnnDebug();

	public static int SinglePredictionActualClassValue = -1;

	public static boolean PrintSinglePrediction;
	public static boolean PrintSingleNeighbour;

	private ResultSet set;

	private MoleculeActivityData train;
	private MoleculeActivityData test;

	public int currentIndex;

	private List<Map<Double, Double>> plotData;

	// double minX = Double.MAX_VALUE;
	// double minY = Double.MAX_VALUE;
	//
	// double maxX = -Double.MAX_VALUE;
	// double maxY = -Double.MAX_VALUE;

	public KnnDebug()
	{
		clear();
	}

	public void clear()
	{
		set = new ResultSet();
		set.setNumDecimalPlaces(5);
		currentIndex = -1;
		plotData = new ArrayList<Map<Double, Double>>();

		// minX = Double.MAX_VALUE;
		// minY = Double.MAX_VALUE;
		// maxX = -Double.MAX_VALUE;
		// maxY = -Double.MAX_VALUE;

	}

	public void add()
	{
		currentIndex = set.addResult();
	}

	public void set(String prop, Object val)
	{
		set.setResultValue(currentIndex, prop, val);
	}

	public void set(int i, String prop, Object val)
	{
		set.setResultValue(i, prop, val);
	}

	public void numSort(String prop)
	{
		set.sortResults(prop, true, true, 1);
	}

	public void setTrainMolecules(MoleculeActivityData d)
	{
		train = d;
	}

	public void setTestMolecules(MoleculeActivityData d)
	{
		test = d;
	}

	public void addTrainSmiles(int index)
	{
		set.setResultValue(index, "train-smiles", train.getMoleculeSmiles(index));
	}

	public void addTestSmiles(int index)
	{
		set.setResultValue(index, "test-smiles", test.getMoleculeSmiles(index));
	}

	public void print()
	{
		if (currentIndex >= 0)
			Status.WARN.println("\n" + set.toNiceString());
	}

	public void addToPlot(String clazzProp, String distProp)
	{
		numSort(distProp);

		// int numNN = 5;
		// double oldDist = ((Double) set.getResultValue(4, distProp)).doubleValue();
		//
		// for (; numNN < set.getNumResults(); numNN++)
		// {
		// double dist = ((Double) set.getResultValue(numNN, distProp)).doubleValue();
		// if (oldDist > dist)
		// throw new Error("WTF");
		// if (oldDist != dist)
		// break;
		//
		// oldDist = dist;
		// }
		// System.out.println(numNN);
		// int numResults = numNN;

		// int numResults = set.getNumResults();

		// double numSameClass;
		// if (SinglePredictionActualClassValue == 1)
		// numSameClass = train.getNumActives();
		// else
		// numSameClass = train.getNumInactives();
		// double correctRatio = numSameClass / train.getNumMolecules();

		double minDist = Double.MAX_VALUE;
		double maxDist = -Double.MAX_VALUE;

		for (int i = 0; i < set.getNumResults(); i++)
		{
			double dist = ((Double) set.getResultValue(i, distProp)).doubleValue();
			if (dist < minDist)
				minDist = dist;
			if (dist > maxDist)
				maxDist = dist;
		}
		// double scale = 1 / (maxDist - minDist);

		int series = plotData.size();

		int sum = 0;
		int sumCorrect[] = new int[set.getNumResults()];
		for (int i = 0; i < sumCorrect.length; i++)
		{
			int clazz = ((Double) set.getResultValue(i, clazzProp)).intValue();
			if (SinglePredictionActualClassValue == 0)
				sum += 1 - clazz;
			else
				sum += clazz;
			sumCorrect[i] = sum;
		}

		double lastDist = -1;
		int lastIndex = 1;

		double distances[] = new double[set.getNumResults()];

		for (int i = 0; i < set.getNumResults(); i++)
		{
			double dist = ((Double) set.getResultValue(i, distProp)).doubleValue();
			distances[i] = dist;
			// dist = (dist - minDist) * scale;

			if (lastDist == dist && i < set.getNumResults() - 1)
				continue;
			lastDist = dist;

			double correctRatio = sumCorrect[i] / (double) (i + 1);

			// if (i == 0)
			// correctRatio = lastRatio;

			addPlotValue(series, lastIndex/* dist */, correctRatio); // sum / (double) numSameClass);

			lastIndex = i + 1;
		}
		addPlotValue(series, set.getNumResults(), sumCorrect[set.getNumResults() - 1] / set.getNumResults()); // sum / (double)

		// HistogramPanel p = new HistogramPanel("series " + series, null, "", "", "", distances, 30, new double[] { 0, 0.99 });
		// JFrame f = new JFrame();
		// f.getContentPane().add(p);
		// f.pack();
		// f.setLocationRelativeTo(null);
		// f.setVisible(true);

		// (String chartTitle, List<String> subtitle, String xAxisLabel, String yAxisLabel, String caption,
		// double[] values, int bins)
		// numSameClass);

		// double lastDist = -1;
		// double lastRatio = -1;
		// for (int i = numResults - 1; i >= 0; i--)
		// {
		// double dist = ((Double) set.getResultValue(i, distProp)).doubleValue();
		// // dist = (dist - minDist) * scale;
		//
		// if (lastDist == dist && i > 0)
		// continue;
		// lastDist = dist;
		//
		// double correctRatio = sumCorrect[i] / (double) (i + 1);
		// if (i == 0)
		// correctRatio = lastRatio;
		//
		// addPlotValue(series, i + 1/* dist */, correctRatio); // sum / (double) numSameClass);
		//
		// lastRatio = correctRatio;
		// }

		// int sum = 0;
		// for (int i = 0; i < numResults; i++)
		// {
		// double dist = ((Double) set.getResultValue(i, distProp)).doubleValue();
		// dist = (dist - minDist) * scale;
		//
		// int clazz = ((Double) set.getResultValue(i, clazzProp)).intValue();
		//
		// if (SinglePredictionActualClassValue == 0)
		// sum += 1 - clazz;
		// else
		// sum += clazz;
		//
		// double correctRatio = sum / (double) (i + 1);
		//
		// addPlotValue(series, i + 1/* dist */, correctRatio); // sum / (double) numSameClass);
		// }
	}

	// public void addDiagonal()
	// {
	// int series = plotData.size();
	// int total = train.getNumActives();
	//
	// for (int i = 0; i < set.getNumResults(); i++)
	// {
	// addPlotValue(series, (double) i * (total / (double) set.getNumResults()));
	// }
	//
	// series = plotData.size();
	//
	// for (int i = 0; i < train.getNumActives(); i++)
	// {
	// addPlotValue(series, i / (double) 2);
	// }
	//
	// }

	public void plot(String title, String x, String y, List<String> captions)
	{

		// List<List<double[]>> l = new ArrayList<List<double[]>>();
		// l.add(plotData.get(0));

		// int series = plotData.size();
		// addPlotValue(series, 0, 0);
		// addPlotValue(series, 1, 1);
		// captions.add("diagonal");

		// int series = plotData.size();
		// addPlotValue(series, 1, 0.5);
		// // addPlotValue(series, 1, 0.5);
		// addPlotValue(series, set.getNumResults(), 0.5);
		// captions.add("correct nn");

		Chart
				.plotStep(title, x, y, captions, plotData, true, new double[] { 1, set.getNumResults() }, new double[] { 0,
						1 });
		// new double[] { minX, minX + (maxX - minX) * 0.1 }, new double[] {
		// 0, 0.1 });

		// List<List<double[]>> l2 = new ArrayList<List<double[]>>();
		// l2.add(plotData.get(1));
		// Chart.plot(title, x, y, captions, l2, true);

		// int zoom = 30;
		// for (List<double[]> l : plotData)
		// {
		// while (l.size() > zoom)
		// l.remove(30);
		// }
		// Chart.plot(title + " ZOOM", x, y, captions, plotData, true);

	}

	public void addPlotValue(int series, double x, double y)
	{
		// if (x < minX)
		// minX = x;
		// if (x > maxX)
		// maxX = x;
		// if (y < minY)
		// minY = y;
		// if (y > maxY)
		// maxY = y;

		while (series >= plotData.size())
			plotData.add(new HashMap<Double, Double>());
		plotData.get(series).put(x, y);
	}

}
