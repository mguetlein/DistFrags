package weka.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class SetDistanceCache
{
	private static Map<String, double[][]> matrixCache = new HashMap<String, double[][]>();
	private static Map<String, List<Double>> setCache = new HashMap<String, List<Double>>();

	public static double[][] getMatrix(String stringA, String stringB)
	{
		assert (stringA.length() > 2 && stringB.length() > 2);

		String key = getDistanceCacheKey(stringA, stringB);
		double[][] m = matrixCache.get(key);
		if (m == null)
		{
			List<Double> set1 = getSet(stringA);
			List<Double> set2 = getSet(stringB);

			m = new double[set1.size()][set2.size()];
			for (int i = 0; i < m.length; i++)
				for (int j = 0; j < m[0].length; j++)
					m[i][j] = Math.abs(set1.get(i) - set2.get(j));

			matrixCache.put(key, m);
		}
		// else
		// MATRIX_CACHE_COUNT++;
		// if (MATRIX_CACHE_COUNT % 1000 == 0)
		// {
		// smartInfo.latestInfo("set cache, size " + setCache.size() + ", access " + SET_CACHE_COUNT
		// + "\nmatrix cache, size " + matrixCache.size() + ", access " + MATRIX_CACHE_COUNT);
		// }

		return m;
	}

	// static SmartIOInfo smartInfo = new SmartIOInfo(System.err);

	public static List<Double> getSet(String string)
	{
		assert (string.length() > 2);

		List<Double> set = setCache.get(string);
		if (set == null)
		{
			set = parseDistance(string);
			setCache.put(string, set);
		}
		// else
		// SET_CACHE_COUNT++;

		return set;
	}

	private static List<Double> parseDistance(String s)
	{
		assert (s.startsWith("{") && s.endsWith("}") && s.length() > 2);

		List<Double> res = new ArrayList<Double>();
		StringTokenizer tok = new StringTokenizer(s.substring(1, s.length() - 1), ";");
		while (tok.hasMoreTokens())
			res.add(Double.parseDouble(tok.nextToken()));
		return res;
	}

	// public static int TOTAL_COUNT = 0;
	// public static int CALC_COUNT = 0;
	// public static int MATRIX_CACHE_COUNT = 0;
	// public static int SET_CACHE_COUNT = 0;

	// public static void print(PrintStream out)
	// {
	// out.println("\ntotal: " + TOTAL_COUNT + "\ncached distances: " + DIST_CACHE_COUNT + "\ncached sets: "
	// + SET_CACHE_COUNT + "\ndistance-calculations: " + CALC_COUNT);
	// }

	private static String getDistanceCacheKey(String s1, String s2)
	{
		StringBuffer b = new StringBuffer(s1);
		b.append('#');
		b.append(s2);
		return b.toString();
	}
}
