package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IParameterWriter;
import confdb.converter.ascii.AsciiParameterWriter;
import confdb.data.BoolParameter;
import confdb.data.InputTagParameter;
import confdb.data.ESInputTagParameter;
import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.StringParameter;
import confdb.data.VInputTagParameter;
import confdb.data.VESInputTagParameter;
import confdb.data.VPSetParameter;
import confdb.data.VStringParameter;
import confdb.data.VectorParameter;

public class PythonParameterWriter  implements IParameterWriter 
{
	protected ConverterEngine converterEngine = null;

	public String toString( Parameter parameter, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		this.converterEngine = converterEngine;
		// if ( !parameter.isTracked() && parameter.isDefault() ) return "";

		return toString( parameter, indent );
	}

	public String getPythonClass( Parameter parameter )
	{
		return "cms." + (parameter.isTracked() ? "" : "untracked." )
			+ parameter.type();
	}
	
	protected String toString( Parameter parameter, String indent ) throws ConverterException 
	{
		return toString( parameter, indent, "\n" );
	}
		
	
	
	private String toString( Parameter parameter, String indent, String appendix ) throws ConverterException 
	{
		if ( AsciiParameterWriter.skip( parameter ) )
			return "";
		
		StringBuffer str = new StringBuffer( 1000 );
		str.append( indent + parameter.name() + " = " );
		
		if ((parameter.valueAsString().length() == 0) && (parameter instanceof ScalarParameter)) { // bug 84064
                    if (parameter instanceof StringParameter) {
			str.append( getPythonClass( parameter ) );
			str.append( "( \"\" )" );
			str.append( appendix );
			return str.toString();
                    } else {
			str.append("None");
			str.append( appendix );
			System.err.println("[confdb.converter.python.PythonParameterWriter.toString] ( value.length() == 0 ) !!!  Printing -> " + str);
			return str.toString();
                    }
		}
			
		
		if ( parameter instanceof VectorParameter )
			appendVector( str, (VectorParameter)parameter, indent );
		else if ( parameter instanceof PSetParameter )
			appendPSet( str, (PSetParameter)parameter, indent );
		else
		{
			str.append( getPythonClass( parameter ) );
			str.append( "( " );
			if ( parameter instanceof InputTagParameter )
				str.append( getInputTagString( parameter.valueAsString() ) );
			else if ( parameter instanceof ESInputTagParameter )
				str.append( getESInputTagString( parameter.valueAsString() ) );
			else if ( parameter instanceof VPSetParameter )
				appendVPSetParameters( str, (VPSetParameter)parameter, indent );
			else if ( parameter instanceof ScalarParameter )
			{
				// strange things happen here: from time to time the value is empty!
				String value = parameter.valueAsString();
				if ( value.length() == 0 ) {
					//throw new ConverterException( "oops, empty scalar parameter value! Don't know what to do");
				}
					
					
				if ( parameter instanceof StringParameter && value.charAt(0) == '"' && value.indexOf( '"', 1 ) < value.length() - 1 )
					value = "'" + value + "'";

				if ( parameter instanceof BoolParameter )
				{
					if ( value.equalsIgnoreCase( "true" ) )
						value = "True";
					else
						value = "False";
				}
				str.append( value );
			}
			else
				throw new ConverterException( "oops, unidentified parameter class " + parameter.getClass().getSimpleName() );
		}

		if ( str.charAt( str.length() - 1 ) == '\n' )
			str.append( indent );
		else
			str.append( ' ' );
		str.append( ')' );
		str.append( appendix );
		
		return str.toString();
	}

	protected String writePSetParameters( PSetParameter pset, String indent, boolean newline ) throws ConverterException 	
	{
		StringBuffer str = new StringBuffer();
		if ( pset.parameterCount() == 0 )
			return str.toString();
		
		if ( newline )
		{
			str.append( "\n" ); 
			for ( int i = 0; i < pset.parameterCount() - 1; i++ )
				str.append( toString( (Parameter)pset.parameter(i), indent + "  ", ",\n" ) );
			str.append( toString( (Parameter)pset.parameter( pset.parameterCount() -1 ), indent + "  ", "\n" ) );
		}
		else
		{
			Parameter first = (Parameter)pset.parameter(0);
			if ( pset.parameterCount() > 1 )
			{
				str.append( " " + toString( first, "", ",\n" ) );
				for ( int i = 1; i < pset.parameterCount() - 1; i++ )
					str.append( toString( (Parameter)pset.parameter(i), indent + "  ", ",\n" ) );
				str.append( toString( (Parameter)pset.parameter( pset.parameterCount() - 1), indent + "  ", "\n" ) );
				//str = new StringBuffer( str.substring( 0, str.length() - 1 ) ); 
			}
			else
				str.append( " " + toString( first, "", "" ) );
		}
		return str.toString();
	}


