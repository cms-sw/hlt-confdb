package confdb.converter.streams;

import confdb.data.BoolParameter;
import confdb.data.ModuleInstance;
import confdb.data.Parameter;
import confdb.data.StringParameter;

public class L1Seed {
	
	public static boolean isL1Seed( ModuleInstance module )
	{
    	if ( module.template().name().equals(  "HLTLevel1GTSeed" ) )
    		return true;
    	else
    		return false;
	}

	public static boolean isL1TechnicalTriggerSeed( ModuleInstance module )
	{
		Parameter p = module.parameter( "L1TechTriggerSeeding", "bool" );
		if ( p == null || !(p instanceof BoolParameter) )
			return false;
		Object value = ((BoolParameter)p).value();
		if ( value == null || !(value instanceof Boolean) )
			return false;
		return ((Boolean)value).booleanValue();
	}

	public static String getL1Seed( ModuleInstance module )
	{
		if ( !isL1Seed(module) )
			return null;
		Parameter p = module.parameter( "L1SeedsLogicalExpression", "string" );
		if ( p == null || !(p instanceof StringParameter) )
			return null;
		Object value = ((StringParameter)p).value();
		if ( value == null || !(value instanceof String) )
			return null;
		return (String)value;
	}
	
}
