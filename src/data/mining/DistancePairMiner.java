package data.mining;

import gui.ProgressDialog;
import io.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import launch.DatasetSizeSettings;
import launch.Settings;

import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.alg.DijkstraShortestPath;
import org.openbabel.OBConversion;
import org.openbabel.OBMol;
import org.openbabel.OBSmartsPattern;
import org.openbabel.vectorInt;
import org.openbabel.vvInt;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.MoleculeGraphs;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;

import util.ListUtil;
import util.StringUtil;
import data.DistancePairData;
import data.DistancePairDataImpl;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;
import data.util.Molecule;

public class DistancePairMiner
{
	private String distancePairsName;
	private MoleculeActivityData moleculeActivityData;
	private FragmentMoleculeData fragments;

	private List<int[]> distancePairs;

	private HashMap<Integer, DistanceMap> obDistanceMap = new HashMap<Integer, DistanceMap>();

	private boolean fragmentInvalid[];

	// private HashMap<Integer, List<Integer>> distancePairToMolecules;
	// private HashMap<Integer, List<Double>> distancePairToInMoleculeDistance;
	private HashMap<Integer, HashMap<Integer, List<Double>>> distancePairToMoleculeDistances;

	private int minFrequency = Integer.MAX_VALUE;
	private int minFrequencyPerClass = Integer.MAX_VALUE;

	static class Info
	{
		public Info()
		{
			startime = System.currentTimeMillis();
		}

		long numPairsTotal = 0;
		int numBeyondMinMoleculeCheck = 0;
		int numBeyondOnlyCCheck = 0;
		int numBeyondOverlapCheck = 0;
		int numBeyondSecondMinMoleculeCheck = 0;

		int numFailedDistanceCalculations = 0;

		int numDistanceCalculations = 0;
		int numOccurencePairs = 0;
		int numOverlaps = 0;
		int numNoPath = 0;

		long startime;

		public String toString()
		{
			return Status.TAB
					+ "#pairs-total: "
					+ numPairsTotal
					+ "\n"
					+ Status.TAB
					+ "#pairs-with-min-molecules: "
					+ numBeyondMinMoleculeCheck
					+ "\n"
					+ Status.TAB
					+ "#pairs-not-only-c: "
					+ numBeyondOnlyCCheck
					+ "\n"
					+ Status.TAB
					+ "#pairs-without-too-much-overlap: "
					+ numBeyondOverlapCheck
					+ "\n"
					+ Status.TAB
					+ "#num-failed-distance-calculations: "
					+ numFailedDistanceCalculations
					+ "\n"
					+ Status.TAB
					+ "#num-successfull-distance-calculations: "
					+ numDistanceCalculations
					+ "\n"
					+ Status.TAB
					+ "  occurence-pairs-per-calculation: "
					+ (numDistanceCalculations > 0 ? StringUtil.formatDouble(numOccurencePairs
							/ (double) numDistanceCalculations) : "-") + "\n" + Status.TAB
					+ "    overlaps-per-occurence-pair: "
					+ (numOccurencePairs > 0 ? StringUtil.formatDouble(numOverlaps / (double) numOccurencePairs) : "-")
					+ "\n" + Status.TAB + "    no-path-per-occurence-pair: "
					+ (numOccurencePairs > 0 ? StringUtil.formatDouble(numNoPath / (double) numOccurencePairs) : "-") + "\n"
					+ Status.TAB + "#pairs-with-min-molecules-2: " + numBeyondSecondMinMoleculeCheck + "\n" + Status.TAB
					+ "mining time: " + StringUtil.formatTime(System.currentTimeMillis() - startime);
		}
	}

	private Info info;

	private String DEBUG_mol;
	private String DEBUG_feat1;
	private String DEBUG_feat2;

	public static DistancePairData mineDistancePairs(String distancePairsName, MoleculeActivityData data,
			FragmentMoleculeData fragments)
	{
		if (!Settings.isModeOpenBabel() && !Settings.isModeCDK())
			throw new IllegalStateException();

		DistancePairMiner d = new DistancePairMiner(distancePairsName, data, fragments);
		DistancePairData res = d.mineDistancePairs();
		Status.INFO.println(d);
		return res;
	}

