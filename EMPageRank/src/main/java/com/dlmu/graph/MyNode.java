package com.dlmu.graph;

import java.io.Serializable;
import java.util.HashMap;


public class MyNode  {
	private String id; // good coding practice would have this as private
	private double node_prior;
	private double node_pr_score;
	private String type;
	private double usefulness;
	private double[][] contribution;
	
	public double getNode_pr_score() {
		return node_pr_score;
	}

	public void setNode_pr_score(double node_pr_score) {
		this.node_pr_score = node_pr_score;
	}


	public double getNode_prior() {
		return node_prior;
	}

	public double[][] getContribution() {
		return contribution;
	}

	public void setContribution(double[][] contribution) {
		this.contribution = contribution;
	}

	public void setNode_prior(double node_prior) {
		this.node_prior = node_prior;
	}


	public MyNode(String id,String type, double node_prior) {
		this.id = id;
		this.type=type;
		this.node_prior = node_prior;
		
	}
	
	public MyNode(String id) {
		this.id = id;
		node_prior = 0.0;
	}
	
	public void reset() {
		node_prior = 0.0;
	} //end of reset
	public String toString() { // Always a good idea for debuging
		return id; // JUNG2 makes good use of these.
	}
	public void setPrior(double prior){
		this.node_prior=prior;
	}
	public double getPrior() {
		return(node_prior);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


	public double getUsefulness() {
		return usefulness;
	}

	public void setUsefulness(double usefulness) {
		this.usefulness = usefulness;
	}
	
}