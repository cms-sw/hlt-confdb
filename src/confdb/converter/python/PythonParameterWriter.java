package confdb.converter.python;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IParameterWriter;
import confdb.converter.ascii.AsciiParameterWriter;
import confdb.data.BoolParameter;
import confdb.data.InputTagParameter;
import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.VInputTagParameter;
import confdb.data.VPSetParameter;
import confdb.data.VectorParameter;

public class PythonParameterWriter  implements IParameterWriter 
{
	private ConverterEngine converterEngine = null;

	public String toString( Parameter parameter, ConverterEngine converterEngine, String indent ) throws ConverterException 
	{
		this.converterEngine = converterEngine;
		if ( !parameter.isTracked() && parameter.isDefault() )
			return "";

		return toString( parameter, indent );
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
		str.append( indent + parameter.name() + " = cms." 
				+ (parameter.isTracked() ? "" : "untracked." ) );
		str.append( parameter.type() );
		str.append( "( " );
		if ( parameter instanceof PSetParameter )
		{
			PSetParameter pset = (PSetParameter)parameter;
			boolean newline = false;
			if ( pset.parameterCount() > 1 )
				newline = true;
			str.append( writePSetParameters( pset, indent, newline ) );
		}
		else
		{
			if ( parameter instanceof InputTagParameter )
				str.append( getInputTagString( parameter.valueAsString() ) );
			else if ( parameter instanceof VInputTagParameter )
			{
				VInputTagParameter params = (VInputTagParameter) parameter;
				for ( int i = 0; i < params.vectorSize(); i++ )
				{
					str.append( "(" + getInputTagString( (String)params.value(i) ) + ")" );
					if ( i < params.vectorSize() - 1 )
						str.append( "," );
				}
			}
			else if ( parameter instanceof ScalarParameter )
			{
				// strange things happen here: from time to time the value is empty!
				String value = parameter.valueAsString();
				if ( value.length() == 0 )
					throw new ConverterException( "oops, empty scalar parameter value! Don't know what to do");

				if ( parameter instanceof BoolParameter )
				{
					if ( value.equalsIgnoreCase( "true" ) )
						value = "True";
					else
						value = "False";
				}
				str.append( value );
			}
			else if ( parameter instanceof VectorParameter )
				str.append( parameter.valueAsString() ); 
			else if ( parameter instanceof VPSetParameter )
				str.append( writeVPSetParameters( (VPSetParameter)parameter, indent ) );
			else
				throw new ConverterException( "oops, unidentified parameter class " + parameter.getClass().getSimpleName() );
		}
		
		str.append( " )" + appendix );
		return str.toString();
	}

	protected String writePSetParameters( PSetParameter pset, String indent, boolean newline ) throws ConverterException 	
	{
		StringBuffer str = new StringBuffer();
		if ( pset.parameterCount() == 0 )
			str.append( ")" );
		else if ( newline )
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


	protected String writeVPSetParameters( VPSetParameter vpset, String indent ) throws ConverterException 	
	{
		StringBuffer str = new StringBuffer( "(" + converterEngine.getNewline() ); 
		for ( int i = 0; i < vpset.parameterSetCount() - 1; i++ )
		{
			PSetParameter pset = vpset.parameterSet(i);
			if ( pset.name().length() != 0 )
				str.append( addComma( toString( pset, indent + "  " ) ) );
			else
				str.append( indent + "  " + writePSetParameters(pset, indent + "  ", false )
					   + "," + converterEngine.getNewline() );
		}
		if ( vpset.parameterSetCount() >  0 )
		{
			PSetParameter pset = vpset.parameterSet(vpset.parameterSetCount() - 1);
			if ( pset.name().length() != 0 )
				str.append( toString( pset, indent + "  " ) );
			else
				str.append( indent + "  " + writePSetParameters(pset, indent + "  ", false ) + converterEngine.getNewline() );
		}
		str.append( indent + ")" ); 
		return str.toString();
	}
	
	
	
	protected String addComma( String text )
	{
		if ( !text.endsWith( converterEngine.getNewline() )  )
			return text + ",";
		return text.substring(0, text.length() - 1) + "," + converterEngine.getNewline(); 
	}

	protected String getInputTagString( String value )
	{
		StringBuffer str = new StringBuffer();
		String[] values = value.split( ":" );
		for ( int i = 0; i < values.length; i++ )
		{
			str.append( "\"" + values[i] + "\"" );
			if ( i < values.length - 1 )
			str.append( "," );
		}
		return str.toString();
	}
	
}