	public static DistancePairData checkDistancePairs(String distancePairsName, MoleculeActivityData testData,
			FragmentMoleculeData testFragments, DistancePairData trainingDistancePairs)
	{
		if (!Settings.isModeOpenBabel() && !Settings.isModeCDK())
			throw new IllegalStateException();

		DistancePairMiner d = new DistancePairMiner(distancePairsName, testData, testFragments);
		DistancePairData res = d.checkDistancePairs(trainingDistancePairs);
		Status.INFO.println(d);
		return res;
	}

	private DistancePairMiner(String distancePairsName, MoleculeActivityData moleculeData, FragmentMoleculeData fragments)
	{
		this.distancePairsName = distancePairsName;
		this.moleculeActivityData = moleculeData;
		this.fragments = fragments;

		DatasetSizeSettings.setCurrentDatasetSize(moleculeData != null ? moleculeData.getDatasetBaseName() : null);
		minFrequency = DatasetSizeSettings.MIN_FREQUENCY;
		minFrequencyPerClass = DatasetSizeSettings.MIN_FREQUENCY_PER_CLASS;
	}

	public String toString()
	{
		return "Distance Miner Info" + "\n" + Status.TAB + "dataset: " + moleculeActivityData.getDatasetName() + "\n"
				+ Status.TAB + "#fragments: " + fragments.getNumFragments() + "\n" + info.toString();
	}

	@SuppressWarnings("unchecked")
	private DistancePairData checkDistancePairs(DistancePairData trainingDistancePairs)
	{
		distancePairs = new ArrayList<int[]>();
		distancePairToMoleculeDistances = new HashMap<Integer, HashMap<Integer, List<Double>>>();

		info = new Info();

		info.numPairsTotal = trainingDistancePairs.getNumDistancePairs();
		int numPairs = 0;
		// ProgressDialog progress = ProgressDialog.printProgress(Status.INFO, moleculeData.getDatasetName()
		// + " distance fragment mining", info.numPairsTotal, numPairs + " / " + info.numPairsTotal);
		ProgressDialog progress = ProgressDialog.showProgress(Settings.SHOW_PROGRESS_DIALOGS ? (JFrame) null : Status.INFO,
				"Checking distance pairs: " + moleculeActivityData.getDatasetName(), Status.INDENT + "> ",
				info.numPairsTotal, numPairs + " / " + info.numPairsTotal);

		for (int k = 0; k < trainingDistancePairs.getNumDistancePairs(); k++)
		{
			numPairs++;
			if (numPairs % 100 == 0)
				progress.update(numPairs, numPairs + " / " + info.numPairsTotal);

			int[] pair = trainingDistancePairs.getDistancePair(k);

			List<Integer> molecules1 = fragments.getMoleculesForFragment(pair[0]);
			List<Integer> molecules2 = fragments.getMoleculesForFragment(pair[1]);
			if (molecules1 == null || molecules2 == null)
				continue;
			List<Integer> molecules = (List<Integer>) ListUtil.cut(molecules1, molecules2);

			HashMap<Integer, List<Double>> distances = calculateFragmentDistance(trainingDistancePairs
					.getFragmentMolecule(pair[0]), trainingDistancePairs.getFragmentSmiles(pair[0]), trainingDistancePairs
					.getFragmentMolecule(pair[1]), trainingDistancePairs.getFragmentSmiles(pair[1]), molecules);

			distancePairs.add(new int[] { pair[0], pair[1] });
			if (distances == null)
				distances = new HashMap<Integer, List<Double>>();
			distancePairToMoleculeDistances.put(distancePairs.size() - 1, distances);
		}

		progress.close(numPairs, numPairs + " / " + info.numPairsTotal);

		// return new DistancePairDataImpl(fragments.getFragmentInfo() + "_distance-pairs", fragments.getFragments(),
		// distancePairs, distancePairToMoleculeDistances);// distancePairToMolecules, distancePairToInMoleculeDistance);

		return new DistancePairDataImpl(distancePairsName, trainingDistancePairs.getFragments(), trainingDistancePairs
				.getDistancePairs(), distancePairToMoleculeDistances);
	}

