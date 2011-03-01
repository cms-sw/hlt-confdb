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
	
	
	/**
	 * Returns the Operator for a given number<br><br>
	 * Used to find a Operator from a database value
	 */
	public static Operator getOperator( int dbValue ) throws EnumConstantNotPresentException
	{
		if ( dbValue == DEFAULT.ordinal() )
			return DEFAULT;
		if ( dbValue == NEGATE.ordinal() )
			return NEGATE;
		if ( dbValue == IGNORE.ordinal() )
			return IGNORE;
	    throw new EnumConstantNotPresentException(  Operator.class, "" + dbValue );
	  } 
}