	protected void appendVPSetParameters( StringBuffer str, VPSetParameter vpset, String indent ) throws ConverterException 	
	{
		if ( vpset.parameterSetCount() < 256 )
			str.append( '\n' );
		else
			str.append( " *(\n" );
		for ( int i = 0; i < vpset.parameterSetCount() - 1; i++ )
		{
			PSetParameter pset = vpset.parameterSet(i);
			if ( pset.name().length() != 0 )
				PythonFormatter.addComma( str, toString( pset, indent + "  " ) );
			else
				str.append( indent + "  cms.PSet( " + writePSetParameters(pset, indent + "  ", false )
					   + indent + "  )," + converterEngine.getNewline() );
		}
		if ( vpset.parameterSetCount() >  0 )
		{
			PSetParameter pset = vpset.parameterSet(vpset.parameterSetCount() - 1);
			if ( pset.name().length() != 0 )
				str.append( toString( pset, indent + "  " ) );
			else
				str.append( indent + "  cms.PSet( " + writePSetParameters(pset, indent + "  ", false ) + indent + "  )" + converterEngine.getNewline() );
		}
		if ( vpset.parameterSetCount() >= 256 )
			str.append(')' );
	}
	
	
	protected String getInputTagString( String value )
	{
		String[] values = value.split( ":" );
		if ( value.equals( "\"\"" ) )
			return value;
		if ( values.length == 1 )
			return "\"" + value + "\"";
		StringBuffer str = new StringBuffer();
		for ( int i = 0; i < values.length; i++ )
			str.append( "'" + values[i] + "'," );
		str.setLength( str.length() - 1 );
		return str.toString();
	}

	/* an ESInputTag arguments must either be
	 * 1) a single empty string
	 * 2) a string containing a : so module:data, module:, :data or :
	 * 3) two strings either empty or not  
	 * we will always return two strings as that is the edmConfigDump behaviour
	 */
	protected String getESInputTagString( String value )
	{		
		String module = new String("");
		String data = new String("");

		int sepnr = value.indexOf(":");
		if(sepnr!=-1){
			if(sepnr!=0){
				module = value.substring(0,sepnr);
			}
			if(sepnr+1!=value.length()){
				data = value.substring(sepnr+1);
			}
		//so we also disallow the default balue of "\"\"" and leave 
		//things set as empty otherwise we double up the quotes
		}else if(value!="\"\""){
			module = value;
		}
		return "\""+module+"\",\""+data+"\"";
	}

	
	protected void appendPSet( StringBuffer str, PSetParameter pset, String indent ) throws ConverterException
	{
		str.append( getPythonClass( pset ) );
		str.append( "( " );
		boolean newline = false;
		if ( pset.parameterCount() > 1 )
			newline = true;
		str.append( writePSetParameters( pset, indent, newline ) );
	}

	
	protected void appendVector( StringBuffer str, VectorParameter vector, String indent )
	{
		if ( vector.vectorSize() < 256 )
			appendSmallVector( str, vector, 0, vector.vectorSize(), indent );
		else
		{
			str.append( "( " );
			for ( int i = 0; i < vector.vectorSize(); i += 255 )
			{
				appendSmallVector( str, vector, i, Math.min( i + 255, vector.vectorSize() ), indent );
				str.append( ")+" );
			}
			if ( vector.vectorSize() > 0 )
				str.setLength( str.length() - 1 );
		}
	}

	protected void appendSmallVector( StringBuffer str, VectorParameter vector, int start, int stop, String indent )
	{
		str.append( getPythonClass( vector ) );
		str.append( "( " );
		if ( vector instanceof VInputTagParameter )
		{
			for ( int i = start; i < stop; i++ )
			{
				String inputTag = (String)vector.value(i);
				str.append( "'" + inputTag + "'," );
			}
			if ( stop > start )
				str.setLength( str.length() - 1 );
		}
		else if ( vector instanceof VESInputTagParameter )
		{
			for ( int i = start; i < stop; i++ )
			{
				String esInputTag = (String)vector.value(i);
				str.append( "'" + esInputTag + "'," );
			}
			if ( stop > start )
				str.setLength( str.length() - 1 );
		}
		else if ( vector instanceof VStringParameter )
		{
			for ( int i = start; i < stop; i++ )
			{
				str.append( '\'' );
				String value = (String)vector.value(i);
				if ( value != null )
					str.append( value );
				str.append( "',\n  " );
				str.append( indent );
			}
			if ( stop > start )
				str.setLength( str.length() - 4 - indent.length() );
		}
		else
		{
			for ( int i = start; i < stop; i++ )
			{
				Object value = vector.value(i);
				if ( value != null )
					str.append( value.toString() );
				str.append( ", " );

			}
			if ( stop > start )
				str.setLength( str.length() - 2 );
		}
	}
}
