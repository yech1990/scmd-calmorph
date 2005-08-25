//--------------------------------------
//SCMD Project
//
//Sample.java 
//Since:  2004/09/03
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lab.cb.scmd.exception.SCMDException;

/**
* @author nakatani
*
*/
public class PowerTransformation {
	double originalMean;
	double originalSD;
	Double[] originalData;
	
	Double[] standardizedData;
	double bestP;
	double bestA;
	double optimizedValueForTransform;
	double minNormalityScore = Double.MAX_VALUE;
	public PowerTransformation(Double[] d) throws SCMDException, IOException{
		originalData=d;
		Double[] ESD=StatisticalTests.getEandSD(originalData);
		originalMean=ESD[0].doubleValue();
		originalSD=ESD[1].doubleValue();
		standardizedData=StatisticalTests.getStandardizedData(originalData);
		findBestParameter();

	}
	private Double[] reverseStandardize(Double[] data){
		Double[] reversed=new Double[data.length];
		for(int i=0;i<data.length;++i){
			reversed[i]=new Double(data[i].doubleValue()*originalSD+originalMean);
		}
		return reversed;
	}
	private Double[] getOriginalData(){
		return originalData;
	}
	protected Double[] getStandardizedData(){
		return standardizedData;
	}
	
	public Double[] getTransformedData() throws SCMDException{
		return transform(bestP,bestA);
	}
	
//	private void findBestParameter() throws SCMDException{
//		DownhillSimplexMethod dsm=new DownhillSimplexMethod(this);
//		dsm.startAmoeba();
//		double[] bestParam=dsm.getBestParamters();
//		bestP=bestParam[0];
//		bestA=bestParam[1];
//		optimizedValueForTransform=dsm.getBestValue();
//	}
	
	//いくつかのポイントの中で最適な値を求める。
	private void findBestParameter() throws SCMDException, IOException{
		PrintWriter debug = new PrintWriter(new FileWriter("C:/Documents and Settings/Administrator/My Documents/SCMD/debug.xls"));
		double minP=-3.0;
		double maxP=3.0;
		//double maxA = StatisticalTests.min(this.getStandardizedData());
		//double minA = -50;
		double fixedA = StatisticalTests.min(this.getStandardizedData())-1.0E-10;
		double minA=fixedA;
		double maxA=fixedA;
		int m=60;
		for(int i=0;i<m;++i){
			double p=minP+i*(maxP-minP)/(double)m;
			//for(int j=1;j<m;++j){//j!=0 to avoid log(0) in transform()
				int j=0;
				double a=minA+j*(maxA-minA)/(double)m;
				Double[] transformedData=normalizedTransform(p,a);
				double mlesd=StatisticalTests.getEandSD(transformedData)[1].doubleValue();
				//double mlesd=StatisticalTests.getSampleSkewness(transformedData);
				debug.print(mlesd+"\t");
				//double normalityScore = evaluateNormality(transformedData);
				double normalityScore=mlesd;
				//System.err.println("p="+p+" a="+a+" score="+normalityScore);
				if(normalityScore < minNormalityScore){
					minNormalityScore=normalityScore;
					bestP=p;
					bestA=a;
				}
			//}
			debug.println();
		}
		debug.close();
	}

	protected Double transform(Double originalDataPoint) throws SCMDException{
		return transform(bestP,bestA,originalDataPoint);//********param->1
	}
	protected Double reverseTranform(Double transformedDataPoint){
		return reverseTransform(bestP,bestA,transformedDataPoint);//*******param->1
	}
	protected Double transform(double p,double a,Double originalDataPoint) throws SCMDException{
		double x=originalDataPoint.doubleValue();
		if(x-a<=0){
			System.err.println("Error in PowerTransformation.transform() 0<x-a="+(x-a)+" x="+x+" a="+a);
			throw new SCMDException("Error in PowerTransformation.transform() 0<x-a="+(x-a)+" x="+x+" a="+a);
		}	
		double d=-19780426;
		if(p!=0)d=(Math.pow((x-a),p)-1)/p;
		else d=Math.log(x-a); 
		if(-Double.MAX_VALUE<d && d<Double.MAX_VALUE){
		}else{
			throw new SCMDException("PowerTransformation.transform() p="+p+" a="+a+" ="+d);
			/***
			System.err.println("PowerTransformation.transform() p="+p+" a="+a+" ="+d);
			System.exit(-1);
			***/
		}
		return new Double(d);		
	}
	protected Double reverseTransform(double p, double a, Double transformedDataPoint){
		double y=transformedDataPoint.doubleValue();
		double x=-19780426;
		if(p!=0){
			if(y*p+1==0)x=0;
			else x=Math.pow(y*p+1,1/p)+a;
		}else x=Math.pow(Math.E,y)+a;
		if(-Double.MAX_VALUE<x && x<Double.MAX_VALUE){
		}else{
			System.err.println("PowerTransformation.reverseTransform() p="+p+" a="+a+" x= "+x+" y="+y);
			System.exit(-1);
		}
		return new Double(x);
	}
	protected Double[] transform(double p, double a) throws SCMDException{
//		if(p<0){
//			System.err.println("Error in PowerTransform.transform() 0<p="+p);
//			System.exit(-1);
//		}
		Double[] transformedData = new Double[originalData.length];
		for(int i=0;i<originalData.length;++i){
			//transformedData[i]=transform(p,a,originalData[i]);
			transformedData[i]=transform(p,a,standardizedData[i]);
		}
		return transformedData;
	}
	protected Double[] normalizedTransform(double p,double a) throws SCMDException{
		Double[] transformedData=transform(p,a);
		Double[] shiftedData=new Double[originalData.length];
		for(int i=0;i<originalData.length;++i){
			shiftedData[i]=new Double(standardizedData[i].doubleValue()-a);
			//shiftedData[i]=new Double(originalData[i].doubleValue()-a);
		}
		double gm=StatisticalTests.getGeometricMean(shiftedData);
		gm=Math.pow(gm,p-1);
		for(int i=0;i<transformedData.length;++i){
			transformedData[i]=new Double(transformedData[i].doubleValue()/gm);
		}
		return transformedData;
	}
	protected Double[] reverseTransform(Double[] transformedData){
		for(int i=0;i<transformedData.length;++i){
			transformedData[i]=reverseTransform(bestP,bestA,transformedData[i]);
		}
		return transformedData;
	}
	protected double evaluateNormality(Double[] d){
		ChiSquareGoodnessOfFitTest cs=new ChiSquareGoodnessOfFitTest(d);
		return cs.getStatistics();
	}
	
