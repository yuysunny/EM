package com.dlmu.algorithm;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonUtil {
	public static double damping_factor; // used in the PageRank calculation 
	public static double contribution_factor; // for contribution vector calculation 
	public static int thread_num;
	public static int all_node_num;
	public static int topRankedNode; // the topK ranking nodes after each E-step
	public static int search_depth; // the maximum length of shortDistance
	public static int max_iteration;  // maximum iteration
	public static double convergency_threshold; // when to stop iteration
	public static String feedback_file; 
	public static String node_file; 
	public static String edge_file;
	public static String edgeType_file;
	public static String output_node_file;
	public static String output_edgeType_file;
	public static String log_file;
	public static String plot_file;
	public static String task_node;
	
	
	public JsonUtil(String file) {
		JSONParser parser = new JSONParser();
		 
        try {
        	System.out.println(file);
            Object obj = parser.parse(new FileReader(file));
            JSONObject jsonObject = (JSONObject) obj;
            damping_factor=Double.valueOf(jsonObject.get("damping_factor").toString());
            contribution_factor=Double.valueOf(jsonObject.get("contribution_factor").toString());
            thread_num=Integer.valueOf(jsonObject.get("thread_num").toString());
            all_node_num=Integer.valueOf(jsonObject.get("all_node_num").toString());
            topRankedNode=Integer.valueOf(jsonObject.get("topRankedNode").toString());
            search_depth=Integer.valueOf(jsonObject.get("search_depth").toString());
            max_iteration=Integer.valueOf(jsonObject.get("max_iteration").toString());
            convergency_threshold=Double.valueOf(jsonObject.get("convergency_threshold").toString());

            feedback_file=jsonObject.get("feedback_file").toString();
            node_file=jsonObject.get("node_file").toString();
            edge_file=jsonObject.get("edge_file").toString();
            edgeType_file=jsonObject.get("edgeType_file").toString();
            output_node_file=jsonObject.get("output_node_file").toString();
            output_edgeType_file=jsonObject.get("output_edgeType_file").toString();
            log_file=jsonObject.get("log_file").toString();
            plot_file=jsonObject.get("plot_file").toString();
            task_node=jsonObject.get("task_node").toString();

            
            
//            JSONArray pathList = (JSONArray) jsonObject.get("meta-paths");
//            List<String> metapaths = new ArrayList<String>();
//            Iterator<JSONObject> iterator = pathList.iterator();
//            while (iterator.hasNext()) {
//            	JSONObject jo=iterator.next();
//            	metapaths.add(jo.get("name")+","+jo.get("value"));
//            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		// TODO Auto-generated constructor stub
	}
	
	

}
