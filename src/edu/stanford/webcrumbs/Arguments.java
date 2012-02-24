package edu.stanford.webcrumbs;

/*
 * Parses the arguments
 *  The arguments that can be given to the program are:
 * -d : Datafile from which to load
 * -t : Type of the DataFile (har/fourthparty/fourthpartydatafile/custom class)
 * -i : Location of ignoreFile
 * -o : Output File location
 * -d2 : Location of second data File
 * -rn : Ranker to use for nodes (class name)
 * -w : Websites to visualize 
 * -f : filter websites (defaults to true) (can take values 'true' / 'false')
 * -v : Visualization option that can depends on the parser. 
 * 		For example the har parser, can take either 'domain' or 'url' as an option. 
 * -convert : To convert to JUNG. Some rankers require this
 * -<custom option> : for all custom options required
 * 
 * Author : Subodh Iyengar
 * 
 */

import java.util.HashMap;
import java.util.Map;

import edu.stanford.webcrumbs.ranker.NodeRanker;

public class Arguments {
	private static String file;
	private static String type;
	private static String ignore;
	private static String outputFile;
	private static String dataFile2;
	private static String nodeRanker;
	private static String[] websites;
	private static boolean filter = true;
	private static String vis = "domain";
	
	private static NodeRanker rankerInstance;
	
	static final String customOptions[] = {"-convert", "-rankerFile"}; 
	
	private static Map<String, String> argumentMap = 
		new HashMap<String, String>();
	
	public static boolean hasArg(String key){
		if (argumentMap.containsKey(key)){
			return true;
		}
		return false;
	}
	
	public static String getVis(){
		return vis;
	}
	
	public static String getFile(){
		return file;
	}
	
	public static String getType(){
		return type;
	}
	
	public static String getIgnore(){
		return ignore;
	}
	
	public static String getOutputFile(){
		return outputFile;
	}
	
	public static String getDataFile(){
		return dataFile2;
	}
	
	
	public static String[] getWebsites(){
		return websites;
	}
	
	public static boolean hasFile(){
		if (file != null){
			return true;
		}
		return false;
	}
	
	public static boolean hasType(){
		if (type != null){
			return true;
		}
		return false;
	}
	
	public static boolean hasIgnoreFile(){
		if (ignore != null){
			return true;
		}
		return false;
	}
	
	public static boolean hasData2(){
		if (dataFile2 != null){
			return true;
		}
		return false;
	}
	
	public static boolean hasOutputFile(){
		if (outputFile != null){
			return true;
		}
		return false;
	}
	
	public static boolean hasNodeRanker(){
		if (nodeRanker != null){
			return true;
		}
		return false;
	}
	
	
	public static boolean hasWebsites(){
		if (websites != null){
			return true;
		}
		return false;
	}
	
	public static NodeRanker getNodeRanker(){
		return rankerInstance;
	}
	
	
	public static boolean getFilter(){
		return filter;
	}
	
	public static String getArg(String key){
		return argumentMap.get(key);
	}
	
	private static void instantiateNodeRanker(String nodeRanker) 
				throws InstantiationException, IllegalAccessException, 
				ClassNotFoundException{
		Class nodeClass = Class.forName(nodeRanker);
		rankerInstance = (NodeRanker) nodeClass.newInstance();
	}

	// pass null to not set the arg
	public static void set(String file, String type, 
					  String ignore, String outputFile,
					  String dataFile2, String ranker,
					  String[] websites,
					  String rankerFile,
					  String convert) throws InstantiationException, 
					  IllegalAccessException, ClassNotFoundException {
		Arguments.file = file;
		Arguments.type = type;
		Arguments.ignore = ignore;
		Arguments.outputFile = outputFile;
		Arguments.dataFile2 = dataFile2;
		Arguments.nodeRanker = ranker;
		if (nodeRanker != null){
			instantiateNodeRanker(nodeRanker);
		}
		
		Arguments.websites = websites;
		if (rankerFile != null)
			argumentMap.put("-rankerfile", rankerFile);
		if (convert != null)
			argumentMap.put("-convert", convert);
	}
	
	
	public static void parse(String[] args) throws Exception{		
		for (int i = 0; i < args.length; i++){
			String arg = args[i];
			if (arg.equals("-d")){
				if (i + 1 < args.length){
					file = args[++i];
				}
				else
					throw new Exception("file arg not specified");	
			}
			else if (arg.equals("-t")){
				if (i + 1 < args.length){
					type = args[++i];
				}
				else
					throw new Exception("type arg not specified");
			}
			else if (arg.equals("-i")){
				if (i + 1 < args.length){
					ignore = args[++i];
				}
				else
					throw new Exception("ignore arg not specified");
			}
			else if (arg.equals("-o")){
				if (i + 1 < args.length){
					outputFile = args[++i];
				}
				else
					throw new Exception("outputfile arg not specified");
			}
			else if (arg.equals("-d2")){
				if (i + 1 < args.length){
					dataFile2 = args[++i];
				}
				else
					throw new Exception("second datafile arg not specified");
			}
			else if (arg.equals("-rn")){
				if (i + 1 < args.length){
					nodeRanker = args[++i];
					instantiateNodeRanker(nodeRanker);
				}
				else
					throw new Exception("node ranker string not specified");
			}
			else if (arg.equals("-w")){
				if (i + 1 < args.length){
					websites = args[++i].split(",");
					// sanity check for form
					for (String site : websites){
						if (site.equals("")){
							throw new Exception("blank website specified in string");
						}
					}
				}
				else
					throw new Exception("website string not specified");
			}
			else if (arg.equals("-f")){
				if (i + 1 < args.length){
					filter = new Boolean(args[++i]);
				}else{
					throw new Exception("filter string not specified");
				}
			}
			else if (arg.equals("-v")){
				if (i + 1 < args.length){
					vis = args[++i];
				}else{
					throw new Exception("vis string not specified");
				}
			}
			else {
				String propName = arg;
				if (i + 1 < args.length){
					String value = args[++i];
					argumentMap.put(propName, value);
				}else{
					throw new Exception("Invalid value for property " + propName);
				}
			}
		}
	}
	
}
