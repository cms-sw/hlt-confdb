package confdb.converter.streams.list;

import java.util.Iterator;

import confdb.converter.ConverterEngine;
import confdb.converter.ConverterException;
import confdb.converter.IListWriter;
import confdb.converter.IParameterSetter;
import confdb.data.IConfiguration;
import confdb.data.Path;
import confdb.data.PrimaryDataset;
import confdb.data.Stream;


/**
 * ListConfigurationWriter
 * ------------------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 * 
 * outputs list of primary datasets / paths in a given stream
 *
 */

public class ListConfigurationWriter implements IListWriter, IParameterSetter
{
	private String streamName;
	private String datasetName = null;
    //
    // member functions
    //

    /** generate ascii summary table representation of the configuration */
    public String toString(IConfiguration conf, WriteProcess writeProcess ) throws ConverterException
    {
    	StringBuffer list = new StringBuffer();
    	
    	Stream stream = conf.stream( streamName );
    	if ( stream == null )
    		return "ERROR: stream '" + streamName + "' not found";
    	
    	if ( datasetName == null )
    	{
    		Iterator<PrimaryDataset> datasets = stream.datasetIterator();
    		while ( datasets.hasNext() )
    		{
    			PrimaryDataset dataset = datasets.next();
    			String datasetName = dataset.label();
    			list.append( datasetName ).append( '\n' );
    		}
    	}
    	else
    	{
    		PrimaryDataset dataset = stream.dataset( datasetName );
    		if ( dataset == null )
        		return "ERROR: dataset '" + datasetName + "' not found in stream '" + streamName + "'";
    		Iterator<Path> paths = dataset.pathIterator();
    		while ( paths.hasNext() )
    		{
    			list.append( paths.next().name() ).append( '\n' );
    		}
    	}

    	return list.toString();
    }

    
	public void setConverterEngine(ConverterEngine converterEngine) {
		// TODO Auto-generated method stub
		
	}


	public void setParameter( String parameter ) 
	{
		if ( parameter.indexOf( '.' ) == -1 )
			streamName = parameter;
		else
		{
			int sep = parameter.indexOf( '.' );
			datasetName = parameter.substring(  sep + 1 );
			streamName = parameter.substring( 0, sep );
		}
	}
    
    
}
