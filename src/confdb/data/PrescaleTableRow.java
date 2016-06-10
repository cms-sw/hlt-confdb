package confdb.data;


import java.util.ArrayList;


/**
 * PrescaleTableRow
 * ----------------
 * @author Philipp Schieferdecker
 *
 * class to hold the data for one prescale table row
 */
public class PrescaleTableRow
{
    public String pathName;
    public ArrayList<Long> prescales;
    public PrescaleTableRow(String pathName,ArrayList<Long> prescales)
    {
	this.pathName = pathName;
	this.prescales = prescales;
    }
    public PrescaleTableRow(String pathName, int prescaleCount)
    {
	this.pathName = pathName;
	prescales = new ArrayList<Long>();
	for (int i=0;i<prescaleCount;i++) prescales.add(new Long(1));
    }
}