	private static final double up8=5.6120;//upper 0.00000001
	private static final double up7=5.1993;//upper 0.0000001
	private static final double up6=4.7534;//upper 0.000001
	private static final double up5=4.2649;//upper 0.00001
	private static final double up4=3.7190;//upper 0.0001
	private static final double up32=3.5401;//upper 0.0002
	private static final double up35=3.2950;//upper 0.0005
	private static final double up3=3.0902;//upper 0.001
	private static final double up25=2.5758;//upper 0.005
	private static final double up2=2.3263;//upper 0.01
	public Double[] getOneSidedCriticalValues() throws SCMDException{
		Double[] transformedData=getTransformedData();//***
		//System.err.println("no power transfomation.");
		//Double[] transformedData=getOriginalData();//***
		Double[] ESD=StatisticalTests.getEandSD(transformedData);
		double mean=ESD[0].doubleValue();
		double sd=ESD[1].doubleValue();
		Double[] criticalValues=new Double[14];
		criticalValues[0]=new Double(up5*sd+mean);
		criticalValues[1]=new Double(up4*sd+mean);
		criticalValues[2]=new Double(up32*sd+mean);
		criticalValues[3]=new Double(up35*sd+mean);
		criticalValues[4]=new Double(up3*sd+mean);
		criticalValues[5]=new Double(up25*sd+mean);
		criticalValues[6]=new Double(up2*sd+mean);
		criticalValues[7]=new Double(-up2*sd+mean);
		criticalValues[8]=new Double(-up25*sd+mean);
		criticalValues[9]=new Double(-up3*sd+mean);
		criticalValues[10]=new Double(-up35*sd+mean);
		criticalValues[11]=new Double(-up32*sd+mean);
		criticalValues[12]=new Double(-up4*sd+mean);
		criticalValues[13]=new Double(-up5*sd+mean);
		criticalValues=reverseTransform(criticalValues);//***
		criticalValues=reverseStandardize(criticalValues);//only if standardized -1116
		return criticalValues;
	}
//	public Double[] /*MINMAX*/getOneSidedCriticalValues(){
//		Double[] criticalValues=new Double[6];
//		criticalValues[0]=criticalValues[1]=criticalValues[2]=new Double(StatisticalTests.max(originalData));
//		criticalValues[3]=criticalValues[4]=criticalValues[5]=new Double(StatisticalTests.min(originalData));		
//		return criticalValues;
//	}
	static public void printHeader(PrintWriter pw){
		String t="\t";
		pw.print("optimizedValue"+t+"p"+t+"a"+t);
		pw.print("transformedE"+t+"transformedSD"+t);
		pw.print("normality"+t);
		pw.print("lower(0.00001)"+t+"lower(0.0001)"+t+"lower(0.001)"+t+"lower(0.01)"+t);
		pw.print("upper(0.01)"+t+"upper(0.001)"+t+"upper(0.0001)"+t+"upper(0.00001)");
	}
	static public void printlnHeader(PrintWriter pw){
		printHeader(pw);
		pw.println();
	}
	public void print(PrintWriter pw) throws SCMDException{
		String t="\t";
		pw.print(optimizedValueForTransform+t+bestP+t+bestA+t);
		
		Double[] transformedData=getTransformedData();
		Double[] ESD=StatisticalTests.getEandSD(transformedData);
		double mean=ESD[0].doubleValue();
		double sd=ESD[1].doubleValue();
		pw.print(mean+t+sd+t);
		pw.print(evaluateNormality(getTransformedData())+t);
		Double[] crt=getOneSidedCriticalValues();
		for(int i=crt.length-1;i>=0;--i){
			pw.print(crt[i].doubleValue()+t);
		}
	}
	public void println(PrintWriter pw) throws SCMDException{
		print(pw);
		pw.println();
	}
}


//--------------------------------------
//$Log: PowerTransformation.java,v $
//Revision 1.2  2004/12/02 09:54:49  nakatani
//delete
//
//Revision 1.1  2004/09/18 23:10:18  nakatani
//*** empty log message ***
//
//--------------------------------------