	@SuppressWarnings("unchecked")
	private DistancePairData mineDistancePairs()
	{
		distancePairs = new ArrayList<int[]>();
		// distancePairToMolecules = new HashMap<Integer, List<Integer>>();
		// distancePairToInMoleculeDistance = new HashMap<Integer, List<Double>>();
		distancePairToMoleculeDistances = new HashMap<Integer, HashMap<Integer, List<Double>>>();

		info = new Info();

		info.numPairsTotal = ((long) fragments.getNumFragments() * (long) (fragments.getNumFragments() - 1)) / (long) 2;
		int numPairs = 0;
		ProgressDialog progress = ProgressDialog.showProgress(Settings.SHOW_PROGRESS_DIALOGS ? (JFrame) null : Status.INFO,
				"Mine distance pairs: " + moleculeActivityData.getDatasetName(), Status.INDENT + "> ", info.numPairsTotal,
				numPairs + " / " + info.numPairsTotal);
		// ProgressDialog progress = ProgressDialog.printProgress(Status.INFO, moleculeData.getDatasetName()
		// + " distance fragment generation", info.numPairsTotal, numPairs + " / " + info.numPairsTotal);

		fragmentInvalid = new boolean[fragments.getNumFragments()];

		for (int i = 0; i < fragments.getNumFragments() - 1; i++)
		{
			if (fragmentInvalid[i])
			{
				numPairs += fragments.getNumFragments() - (i + 1);
				continue;
			}

			List<Integer> molecules1 = fragments.getMoleculesForFragment(i);
			if (!checkNumMolecules(molecules1))
			{
				fragmentInvalid[i] = true;
				numPairs += fragments.getNumFragments() - (i + 1);
				continue;
			}

			for (int j = i + 1; j < fragments.getNumFragments(); j++)
			{
				if (fragmentInvalid[j])
				{
					numPairs++;
					continue;
				}
				if (i == j)
					continue;

				// StopWatchUtil.stop("select-pairs", false);

				numPairs++;
				if (numPairs % 100 == 0)
					progress.update(numPairs, numPairs + " / " + info.numPairsTotal);

				// StopWatchUtil.start("select-pairs");

				List<Integer> molecules2 = fragments.getMoleculesForFragment(j);
				if (!checkNumMolecules(molecules2))
				{
					fragmentInvalid[j] = true;
					continue;
				}

				List<Integer> molecules = (List<Integer>) ListUtil.cut(molecules1, molecules2);
				if (!checkNumMolecules(molecules))
					continue;

				// StopWatchUtil.stop("select-pairs");

				HashMap<Integer, List<Double>> distances = calculateFragmentDistance(fragments.getFragmentMolecule(i),
						fragments.getFragmentSmiles(i), fragments.getFragmentMolecule(j), fragments.getFragmentSmiles(j),
						molecules, true, i, j);
				if (distances != null)
				{
					distancePairs.add(new int[] { i, j });
					distancePairToMoleculeDistances.put(distancePairs.size() - 1, distances);
				}
			}
		}

		progress.close(numPairs, numPairs + " / " + info.numPairsTotal);

		return new DistancePairDataImpl(distancePairsName, fragments.getFragments(), distancePairs,
				distancePairToMoleculeDistances);// distancePairToMolecules, distancePairToInMoleculeDistance);
	}

	private boolean checkNumMolecules(List<Integer> molecules)
	{
		if (molecules.size() < minFrequency)
			return false;
		if (moleculeActivityData != null)
		{
			int numActive = 0;
			for (Integer index : molecules)
				if (moleculeActivityData.getMoleculeActivity(index) == 1)
					numActive++;
			int numInactive = molecules.size() - numActive;

			if (numActive < minFrequencyPerClass || numInactive < minFrequencyPerClass)
				return false;
		}
		return true;
	}

