package confdb.data;

import java.util.ArrayList;


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
				   boolean isTracked,boolean isDefault)
    {
	if (type.equals("bool"))
	    return new BoolParameter(name,value,isTracked,isDefault);
	if (type.equals("int32"))
	    return new Int32Parameter(name,value,isTracked,isDefault);
	if (type.equals("uint32"))
	    return new UInt32Parameter(name,value,isTracked,isDefault);
	if (type.equals("double"))
	    return new DoubleParameter(name,value,isTracked,isDefault);
	if (type.equals("string"))
	    return new StringParameter(name,value,isTracked,isDefault);
	if (type.equals("EventID"))
	    return new EventIDParameter(name,value,isTracked,isDefault);
	if (type.equals("InputTag"))
	    return new InputTagParameter(name,value,isTracked,isDefault);
	if (type.equals("FileInPath"))
	    return new FileInPathParameter(name,value,isTracked,isDefault);
	if (type.equals("vint32"))
	    return new VInt32Parameter(name,value,isTracked,isDefault);
	if (type.equals("vuint32"))
	    return new VUInt32Parameter(name,value,isTracked,isDefault);
	if (type.equals("vdouble"))
	    return new VDoubleParameter(name,value,isTracked,isDefault);
	if (type.equals("vstring"))
	    return new VStringParameter(name,value,isTracked,isDefault);
	if (type.equals("VEventID"))
	    return new VEventIDParameter(name,value,isTracked,isDefault);
	if (type.equals("VInputTag"))
	    return new VInputTagParameter(name,value,isTracked,isDefault);
	if (type.equals("PSet"))
	    return new PSetParameter(name,value,isTracked,isDefault);
	if (type.equals("VPSet"))
	    return new VPSetParameter(name,value,isTracked,isDefault);
	return null;
    }
    
}
