package com.dlmu.graph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.dlmu.algorithm.EMAlgorithm;
import com.dlmu.algorithm.JsonUtil;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class MyGraph extends SparseMultigraph<MyNode,MyLink> {

	private static HashMap<String, MyNode> allNodes;
	/*
	 * the usefulness probability of each edge type
	 */
	private static Double[] allPu;
	/*
	 * edgeTypeName,position in array (initial value is the line number in the typeFile)
	 */
	private static HashMap<String, Byte> allTypeNameToPos;
	/*
	 * position in array (initial value is the line number in the typeFile),edgeTypeName
	 */
	private static HashMap<Byte, String> allTypePosToName;
	protected static Logger log = Logger.getLogger(MyGraph.class.getName());
	DecimalFormat df = new DecimalFormat("#,##0.0000000000");
	public MyGraph(String nodeFileName, String edgeFileName,String typeFileName) {
		super();
		try {
			allNodes = new HashMap<String, MyNode>();
			log.debug("BuildGraph: starting to buildGraph " );
			loadTypes(typeFileName.trim());
			loadNodes(nodeFileName.trim());
			loadEdges(edgeFileName.trim());
			return;
		} catch (Exception e) {
//			log.error("Graph-constructor: An exception occurred and the graph was not initialized.");
		}
	}

	public void clear() {
		try {
			allNodes.clear();
		} catch (Exception ex) {
		}
	}

	public MyNode getNode(String nodeId) {
		MyNode myNode = allNodes.get(nodeId);
		return (myNode);
	}
	private void loadTypes(String path_loading_name) throws Exception {
		BufferedReader bin = null;
		int i = 0;
		allTypeNameToPos=new HashMap<String,Byte>();
		allTypePosToName=new HashMap<Byte,String>();
		log.debug("BuildGraph-loadingEdgeTypes: starting to load edge's types from " +  
		path_loading_name);

		try {
			String readline;
			bin = new BufferedReader(new FileReader(path_loading_name));
			while ((readline = bin.readLine()) != null) { // loop through all of
				allTypeNameToPos.put(readline, (byte) i);	
				allTypePosToName.put( (byte) i,readline);
				i++; 
			}
			allPu=new Double[i];
			// initially, all value are equal
			for(int j=0;j<i;j++){
				allPu[j]=1.0/Double.valueOf(i);

			}
			log.debug("BuildGraph-loadingEdgeTypes: there are "+allPu.length+" types in graph");
		} catch (Exception e) {
			throw new Exception("An exception occurred when loading edge's types in line " + i
					+ " from the file at " + path_loading_name
					+ ". Exception = " + e.getMessage(), e);
		} finally {
			try {
				bin.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * 
	 * @param path_loading_name
	 *            format: id,type if no type, all equals ""
	 * @throws Exception
	 */

	private void loadNodes(String path_loading_name) throws Exception {
		BufferedReader bin = null;
		int i = 0;

		log.debug("BuildGraph-loadingNodes: starting to load nodes from "
				+ path_loading_name);
		long startTime = System.currentTimeMillis();
		try {
			bin = new BufferedReader(new FileReader(
					path_loading_name));
			String readline;
			while ((readline = bin.readLine()) != null) { // loop through all of
															// the nodes records
				// id, type, prior
				if (!readline.contains("id")) { // first line contains the
												// heading
					i++; // count of nodes added
					String strs[] = readline.split(",");

					if (strs.length >=2) {
						MyNode node =  new MyNode(strs[0], strs[1], Double.valueOf(1)/JsonUtil.all_node_num);
						node.setNode_pr_score(Double.valueOf(1)/JsonUtil.all_node_num);
						double[][] contribution=new double[allPu.length][2];
						// the initial value of contribution equals Pu (all edge type are equal)
						for(int j=0;j<contribution.length;j++)
						{
							contribution[j][0]=allPu[j];  // 0 for even iteration's current value
							contribution[j][1]=allPu[j];  // 1 for odd iteration's current value
						}
						node.setContribution(contribution);
						node.setUsefulness(0d);
						allNodes.put(strs[0], node);
						this.addVertex(node);
					}
				}
			}
			return;
		} catch (Exception e) {
			throw new Exception("An exception occurred when loading nodes in line " + i
					+ " from the nodes file at " + path_loading_name
					+ ". Exception = " + e.getMessage(), e);
		} finally {
			log.info("BuildGraph-loadingNodes: time to load " + i + " nodes = "
					+ (System.currentTimeMillis() - startTime)/1000 + "s");
			try {
				bin.close();
			} catch (Exception ex) {
			}
		}
	}

	public void loadNodesScore(String path_loading_name) throws Exception {
		BufferedReader bin = null;
		String readline = null;
		log.debug("BuildGraph-loadingNodesPrior: starting to load nodes prior from "
				+ path_loading_name);
		try {
			// clear the node values
			for (MyNode node : allNodes.values())
				node.reset();

			bin = new BufferedReader(new InputStreamReader(new FileInputStream(
					path_loading_name)));
			while ((readline = bin.readLine()) != null) {
				String strs[] = readline.split(",");
				Double score = Double.valueOf(strs[1]);
				MyNode n = getNode(strs[0]);
				if (n != null) {
					n.setPrior(score);
				} else {
					log.error("BuildGraph-loadingNodesPrior: in the file "
							+ path_loading_name
							+ " there was a record for the node "
							+ strs[0]
							+ " but that node was not in the graph.");
				}

			}
			bin.close();
			return;
		} catch (Exception e) {
			throw new Exception("An exception occurred in load prior for "
					+ path_loading_name + ". Exception: " + e.getMessage(), e);
		} finally {
			try {
				bin.close();
			} catch (Exception ex) {
			}
		}
	} 

	/**
	 * 
	 * @param path_loading_name
	 *            startid,endid,type,weight
	 * @throws Exception
	 */
	private void loadEdges(String path_loading_name) throws Exception {
		BufferedReader bin = null;
		int i = 0;

		log.debug("BuildGraph-loadingEdges: starting to load edges from "
				+ path_loading_name);
		long startTime = System.currentTimeMillis();
		try {
			bin = new BufferedReader(new InputStreamReader(new FileInputStream(
					path_loading_name)));
			String readline;

			while ((readline = bin.readLine()) != null) {
				if (!readline.contains("id")) { // first line contains the
												// heading
					i++; // record count of edge lines being processed
					String strs[] = readline.split(",");
					
					MyNode beginNode = getNode(strs[0]);
					MyNode endNode = getNode(strs[1]);
					if (beginNode != null && endNode != null) {
						if (strs.length == 4) {
							
							MyLink edge = new MyLink(i,
									Double.valueOf(df.format(Double.valueOf(strs[3]))), allTypeNameToPos.get(strs[2])); // id, weight, typeID
							this.addEdge(edge, beginNode, endNode,
									EdgeType.DIRECTED);
						} else {
							log.error("PageRank-loadingEdges: an edge was encountered with no type or weight"
									+ " at line = "
									+ i
									+ " in edge file: "
									+ path_loading_name);
						}
					} else {
						log.error("BuildGraph-loadingEdges: an edge was encountered with an invalid node."
								+ " begin node = "
								+ beginNode
								+ ", end node = " + endNode);
					}
				}
			}
//			System.out.println("edges:"+i);
			bin.close();
		} catch (Exception e) {
			throw new Exception(
					"BuildGraph-loadingEdges: an exception was encountered loading the "
							+ i + " edge record from the file: "
							+ path_loading_name + ".  Exception: "
							+ e.getMessage(), e);
		} finally {
			log.info("BuildGraph-loadingEdges: time to load " + i
					+ " edges = " + (System.currentTimeMillis() - startTime)/1000
					+ "s");
			try {
				bin.close();
			} catch (Exception ex) {
			}
		}
	} // end of loadEdges

	public HashMap<String, MyNode> getAllNodes() {
		return allNodes;
	}

	public void setAllNodes(HashMap<String, MyNode> allNodes) {
		this.allNodes = allNodes;
	}

	public static Double[] getAllPu() {
		return allPu;
	}

	public static void setAllPu(Double[] allPu) {
		MyGraph.allPu = allPu;
	}

	public static HashMap<String, Byte> getAllTypeNameToPos() {
		return allTypeNameToPos;
	}

	public static void setAllTypeNameToPos(HashMap<String, Byte> allTypeNameToPos) {
		MyGraph.allTypeNameToPos = allTypeNameToPos;
	}

	public static HashMap<Byte, String> getAllTypePosToName() {
		return allTypePosToName;
	}

	public static void setAllTypePosToName(HashMap<Byte, String> allTypePosToName) {
		MyGraph.allTypePosToName = allTypePosToName;
	}

	


}
