package com.dlmu.algorithm;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.rosuda.REngine.Rserve.RConnection;

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
	

	
	// java -Dcsx.boot=bootstrap/disambiguation.txt -Dcsx.conf=conf edu.psu.citeseerx.disambiguation.DisambiguationService
    public static void main(String[] args) throws Exception {
    } //- main
}
