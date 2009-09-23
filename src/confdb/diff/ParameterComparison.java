package confdb.diff;

import java.util.ArrayList;

import confdb.data.Parameter;
import confdb.data.VectorParameter;
import confdb.data.PSetParameter;
import confdb.data.VPSetParameter;


/**
 * ParameterComparison
 * -------------------
 * @author Philipp Schieferdecker
 *
 */
public class ParameterComparison extends Comparison
{
    //
    // member data
    //
    
    /** old parameter */
    private Parameter oldParameter = null;

    /** new parameter */
    private Parameter newParameter = null;
    
    
    //
    // construction
    //
    
    /** standard constructor */
    public ParameterComparison(Parameter oldParameter,Parameter newParameter)
    {
	this.oldParameter = oldParameter;
	this.newParameter = newParameter;
    }

    
    //
    // member functions
    //

    /** indicate if [V]Pset parameters are being compared */
    public boolean isPSet()
    {
	Parameter p = (newParameter==null) ? oldParameter : newParameter;
	if ((p instanceof PSetParameter)||(p instanceof VPSetParameter)) return true;
	return false;
    }
    
    /** determine the result of the comparison */
    public int result()
    {
	if      (oldParameter==null&&newParameter!=null) return RESULT_ADDED;
	else if (oldParameter!=null&&newParameter==null) return RESULT_REMOVED;
	else if (comparisonCount()==0&&
		 oldParameter.name().equals(newParameter.name())&&
		 oldParameter.type().equals(newParameter.type())&&
		 oldParameter.isTracked()==newParameter.isTracked()&&
		 (isPSet()||oldParameter.valueAsString()
		  .equals(newParameter.valueAsString())))
	    return RESULT_IDENTICAL;
	else
	    return RESULT_CHANGED;
    }
    
    /** plain-text representation of the comparison */
    public String toString()
    {
	StringBuffer result = new StringBuffer();
	Parameter p = (newParameter==null) ? oldParameter : newParameter;

	result.append(typeAndNameAsString(oldParameter,newParameter));
	
	if (p instanceof VectorParameter) {
	    result.append(typeChangedAsString(oldParameter,newParameter));
	    result.append(trackinessChangedAsString(oldParameter,newParameter));
	    result.append(" [").append(resultAsString()).append("]");
	    
	    if (isChanged()&&
		(oldParameter instanceof VectorParameter &&
		 newParameter instanceof VectorParameter)) {
		VectorParameter vpold = (VectorParameter)oldParameter;
		VectorParameter vpnew = (VectorParameter)newParameter;
		result
		    .append(vectorParameterChangeAsString(vpold,vpnew));
	    }
	}
	else if(!(p instanceof PSetParameter)&&!(p instanceof VPSetParameter)){
	    result.append(" = ").append(p.valueAsString());
	    result.append(typeChangedAsString(oldParameter,newParameter));
	    result.append(trackinessChangedAsString(oldParameter,newParameter));
	    result.append(oldValueAsString(oldParameter,newParameter));
	    if (!isChanged())
		result.append(" [").append(resultAsString()).append("]");
	}
	else {
	    result.append(trackinessChangedAsString(oldParameter,newParameter));
	    result.append(" [").append(resultAsString()).append("]");
	}
	return result.toString();
    }
    
    /** html representation of the comparison */
    public String toHtml()
    {
	StringBuffer result = new StringBuffer();
	Parameter p = (newParameter==null) ? oldParameter : newParameter;
	
	result.append("<html>");
	result.append(typeAndNameAsHtml(oldParameter,newParameter));
	if (!(p instanceof PSetParameter)&&!(p instanceof VPSetParameter)) {
	    result
		.append(" = <font color=#00ff00>")
		.append(p.valueAsString())
		.append("</font>");
	    result.append(typeChangedAsHtml(oldParameter,newParameter));
	    result.append(trackinessChangedAsHtml(oldParameter,newParameter));
	    result.append(oldValueAsHtml(oldParameter,newParameter));
	}
	result.append("</html>");
	return result.toString();
    }

    //
    // private member functions
    //

    /** retrieve string represantation of parameter type & name */
    private String typeAndNameAsString(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	Parameter p = (pnew==null) ? pold : pnew;
	if (!p.isTracked()) result.append("untracked ");
	result.append(p.type()).append(" ").append(p.fullName());
	return result.toString();
    }
    
