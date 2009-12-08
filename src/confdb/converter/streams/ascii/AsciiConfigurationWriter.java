package confdb.converter.streams.ascii;

import confdb.converter.ConverterException;
import confdb.converter.table.AsciiTableWriter;
import confdb.data.IConfiguration;


/**
 * AsciiConfigurationWriter
 * ------------------------
 * @author Ulf Behrens
 * @author Philipp Schieferdecker
 * 
 * Display the summary information about each path (trigger) in an
 * ascii table.
 *
 * FORMAT:
 * ----------------------------------------------------
 * | stream  | primary dataset   | path   | L1 seed   |
 * ----------------------------------------------------
 */
public class AsciiConfigurationWriter extends AsciiTableWriter
{
    //
    // member functions
    //

    /** generate ascii summary table representation of the configuration */
    public String toString(IConfiguration conf, WriteProcess writeProcess) throws ConverterException
    {
    	return createAsciiTable( new StreamsTable( conf ) );
    }
    
    
}
