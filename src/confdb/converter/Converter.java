package confdb.converter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import confdb.data.Configuration;
import confdb.data.Template;
import confdb.db.CfgDatabase;
import confdb.db.DatabaseException;

public class Converter {
	private String releaseTag = "";
	private CfgDatabase database = null;
	private Configuration configuration = null;
	private IConfigurationWriter configurationWriter = null;
	private IParameterWriter parameterWriter = null;

	private IEDSourceWriter edsourceWriter  = null;
	private IESSourceWriter essourceWriter = null;
	private IServiceWriter serviceWriter = null;
	private IModuleWriter moduleWriter = null;
	private IPathWriter pathWriter = null;
	private ISequenceWriter sequenceWriter = null;

	private ArrayList<Template> edsourceTemplateList = new ArrayList<Template>();
	private ArrayList<Template> essourceTemplateList = new ArrayList<Template>();
	private ArrayList<Template> serviceTemplateList = new ArrayList<Template>();
	private ArrayList<Template> moduleTemplateList = new ArrayList<Template>();

	static private Preferences  prefs = Preferences.userNodeForPackage( Converter.class );
	static protected String keyVersionSeparator = ".V";
	
	static final public String newline = "\n";

	
	protected Converter( String releaseTag )
	{
		this.releaseTag = releaseTag;
	}
	
	
	public boolean readConfiguration( String configKey ) throws SQLException
	{
		String configName = configKey;
		int configVersion = 1;
		int startVersion = configKey.lastIndexOf( keyVersionSeparator );
		if ( startVersion != -1 )
		{
			configName = configKey.substring( 0, startVersion );
			String versionStr = configKey.substring( startVersion + keyVersionSeparator.length() );
			configVersion = Integer.parseInt( versionStr );
		}
		int configVersionMax = 100000;
		configuration = 
			database.loadConfigurationThrowsException( configName, configVersion, configVersionMax, 
													   releaseTag, 
													   edsourceTemplateList, essourceTemplateList, serviceTemplateList, moduleTemplateList);
		if ( configuration == null )
			return false;
		return true;
	}
	
	public String convertConfiguration()
	{
		String str = "// " + configuration.name() + " V" + configuration.version()
			+ " (" + configuration.releaseTag() + ")" + getNewline();
		return str + configurationWriter.toString( configuration, this );
	}
	

	
	
	
	public static void main(String[] args) 
	{
		String usage = "java " + Converter.class.getName() 
		  + "  configKey [ CMSSWrelease dbName dbType dbHost dbUser dbPwrd keyVersionSeparator]";
		
		if ( args.length < 1 )
		{
			System.err.println( "usage:" );
			System.err.println( usage );
			System.exit(1);
		}
		String configKey = args[0];

		int argI = 1;
		
		String CMSSWrelease = Converter.getPrefs().get( "CMSSWrelease", "CMSSW_1_2_0_pre5" );
		String dbName = Converter.getPrefs().get( "dbName", "hltdb" );
		String dbType = Converter.getPrefs().get( "dbType", "mysql" );
		String dbHost = Converter.getPrefs().get( "dbHost", "localhost" );
		String dbUser = Converter.getPrefs().get( "dbUser", "hlts" );
		String dbPwrd = Converter.getPrefs().get( "dbPwrd", "cms" );
		keyVersionSeparator = Converter.getPrefs().get( "keyVersionSeparator", keyVersionSeparator );

		if ( args.length > argI )
		{
			CMSSWrelease = args[argI++];
			prefs.put( "CMSSWrelease", CMSSWrelease );
		}
			
		
		if ( args.length > argI )
		{
			dbName = args[argI++];
			prefs.put( "dbName", dbName );
		}

		if ( args.length > argI )
		{
			dbType = args[argI++];
			prefs.put( "dbType", dbType );
		}

		if ( args.length > argI )
		{
			dbHost = args[argI++];
			prefs.put( "dbHost", dbHost );
		}


		if ( args.length > argI )
		{
			dbUser = args[argI++];
			prefs.put( "dbUser", dbUser );
		}

		if ( args.length > argI )
		{
			dbPwrd = args[argI++];
			prefs.put( "dbPwrd", dbPwrd );
		}

		if ( args.length > argI )
		{
			keyVersionSeparator = args[argI++];
			prefs.put( "keyVersionSeparator", keyVersionSeparator );
		}


		String dbUrl = "jdbc:mysql://" + dbHost + ":3306/" + dbName;
		if ( dbType.equals("oracle") )
		    dbUrl = "jdbc:oracle:thin:@//" + dbHost + "/" + dbName;
		
		CfgDatabase database = new CfgDatabase();
		try {
			database.connect( dbType, dbUrl, dbUser, dbPwrd );
			database.prepareStatements();
			
			Converter converter = ConverterFactory.getFactory( CMSSWrelease ).getConverter();
			converter.setDatabase( database );
			if ( !converter.readConfiguration( configKey ) )
			{
				System.err.println( "config " + configKey + " doesn't exist!");
				System.exit(1);
			}
			String config = converter.convertConfiguration();
			System.out.println( config );
			database.disconnect();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public String getNewline()
	{
		return newline;
	}

	public CfgDatabase getDatabase() {
		return database;
	}


	public void setDatabase(CfgDatabase database) {
		this.database = database;
	}
	
	public IConfigurationWriter getConfigurationWriter() {
		return configurationWriter;
	}

	protected void setConfigurationWriter(IConfigurationWriter configurationWriter) {
		this.configurationWriter = configurationWriter;
	}


	public IPathWriter getPathWriter() {
		return pathWriter;
	}


	protected void setPathWriter(IPathWriter pathWriter) {
		this.pathWriter = pathWriter;
	}


	public IServiceWriter getServiceWriter() {
		return serviceWriter;
	}


	protected void setServiceWriter(IServiceWriter serviceWriter) {
		this.serviceWriter = serviceWriter;
	}


	public IParameterWriter getParameterWriter() {
		return parameterWriter;
	}


	protected void setParameterWriter(IParameterWriter parameterWriter) {
		this.parameterWriter = parameterWriter;
	}


	public IEDSourceWriter getEDSourceWriter() {
		return edsourceWriter;
	}


	protected void setEDSourceWriter(IEDSourceWriter edsourceWriter) {
		this.edsourceWriter = edsourceWriter;
	}


	public IESSourceWriter getESSourceWriter() {
		return essourceWriter;
	}


	protected void setESSourceWriter(IESSourceWriter essourceWriter) {
		this.essourceWriter = essourceWriter;
	}


	public IModuleWriter getModuleWriter() {
		return moduleWriter;
	}


	protected void setModuleWriter(IModuleWriter moduleWriter) {
		this.moduleWriter = moduleWriter;
	}


	public ISequenceWriter getSequenceWriter() {
		return sequenceWriter;
	}


	protected void setSequenceWriter(ISequenceWriter sequenceWriter) {
		this.sequenceWriter = sequenceWriter;
	}


	public static Preferences getPrefs() {
		return prefs;
	}

	
}
