package confdb.data;

/**
 * Operator
 * @author behrens
 *
 * enum to be used for entries in paths / sequences to specify 
 * whether the outcome of the module/sequence/path should be ignored / negated ...
 *  
 */
public enum Operator {
	DEFAULT, NEGATE, IGNORE;
	
	public String getPythonHeader()
	{
		if ( this == NEGATE )
			return " + ~";
		
		if ( this == IGNORE )
			return " + cms.ignore(";
		
		return " + ";
	}
	
	public String getPythonTrailer()
	{
		if ( this == IGNORE )
			return ")";
		return "";
	}
	
	
}