	private HashMap<Integer, List<Double>> calculateFragmentDistance(Molecule feature1, String smiles1, Molecule feature2,
			String smiles2, Collection<Integer> molecules)
	{
		return calculateFragmentDistance(feature1, smiles1, feature2, smiles2, molecules, false, -1, -1);
	}

	private HashMap<Integer, List<Double>> calculateFragmentDistance(Molecule feature1, String smiles1, Molecule feature2,
			String smiles2, Collection<Integer> molecules, boolean pairSelectionCheck, int index1, int index2)
	{
		info.numBeyondMinMoleculeCheck++;

		// Status.WARN.println(DEBUG_feat1 + " <-> " + DEBUG_feat2);

		assert (feature1 != null && feature2 != null);

		if (pairSelectionCheck)
		{
			// StopWatchUtil.start("ob-only-c");
			// try
			// {
			if (feature1.hasOnlyCAtoms())
			{
				fragmentInvalid[index1] = true;
				return null;
			}
			if (feature2.hasOnlyCAtoms())
			{
				fragmentInvalid[index2] = true;
				return null;
			}
			// }
			// finally
			// {
			// StopWatchUtil.stop("ob-only-c");
			// }
		}

		info.numBeyondOnlyCCheck++;

		if (pairSelectionCheck)
		{
			int mcsSize = getMCSSize(feature1, feature2);
			if (mcsSize > 0)
			{
				int maxOverlapSize = Math.max(0, Math.min(feature1.getAtomCount(), feature2.getAtomCount()) - 3);
				/*
				 * maxOverlapSize = Math.max(0, Math.min(feat1.getAtomCount(), feat2.getAtomCount()) - 3); means:
				 * 
				 * smaller feature <= 3 atoms -> no overlap allowed
				 * 
				 * smaller feature <= 4 atoms -> max overlap = 1
				 * 
				 * smaller feature <= 5 atoms -> max overlap = 2
				 * 
				 * ...
				 */
				if (mcsSize > maxOverlapSize)
					return null;
			}
		}

		info.numBeyondOverlapCheck++;

		// List<Integer> moleculesWithDistanceValues = new ArrayList<Integer>();
		// List<Double> inMoleculeDistance = new ArrayList<Double>();

		HashMap<Integer, List<Double>> distances = new HashMap<Integer, List<Double>>();
		boolean onlyZeroDistances = true;

		for (Integer moleculeIndex : molecules)
		{

			Molecule molecule = moleculeActivityData.getMolecule(moleculeIndex);
			if (molecule == null)
				continue;
			DEBUG_mol = moleculeActivityData.getMoleculeSmiles(moleculeIndex);

			List<Double> d;
			if (Settings.isModeOpenBabel())
				d = getOBDistanceInMolecule(molecule.getOBMolecule(), getDistanceMap(moleculeIndex), smiles1, smiles2);
			else
			{
				DEBUG_feat1 = smiles1;
				DEBUG_feat2 = smiles2;
				d = getCDKDistanceInMolecule(molecule.getCDKMolecule(), feature1.getCDKMolecule(), feature2.getCDKMolecule());
			}

			// Status.WARN.println("molecule: " + moleculeData.getMoleculeSmiles(moleculeIndex));

			if (d == null)
			{
				info.numFailedDistanceCalculations++;
				continue;
			}
			else
				info.numDistanceCalculations++;

			for (Double distance : d)
			{
				if (onlyZeroDistances && distance != 0)
				{
					onlyZeroDistances = false;
					break;
				}
			}
			distances.put(moleculeIndex, d);

			// moleculesWithDistanceValues.add(moleculeIndex);
			// inMoleculeDistance.add(d);
		}

		if (pairSelectionCheck)
			if (onlyZeroDistances)
				return null;

		// StopWatchUtil.start("post-select-pairs");
		// try
		// {
		if (pairSelectionCheck)
		{
			if (distances.size() < minFrequency)
				return null;

			if (moleculeActivityData != null)
			{
				int numActive = 0;
				for (Integer index : distances.keySet())
					if (moleculeActivityData.getMoleculeActivity(index) == 1)
						numActive++;
				int numInactive = molecules.size() - numActive;

				if (numActive < minFrequencyPerClass || numInactive < minFrequencyPerClass)
					return null;
			}
		}
		// }
		// finally
		// {
		// StopWatchUtil.stop("post-select-pairs");
		// }

		info.numBeyondSecondMinMoleculeCheck++;

		return distances;

		// distancePairToMolecules.put(distancePairs.size() - 1, moleculesWithDistanceValues);
		// distancePairToInMoleculeDistance.put(distancePairs.size() - 1, inMoleculeDistance);
	}

