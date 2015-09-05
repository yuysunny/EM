/*
 * Created on Jul 6, 2007
 *
 * Copyright (c) 2007, the JUNG Project and the Regents of the University 
 * of California
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * http://jung.sourceforge.net/license.txt for a description.
 */
package com.dlmu.algorithm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.collections15.Transformer;

import com.dlmu.graph.MyLink;
import com.dlmu.graph.MyNode;
import com.dlmu.graph.MyGraph;

import edu.uci.ics.jung.algorithms.scoring.AbstractIterativeScorerWithPriors;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.algorithms.scoring.util.UniformDegreeWeight;
import edu.uci.ics.jung.graph.Hypergraph;

/**
 * A generalization of PageRank that permits non-uniformly-distributed random jumps.
 * The 'vertex_priors' (that is, prior probabilities for each vertex) may be
 * thought of as the fraction of the total 'potential' that is assigned to that 
 * vertex at each step out of the portion that is assigned according
 * to random jumps (this portion is specified by 'alpha').
 * 
 * @see "Algorithms for Estimating Relative Importance in Graphs by Scott White and Padhraic Smyth, 2003"
 * @see PageRank
 */
public class PageRankWithPriorsAndType<V, E>  extends PageRankWithPriors<V, E>
{
    public int iteration;
    public double contribution_factor;
	/**
     * Creates an instance with the specified graph, edge weights, vertex priors, and 
     * 'random jump' probability (alpha).
     * @param graph the input graph
     * @param edge_weights the edge weights, denoting transition probabilities from source to destination
     * @param vertex_priors the prior probabilities for each vertex
     * @param alpha the probability of executing a 'random jump' at each step
     */
    public PageRankWithPriorsAndType(Hypergraph<V,E> graph, 
    		Transformer<E, ? extends Number> edge_weights, 
            Transformer<V, Double> vertex_priors, double alpha,int iteration,double contribution_factor)
    {
        super(graph, edge_weights, vertex_priors, alpha);
        this.iteration=iteration;
        this.contribution_factor=contribution_factor;
    }
    
    /**
     * Updates the value for this vertex.  Called by <code>step()</code>.
     * this graph will never be a hypergraph
     * In this method, it will compute two value:
     * (1) the pagerank score of node v: PR(v)=PR(w)*Weight(e)*Usefulness(e.type)
     * (2) the contribution vector of node v
     * 
     */
    @Override
    public double update(V v)
    {
    	
    	int current_index=this.iteration%2; // current_index is the dimension to be updated (time t+1)
		int previous_index=(this.iteration+1)%2;//  previous_index is dimension used for calculation  (time t)
		double[][] contributionV=((MyNode) v).getContribution();
		double sum=0d;
		for(int i=0;i<contributionV.length;i++)
		{
			contributionV[i][current_index]=0d;
		}
        double v_input = 0;
        for (E e : graph.getInEdges(v))
        {
        	for (V w : graph.getIncidentVertices(e)) 
        	{
        		if (!w.equals(v) ) { // w is the other node of link e
        			
        				byte type=((MyLink) e).getType(); // get the type of edge e
        				// calculate the page rank score of node v
        				v_input += getCurrentValue(w) * 
            					getEdgeWeight(w,e).doubleValue()*((MyGraph) graph).getAllPu()[Integer.valueOf(type)];// calculate the node score
        				// calculate the contribution of node v 
        				
        				double[][] contributionW=((MyNode) w).getContribution();
        				//get the contribution of node w, and add it to contribution to node v
        				for(int i=0;i<contributionW.length;i++) // i is the edgeType position in contribution vector
        				{
        					Double temp=0d;
        					if(i==Integer.valueOf(type))
        						temp=getCurrentValue(w)*(contribution_factor*contributionW[i][previous_index]+(1-contribution_factor) * getEdgeWeight(w,e).doubleValue());
//        						temp=(contribution_factor*contributionW[i][previous_index]+(1-contribution_factor) * getCurrentValue(w)*getEdgeWeight(w,e).doubleValue());

        					else
        						temp=getCurrentValue(w)*contribution_factor*contributionW[i][previous_index];
        					Double newTemp=contributionV[i][current_index];
        					newTemp+=temp;
        					contributionV[i][current_index]= newTemp; //update each edgeType value in current_index vector
        					sum+=temp; // for normalization
        				}
//        				System.out.println("w is "+((MyNode)w).getType()+" "+((MyNode)w).getId()+" "+getEdgeWeight(w,e).doubleValue());
        			}
        	}
        }
    	
        //normalize the contribution
        for(int i=0;i<contributionV.length;i++)
		{
			contributionV[i][current_index]=contributionV[i][current_index]/sum;
		}
        ((MyNode) v).setContribution(contributionV); //update the contribution vector for node v
        double new_value = alpha > 0 ? 
        		v_input * (1 - alpha) + ((MyNode)v).getNode_prior()* alpha :
        		v_input;
        
        
        DecimalFormat df = new DecimalFormat("#,##0.0000000000");
//        System.out.println(new_value+"   "+df.format(new_value));
        setOutputValue(v, Double.valueOf(df.format(new_value)));
        ((MyNode) v).setPrior(new_value); //update the prior for node v in E-step
        return Math.abs(getCurrentValue(v) - new_value);
    }

    
   
}
