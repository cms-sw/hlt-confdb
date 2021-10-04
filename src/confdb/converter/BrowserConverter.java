package confdb.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import confdb.converter.ConverterBase;
import confdb.converter.ConverterException;
import confdb.converter.DbProperties;
import confdb.converter.OfflineConverter;
import confdb.data.*;
import confdb.db.ConfDB;
import confdb.db.ConfDBSetups;


public class BrowserConverter extends OfflineConverter
{
    static private HashMap<Integer, BrowserConverter> map = new HashMap<Integer, BrowserConverter>();
    static private String[] dbNames = null;

    private PreparedStatement psSelectHltKeyFromRunSummary = null;

    private BrowserConverter(String dbType, String dbUrl,
                 String dbUser, String dbPwrd) throws ConverterException
    {
        super( "HTML", dbType, dbUrl, dbUser, dbPwrd );
    }

    private BrowserConverter(String format,
                             String dbType, String dbUrl,
                 String dbUser, String dbPwrd) throws ConverterException
    {
        super( format, dbType, dbUrl, dbUser, dbPwrd );
    }

    public int getKeyFromRunSummary( int runnumber ) throws SQLException
    {
        if ( psSelectHltKeyFromRunSummary == null )
            psSelectHltKeyFromRunSummary = getDatabase().getDbConnector().getConnection().prepareStatement( "SELECT HLTKEY FROM WBM_RUNSUMMARY WHERE RUNNUMBER=?" );
        psSelectHltKeyFromRunSummary.setInt( 1, runnumber );
        ResultSet rs = psSelectHltKeyFromRunSummary.executeQuery();
        int key = -1;
        if ( rs.next() )
            key = rs.getInt(1);
        return key;
    }

    protected void finalize() throws Throwable
    {
        super.finalize();
        ConfDB db = getDatabase();
        if ( db != null )
            db.disconnect();
    }

    static public BrowserConverter getConverter( String dbName ) throws ConverterException
    {
        return getConverter( getDbIndex(dbName) );
    }


    static public BrowserConverter getConverter( int dbIndex ) throws ConverterException
    {
        ConfDBSetups dbs = new ConfDBSetups();
        DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
        String dbUser = dbProperties.getDbUser();
        if (dbUser.endsWith("_w"))
            dbUser = dbUser.substring(0, dbUser.length()-1)+"r";
            else if (dbUser.endsWith("_r"))
                {}      // do nothing
        else if (dbUser.endsWith("_writer"))
            dbUser = dbUser.substring(0, dbUser.length()-6)+"reader";
            else if (dbUser.endsWith("_reader"))
                {}      // do nothing
        else
            dbUser = "cms_hlt_gdr_r";

        dbProperties.setDbUser(dbUser);

        BrowserConverter converter = map.get( new Integer( dbIndex ) );
        if ( converter == null )
        {
            converter = new BrowserConverter( dbs.type( dbIndex ), dbProperties.getDbURL(), dbProperties.getDbUser(), "convertme!" );
            map.put( new Integer( dbIndex ), converter );
        }
        return converter;
    }

    static public void deleteConverter( ConverterBase converter )
    {
        Set<Map.Entry<Integer, BrowserConverter>> entries = map.entrySet();
        for ( Map.Entry<Integer, BrowserConverter> entry : entries )
        {
            if ( entry.getValue() == converter )
            {
                map.remove( entry.getKey() );
                return;
            }
        }
    }

    static public String getDbName( int dbIndex )
    {
        if ( dbNames == null )
        {
            ConfDBSetups dbs = new ConfDBSetups();
            dbNames = dbs.labelsAsArray();
        }
        return dbNames[ dbIndex ];
    }


    static public int getDbIndex( String dbName )
    {
        if ( dbName.equalsIgnoreCase( "hltdev" ) )
            dbName = "HLT Development";

        int setupCount = new ConfDBSetups().setupCount();
        for ( int i = 0; i < setupCount; i++ )
        {
            if ( dbName.equalsIgnoreCase( getDbName( i ) ) )
                return i;
        }
        return -1;
    }

