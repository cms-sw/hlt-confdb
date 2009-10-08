package confdb.data;



/**
 * ParameterFactory
 * ----------------
 * @author Philipp Schieferdecker
 *
 * instantiate parameters of certain scalar or vector types.
 */
public class ParameterFactory
{
    /** create a scalar- or vector-type parameter */
    public static Parameter create(String type,String name,String value,
				   boolean isTracked)
    {
	if (type.equals("bool"))
	    return new BoolParameter(name,value,isTracked);
	if (type.equals("int32"))
	    return new Int32Parameter(name,value,isTracked);
	if (type.equals("uint32"))
	    return new UInt32Parameter(name,value,isTracked);
	if (type.equals("int64"))
	    return new Int64Parameter(name,value,isTracked);
	if (type.equals("uint64"))
	    return new UInt64Parameter(name,value,isTracked);
	if (type.equals("double"))
	    return new DoubleParameter(name,value,isTracked);
	if (type.equals("string"))
	    return new StringParameter(name,value,isTracked);
	if (type.equals("EventID"))
	    return new EventIDParameter(name,value,isTracked);
	if (type.equals("InputTag"))
	    return new InputTagParameter(name,value,isTracked);
	if (type.equals("FileInPath"))
	    return new FileInPathParameter(name,value,isTracked);
	if (type.equals("vint32"))
	    return new VInt32Parameter(name,value,isTracked);
	if (type.equals("vuint32"))
	    return new VUInt32Parameter(name,value,isTracked);
	if (type.equals("vint64"))
	    return new VInt64Parameter(name,value,isTracked);
	if (type.equals("vuint64"))
	    return new VUInt64Parameter(name,value,isTracked);
	if (type.equals("vdouble"))
	    return new VDoubleParameter(name,value,isTracked);
	if (type.equals("vstring"))
	    return new VStringParameter(name,value,isTracked);
	if (type.equals("VEventID"))
	    return new VEventIDParameter(name,value,isTracked);
	if (type.equals("VInputTag"))
	    return new VInputTagParameter(name,value,isTracked);
	if (type.equals("PSet"))
	    return new PSetParameter(name,value,isTracked);
	if (type.equals("VPSet"))
	    return new VPSetParameter(name,value,isTracked);
	return null;
    }
    
}
