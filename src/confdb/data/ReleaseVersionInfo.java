package confdb.data;

public class ReleaseVersionInfo {		
    private Integer cycle = null;
    private Integer major = null;
    private Integer minor = null;
    private Integer prerel = null;
    private Integer patch = null;

    /** parses a release tag CMSSW
     * 
     * format expected CMSSW_<cycle>_<major>_<minor>_<preX/patchY>_<suffix>
     * 
     * cycle,major,minor are all ints except for minor which can be X which is for nightlies
     * 
     * <pre/patch> and <suffix> are both optional and there is no way to know 
     * if the 5th entry is pre/patch version or the suffix without the additional
     * contraint that pre/patch format is pre<number> or patch<number>
     * 
     * anything infront of CMSSW is ignored
     * 
     */
    public ReleaseVersionInfo(String releaseTag) {
        if(!setValues(releaseTag)){
            throw new IllegalArgumentException("release tag "+releaseTag+" is not of format CMSSW_A_B_C where A,B, C are ints (although C can be X) and thus can not be parsed");
        }
    }

    private boolean setValues(String releaseTag){
        String releaseTagTrimmed = releaseTag;
        if(!releaseTag.startsWith("CMSSW")){
            int index = releaseTag.indexOf("CMSSW");
            if(index!=-1){
                releaseTagTrimmed = releaseTag.substring(index);
            }
        }

        String[] splitTag = releaseTagTrimmed.split("_");
        
        if(splitTag.length<4) return false;
        if(!splitTag[0].equals("CMSSW")) return false;

        try {
            cycle = Integer.parseInt(splitTag[1]);
            major = Integer.parseInt(splitTag[2]);
            if (splitTag[3].equals("X")){ //for integration builds
                minor = Integer.MAX_VALUE;
            }else{
                minor = Integer.parseInt(splitTag[3]);
            }
        } catch (NumberFormatException ex) {            
            return false;            
        }

        if(splitTag.length>4){            
            if(splitTag[4].startsWith("pre")){
                try{                    
                    prerel = Integer.parseInt(splitTag[4].substring(3));
                } catch(NumberFormatException ex){

                }
            }
            if(splitTag[4].startsWith("patch")){
                try{
                    patch = Integer.parseInt(splitTag[4].substring(5));
                } catch(NumberFormatException ex){
                    
                }
            }
        }
        System.err.print("parsed "+releaseTag);
        System.err.println("  "+cycle+" "+major+" "+minor+" pre "+prerel+" patch "+patch);
        return true;

    }
    public Integer cycle(){return cycle;}
    public Integer major(){return major;}
    public Integer minor(){return minor;} 
    public Integer patch(){return patch;} 
    public Integer prerel(){return prerel;} 

} 