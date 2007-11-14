package confdb.converter.ascii;

import confdb.converter.ConverterEngine;
import confdb.converter.IParameterWriter;
import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.VPSetParameter;
import confdb.data.VectorParameter;

public class AsciiParameterWriter  implements IParameterWriter 
{
	private ConverterEngine converterEngine = null;

	public String toString( Parameter parameter, ConverterEngine converterEngine, String indent ) 
	{
		this.converterEngine = converterEngine;
		return toString( parameter, indent );
	}

	protected String toString( Parameter parameter, String indent ) 
	{
		if ( skip( parameter ) )
			return "";

		StringBuffer str = new StringBuffer( indent + (parameter.isTracked() ? "" : "untracked " )
			 + parameter.type() + " " + parameter.name() + " = " );
		
		if ( parameter instanceof ScalarParameter )
			str.append( parameter.valueAsString() );
		else if ( parameter instanceof PSetParameter )
			str.append( writePSetParameters( (PSetParameter)parameter, indent, true ) );
		else if ( parameter instanceof VectorParameter )
			str.append( "{ " + parameter.valueAsString() + " }" ); 
		else if ( parameter instanceof VPSetParameter )
			str.append( writeVPSetParameters( (VPSetParameter)parameter, indent ) );
		
		str.append( converterEngine.getNewline() );
		return str.toString();
	}

	protected String writePSetParameters( PSetParameter pset, String indent, boolean newline ) 	
	{
		StringBuffer str = new StringBuffer();
		if ( pset.parameterCount() == 0 )
			str.append( "{}" );
		else if ( newline )
		{
			str.append( "{" + converterEngine.getNewline() ); 
			for ( int i = 0; i < pset.parameterCount(); i++ )
				str.append( toString( (Parameter)pset.parameter(i), indent + "  " ) );
			str.append( indent + "}" ); 
		}
		else
		{
			Parameter first = (Parameter)pset.parameter(0);
			str.append( "{ " + toString( first, "" ) );
			for ( int i = 1; i < pset.parameterCount(); i++ )
				str.append( toString( (Parameter)pset.parameter(i), indent + "  " ) );
			str = new StringBuffer( str.substring( 0, str.length() - 1 ) ); 
			str.append( " }" ); 
		}
		return str.toString();
	}


	protected String writeVPSetParameters( VPSetParameter vpset, String indent ) 	
	{
		StringBuffer str = new StringBuffer( "{" + converterEngine.getNewline() ); 
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
		str.append( indent + "}" ); 
		return str.toString();
	}
	
	static public boolean skipPSet( PSetParameter pset )
	{
		if ( pset.isTracked() )
			return false;

		if ( pset.parameterCount() == 0 )
			return true;
		for ( int i = 0; i < pset.parameterCount(); i++ )
		{
			Parameter p = pset.parameter(i);
			if ( p.isTracked() ||  p.isValueSet() )
				return false;
		}
		return true;
	}


	static public boolean skip( Parameter parameter )
	{
		if ( parameter.isTracked() )
			return false;
		
		if ( parameter.isDefault() )
			return true;

		if (  parameter instanceof PSetParameter )
		{
			PSetParameter pset = (PSetParameter) parameter;
			return skipPSet(pset);
		}

		if (  parameter instanceof VPSetParameter )
		{
			VPSetParameter vpset = (VPSetParameter) parameter;
			if ( vpset.parameterSetCount() == 0 )
				return true;
			for ( int i = 0; i < vpset.parameterSetCount(); i++ )
			{
				PSetParameter pset = vpset.parameterSet(i);
				if ( !skipPSet(pset) )
					return false;
			}
			return true;
		}

		if ( parameter.isValueSet() )
			return false;

		return true;
	}
	
	
	
	protected String addComma( String text )
	{
		if ( !text.endsWith( converterEngine.getNewline() )  )
			return text + ",";
		return text.substring(0, text.length() - 1) + "," + converterEngine.getNewline(); 
	}
	
}