    static public String[] listDBs()
    {
        ConfDBSetups dbs = new ConfDBSetups();
        if ( dbNames == null )
            dbNames = dbs.labelsAsArray();
        ArrayList<String> list = new ArrayList<String>();
        for ( int i = 0; i < dbs.setupCount(); i++ )
        {
            String name = dbs.name(i);
            if ( name != null && name.length() > 0  )
            {
                String host = dbs.host(i);
                if (     host != null
                     && !host.equalsIgnoreCase("localhost")
                     && !host.endsWith( ".cms") )
                     {
                         list.add( getDbName(i) );
                     }
            }
        }
        return list.toArray( new String[ list.size() ] );
    }

    static public String[] getAnchors( int dbIndex, int configKey ) throws ConverterException
    {
        ArrayList<String> list = new ArrayList<String>();
        ConverterBase converter = null;
        try {
            converter = BrowserConverter.getConverter( dbIndex );
            IConfiguration conf = converter.getConfiguration( configKey );
            if ( conf == null )
                list.add( "??" );
            else
            {
                if ( conf.pathCount() > 0 )
                    list.add( "paths" );
                if ( conf.sequenceCount() > 0 )
                    list.add( "sequences" );
                if ( conf.moduleCount() > 0 )
                    list.add( "modules" );
                if ( conf.edsourceCount() > 0 )
                    list.add( "ed_sources" );
                if ( conf.essourceCount() > 0 )
                    list.add( "es_sources" );
                if ( conf.esmoduleCount() > 0 )
                    list.add( "es_modules" );
                if ( conf.serviceCount() > 0 )
                    list.add( "services" );
            }
        } catch (ConverterException e) {
            if ( converter != null )
                BrowserConverter.deleteConverter( converter );
            throw e;
        }
        return list.toArray( new String[ list.size() ] );
    }

    static public int getCacheEntries()
    {
        return ConfCache.getNumberCacheEntries();
    }

    static public class UrlParameter {
        public boolean asFragment = false;
        public String format = "python";
        public int configId = -1;
        public String configName = null;
        public int runNumber = -1;
        public String dbName = "orcoff";
        public HashMap<String, String> toModifier = new HashMap<String, String>();

        UrlParameter() {}
    }

    static public UrlParameter getUrlParameter( Map<String, String[]> map ) throws ConverterException
    {
        if ( map.isEmpty())
            throw new ConverterException( "ERROR: configId or configName or runNumber must be specified!" );

        UrlParameter p = new UrlParameter();
        Set<Map.Entry<String, String[]>> parameters = map.entrySet();
        for ( Map.Entry<String, String[]> entry : parameters )
        {
            if ( entry.getValue().length > 1 )
                throw new ConverterException( "ERROR: Only one parameter '" + entry.getKey() + "' allowed!" );

            String value = entry.getValue()[ 0 ];
            String key = entry.getKey();
            if ( key.equals("configId"))
                p.configId = Integer.parseInt( value );
            else if ( key.equals("configKey"))
                p.configId = Integer.parseInt( value );
            else if ( key.equals("runNumber"))
                p.runNumber = Integer.parseInt( value );
            else if (key.equals( "configName")) {
                p.configName = value;
            }
            else if (key.equals( "cff")) {
                p.asFragment =true;
                p.toModifier.put( key, value );
            }
            else if (key.equals( "format")) {
                p.format = value;
            }
            else if ( key.equals( "dbIndex" ) )
                p.dbName = BrowserConverter.getDbName( Integer.parseInt( value ) );
            else if ( key.equals( "dbName" ) )
                p.dbName = value;
            else {
                p.toModifier.put(entry.getKey(), value);
            }
        }

        if ( p.configId == -1  &&  p.configName == null && p.runNumber == -1 )
            throw new ConverterException( "ERROR: configId or configName or runNumber must be specified!" );

        int moreThanOne = ( p.configId != -1 ? 1 : 0 )
            +  ( p.configName != null ? 1 : 0 )
            +  ( p.runNumber != -1 ? 1 : 0 );
        if ( moreThanOne > 1 )
            throw new ConverterException( "ERROR: configId *OR* configName *OR* runNumber must be specified!" );
        return p;
    }


