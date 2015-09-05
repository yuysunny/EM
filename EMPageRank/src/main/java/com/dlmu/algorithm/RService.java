package com.dlmu.algorithm;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.REXP;


/**
 * DisambiguationService
 * 
 * @author Puck Treeratpituk
 * @version $Rev$ $Date$
 */
public class RService {
	RConnection c;
	String rf_path= "";
	
	public RService() throws Exception {
		c = new RConnection();
		
		
	}
	public void plot(String file) throws Exception
	{
		c.eval("library(ggplot2)");
		c.eval("library(reshape2)");
		c.eval("require(ggplot2)");
		c.eval("data<-read.csv('"+file+"',sep=',')");
		c.eval("mdf<-melt(data,id.vars='iteration')");
//		c.eval("png(file='/Users/yyy/Desktop/test.png',width=400,height=350,res=72)");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");  
        Date date = new Date();  
        String str = simpleDateFormat.format(date);  
        c.eval("png(file='/Users/yyy/Desktop/acm40_300.png',width=400,height=350,res=72)");

//        c.eval("png(file='"+JsonUtil.plot_file+str+".png',width=400,height=350,res=72)");
		c.parseAndEval("print(ggplot(data=mdf,aes(x=iteration,y=value,group=variable,colour=variable))+geom_line());dev.off()");
		

//        REXP xp = c.parseAndEval("r=readBin('/Users/yyy/Desktop/Yash_GenderVsTotalAccountBalance.png','raw',1024*1024)");
//        c.parseAndEval("unlink('/Users/yyy/Desktop/Yash_GenderVsTotalAccountBalance.jpg'); r");
//        Image img = Toolkit.getDefaultToolkit().createImage(xp.asBytes());
//        System.out.println("img = "+img);
//        c.eval("ggsave(file='/Users/yyy/Desktop/yyy.pdf')");
        c.close();
	}
	public int setModelFile(String modelFile,int year) throws Exception {
		this.rf_path = modelFile;
		c.eval("author<-read.table('"+modelFile+"',header=T)");
//		c.eval("detach(author)");
		c.eval("attach(author)");
		c.eval("model1=lm(num~year,data=author)");
		c.eval("coeffs=coefficients(model1)");
		REXP x = c.eval("value=coeffs[1]+"+year+"*coeffs[2]");
//		System.out.println(x.asInteger());
		return x.asInteger();
		
	}
	
	public double predictImportance(String modelFile,int year) throws Exception {
		this.rf_path = modelFile;
		c.eval("author<-read.table('"+modelFile+"',header=T)");
//		c.eval("detach(author)");
		c.eval("attach(author)");
		c.eval("model1=lm(score~year,data=author)");
		c.eval("coeffs=coefficients(model1)");
		REXP x = c.eval("value=coeffs[1]+"+year+"*coeffs[2]");
//		System.out.println(x.asInteger());
		return x.asDouble();
		
	}
	public double calcSingleDistance(String feat) throws Exception {
		c.eval("test = c(" + feat + ")");
		REXP x = c.eval("predict(rf, test, proximity=FALSE, type='prob')[,1]");
		return x.asDouble();
	}
	
	public double[] calcDistances(String dist_file) throws Exception {
		
		c.eval("test = read.table('" + dist_file + "', header=TRUE, sep=',')");
		REXP x = c.eval("predict(rf, newdata=test, proximity=FALSE, type='prob')[,1]");
		
		double[] probs = x.asDoubles();
		return probs;
	}

	public void startup() throws Exception {
		if (c != null)
			c.shutdown();
		c = new RConnection();
	}
	public void shutdown() throws Exception {
		c.shutdown();
		c = null;
	}

	public void predict(int year) throws Exception
	{
		String path="/Users/yyy/Documents/workspace/ABMGraph/author_split_inEachYear_284/";
    	File file=new File(path);
    	String filelist[]=file.list();
    	BufferedWriter outPut= new BufferedWriter(new FileWriter("./author_284_publication_num"));
		
    	for(int i=0;i<filelist.length;i++)
    	{
    		RService r=new RService();
//    		System.out.println(path+filelist[i]);
        	int num=r.setModelFile(path+filelist[i],year);
        	outPut.write((i+1)+","+filelist[i].split("_")[1]+","+num);
        	outPut.newLine();
        	outPut.flush();
    	}
    	outPut.close();
	}
	// java -Dcsx.boot=bootstrap/disambiguation.txt -Dcsx.conf=conf edu.psu.citeseerx.disambiguation.DisambiguationService
    public static void main(String[] args) throws Exception {
    	String path="/Users/yyy/Documents/workspace/ABMGraph/paper_number_compare";
    	BufferedWriter outPut= new BufferedWriter(new FileWriter("./coauthor_number_predict"));
    	RService r=new RService();
    	for(int i=2005;i<2010;i++){
    		int score=r.setModelFile(path,i);
    		outPut.write(""+score);
    		outPut.newLine();
    		outPut.flush();
    	}
    	outPut.close();
    } //- main
    public static void predictImportance()throws Exception 
    {
    	String path="/Users/yyy/Documents/workspace/ABMGraph/author_split_importance_inEachYear/";
    	File file=new File(path);
    	String filelist[]=file.list();
    	BufferedWriter outPut= new BufferedWriter(new FileWriter("./author_284_importance_2009"));
    	HashMap<Integer,Double> count1= new HashMap<Integer,Double> ();
    	for(int i=0;i<filelist.length;i++)
    	{
    		RService r=new RService();
    		System.out.println(path+filelist[i]);
        	double score=r.predictImportance(path+filelist[i],2009);
        	int key1=Integer.valueOf(filelist[i].split("_")[1]);
			Double value=score;
			count1.put(key1, value);
//        	outPut.write((i+1)+","+filelist[i].split("_")[1]+","+num);
//        	outPut.newLine();
//        	outPut.flush();
    	}

		List<Map.Entry<Integer, Double>> infoIds = new ArrayList<Map.Entry<Integer, Double>>(count1.entrySet());  
		  
        Collections.sort(infoIds, new Comparator<Map.Entry<Integer, Double>>() {  
            public int compare(Map.Entry<Integer, Double> o1,  
                    Map.Entry<Integer, Double> o2) {  
//              System.out.println(o1.getKey()+"   ===  "+o2.getKey());  
                return (Double.valueOf(o2.getValue().toString()).compareTo(Double.valueOf(o1.getValue().toString())));  
            }  
        }); 
        for (int i = 0; i < infoIds.size(); i++) {  
            String id = infoIds.get(i).toString();  
            String str[]=id.split("=");
            outPut.append((i+1)+","+str[0]+","+str[1]);
            outPut.newLine();
            outPut.flush();
        }  
		outPut.close();
    }
}