	private static int getMCSSize(Molecule f1, Molecule f2)
	{
		if (Settings.isModeOpenBabel())
			return getOBMCSSize(f1.getOBMolecule(), f2.getOBMolecule());
		else
			return getCDKMCSSize(f1.getCDKMolecule(), f2.getCDKMolecule());
	}

	private static int getOBMCSSize(OBMol f1, OBMol f2)
	{
		return 0;
		// throw new NotImplementedException("not yet implemented");
	}

	private static int getCDKMCSSize(IMolecule f1, IMolecule f2)
	{
		if (true == true)
			return 0;

		if (f1.getAtomCount() == 1 || f2.getAtomCount() == 1)
		{
			for (int i = 0; i < f1.getAtomCount(); i++)
			{
				IAtom atom1 = f1.getAtom(i);
				for (int j = 0; j < f2.getAtomCount(); j++)
				{
					IAtom atom2 = f2.getAtom(j);

					if (atom1.getSymbol().equals(atom2.getSymbol()))
						return 1;
				}
			}
			return 0;
		}
		else
		{
			try
			{
				int overlap = 0;
				List<IAtomContainer> m = UniversalIsomorphismTester.getOverlaps(f1, f2);
				for (IAtomContainer a : m)
				{
					if (a.getAtomCount() > overlap)
						overlap = a.getAtomCount();
				}
				return overlap;
			}
			catch (CDKException e)
			{
				e.printStackTrace();
				System.exit(1);
				return -1;
			}
		}
	}

	private static void removeOverlappingOccurences(List<List<RMap>> l)
	{
		List<Integer> atoms = new ArrayList<Integer>();
		boolean disjunct[] = new boolean[l.size()];

		int count = 0;
		for (List<RMap> occurence : l)
		{
			boolean match = false;
			for (int i = 0; i < occurence.size(); i++)
			{
				int a = occurence.get(i).getId1();
				for (int atom : atoms)
				{
					if (a == atom)
					{
						match = true;
						break;
					}
				}
				if (match)
					break;
			}
			if (match)
				disjunct[count] = false;
			else
			{
				disjunct[count] = true;
				for (int i = 0; i < occurence.size(); i++)
					atoms.add(occurence.get(i).getId1());
			}
			count++;
		}
		for (int i = disjunct.length - 1; i >= 0; i--)
			if (!disjunct[i])
				l.remove(i);

	}

