package confdb.data;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * ModifierInstructions
 * --------------------
 * @author Philipp Schieferdecker
 *
 * Instructions for the ConfigurationModifier how to filter/manipulate its
 * master configuration. 
 */
public class ModifierInstructions
{
    //
    // member data
    //
    
    /** indicate if any instructions were applied at all */
    private boolean isModified = false;

    /** global PSets */
    private boolean filterAllPSets = false;
    private ArrayList<String> psetBlackList = new ArrayList<String>();
    private ArrayList<String> psetWhiteList = new ArrayList<String>();
    
    /** EDSources */
    private boolean filterAllEDSources = false;
    private ArrayList<String> edsourceBlackList = new ArrayList<String>();
    private ArrayList<String> edsourceWhiteList = new ArrayList<String>();

    /** ESSources */
    private boolean filterAllESSources = false;
    private ArrayList<String> essourceBlackList = new ArrayList<String>();
    private ArrayList<String> essourceWhiteList = new ArrayList<String>();

    /** ESModules */
    private boolean filterAllESModules = false;
    private ArrayList<String> esmoduleBlackList = new ArrayList<String>();
    private ArrayList<String> esmoduleWhiteList = new ArrayList<String>();
    
    /** Services */
    private boolean filterAllServices = false;
    private ArrayList<String> serviceBlackList = new ArrayList<String>();
    private ArrayList<String> serviceWhiteList = new ArrayList<String>();
    
    /** Paths */
    private boolean filterAllPaths = false;
    private ArrayList<String> pathBlackList = new ArrayList<String>();
    private ArrayList<String> pathWhiteList = new ArrayList<String>();

    /** sequences requested regardless of being referenced in requested paths */
    private ArrayList<String> requestedSequences = new ArrayList<String>();

    /** modules reqested regardless of being referenced in requested path */
    private ArrayList<String> requestedModules = new ArrayList<String>();
    
    /** name of edsource template to be substituted */
    private String edsourceName = "";
    
    /** list of file names, in case edsource is a PoolSource */
    private ArrayList<String> poolSourceInput = new ArrayList<String>();

    /** */
    
    
    //
    // construction
    //

    /** standard constructor */
    public ModifierInstructions()
    {

    }
    

    //
    // member functions
    //

}
