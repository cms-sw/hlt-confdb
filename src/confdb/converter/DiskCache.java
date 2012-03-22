package confdb.converter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;


public class DiskCache 
{
    protected String dir = "";
    protected long maxSpace = 45 * 1024 * 1024;
    protected long inUse = 0;
    
    private Statistics deserialize = new Statistics();
    private Statistics serialize = new Statistics();

    static public ArrayList<ExceptionBufferEntry> exceptions = new ArrayList<DiskCache.ExceptionBufferEntry>();
    
    public DiskCache( String p, long space ) throws IOException, SecurityException
    {
    	maxSpace = space * 1024 * 1024;
    	p += File.separator;
    	File file = new File( p + ".start" );
    	if ( file.exists() )
    		file.delete();
    	if ( !file.createNewFile() )
    		throw new IOException( "dir '" + dir + "' not usable" );
    	dir = p;
    	getInUse();
    }

    public void writeToDisk( Object o, String fileName )
    {
    	long start = System.currentTimeMillis();
        try {
        	while ( maxSpace - inUse < 5 * 1024 * 1024 )
        		deleteOldestFile();
        	File file = new File( dir + fileName );
        	FileOutputStream fos = new FileOutputStream( file );
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject( o );
	        oos.close();
	        fos.close();
	        inUse += file.length();
			serialize.add( System.currentTimeMillis() - start );
		} catch (Exception e) {
			storeException(e);
			try {
	        	File file = new File( dir + fileName );
				if ( file.exists() )
					file.delete();
				getInUse();
			} catch (Exception x) {
			}
		}
    }
    
    public Object loadFromDisk( String fileName )
    {
    	long start = System.currentTimeMillis();
    	
    	File file = new File( dir + fileName );
    	if ( !file.exists() )
			return null;

        try {
        	FileInputStream fis = new FileInputStream( file );
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        Object o = ois.readObject();
	        ois.close();
	    	long now = System.currentTimeMillis();
	        file.setLastModified( now );
			deserialize.add( now - start );
			fis.close();
	        return o;
		} catch (Exception e) {
			storeException(e);
		}
		return null;
    }
    
    public long getInUse()
    {
    	inUse = 0;
    	File[] files = (new File(dir)).listFiles();
    	for ( File file : files )
    		inUse += file.length();
    	return inUse;
    }
    
    public int getAvailableSpace() // in MB
    {
    	return (int)((maxSpace - getInUse()) / 1024 / 1024);
    }
    
    protected void deleteOldestFile()
    {
    	File[] files = (new File(dir)).listFiles();
    	File oldest = null;
    	for ( File file : files )
    	{
    		if ( oldest == null ||  file.lastModified() < oldest.lastModified() )
    			oldest = file;    			
    	}
    	if ( oldest != null )
    		oldest.delete();
    	getInUse();
    }
    
    protected void storeException( Exception e )
    {
    	if ( exceptions.size() > 100 )
    		exceptions.remove(0);
    	exceptions.add( new ExceptionBufferEntry(e) );
    }
    
	public class ExceptionBufferEntry
	{
		boolean exceptionThrown = true;
		String exception = "";
		String message = "";
		String stacktrace = "";
		long timestamp = 0;

		ExceptionBufferEntry( Exception e ) 
		{
			timestamp = System.currentTimeMillis();
			message = e.getMessage();
			exception = e.getClass().getCanonicalName();	
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(buffer);
			e.printStackTrace(writer);
			writer.close();
			stacktrace = buffer.toString();
		}

		public boolean isExceptionThrown() {
			return exceptionThrown;
		}

		public String getException() {
			return exception;
		}

		public String getStacktrace() {
			return stacktrace;
		}
		
		public long getTime()
		{
			return timestamp;
		}
		
		public String getMessage()
		{
			return message;
		}
		
		
	}

	/* setters and getters */
	
	public Statistics getSerialize() {
		return serialize;
	}

	public Statistics getDeserialize() {
		return deserialize;
	}

}



