package io.arff;


public class FragmentAndDistancePairDataToArff // implements ArffWritable
{
	// public static String ARFF_NAME = "";
	//
	// MoleculeActivityData d;
	// FragmentMoleculeData f;
	// DistancePairData p;
	//
	// public FragmentAndDistancePairDataToArff(MoleculeActivityData d, FragmentMoleculeData f, DistancePairData p)
	// {
	// this.d = d;
	// this.f = f;
	// this.p = p;
	// }
	//
	// @Override
	// public List<String> getAdditionalInfo()
	// {
	// return null;
	// }
	//
	// @Override
	// public String getAttributeName(int attribute)
	// {
	// if (attribute < f.getNumFragments())
	// return f.getFragmentSmiles(attribute);
	// else if (attribute < f.getNumFragments() + p.getNumDistancePairs())
	// return p.getDistancePairName(attribute - f.getNumFragments());
	// else
	// return d.getEndpoint();
	// }
	//
	// @Override
	// public String getAttributeValueSpace(int attribute)
	// {
	// if (attribute < f.getNumFragments())
	// return "{0,1}";
	// else if (attribute < f.getNumFragments() + p.getNumDistancePairs())
	// return "numeric";
	// else
	// return "{0,1}";
	// }
	//
	// @Override
	// public String getAttributeValue(int instance, int attribute)
	// {
	// if (attribute < f.getNumFragments())
	// {
	// List<Integer> mols = f.getMoleculesForFragment(attribute);
	// if (mols == null)
	// return null;
	// else if (mols.contains(instance))
	// return "1";
	// else
	// return null;
	// }
	// else if (attribute < f.getNumFragments() + p.getNumDistancePairs())
	// {
	// Double d = p.getDistance(attribute - f.getNumFragments(), instance);
	// if (d == null)
	// return null;
	// else
	// return d.toString();
	// }
	// else
	// return d.getMoleculeActivity(instance) + "";
	// }
	//
	// @Override
	// public int getNumAttributes()
	// {
	// return f.getNumFragments() + p.getNumDistancePairs() + 1;
	// }
	//
	// @Override
	// public int getNumInstances()
	// {
	// return d.getNumMolecules();
	// }
	//
	// // @Override
	// // public String getRelationName()
	// // {
	// // return d.getDatasetName() + " " + f.getFragmentName() + "_" + p.getFragmentName();
	// // }
	//
	// @Override
	// public boolean isSparse()
	// {
	// return false;
	// }
	//
	// @Override
	// public String getMissingValue(int attribute)
	// {
	// return "?";
	// }
	//
	// @Override
	// public boolean isInstanceWithoutAttributeValues(int instance)
	// {
	// return f.getFragmentsForMolecule(instance) == null && p.getNumDistancePairsForMolecule(instance) == 0;
	// }
	//
	// // @Override
	// // public File getArffFile()
	// // {
	// // return DataFileManager.getCombinedArffFile(d.getDatasetName(), f.getFragmentName(), p.getFragmentName());
	// // }

}
