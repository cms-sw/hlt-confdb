package confdb.converter.python;

import confdb.converter.ConverterEngine;
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

	public String toString( Parameter parameter, ConverterEngine converterEngine, String indent ) 
	{
		this.converterEngine = converterEngine;
		if ( !parameter.isTracked() && parameter.isDefault() )
			return "";

		return toString( parameter, indent );
	}

	protected String toString( Parameter parameter, String indent ) 
	{
		return toString( parameter, indent, "\n" );
	}
		
	private String toString( Parameter parameter, String indent, String appendix ) 
	{
		if ( AsciiParameterWriter.skip( parameter ) )
			return "";

		
		StringBuffer str = new StringBuffer( 1000 );
		if ( parameter instanceof PSetParameter )
		{
			str.append( indent + "'" + parameter.name() + "' : " 
					+ (parameter.isTracked() ? "" : "Untracked" ) + "PSet( " );
			PSetParameter pset = (PSetParameter)parameter;
			boolean newline = false;
			if ( pset.parameterCount() > 1 )
				newline = true;
			str.append( writePSetParameters( pset, indent, newline ) );
		}
		else
		{
			str.append( indent + "'" + parameter.name() + "' : " 
					+ (parameter.isTracked() ? "" : "Untracked" ) + "Parameter( " 
					+ "'" + parameter.type() + "', " );
			if ( parameter instanceof BoolParameter )
			{
				String trueFalse = parameter.valueAsString();
				if ( trueFalse.equalsIgnoreCase( "true" ) )
					str.append( "True" );
				else
					str.append( "False" );
			}
			else if ( parameter instanceof InputTagParameter )
				str.append( "'" + parameter.valueAsString() + "'" );
			else if ( parameter instanceof VInputTagParameter )
				str.append( "'(" + parameter.valueAsString() + ")'" );
			else if ( parameter instanceof ScalarParameter )
			{
				// strange things happen here: from time to time the value is empty!
				String value = parameter.valueAsString();
				if ( value.length() == 0 )
				{
					Object doubleObject = ((ScalarParameter)parameter).value();
					if ( doubleObject != null )
						value = doubleObject.toString() + " # method value() used";
					else
						value = " # Double == null !! Don't know what to do";
				}
				str.append( value );
			}
			else if ( parameter instanceof VectorParameter )
				str.append( "( " + parameter.valueAsString() + " )" ); 
			else if ( parameter instanceof VPSetParameter )
				str.append( writeVPSetParameters( (VPSetParameter)parameter, indent ) );
			else
				str.append( parameter.valueAsString() + " # unidentified parameter class " + parameter.getClass().getSimpleName() );
		}
		
		str.append( " )" + appendix );
		return str.toString();
	}

	protected String writePSetParameters( PSetParameter pset, String indent, boolean newline ) 	
	{
		StringBuffer str = new StringBuffer();
		if ( pset.parameterCount() == 0 )
			str.append( "{}" );
		else if ( newline )
		{
			str.append( "{\n" ); 
			for ( int i = 0; i < pset.parameterCount(); i++ )
				str.append( toString( (Parameter)pset.parameter(i), indent + "  ", ",\n" ) );
			str.append( indent + "}" ); 
		}
		else
		{
			Parameter first = (Parameter)pset.parameter(0);
			str.append( "{ " + toString( first, "", ",\n" ) );
			for ( int i = 1; i < pset.parameterCount(); i++ )
				str.append( toString( (Parameter)pset.parameter(i), indent + "  ", ",\n" ) );
			str = new StringBuffer( str.substring( 0, str.length() - 1 ) ); 
			str.append( " }" ); 
		}
		return str.toString();
	}


	protected String writeVPSetParameters( VPSetParameter vpset, String indent ) 	
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
	
}
