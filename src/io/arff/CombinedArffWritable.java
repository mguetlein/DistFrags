package io.arff;

import java.util.ArrayList;
import java.util.List;

public class CombinedArffWritable implements ArffWritable
{
	ArffWritable w1, w2;

	public CombinedArffWritable(ArffWritable w1, ArffWritable w2)
	{
		this.w1 = w1;
		this.w2 = w2;

		if (true == true)
			throw new IllegalStateException("this is not used, should work, but has to be tested");

		if (w1.getNumInstances() != w2.getNumInstances())
			throw new IllegalStateException("not equal num instances");
		if (!w1.getAttributeName(w1.getNumAttributes() - 1).equals(w2.getAttributeName(w2.getNumAttributes() - 1)))
			throw new IllegalStateException("class attribute-name not equal");
		if (!w1.getAttributeValueSpace(w1.getNumAttributes() - 1).equals(
				w2.getAttributeValueSpace(w2.getNumAttributes() - 1)))
			throw new IllegalStateException("class attribute-space not equal");
		if (w1.isSparse() != w2.isSparse())
			throw new IllegalStateException("sparsisity not equal");
	}

	@Override
	public List<String> getAdditionalInfo()
	{
		List<String> l1 = w1.getAdditionalInfo();
		List<String> l2 = w2.getAdditionalInfo();
		if (l1 == null && l2 == null)
			return null;

		List<String> l = new ArrayList<String>();
		l.add("1:");
		for (String string : l1)
			l.add(string);
		l.add("2:");
		for (String string : l2)
			l.add(string);
		return l;
	}

	@Override
	public String getAttributeName(int attribute)
	{
		if (attribute < (w1.getNumAttributes() - 1))
			return w1.getAttributeName(attribute);
		else
			return w2.getAttributeName(attribute - (w1.getNumAttributes() - 1));
	}

	@Override
	public String getAttributeValue(int instance, int attribute)
	{
		if (attribute < (w1.getNumAttributes() - 1))
			return w1.getAttributeValue(instance, attribute);
		else
			return w2.getAttributeValue(instance, attribute - (w1.getNumAttributes() - 1));
	}

	@Override
	public String getAttributeValueSpace(int attribute)
	{
		if (attribute < (w1.getNumAttributes() - 1))
			return w1.getAttributeValueSpace(attribute);
		else
			return w2.getAttributeValueSpace(attribute - (w1.getNumAttributes() - 1));
	}

	@Override
	public String getMissingValue(int attribute)
	{
		if (attribute < (w1.getNumAttributes() - 1))
			return w1.getMissingValue(attribute);
		else
			return w2.getMissingValue(attribute - (w1.getNumAttributes() - 1));
	}

	@Override
	public int getNumAttributes()
	{
		return w1.getNumAttributes() + w2.getNumAttributes() - 1;
	}

	@Override
	public int getNumInstances()
	{
		return w1.getNumInstances();
	}

	@Override
	public boolean isInstanceWithoutAttributeValues(int instance)
	{
		return w1.isInstanceWithoutAttributeValues(instance) && w2.isInstanceWithoutAttributeValues(instance);
	}

	@Override
	public boolean isSparse()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