	public static void main(String args[])
	{
		System.loadLibrary("openbabel");
		DistancePairMiner mine = new DistancePairMiner(null, null, null);

		OBConversion conv = new OBConversion();
		conv.SetInFormat("smiles");

		// String m = "C2C(CCCC2Cl)F";
		// String m = "N-C-C-C-C-O";
		String m = "C=C(Cl)C=C";
		// String m = "C1CCCCC1";
		mine.DEBUG_mol = m;

		OBMol mol = new OBMol();
		// conv.ReadString(mol, "CCOC(=O)N(C)N=O");
		// conv.ReadString(mol, "ClC54C(=O)C1(Cl)C2(Cl)C5(Cl)C3(Cl)C4(Cl)C1(Cl)C2(Cl)C3(Cl)Cl");
		conv.ReadString(mol, m);

		for (int i = 1; i <= mol.NumAtoms(); i++)
			mol.GetAtom(i).UnsetAromatic();
		for (int i = 0; i < mol.NumBonds(); i++)
			mol.GetBond(i).UnsetAromatic();
		mol.SetAromaticPerceived();

		// OBAromaticTyper typ = new OBAromaticTyper();
		// typ.AssignAromaticFlags(mol);
		// conv
		// .ReadString(
		// mol,
		// "C(/C1=C(C=C(C=C1)O)S(=O)(=O)[O-])(C2=CC=C(C=C2)N(CC3=CC(=CC=C3)S(=O)(=O)[O-])CC)=C4/C=C/C(C=C4)=[N+](\\CC5=CC(=CC=C5)S(=O)(=O)[O-])CC.[Na+].[Na+]");

		// String smiles1 = "N";
		String smiles1 = "C-C";
		// String smiles1 = "O";
		// OBMol feat1 = new OBMol();
		// conv.ReadString(feat1, smiles1);

		// String smiles2 = "O";
		String smiles2 = "C";
		// String smiles2 = "C-c:c:c:c-N-C";
		// OBMol feat2 = new OBMol();
		// conv.ReadString(feat2, smiles2);

		mine.info = new Info();

		DistanceArrayMap distanceMap = new DistanceArrayMap(mol);
		Status.INFO.println(distanceMap.toString(mol));

		Status.INFO.println(mine.getOBDistanceInMolecule(mol, distanceMap, smiles1, smiles2));

		Status.INFO.println(mine.info);
	}