    /** retrieve string represantion for parameter type change */
    private String typeChangedAsString(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	if (isChanged()&&(!pold.type().equals(pnew.type())))
	    result.append(" {").append(pold.type()).append("}");
	return result.toString();
    }
    
    /** retrieve string represantion for parameter trackiness change */
    private String trackinessChangedAsString(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	if (isChanged()&&(pold.isTracked()!=pnew.isTracked())) {
	    String trackiness = (pnew.isTracked())?"TRACKED":"UNTRACKED";
	    result.append(" [").append(trackiness).append("]");
	}
	return result.toString();
    }

    /** retrieve string represantation of old parameter value */
    private String oldValueAsString(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();

	if (isChanged()&&!pold.valueAsString().equals(pnew.valueAsString()))
	    result.append(" [").append(pold.valueAsString()).append("]");
	return result.toString();
    }

    /** retrieve string representation of vector parameter change */
    private String vectorParameterChangeAsString(VectorParameter vpold,
						 VectorParameter vpnew)
    {
	StringBuffer offsetAsString = new StringBuffer();
	for (int i=0;i<8;i++) offsetAsString.append(" ");
	
	StringBuffer result = new StringBuffer();
	ArrayList<String> oldValues = new ArrayList<String>();
	ArrayList<String> newValues = new ArrayList<String>();
	for (int i=0;i<vpold.vectorSize();i++)
	    oldValues.add(vpold.value(i).toString());
	for (int i=0;i<vpnew.vectorSize();i++)
	    newValues.add(vpnew.value(i).toString());
	
	for (int i=0;i<newValues.size();i++) {
	    result.append("\n").append(offsetAsString);
	    String vnew = newValues.get(i);
	    int    iold = oldValues.indexOf(vnew);
	    if (i>=oldValues.size()) {
		if (iold<0) result.append("+  ").append(vnew);
		else        result.append("\\/ ").append(vnew);
	    }
	    else {
		String vold = oldValues.get(i);
		if (vnew.equals(vold)) result.append("=  ").append(vnew);
		else if (iold<0) result.append("+  ").append(vnew);
		else if (iold<i) result.append("\\/ ").append(vnew);
		else if (iold>i) result.append("^  ").append(vnew);
		int inew = newValues.indexOf(vold);
		if (inew<0) result.append("  [-  ").append(vold).append("]");
	    }
	}
	for (int i=newValues.size();i<oldValues.size();i++) {
	    String vold = oldValues.get(i);
	    int    inew = newValues.indexOf(vold);
	    if (inew<0) result
			    .append("\n")
			    .append(offsetAsString)
			    .append("-  ")
			    .append(vold);
	}
	return result.toString();
    }
    
    
    
    /** retrieve HTML represantation of parameter type & name */
    private String typeAndNameAsHtml(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	Parameter p = (pnew==null) ? pold : pnew;
	if (!p.isTracked()) result.append("untracked ");
	result.append(p.type()).append(" <b>").append(p.name()).append("</b>");
	return result.toString();
    }
    
    /** retrieve HTML represantion for parameter type change */
    private String typeChangedAsHtml(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	if (isChanged()&&(!pold.type().equals(pnew.type())))
	    result
		.append(" {<font color=#0000ff>")
		.append(pold.type())
		.append("</font>}");
	return result.toString();
    }
    
    /** retrieve HTML represantion for parameter trackiness change */
    private String trackinessChangedAsHtml(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	if (isChanged()&&(pold.isTracked()!=pnew.isTracked())) {
	    String trackiness = (pnew.isTracked()) ? "TRACKED" : "UNTRACKED";
	    result.append(" [<i>").append(trackiness).append("</i>]");
	}
	return result.toString();
    }

    /** retrieve HTML represantation of old parameter value */
    private String oldValueAsHtml(Parameter pold,Parameter pnew)
    {
	StringBuffer result = new StringBuffer();
	if (isChanged()&&!pold.valueAsString().equals(pnew.valueAsString()))
	    result
		.append(" [<font color=#ff0000>")
		.append(pold.valueAsString())
		.append("</font>]");
	return result.toString();
    }


}
