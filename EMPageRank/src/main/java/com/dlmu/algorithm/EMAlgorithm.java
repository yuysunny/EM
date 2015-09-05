package com.dlmu.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import com.dlmu.graph.MyGraph;
import com.dlmu.graph.MyLink;
import com.dlmu.graph.MyNode;

public class EMAlgorithm {
	private MyGraph graph;
	private static double convergency = 1d;
	protected static Logger log = Logger.getLogger(EMAlgorithm.class.getName());
	private static DijkstraDistance<MyNode, MyLink> alg;
	private static ArrayList<String> feedbacks = new ArrayList<String>();
	private static ArrayList<Entry<String, Double>> sortedLists; // ranked node after pagerank algorithm
	private static BufferedWriter writer;

	public EMAlgorithm(MyGraph g) {
		this.graph = g;
		
		// TODO Auto-generated constructor stub
	}

	public void mainMethod() throws Exception {
		loadFeedbacks(JsonUtil.feedback_file);

		iterations();
	}

	/**
	 * load the given feedback nodes
	 * 
	 * @param path_loading_name
	 *            : nodeID
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> loadFeedbacks(String path_loading_name)
			throws Exception {
		BufferedReader bin = null;
		log.debug("EM-loadingFeedbacks: starting to load feedbacks from "
				+ path_loading_name);
		int i = 0;
		try {
			String readline;
			bin = new BufferedReader(new FileReader(path_loading_name));
			while ((readline = bin.readLine()) != null) { // loop through all of
				i++;
				feedbacks.add(readline);
			}
		} catch (Exception e) {
			throw new Exception(
					"EM-loadingFeedbacks: an exception was encountered loading the "
							+ i + " line record from the file: "
							+ path_loading_name + ".  Exception: "
							+ e.getMessage(), e);
		} finally {
			log.info("EM-loadingFeedbacks: there are " + i + " feedback nodes");
			try {
				bin.close();
			} catch (Exception ex) {
			}
		}

		return feedbacks;
	}

	/**
	 * 
	 * @param Ni
	 *            : node id
	 * @return the usefulness probability of node Ni via sigmoid function
	 */
	public Double sigmoid(String Ni) {
		double dNi = 0;
		double dp = 0;
//		long startTime=System.currentTimeMillis();
		MyNode source = graph.getNode(Ni);
		for (String Nj : feedbacks) {
			MyNode target = graph.getNode(Nj);
			dp += shortLength(source, target);
		}
		dNi = dp / feedbacks.size();
//		log.info("EM-mStep: time of sigmoid for node "+Ni+" : "
//				+ Double.valueOf((System.currentTimeMillis() - startTime))/1000 + "s");
		// System.out.println(Ni+":"+2 / (1 + (Math.pow(Math.E, 1.0 * dNi))));
		return 2 / (1 + (Math.pow(Math.E, 1.0 * dNi)));
	}

	/**
	 * 
	 * @param source
	 *            : node id
	 * @param target
	 *            : node id
	 * @return
	 */
	public int shortLength(MyNode source, MyNode target) {
//		 System.out.println(source.getId()+","+target.getId());
		alg = new DijkstraDistance(graph);
		alg.setMaxDistance(JsonUtil.search_depth);
		 if(alg.getDistance(source, target)==null)
		 {
			 return JsonUtil.search_depth;
		 }
		 else{
			 return alg.getDistance(source, target).intValue();
		 }
	}

