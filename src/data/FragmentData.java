package data;

import java.util.List;

public interface FragmentData
{
	public String getFragmentName();

	public int getNumFragments();

	public String getFragmentSmiles(int index);

	public List<String> getFragments();
}
