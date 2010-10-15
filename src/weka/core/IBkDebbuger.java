package weka.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;

import util.ArrayUtil;
import util.StringUtil;
import util.SwingUtil;
import weka.classifiers.rules.ZeroR;

public class IBkDebbuger
{
	public static IBkDebbuger INSTANCE;
	public static DistanceDebugger DISTANCE_INSTANCE;
	public static int K = 5;

	List<Vector<Object>> values = new ArrayList<Vector<Object>>();

	Vector<Instance> testInstances = new Vector<Instance>();
	Vector<Boolean> testClazz = new Vector<Boolean>();
	Vector<double[]> totalDistances = new Vector<double[]>();
	Vector<double[]> tanimoto = new Vector<double[]>();
	Vector<double[]> setDistance = new Vector<double[]>();
	Vector<double[]> setDistanceOrig = new Vector<double[]>();
	Vector<double[]> setDistanceWeights = new Vector<double[]>();

	int tanimotoCorrect = 0;
	int setCorrect = 0;
	int totalCorrect = 0;

	Color background[];

	private Instances train;
	// private LinearNNSearchCombineDistances combineDistanceSearch;

	public ZeroR zeroR;
	public SetDistanceFunction distanceFunction;

	public IBkDebbuger(int numTestInstances, Instances train)
	{
		this.train = train;
		background = new Color[numTestInstances];
	}

