package com.dlmu.graph;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;


public class MyLink{
	private double weight; 
	private int id;
	private byte type;
	public MyLink(int id, double weight,byte type) { // , double capacity
		this.id = id; // This is defined in the outer class.
		this.type=type;
		this.weight = weight;
		// this.capacity = capacity;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Byte getType() {
		return type;
	}

	public void setType(Byte type) {
		this.type = type;
	}



	public String toString() { // Always good for debugging
		return "E" + id;
	}
}