    //
    // main method, for testing
    //
    public static void main(String[] args)
    {
        String  configId    =                  "";
        String  configName  =                  "";
        String  runNumber   =                  "";
        String  format      =            "python";
        boolean asFragment  =               false;

        String  dbType      =            "oracle";
        String  dbHost      =   "cmsr1-v.cern.ch";
        String  dbPort      =             "10121";
        String  dbName      =  "cms_cond.cern.ch";
        String  dbUser      = "cms_hltdev_reader";
        String  dbPwrd      =        "convertme!";

        HashMap<String, String> cnvArgs = new HashMap<String, String>();

        for (int iarg=0; iarg < args.length; iarg++) {
            String arg = args[iarg];
            if (arg.equals("-id") || arg.equals("--configId")) {
                iarg++; configId = args[iarg];
            }
            else if (arg.equals("-cfg") || arg.equals("--configName")) {
                iarg++; configName = args[iarg];
            }
            else if (arg.equals("-run") || arg.equals("--runNumber")) {
                iarg++; runNumber = args[iarg];
            }
            else if (arg.equals("-f") || arg.equals("--format")) {
                iarg++; format = args[iarg];
            }
            else if (arg.equals("--cff")) {
                asFragment = true;
            }
            else if (arg.equals("-t") || arg.equals("--dbtype")) {
                iarg++; dbType = args[iarg];
            }
            else if (arg.equals("-h") || arg.equals("--dbhost")) {
                iarg++; dbHost = args[iarg];
            }
            else if (arg.equals("-p") || arg.equals("--dbport")) {
                iarg++; dbPort = args[iarg];
            }
            else if (arg.equals("-d") || arg.equals("--dbname")) {
                iarg++; dbName = args[iarg];
            }
            else if (arg.equals("-u") || arg.equals("--dbuser")) {
                iarg++; dbUser = args[iarg];
            }
            else if (arg.equals("-s") || arg.equals("--dbpwrd")) {
                iarg++; dbPwrd = args[iarg];
            }
            else if (arg.startsWith("--no")) {
                String key = arg.substring(2);
                String val = "";
                cnvArgs.put(key, val);
            }
            else if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String val = args[++iarg];
                cnvArgs.put(key, val);
            }
            else {
                System.err.println("ERROR: invalid option '" + arg + "'!");
                System.exit(1);
            }
        }

        if (configId.isEmpty() && configName.isEmpty() && runNumber.isEmpty()) {
            System.err.println("ERROR: please specify either --configId, --configName or --runNumber");
            System.exit(1);
        }

        if ((! configId.isEmpty() && ! configName.isEmpty()) ||
            (! configId.isEmpty() && ! runNumber.isEmpty()) ||
            (! configName.isEmpty() && ! runNumber.isEmpty()) )
        {
            System.err.println("ERROR: please specify nly one of --configId, --configName or --runNumber");
            System.exit(1);
        }

        if (! format.equals("ascii") &&
            ! format.equals("python") &&
            ! format.equals("summary.ascii") &&
            ! format.equals("html"))
        {
            System.err.println("ERROR: Invalid format '"+format+"'");
            System.exit(1);
        }

        String dbUrl = "";
        if (dbType.equalsIgnoreCase("mysql")) {
            dbUrl  = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
        }
        else if (dbType.equalsIgnoreCase("oracle")) {

            dbUrl = ConfDB.getDbURL(dbHost,dbPort,dbName);
        }
        else {
            System.err.println("ERROR: Unknwown database type '"+dbType+"'");
            System.exit(1);
        }

        System.err.println("dbURl  = " + dbUrl);
        System.err.println("dbUser = " + dbUser);
        System.err.println("dbPwrd = " + dbPwrd);

        try {
            ModifierInstructions modifications = new ModifierInstructions();
            modifications.interpretArgs(cnvArgs);
            BrowserConverter converter = new BrowserConverter(format, dbType, dbUrl, dbUser, dbPwrd);

            int id = 0;
            if (! runNumber.isEmpty()) {
                int run = Integer.parseInt(runNumber);
                id  = converter.getKeyFromRunSummary(run);
            }
            else if (! configId.isEmpty()) {
                id = Integer.parseInt(configId);
            }
            else {
                id = converter.getDatabase().getConfigId(configName);
            }
            System.out.println( converter.getConfigString(id, format, modifications, asFragment) );
        }
        catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