	private double calcPropCorrect(Instance testInstance, boolean clazz, double distances[])
	{
		int ordering[] = ArrayUtil.getOrdering(distances, true);
		double dist[] = null;

		if (distances[ordering[0]] == distances[ordering[distances.length - 1]])
		{
			try
			{
				dist = zeroR.distributionForInstance(testInstance);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// System.err.println(ArrayUtil.toString(distances));
			// throw new Error("nope");
		}
		else
		{
			Instances neighbours = new Instances(train, K);
			ArrayList<Double> finalDistances = new ArrayList<Double>(K);

			int i = 0;
			while (neighbours.size() < K || distances[ordering[i]] == finalDistances.get(i - 1))
			{
				finalDistances.add(distances[ordering[i]]);
				neighbours.add(train.get(ordering[i]));
				i++;
			}
			dist = makeDistribution(neighbours, ArrayUtil.toPrimitiveDoubleArray(finalDistances));
		}

		if (clazz)
			return dist[1];
		else
			return dist[0];

	}

	private double[] makeDistribution(Instances neighbours, double[] distances) // throws Exception
	{
		double total = 0, weight;
		double[] distribution = new double[2];

		for (int i = 0; i < 2; i++)
		{
			distribution[i] = 1.0 / Math.max(1, train.numInstances());
		}
		total = (double) 2 / Math.max(1, train.numInstances());

		for (int i = 0; i < neighbours.numInstances(); i++)
		{
			// Collect class counts
			Instance current = neighbours.instance(i);
			distances[i] = distances[i] * distances[i];
			distances[i] = Math.sqrt(distances[i] / (train.numAttributes() - 1));

			weight = 1.0 - distances[i];
			weight *= current.weight();

			distribution[(int) current.classValue()] += weight;
			total += weight;
		}
		// Normalise distribution
		if (total > 0)
		{
			Utils.normalize(distribution, total);
		}
		return distribution;
	}

	public void addTestInstance(Instance instance, boolean clazz, double totalDistances[], double[][] distances, double[][] distancesOrig,
			double[][] distancesWeights, int[] maxDistancePairs)
	{
		assert train.size() == totalDistances.length;
		assert 2 == distances.length;
		assert train.size() == distances[0].length;

		testInstances.add(instance);
		testClazz.add(clazz);
		this.totalDistances.add(totalDistances);
		this.tanimoto.add(distances[0]);
		this.setDistance.add(distances[1]);
		this.setDistanceOrig.add(distancesOrig[1]);
		this.setDistanceWeights.add(distancesWeights[1]);

		Vector<Object> v = new Vector<Object>();
		v.add(values.size());
		v.add(clazz ? "+" : "-");

		double tanimoto = calcPropCorrect(instance, clazz, distances[0]);
		double set = calcPropCorrect(instance, clazz, distances[1]);
		double total = calcPropCorrect(instance, clazz, totalDistances);

		if (total < tanimoto - 0.0001)
			background[values.size()] = new Color(255, 180, 180);
		if (tanimoto < total - 0.0001)
			background[values.size()] = new Color(180, 255, 180);

		if (tanimoto > 0.5)
		{
			tanimotoCorrect++;
			if (total <= 0.5)
				background[values.size()] = new Color(255, 50, 50);
		}
		if (set > 0.5)
			setCorrect++;
		if (total > 0.5)
		{
			totalCorrect++;
			if (tanimoto <= 0.5)
				background[values.size()] = new Color(50, 255, 50);
		}

		v.add(tanimoto);
		v.add(set);
		v.add(maxDistancePairs[1]);
		v.add(total);

		int correct = 1;
		if (total < tanimoto - 0.0001)
			correct = -1;

		v.add(Math.abs(total - tanimoto) * correct);

		values.add(v);
	}

	public void analyze()
	{
		String title = "test-instances";
		List<String> names = new ArrayList<String>();
		names.add("index");
		names.add("clazz");
		// names.add("num neighbours");
		names.add("tanimoto prop");
		names.add("set prop");
		names.add("max set pairs");
		names.add("total prop");
		names.add("diff prop");
		SwingUtil.showTable(title, names, values, 6, SortOrder.ASCENDING, true, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int index = (Integer) e.getSource();
				System.out.println(e.getSource());

				boolean[] trainClazzes = new boolean[train.numInstances()];
				for (int i = 0; i < trainClazzes.length; i++)
					trainClazzes[i] = train.get(i).classValue() == 1;

				TestInstanceDebugger deb = new TestInstanceDebugger(testInstances.get(index), index, testClazz.get(index), trainClazzes,
						totalDistances.get(index), tanimoto.get(index), setDistance.get(index), setDistanceOrig.get(index),
						setDistanceWeights.get(index));
				deb.analyze();
			}
		}, "<html><b>index</b> number ot the test-instance<br>" + "<b>clazz</b> actual clazz of the test-instance<br>"
				+ "<b>tanimoto prop</b> propablity of correct classification by ibk using only tanimoto distance<br>"
				+ "<b>set prop</b> propablity of correct classification by ibk using only set distance<br>"
				+ "<b>max set pairs</b> highest number of distance pair features involved in set distance calculation<br>"
				+ "<b>total prop</b> propablity of correct classification by ibk using both distances<br>"
				+ "<b>diff prop</b> difference between tanimoto and total prop<br><br>"
				+ "light green/red: total prop is better/worse than tanimoto prop, still same classification result<br>"
				+ "dark green/red: total prop changed tanimoto prop classification result for better/worse<br><br>"
				+ "correctly classified intances tanimoto/set/total: <b>" + tanimotoCorrect + "/" + setCorrect + "/" + totalCorrect
				+ "</b>" + "</html>", new DefaultTableCellRenderer()
		{
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
					int column)
			{
				if (value instanceof Double)
					value = StringUtil.formatDouble(((Double) value).doubleValue());
				Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				int index = (Integer) table.getValueAt(row, 0);
				if (!isSelected)
				{
					if (background[index] == null)
						comp.setBackground(Color.WHITE);
					else
						comp.setBackground(background[index]);
				}
				return comp;
			}
		});

	}

	public class TestInstanceDebugger
	{
		Instance testInstance;
		String title;
		List<Vector<Object>> values = new ArrayList<Vector<Object>>();
		private boolean testClazz;
		Color background[];

		private boolean[] trainClazzes;
		private double[] totalDistances;
		private double[] tanimotos;
		private double[] setDistances;
		private double[] setDistancesOrig;
		private double[] setDistancesWeights;

		public TestInstanceDebugger(Instance testInstance, int index, boolean testClazz, boolean[] trainClazzes, double[] totalDistances,
				double[] tanimotos, double[] setDistances, double[] setDistancesOrig, double[] setDistancesWeights)
		{
			this.testInstance = testInstance;
			this.testClazz = testClazz;
			title = "test-instance " + index + " " + testClazz;

			background = new Color[train.numInstances()];

			this.trainClazzes = trainClazzes;
			this.totalDistances = totalDistances;
			this.tanimotos = tanimotos;
			this.setDistances = setDistances;
			this.setDistancesOrig = setDistancesOrig;
			this.setDistancesWeights = setDistancesWeights;
		}

		public void analyze()
		{
			final int[] totalRanking = ArrayUtil.getRanking(ArrayUtil.getOrdering(totalDistances, true));
			final int[] setRanking = ArrayUtil.getRanking(ArrayUtil.getOrdering(setDistances, true));
			final int[] tanimotoRanking = ArrayUtil.getRanking(ArrayUtil.getOrdering(tanimotos, true));

			for (int i = 0; i < trainClazzes.length; i++)
			{
				Vector<Object> v = new Vector<Object>();
				v.add(i);
				v.add(trainClazzes[i] ? "+" : "-");
				v.add(tanimotos[i]);
				v.add(setDistances[i]);
				if (setDistancesWeights[i] != 0)
					v.add(StringUtil.formatDouble(setDistancesOrig[i]) + " * " + StringUtil.formatDouble(setDistancesWeights[i]));
				else
					v.add("-");
				v.add(totalDistances[i]);
				values.add(v);
			}

			List<String> names = new ArrayList<String>();
			names.add("index");
			names.add("clazz");
			names.add("tanimoto");
			names.add("setDistance");
			names.add("setD orig * weight");
			names.add("total");
			// names.add("diff prop");
			SwingUtil.showTable(title, names, values, 2, SortOrder.ASCENDING, true, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					int index = (Integer) e.getSource();
					System.out.println(e.getSource());

					DistanceDebugger deb = new DistanceDebugger(testClazz, train.get(index).classValue() == 1);
					DISTANCE_INSTANCE = deb;
					distanceFunction.distance(testInstance, train.get(index), Double.POSITIVE_INFINITY, null);
					DISTANCE_INSTANCE = null;

					deb.analyze();
				}
			}, "<html><b>index</b> number ot the training-instance<br>"
					+ "<b>clazz</b> class of the training-instance (actual test-instance class is " + testClazz + ")<br>"
					+ "<b>tanimoto</b> similarty based on tanimoto, rank in brackets<br>"
					+ "<b>setDistanec</b> similarity based on set distance, rank in brackets<br>"
					+ "<b>set D orig * weight</b> original set distance similarity * weight (- if no distance pairs<br>"
					+ "<b>total</b> similarity based on both distances rank in brackets<br><br>"
					+ "Colors indicated changes using total similarity compared to tanimoto:<br>"
					+ "green: bad neighbor sorted out of Top-K / good neighboor was added to Top-K<br>"
					+ "red: good neighbor sorted out of Top-K / bad neighboor was added to Top-K<br></html>",
					new DefaultTableCellRenderer()
					{
						public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
								int row, int column)
						{
							int index = (Integer) table.getValueAt(row, 0);

							if (value instanceof Double)
								value = StringUtil.formatDouble(((Double) value).doubleValue(), 2);
							if (table.getColumnName(column).equals("tanimoto"))
								value = value + " (" + tanimotoRanking[index] + ")";
							else if (table.getColumnName(column).equals("setDistance"))
								value = value + " (" + setRanking[index] + ")";
							else if (table.getColumnName(column).equals("total"))
								value = value + " (" + totalRanking[index] + ")";
							Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
							// int index = (Integer) table.getValueAt(row, 0);
							if (!isSelected)
							{
								if (tanimotoRanking[index] < K && totalRanking[index] >= K)
								{
									if (trainClazzes[index] == testClazz)
										comp.setBackground(new Color(255, 180, 180));
									else
										comp.setBackground(new Color(180, 255, 180));
								}
								else if (tanimotoRanking[index] >= K && totalRanking[index] < K)
								{
									if (trainClazzes[index] != testClazz)
										comp.setBackground(new Color(255, 180, 180));
									else
										comp.setBackground(new Color(180, 255, 180));
								}
								else if (row < K)
									comp.setBackground(new Color(230, 230, 230));
								else
									comp.setBackground(Color.WHITE);
							}
							return comp;
						}
					});
		}
	}

	public class DistanceDebugger
	{
		String title;
		List<Vector<Object>> values = new ArrayList<Vector<Object>>();

		public DistanceDebugger(boolean testClazz, boolean trainClazz)
		{
			title = "test clazz: " + testClazz + ", train clazz: " + trainClazz;
		}

		public void addAttribute(int index, String name, double distance, double normalizedDistance, String testSet, String trainSet)
		{
			Vector<Object> v = new Vector<Object>();
			v.add(index);
			v.add(name);
			v.add(distance);
			v.add(normalizedDistance);
			v.add(testSet);
			v.add(trainSet);
			values.add(v);
		}

		public void analyze()
		{
			List<String> names = new ArrayList<String>();
			names.add("index");
			names.add("name");
			names.add("distance");
			names.add("normalized dist.");
			names.add("test set");
			names.add("train set");
			// names.add("diff prop");
			if (values.size() == 0)
				title += " - No common distance pairs";
			SwingUtil.showTable(title, names, values, -1, null, true, null, "<html><b>index</b> number of distance pair<br>"
					+ "<b>name</b> name of distance pair<br>" + "<b>distance set</b> distance of test-set to training-set<br>"
					+ "<b>normalized dist</b> normalized distance (considering all occured distances)<br>" + "<b>test set</b><br>"
					+ "<b>training set</b></html>", null);
		}
	}

}
