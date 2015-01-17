package confdb.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * GenericTransferable.java
 * 
 * <p>This transferable takes an object as data that is to be
 * transferred. It uses DataFlavor.stringFlavor, which is supported by
 * all objects. This transferable can be used in cases where a special
 * handling in terms of which data flavors are acceptable or which
 * data is transported do not matter.</p>
 * 
 * @author Ulrich Hilger
 * @author Light Development
 * @author <a href="http://www.lightdev.com">http://www.lightdev.com</a>
 * @author <a href="mailto:info@lightdev.com">info@lightdev.com</a>
 * @author published under the terms and conditions of the
 *      GNU General Public License,
 *      for details see file gpl.txt in the distribution
 *      package of this software
 *
 * @version 1, 30.07.2005
 */

public class GenericTransferable implements Transferable
{
    //
    // member data
    //
	  final static int GENERIC_OBJECT	= 0;
	  final static int STRING 			= 1;
	  
	  /** the data this transferable transports */
	  private Object data;
    
    /** the actual flavors supported by this transferable */
    final public static DataFlavor GENERIC_OBJECT_FLAVOR = new DataFlavor(Object.class, "Object flavor");
    
    /** storage for data flavors supported of this transferable */
    static DataFlavor flavors[] = { GENERIC_OBJECT_FLAVOR	,
    								DataFlavor.stringFlavor };
    
    //
    // construction
    //
    
    /** standard constructor */
    public GenericTransferable(Object data)  {
    	super();
    	this.data = data;
    }
    

    //
    // member functions
    //

    /** get the data flavors supported by this object */
    public DataFlavor[] getTransferDataFlavors() { return flavors; }
    
    /**determine whether or not a given data flavor is supported */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        boolean returnValue = false;
        for (int i = 0, n = flavors.length; i < n; i++) {
          if (flavor.equals(flavors[i])) {
            returnValue = true;
            break;
          }
        }
        return returnValue;
    }
    

    /**get the data this transferable transports */
    public Object getTransferData(DataFlavor flavor)
    	throws UnsupportedFlavorException, IOException {
		  Object returnObject;
		  if (flavor.equals(flavors[GENERIC_OBJECT])) {
		      returnObject = data;
		  } else if (flavor.equals(flavors[STRING])) {
		      returnObject = data.toString();
		  } else {
		    throw new UnsupportedFlavorException(flavor);
		  }
		  return returnObject;
    }
    
}
