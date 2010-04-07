package io.arff;

import java.util.List;

public interface ArffWritable
{

	public List<String> getAdditionalInfo();

	// public String getRelationName();

	public int getNumAttributes();

	public String getAttributeName(int attribute);

	public String getAttributeValueSpace(int attribute);

	public int getNumInstances();

	public String getAttributeValue(int instance, int attribute);

	public boolean isSparse();

	public String getMissingValue(int attribute);

	public boolean isInstanceWithoutAttributeValues(int instance);

}
