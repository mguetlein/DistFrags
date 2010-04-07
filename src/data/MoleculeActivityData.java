package data;


public interface MoleculeActivityData extends MoleculeData
{
	public String getEndpoint();

	public int getMoleculeActivity(int index);

	public int getNumActives();

	public int getNumInactives();

}