	private List<Double> getOBDistanceInMolecule(OBMol molecule, DistanceMap distanceMap, String smiles1, String smiles2)
	{
		// StopWatchUtil.start("ob-get-map");
		OBSmartsPattern smarts = new OBSmartsPattern();
		smarts.Init(smiles1);
		smarts.Match(molecule);
		vvInt map1 = smarts.GetUMapList();
		if (map1 == null || map1.size() == 0)
		{
			Status.WARN.println(smiles1 + " no substructure of molecule " + DEBUG_mol + " (" + molecule.GetFormula() + ")");
			if (true == true)
				System.exit(1);
			return null;

		}
		List<List<Integer>> occurences1 = new ArrayList<List<Integer>>();
		for (int i = 0; i < map1.size(); i++)
		{
			vectorInt atomList1 = map1.get(i);
			if (atomList1.size() == 0)
			{
				System.err.println("empy occurence map");
				System.exit(1);
			}
			List<Integer> occ = new ArrayList<Integer>();
			for (int k = 0; k < atomList1.size(); k++)
			{
				try
				{
					if (atomList1.get(k) > molecule.NumAtoms())
						throw new IndexOutOfBoundsException("no atom index");
					occ.add(atomList1.get(k));
				}
				catch (IndexOutOfBoundsException e)
				{
					Status.WARN.println(e.getMessage() + " -> retry");
					return getOBDistanceInMolecule(molecule, distanceMap, smiles1, smiles2);
				}
			}
			occurences1.add(occ);
		}

		smarts.Init(smiles2);
		smarts.Match(molecule);
		vvInt map2 = smarts.GetUMapList();
		// StopWatchUtil.stop("ob-get-map");
		if (map2 == null || map2.size() == 0)
		{
			Status.WARN.println(smiles2 + " no substructure of molecule " + DEBUG_mol);
			return null;
		}

		// Status.INFO.println(smiles1 + " num matches: " + map1.size());
		// Status.INFO.println(smiles2 + " num matches: " + map2.size());

		// removeOverlappingOccurences(map1);
		// removeOverlappingOccurences(map2);

		// StopWatchUtil.start("ob-calculate-distance");

		// double molDistance = 0.0;
		// int occCount = 0;

		List<List<Integer>> occurences2 = new ArrayList<List<Integer>>();
		for (int i = 0; i < map2.size(); i++)
		{
			vectorInt atomList2 = map2.get(i);
			if (atomList2.size() == 0)
			{
				System.err.println("empy occurence map");
				System.exit(1);
			}
			List<Integer> occ = new ArrayList<Integer>();
			for (int k = 0; k < atomList2.size(); k++)
			{
				try
				{
					if (atomList2.get(k) > molecule.NumAtoms())
						throw new IndexOutOfBoundsException("no atom index");
					occ.add(atomList2.get(k));
				}
				catch (IndexOutOfBoundsException e)
				{
					Status.WARN.println(e.getMessage() + " -> retry");
					return getOBDistanceInMolecule(molecule, distanceMap, smiles1, smiles2);
				}
			}
			occurences2.add(occ);
		}

		List<Double> moleculeDistances = new ArrayList<Double>();

		for (List<Integer> atomList1 : occurences1)
		// for (int i = 0; i < map1.size(); i++)
		{
			// // Status.INFO.println("feat1 match: " + i);
			// vectorInt atomList1 = map1.get(i);

			// for (int j = 0; j < map2.size(); j++)
			for (List<Integer> atomList2 : occurences2)
			{
				// // Status.INFO.println(" feat2 match: " + i);
				// vectorInt atomList2 = map2.get(j);

				if (atomList1.size() == 0 || atomList2.size() == 0)
				{
					System.err.println("empy occurence map");
					System.exit(1);
				}

				double occDist = 0;
				double atompairCount = 0;
				boolean overlap = false;
				boolean noPath = false;

				for (int k = 0; k < atomList1.size(); k++)
				{
					// OBAtom atom1 = molecule.GetAtom(atomList1.get(k));
					int index1 = atomList1.get(k);

					for (int l = 0; l < atomList2.size(); l++)
					{
						// OBAtom atom2 = molecule.GetAtom(atomList2.get(l));
						int index2 = atomList2.get(l);

						// if (atom1 == null || atom2 == null)
						// {
						// System.err.println("atom1 " + atom1 + " idx:" + atomList1.get(k));
						// System.err.println("atom2 " + atom2 + " idx:" + atomList2.get(l));
						// System.err.println(DEBUG_mol + " " + molecule.GetFormula());
						// System.err.println(smiles1 + " " + feat1.GetFormula());
						// System.err.println(smiles2 + " " + feat2.GetFormula());
						// System.err.println("try again!");
						// System.err.flush();
						// return getOBDistanceInMolecule(molecule, distanceMap, feat1, smiles1, feat2, smiles2);
						// }

						double atompairDist = 0;
						if (index1 == index2)// (atom1.GetIdx() == atom2.GetCIdx())
						{
							overlap = true;
							break;
						}
						else
						{
							int distance = distanceMap.getDistance(index1, index2);// atom1.GetIdx(), atom2.GetIdx());
							if (distance == -1)
							{
								noPath = true;
								break;
							}
							atompairDist = distance;
						}
						occDist = (occDist * atompairCount + atompairDist) / (double) (atompairCount + 1);
						atompairCount++;
					}

					if (overlap || noPath)
						break;
				}

				if (!noPath)
				{
					if (!overlap)
						moleculeDistances.add(occDist);// occDist = 0;

					// molDistance = (molDistance * occCount + occDist) / (double) (occCount + 1);
					// occCount++;
					// moleculeDistances.add(occDist);
				}

				if (noPath)
					info.numNoPath++;
				if (overlap)
					info.numOverlaps++;
				info.numOccurencePairs++;
			}
		}

		// StopWatchUtil.stop("ob-calculate-distance");

		if (moleculeDistances.size() > 0)
			return moleculeDistances;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	private List<Double> getCDKDistanceInMolecule(IMolecule molecule, IMolecule feat1, IMolecule feat2)
	{
		try
		{
			List<Double> moleculeDistances = new ArrayList<Double>();

			List<List<RMap>> map1 = (List<List<RMap>>) UniversalIsomorphismTester.getSubgraphAtomsMaps(molecule, feat1);
			if (map1 == null || map1.size() == 0)
			{
				Status.WARN.println(DEBUG_feat1 + " no substructure of molecule " + DEBUG_mol);
				System.exit(1);
				return null;
			}

			List<List<RMap>> map2 = (List<List<RMap>>) UniversalIsomorphismTester.getSubgraphAtomsMaps(molecule, feat2);
			if (map2 == null || map2.size() == 0)
			{
				Status.WARN.println(DEBUG_feat2 + " no substructure of molecule " + DEBUG_mol);
				System.exit(1);
				return null;
			}

			removeOverlappingOccurences(map1);
			removeOverlappingOccurences(map2);

			// double molDistance = 0.0;
			// int occCount = 0;
			UndirectedGraph molGraph = MoleculeGraphs.getMoleculeGraph(molecule);

			for (List<RMap> occurence1 : map1)
			{
				for (List<RMap> occurence2 : map2)
				{
					if (occurence1.size() == 0 || occurence2.size() == 0)
					{
						System.err.println("empy occurence map");
						System.exit(1);
					}

					double occDist = 0;
					double atompairCount = 0;
					boolean overlap = false;
					boolean noPath = false;

					for (int i = 0; i < occurence1.size(); i++)
					{
						int id1 = occurence1.get(i).getId1();
						IAtom atom1 = molecule.getAtom(id1);

						for (int j = 0; j < occurence2.size(); j++)
						{
							int id2 = occurence2.get(j).getId1();

							double atompairDist = 0;
							if (id1 == id2)
							{
								overlap = true;
								break;
							}
							else
							{
								// calculate distance form atom1 && atom2
								IAtom atom2 = molecule.getAtom(id2);
								List<Object> l = DijkstraShortestPath.findPathBetween(molGraph, atom1, atom2);
								if (l == null)
								{
									// WARNING.println("no path found!! " + data.getFeature(feature1) + " "
									// + data.getFeature(feature2));
									noPath = true;
									break;
								}
								atompairDist = l.size();
							}
							occDist = (occDist * atompairCount + atompairDist) / (double) (atompairCount + 1);
							atompairCount++;
						}

						if (overlap || noPath)
							break;
					}

					if (!noPath)
					{
						if (overlap)
							occDist = 0;

						// molDistance = (molDistance * occCount + occDist) / (double) (occCount + 1);
						// occCount++;
						moleculeDistances.add(occDist);
					}

					if (noPath)
						info.numNoPath++;
					if (overlap)
						info.numOverlaps++;
					info.numOccurencePairs++;
				}
			}

			if (moleculeDistances.size() > 0)
				return moleculeDistances;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private DistanceMap getDistanceMap(int index)
	{
		assert (Settings.isModeOpenBabel());

		if (Settings.CACHE_DISTANCE_MAPS && obDistanceMap.containsKey(index))
			return obDistanceMap.get(index);
		else
		{
			DistanceMap map = new DistanceArrayMap(moleculeActivityData.getMolecule(index).getOBMolecule());
			if (Settings.CACHE_DISTANCE_MAPS)
				obDistanceMap.put(index, map);
			return map;
		}
	}

	// private static HashMap<Long, HashMap<Long, Integer>> getDistanceMap(OBMol mol)
	// {
	// HashMap<Long, HashMap<Long, Integer>> map = new HashMap<Long, HashMap<Long, Integer>>();
	//
	// SWIGTYPE_p_std__vectorT_OpenBabel__OBAtom_p_t__iterator atomIterator = mol.BeginAtoms();
	// OBAtom atom = mol.BeginAtom(atomIterator);
	// do
	// {
	// OBMolAtomBFSIter bfsIterator = new OBMolAtomBFSIter(mol, (int) atom.GetIdx());
	// for (OBAtom atom2 : bfsIterator)
	// {
	// HashMap<Long, Integer> map2 = map.get(atom.GetIdx());
	// if (map2 == null)
	// {
	// map2 = new HashMap<Long, Integer>();
	// map.put(atom.GetIdx(), map2);
	// }
	// map2.put(atom2.GetIdx(), atom2.GetCurrentDepth());
	// }
	// }
	// while ((atom = mol.NextAtom(atomIterator)) != null);
	//
	// return map;
	// }
}
