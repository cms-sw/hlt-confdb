package confdb.converter.streams.ascii;

import java.util.Iterator;
import java.util.ArrayList;

import confdb.converter.table.ITableRow;
import confdb.data.*;

/**
 * SummaryTableRow
 * ---------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 *
 * Organize the summary data associated with one trigger path.
 */
public class StreamsTableRow implements ITableRow
{
    //
    // data members
    //
	private enum COLUMN { STREAM, DATASET, PATH, L1SEED };
	private String[] columns;
	private ArrayList<String> l1Lines  = new ArrayList<String>();

    //
    // construction
    //
    
    /** standard constructor */
    public StreamsTableRow( String stream, String dataset, Path path  )
    {
    	columns = new String[ COLUMN.values().length ];
    	columns[ COLUMN.STREAM.ordinal() ] = stream; 
    	columns[ COLUMN.DATASET.ordinal() ] = dataset; 
    	columns[ COLUMN.PATH.ordinal() ] = path.name();
    	String l1 = getL1Seed(path);
    	if ( l1.length() < 30 )
    	{
    		columns[ COLUMN.L1SEED.ordinal() ] = l1;
    		l1Lines.add( l1 );
    		return;
    	}

    	String[] lines = l1.split( "\\s" );
    	for ( String item : lines )
    	{
    		if (  !item.equalsIgnoreCase( "or" ) 
    		   && !item.equalsIgnoreCase( "and" ) 
			   && !item.equalsIgnoreCase( "not" ) 
			   && !item.equalsIgnoreCase( ")" ) ) 
    			l1Lines.add( item );
    		else if ( l1Lines.size() == 0 )
    			l1Lines.add( item );
    		else
    		{
    			int i = l1Lines.size() - 1;
    			String old = l1Lines.get( i );
    			l1Lines.set( i, old + " " + item );
    		}
    	}
    	if ( l1Lines.size() > 0 )
    		columns[ COLUMN.L1SEED.ordinal() ] = l1Lines.get( 0 );
    	else
    		columns[ COLUMN.L1SEED.ordinal() ] = "";
    }

    
    private static final String l1TemplateName    = "HLTLevel1GTSeed";
    private static final String l1CondParamName   = "L1SeedsLogicalExpression";

    static public String getL1Seed( Path path )
    {
    	ModuleInstance l1Module = null;
    	//ArrayList<ModuleInstance> filters = new ArrayList<ModuleInstance>();
    	Iterator<ModuleInstance> itM = path.moduleIterator();
    	while (itM.hasNext()) 
    	{
        	ModuleInstance module = itM.next();
        	String templateName = module.template().name();
        	//String templateType = module.template().type();
        	if ( templateName.equals( l1TemplateName ) )
        	{
        		l1Module = module;
        		break;
        	}
    	}

    	boolean	isNoTrigger = (l1Module==null);

    	String l1Condition = isNoTrigger ?
        	"-" : l1Module.parameter(l1CondParamName,"string").valueAsString();
    	if (l1Condition.startsWith("\""))
        	l1Condition = l1Condition.substring(1);
    	if (l1Condition.endsWith("\""))
        	l1Condition = l1Condition.substring(0,l1Condition.length()-1);
    	return l1Condition;
    }

	public int lineCount() {
		return l1Lines.size();
	}

    /** number of columns for _this_ trigger */
    public int columnCount() { 
    	return columns.length;
    }

    /** get the data entry to be displayed in particluar column/line */
    public String dataEntry( int iColumn, int iLine )
    {
    	if ( iLine == 0 )
    		return columns[ iColumn ];
    	if ( iColumn == COLUMN.L1SEED.ordinal() )
    		return ( iLine < l1Lines.size() ) ? l1Lines.get( iLine ) : "";
    	return "";
    }

    /** width needed to display a particular column for this trigger */
    public int columnWidth( int iColumn )
    {
    	if ( iColumn >= columnCount() ) 
    		return 0;
    	if ( iColumn != COLUMN.L1SEED.ordinal() )
    		return columns[ iColumn ].length() + 1;
	    int width = 0;
	    for ( String s : l1Lines ) 
	    {
	    	if ( s.length() > width ) 
	    		width = s.length();
	    }
	    return width + 1;
	}


}
