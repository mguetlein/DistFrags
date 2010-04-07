package data.util;

import io.Status;
import launch.Settings;

import org.apache.commons.lang.NotImplementedException;
import org.openbabel.OBConversion;
import org.openbabel.OBMol;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.smiles.SmilesParser;

public class MoleculeCache
{
	private SmilesContainer smiles;

	private Molecule molecules[];

	private OBConversion obConversion;

	private boolean[] invalidSmiles;

	public MoleculeCache(SmilesContainer smiles)
	{
		if (Settings.isModeExternOpenBabel())
			throw new NotImplementedException("no molecule cache necessary");

		this.smiles = smiles;
		molecules = new Molecule[smiles.getNumSmiles()];
		invalidSmiles = new boolean[smiles.getNumSmiles()];

		// for (int i = 0; i < smiles.getNumSmiles(); i++)
		// getMolecule(i);
	}

	public Molecule getMolecule(int index)
	{
		if (invalidSmiles[index])
			return null;

		if (Settings.CACHE_MOLECULES && molecules[index] != null)
			return molecules[index];
		else
		{
			Molecule molecule = null;

			if (Settings.isModeOpenBabel())
			{
				// StopWatchUtil.start("ob-build-molecule");
				OBMol mol = new OBMol();
				getOBCoversion().ReadString(mol, smiles.getSmiles(index));

				// for (int i = 1; i <= mol.NumAtoms(); i++)
				// mol.GetAtom(i).UnsetAromatic();
				// for (int i = 0; i < mol.NumBonds(); i++)
				// mol.GetBond(i).UnsetAromatic();
				// mol.SetAromaticPerceived();

				// Status.WARN.println("building molecule for smiles: " + smiles.getSmiles(index));
				// StopWatchUtil.stop("ob-build-molecule");
				molecule = new Molecule(mol);
				if (Settings.CACHE_MOLECULES)
					molecules[index] = molecule;
				if (mol.NumAtoms() == 0)
				{
					Status.WARN.println("Invalid smiles for molecule '" + smiles.getSmiles(index) + "'");
					invalidSmiles[index] = true;
					// e.printStackTrace();
					return null;
				}
			}
			else
			{
				try
				{
					molecule = new Molecule(new SmilesParser(DefaultChemObjectBuilder.getInstance()).parseSmiles(smiles
							.getSmiles(index)));
					if (Settings.CACHE_MOLECULES)
						molecules[index] = molecule;
				}
				catch (InvalidSmilesException e)
				{
					Status.WARN.println("Invalid smiles for molecule '" + smiles.getSmiles(index) + "'");
					invalidSmiles[index] = true;
					// e.printStackTrace();
					return null;
				}
			}

			// if (Settings.CACHE_MOLECULES)
			// return molecules[index];
			// else
			return molecule;
		}
	}

	private OBConversion getOBCoversion()
	{
		if (obConversion == null)
		{
			obConversion = new OBConversion();
			obConversion.SetInFormat("smiles");
		}
		return obConversion;
	}

}
