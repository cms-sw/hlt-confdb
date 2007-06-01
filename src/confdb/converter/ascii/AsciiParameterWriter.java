package confdb.converter.ascii;

import confdb.converter.Converter;
import confdb.converter.IParameterWriter;
import confdb.data.PSetParameter;
import confdb.data.Parameter;
import confdb.data.ScalarParameter;
import confdb.data.VPSetParameter;
import confdb.data.VectorParameter;

public class AsciiParameterWriter  implements IParameterWriter 
{
	private Converter converter = null;

	public String toString( Parameter parameter, Converter converter, String indent ) 
	{
		this.converter = converter;
		return toString( parameter, indent );
	}

	protected String toString( Parameter parameter, String indent ) 
	{
		if ( skip( parameter ) )
			return "";

		String str = indent + (parameter.isTracked() ? "" : "untracked " )
			 + parameter.type() + " " + parameter.name() + " = ";
		
		if ( parameter instanceof ScalarParameter )
			str += parameter.valueAsString();
		else if ( parameter instanceof PSetParameter )
			str += writePSetParameters( (PSetParameter)parameter, indent, true );
		else if ( parameter instanceof VectorParameter )
			str += "{ " + parameter.valueAsString() + " }"; 
		else if ( parameter instanceof VPSetParameter )
			str += writeVPSetParameters( (VPSetParameter)parameter, indent );
		
		str += converter.getNewline();
		return str;
	}

	protected String writePSetParameters( PSetParameter pset, String indent, boolean newline ) 	
	{
		String str = "";
		if ( pset.parameterCount() == 0 )
			str += "{}";
		else if ( newline )
		{
			str += "{" + converter.getNewline(); 
			for ( int i = 0; i < pset.parameterCount(); i++ )
				str += toString( (Parameter)pset.parameter(i), indent + "  " );
			str += indent + "}"; 
		}
		else
		{
			Parameter first = (Parameter)pset.parameter(0);
			str += "{ " + toString( first, "" );
			for ( int i = 1; i < pset.parameterCount(); i++ )
				str += toString( (Parameter)pset.parameter(i), indent + "  " );
			str = str.substring( 0, str.length() - 1 ) + " }"; 
		}
		return str;
	}


	protected String writeVPSetParameters( VPSetParameter vpset, String indent ) 	
	{
		String str = "{" + converter.getNewline(); 
		for ( int i = 0; i < vpset.parameterSetCount() - 1; i++ )
		{
			PSetParameter pset = vpset.parameterSet(i);
			if ( pset.name().length() != 0 )
				str += addComma( toString( pset, indent + "  " ) );
			else
				str += indent + "  " + writePSetParameters(pset, indent + "  ", false )
					   + "," + converter.getNewline();
		}
		if ( vpset.parameterSetCount() >  0 )
		{
			PSetParameter pset = vpset.parameterSet(vpset.parameterSetCount() - 1);
			if ( pset.name().length() != 0 )
				str += toString( pset, indent + "  " );
			else
				str += indent + "  " + writePSetParameters(pset, indent + "  ", false ) + converter.getNewline();
		}
		str += indent + "}"; 
		return str;
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
		if ( !text.endsWith( converter.getNewline() )  )
			return text + ",";
		return text.substring(0, text.length() - 1) + "," + converter.getNewline(); 
	}
	
}