	public void iterations() {
		try {
			writer = new BufferedWriter(new FileWriter(
					JsonUtil.output_edgeType_file));
			String head = "iteration,";
			for (int i = 0; i < graph.getAllPu().length; i++) {
				head += graph.getAllTypePosToName().get((byte) i) + ",";
			}
			writer.write(head.substring(0, head.length() - 1));
			writer.newLine();
			writer.flush();

			int i = 0;
			while (i < JsonUtil.max_iteration
					&& convergency > JsonUtil.convergency_threshold) {
				log.debug("EM_iteration: now the iteration is " + i);
				convergency = 0;
				Estep(i);
				Mstep(i);
				outputEdgeType(i);
				i++;
//				System.out.println(i);
//				if(i>=3)
//					break;
			}
			writer.close();
			outputNodeScore();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param i
	 *            : the iteration number
	 */
	public void Estep(int iteration) {
		pageRank(iteration);
	}

	/**
	 * @param i
	 *            : the iteration number
	 */
	public void Mstep(int iteration) {
		log.info("EM-mStep: start ranking node and getting the top " + JsonUtil.topRankedNode
				+ " node for updating Pu");
		long startTime = System.currentTimeMillis();
		HashMap<String, Double> topNodes = rankNode(JsonUtil.topRankedNode,JsonUtil.task_node);
		log.info("EM-mStep: time of ranking node is " + Double.valueOf((System.currentTimeMillis() - startTime))/1000 + "s");
		log.info("EM-mStep: start calculating the node usefulness " );
		startTime = System.currentTimeMillis();
		calculateNodeUsefulness(topNodes);
		log.info("EM-mStep: time of calculating the node usefulness " + Double.valueOf((System.currentTimeMillis() - startTime))/1000 + "s");
		startTime = System.currentTimeMillis();
		updatePu(topNodes, iteration);
		log.info("EM-mStep: time of m-step : "
				+ Double.valueOf((System.currentTimeMillis() - startTime))/1000 + "s");

	}

	public void outputNodeScore() {
		// output node pageRank score
		try {
			writer = new BufferedWriter(new FileWriter(JsonUtil.output_node_file));
			String head = "rank,nodeID,nodeType,score";
			writer.write(head);
			writer.newLine();
			writer.flush();
			String value = "";
			rankNode(0,"");
			for (int i = 0; i < sortedLists.size(); i++) {
				String[] strs = sortedLists.get(i).toString().split("=");
				writer.write((i + 1) + "," + strs[0] + ","
						+ graph.getNode(strs[0]).getType() + ","
						+ Double.valueOf(strs[1]));
				writer.newLine();
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param iteration
	 *            : the iteration number
	 */
	public void outputEdgeType(int iteration) {
		// output edge type usefulness probability
		try {

			String value = "";
			for (int i = 0; i < graph.getAllPu().length; i++) {
				value += graph.getAllPu()[i] + ",";
			}
			writer.write((iteration + 1) + ","
					+ value.substring(0, value.length() - 1));
			writer.newLine();
			writer.flush();
			// log.info(value.substring(0,value.length()-1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param topNodes
	 */
	public void calculateNodeUsefulness(HashMap<String, Double> topNodes) {

		for (Entry<String, Double> e : topNodes.entrySet()) {
			if (graph.getNode(e.getKey()).getUsefulness() <= 0) // so that we don't need to calculate it every time
			{
				double usefulness = sigmoid(e.getKey());
				graph.getNode(e.getKey()).setUsefulness(usefulness);
//				String contributionVector = "";
//				for (int k = 0; k < graph.getNode(e.getKey()).getContribution().length; k++) {
//					contributionVector += graph.getNode(e.getKey()).getContribution()[k][0] + ",";
//				}
//				log.info("Feedback-node usefulness: node " + e.getKey() + " :"
//						+ usefulness+", contribution vector is: "+contributionVector);
			}
		}
	}

	/**
	 * 
	 * @param topK
	 * @return <nodeID,node_prior>
	 */
	public HashMap<String, Double> rankNode(int topK,String nodeType) {
		HashMap<String, Double> topNodes = new HashMap<String, Double>();
		HashMap<String, Double> list = new HashMap<String, Double>();
		for (MyNode vertex : graph.getVertices()) {
			if(!nodeType.equals("")) { // if nodeType!="", we just rank the nodes belong to the "nodeType"
				if (vertex.getType().equals(nodeType)) //  the topK nodes should belong to the task node type
					list.put(vertex.getId(), vertex.getNode_pr_score());
			}
			else
				list.put(vertex.getId(), vertex.getNode_pr_score()); // rank all the nodes in the graph
		}
		sortedLists = null;
		sortedLists=new ArrayList<Entry<String, Double>>(list.entrySet());
		Collections.sort(sortedLists, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
//		int count=1;
//		for (int i = 0; i < sortedLists.size(); i++) {
//			if(count<=topK)
//			{
//				String[] strs = sortedLists.get(i).toString().split("=");
//				if(graph.getNode(strs[0]).getType().equals(JsonUtil.task_node)) 
//				{
//					topNodes.put(strs[0], Double.valueOf(strs[1]));
//					count++;
//				}
//				
//			}
//			else
//				break;
//		}
		if (sortedLists.size() < topK)  // the topK nodes from all nodes
			topK = sortedLists.size();
		for (int i = 0; i < topK; i++) {
			String[] strs = sortedLists.get(i).toString().split("=");
			topNodes.put(strs[0], Double.valueOf(strs[1]));
		}

		return topNodes;
	}

	/**
	 * Based on the top ranked nodes, we use this method to update the
	 * usefulness probability of edge type
	 * 
	 * @param topNodes
	 * @return
	 */
	public void updatePu(HashMap<String, Double> topNodes, int iteration) {
		Double[] tempPu = new Double[graph.getAllPu().length];
		int index = iteration % 2;
		double sum = 0d;
		// for each type
		for (int i = 0; i < graph.getAllPu().length; i++) {
			Double pu = 0d;
			for (Entry<String, Double> e : topNodes.entrySet()) {
				MyNode ni = graph.getNode(e.getKey());
				pu += ni.getUsefulness() * ni.getContribution()[i][index];
			}
			tempPu[i] = pu;
			sum += pu;
		}
		// log.info("Normalization -usfulness of edge type :"+ sum);
		// normalization
		for (int i = 0; i < tempPu.length; i++) {
			// log.info("The new usfulness of edge type :"+ i+" is "+tempPu[i]);
			tempPu[i] = tempPu[i] / sum;
//			convergency+=Math.abs(graph.getAllPu()[i]-tempPu[i]);
		}
		graph.setAllPu(tempPu);
	}

	/**
	 * 
	 * @param i
	 *            , current iteration
	 */
	private void pageRank(int i) {
		log.info("EM-eStep: page rank start");
		long startTime = System.currentTimeMillis();
		// Pagerank with priors
		// Node Priors
		
		PageRankWithPriorsAndType ranker_priors = new PageRankWithPriorsAndType(
				graph, edge_priors, vertex_priors, (1 - JsonUtil.damping_factor),
				i, JsonUtil.contribution_factor);
		ranker_priors.step();
		// next normalize the pagerank score
		log.info("EM-eStep: normalize the pagerank score");
		double sum = 0d;
		for (MyNode vertex : graph.getVertices()) {
			double result = Double.valueOf(ranker_priors.getVertexScore(vertex)
					.toString());
			sum += result;

		}
		// log.info("pagerank normalization: "+sum);
		for (MyNode vertex : graph.getVertices()) {
			double result = Double.valueOf(ranker_priors.getVertexScore(vertex)
					.toString());
			double previous_score = vertex.getNode_pr_score();
			vertex.setNode_pr_score(result / sum);
			convergency += Math.abs(vertex.getNode_pr_score() - previous_score);
//			 log.info("The pagerank score of " + vertex + " is "+result
//			 +"; after normalization is "+sum+"....."+vertex.getNode_pr_score() );
			String contributionVector = "";
			for (int k = 0; k < vertex.getContribution().length; k++) {
				contributionVector += vertex.getContribution()[k][i % 2] + ",";
			}
//			 log.info("The contribution of "+vertex+" is "+contributionVector);
		}
		log.info("EM-eStep: time of e-step: "
				+ (System.currentTimeMillis() - startTime)/1000 + "s");
	}
	Transformer<MyNode, Double> vertex_priors = new Transformer<MyNode, Double>() {
		public Double transform(MyNode node) {
			return node.getNode_pr_score(); // not prior but pr_score
		}
	};
	// Edge Priors
	Transformer<MyLink, Double> edge_priors = new Transformer<MyLink, Double>() {
		public Double transform(MyLink edge) {
			return edge.getWeight();
		}
	};
}
