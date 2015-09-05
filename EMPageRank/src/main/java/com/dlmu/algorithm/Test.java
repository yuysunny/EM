package com.dlmu.algorithm;


import java.text.DecimalFormat;

import org.apache.log4j.PropertyConfigurator;

import com.dlmu.graph.MyGraph;


public class Test {
	public static void main(String[] args) throws Exception
	{
        
		JsonUtil util=new JsonUtil(args[0]);
		PropertyConfigurator.configure(util.log_file);	
		MyGraph graph =new MyGraph(util.node_file,util.edge_file,util.edgeType_file);
		EMAlgorithm em=new EMAlgorithm(graph);
		em.mainMethod();
		// RService r=new RService();
		// r.plot("/Users/yyy/Desktop/edge_type_score");
	}



}
