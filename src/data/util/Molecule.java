package data.util;

import launch.Settings;

import org.apache.commons.lang.NotImplementedException;
import org.openbabel.OBAtom;
import org.openbabel.OBMol;
import org.openbabel.OBMolAtomIter;
import org.openbabel.OBSmartsPattern;
import org.openbabel.SWIGTYPE_p_std__vectorT_OpenBabel__OBAtom_p_t__iterator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;

public class Molecule
{
	OBMol obMol;
	IMolecule cdkMol;

	// OBSmartsPattern obSmartsPattern;

	public Molecule(OBMol obMol)
	{
		assert (Settings.isModeOpenBabel() && obMol != null);
		this.obMol = obMol;
	}

	public Molecule(IMolecule cdkMol)
	{
		assert (Settings.isModeCDK() && cdkMol != null);
		this.cdkMol = cdkMol;
	}

	public OBMol getOBMolecule()
	{
		assert (Settings.isModeOpenBabel());
		return obMol;
	}

	public IMolecule getCDKMolecule()
	{
		assert (Settings.isModeCDK());
		return cdkMol;
	}

	public int getAtomCount()
	{
		if (Settings.isModeOpenBabel())
			return (int) obMol.NumAtoms();
		else
			return cdkMol.getAtomCount();
	}

	public boolean hasOnlyCAtoms()
	{
		if (Settings.isModeOpenBabel())
		{
			SWIGTYPE_p_std__vectorT_OpenBabel__OBAtom_p_t__iterator atomIterator = obMol.BeginAtoms();
			OBAtom atom25 = obMol.BeginAtom(atomIterator);
			//atom25.heyHoImAnAtom();

			for (OBAtom atom : new OBMolAtomIter(obMol))
				if (!atom.IsCarbon())
					return false;
			return true;
		}
		else
		{
			for (int i = 0; i < cdkMol.getAtomCount(); i++)
			{
				String symbol = cdkMol.getAtom(i).getSymbol();
				if (!symbol.equals("C") && !symbol.equals("c"))
					return false;
			}
			return true;
		}
	}

	public boolean isOBSubgraph(String smiles)
	{
		if (Settings.isModeCDK())
			throw new NotImplementedException("please use isCDKSubgraph(MOLECULE) method");

		OBSmartsPattern obSmartsPattern = new OBSmartsPattern();
		obSmartsPattern.Init(smiles);
		return obSmartsPattern.Match(obMol);
	}

	public boolean isCDKSubgraph(Molecule mol)
	{
		if (Settings.isModeOpenBabel())
			throw new NotImplementedException("please use isOBSubgraph(STRING) method");
		try
		{
			return UniversalIsomorphismTester.isSubgraph(cdkMol, mol.getCDKMolecule());
		}
		catch (CDKException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean isIsomorph(Molecule mol)
	{
		if (Settings.isModeOpenBabel())
			throw new NotImplementedException("not yet implemented");
		else
		{
			try
			{
				return UniversalIsomorphismTester.isIsomorph(cdkMol, mol.getCDKMolecule());
			}
			catch (CDKException e)
			{
				e.printStackTrace();
				return false;
			}
		}
	}

}
