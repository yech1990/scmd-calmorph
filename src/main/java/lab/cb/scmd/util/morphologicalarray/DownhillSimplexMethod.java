//--------------------------------------
//SCMD Project
//
//DownhillSimplexMethod.java 
//Since:  2004/09/01
//
// $URL$ 
// $LastChangedBy$ 
//--------------------------------------

package lab.cb.scmd.util.morphologicalarray;

import lab.cb.scmd.exception.SCMDException;

/**
* @author nakatani
*
*/
public class DownhillSimplexMethod {
	static final int NMAX = 5000;//maximum allowed number of function evaluations.
	static final int MAX_RESTART = 10;
	static final double TINY = 1.0e-10;
	static final int mpts = 3;
	static final int ndim = 2;
	static final double ftol = 1.0e-10;
	double[] y;
	double[] psum;
	double[][] p;
	
	PowerTransformation transform;
	double minX;
	Double[] transformThis;
	
	//(x,y)-->(p,a): a=-e^{y}+min(X)
	private double[] translate(double[] parameters){
		//return parameters;//debug
		double[] pa=new double[ndim];
		//pa[0]=  Math.pow(Math.E,parameters[0]);
		pa[0]=parameters[0];
		pa[1]= -Math.pow(Math.E,parameters[1])+minX-TINY;
		return pa;
	}
	public double[] getBestParamters(){
		return translate(p[0]);
	}
	public double getBestValue(){
		return y[0];
	}
	public void startAmoeba() throws SCMDException {
		double stepSize = 1;
		for (int i = 0; i < MAX_RESTART; ++i,stepSize *= -0.8) {
			try {
				startAmoeba(stepSize);
				if(p[0][0]<-10||p[0][0]>10)throw new SCMDException("too large p="+p+", give up PowerTransformation.");
				//if(p[0][0]<-10)throw new SCMDException("too small p="+p+", give up PowerTransformation.");
				return;
			} catch (SCMDException e) {
				//System.out.print("Exception in startAmoeba(). i="+i+" message=");
				e.what(System.out);
				//e.printStackTrace();
				//System.exit(-1);
			}
		}
		throw new SCMDException("Maximum allowed number of restarts.");
	}
	public void startAmoeba(double step) throws SCMDException{
		double[] startPoint=new double[ndim];
		startPoint[0]=0;
		startPoint[1]=0;
		double[] secondPoint=new double[ndim];
		secondPoint[0]=step;
		secondPoint[1]=0;
		double[] thirdPoint=new double[ndim];
		thirdPoint[0]=0;
		thirdPoint[1]=step;
		
		p[0]=startPoint;
		p[1]=secondPoint;
		p[2]=thirdPoint;
		for(int i=0;i<mpts;++i){
			y[i]=funk(p[i]);
		}
		amoeba();
	}
	//chi square
//	private double funk(double[] parameters) throws SCMDException{
//		double[] param=translate(parameters);
//		Double[] transformedData=transform.transform(param[0],param[1]);
//		return transform.evaluateNormality(transformedData);
//	}
	
	//skewness&kurtosis
//	
//	private double funk(double[] parameters) throws SCMDException{
//		double[] param=translate(parameters);
//		Double[] transformedData=transform.transform(param[0],param[1]);
//		double x1=StatisticalTests.getSampleKurtosis(transformedData);
//		double x2=StatisticalTests.getSampleSkewness(transformedData);
//		double k=(x1-3)*(x1-3);
//		double s=x2*x2;
//		return k*s;
//		//return ( k > s )?k:s;
//	}
	
	//kurtosis

//	private double funk(double[] parameters) throws SCMDException{
//		double[] param=translate(parameters);
//		Double[] transformedData=transform.transform(param[0],param[1]);
//		double x=StatisticalTests.getSampleKurtosis(transformedData);
//		return (x-3)*(x-3);
//	}
	//skewness
	
	private double funk(double[] parameters) throws SCMDException{
		double[] param=translate(parameters);
		Double[] transformedData=transform.transform(param[0],param[1]);
		double x=StatisticalTests.getSampleSkewness(transformedData);
		return x*x;
	}
	
	//MLE
//	private double funk(double[] parameters) throws SCMDException{
//		double[] param=translate(parameters);//get (p,a)
//		//Double[] transformedData=transform.transform(param[0],param[1]);
//		Double[] normalizedData=transform.normalizedTransform(param[0],param[1]);
//		//Double[] standardizedData=StatisticalTests.getStandardizedData(normalizedData);
//		Double[] EandSD=StatisticalTests.getEandSD(normalizedData);
//		return EandSD[1].doubleValue();
//	}
//	//debug
//	private double funk(double[] parameters){
//		double[] param=translate(parameters);
//		return (param[0]-Math.PI)*(param[0]-Math.PI)/*+(param[1]-Math.E)*(param[1]-Math.E)*/;
//	}
	public DownhillSimplexMethod(PowerTransformation caller){
		transform=caller;
		//minX=StatisticalTests.min(transform.getOriginalData() );
		minX=StatisticalTests.min(transform.getStandardizedData());
		y=new double[mpts];
		psum=new double[ndim];
		p=new double[mpts][ndim];
	}

	
	private double[] get_psum(double[][] p)
	{
		double[] psum=new double[ndim];
		for(int j=0;j<ndim;++j){
			double sum=0;
			for(int i=0;i<mpts;++i){
				sum+=p[i][j];
			}
			psum[j]=sum;
		}
		return psum;
	}
	private void amoeba() throws SCMDException{
		int nfunk=0;
		int ilo;//ith point (lowest)
		int ihi;//ith point (highest)
		int inhi;//ith point (next highest)
		while(true){
			//debug
			/***
			System.out.print(nfunk+" minX="+minX);
			for(int i=0;i<mpts;++i){
				double[] pi=translate(p[i]);
				System.out.print(" (y="+funk(p[i])+" p="+pi[0]+" a="+pi[1]+" pp="+p[i][0]+" aa="+p[i][1]+") ");
			}
			System.out.println();
			***/
			
			ilo=0;
			//first we must determine which point is the highest (worst), next-highest, and lowest(best), by looping over the points int the simplex.
			if(y[0]>y[1]){
				inhi=1;
				ihi=0;
			}else{
				inhi=0;
				ihi=1;
			}
			for(int i=0;i<mpts;i++){
				if(y[i]<y[ilo])ilo=i;
				if(y[i]>y[ihi]){
					inhi=ihi;
					ihi=i;
				}else if(y[i]>y[inhi] && i!=ihi)inhi=i;
			}
			double rtol=2.0*Math.abs(y[ihi]-y[ilo])/(Math.abs(y[ihi])+Math.abs(y[ilo])+TINY);
			//compute the fractional range from highest to lowest and return if satisfactory
			if(rtol<ftol){
				//System.out.println("rtol="+rtol+" ftol="+ftol);
				break;
			}
			if(nfunk >= NMAX){
				//System.err.println("Maximum allowed number of function evaluations.");
				throw new SCMDException("Transformation filed. Maximum allowed number of function evaluations.");
			}
			nfunk+=2;
			//begin a new iteration. First extrapolate by a factor -1 through the face of the simplex across from the high point, i.e., reflect the simplex from the high point.
			double ytry=amotry(ihi,-1.0);
			//System.out.println("ytry="+ytry+" y[inhi]="+y[inhi]);
			if(ytry<=y[ilo]){
				//gives a result better than the best point, so try an additional extrapolation by a factor 2.
				ytry=amotry(ihi,2.0);
			}else if(ytry >= y[inhi]){
				//the reflected point is worse than the second-highest, so look for an intermediate lower point, i.e., do a one-dimensional contraction.
				double ysave=y[ihi];
				ytry=amotry(ihi,0.5);
				if(ytry>=ysave){
					//can not seem to get rid of that high point. better contract around the lowest (best) point.
					boolean stopLooping=false;
					for(int i=0;i<mpts;i++){
						if(i!=ilo){
							boolean changed=false;
							for(int j=0;j<ndim;++j){
								double newValue=psum[j]=0.5*(p[i][j]+p[ilo][j]);
								if(newValue!=p[i][j])changed=true;
								p[i][j]=newValue;
							}
							y[i]=funk(psum);
							if(changed==false)stopLooping=true;
						}
					}
					nfunk+=ndim;//Keep track of function evaluations.
					psum=get_psum(p);//recompute psum.
					if(stopLooping==true)break;
				}
			}else --nfunk;//Correct the evaluation count.
		}//Go back for the test of doneness and the next iteration.
		
		double tmp=y[0];
		y[0]=y[ilo];
		y[ilo]=tmp;
		for(int i=0;i<ndim;++i){
			tmp=p[0][i];
			p[0][i]=p[ilo][i];
			p[ilo][i]=tmp;
		}
	}
	private double amotry(int ihi, double fac) throws SCMDException{
		double[] ptry = new double[ndim];
		double fac1=(1.0-fac)/ndim;
		double fac2=fac1-fac;
		
		for(int j=0;j<ndim;j++){
			ptry[j]=psum[j]*fac1-p[ihi][j]*fac2;
		}
		double ytry=funk(ptry);
		if(ytry<y[ihi]){
			y[ihi]=ytry;
			for(int j=0;j<ndim;j++){
				psum[j]+=ptry[j]-p[ihi][j];
				p[ihi][j]=ptry[j];
			}
		}
		return ytry;
	}
	/*
	public static void main(String[] args) {
		DownhillSimplexMethod dsm=new DownhillSimplexMethod(10);
		dsm.startAmoeba();
		double min=dsm.getBestValue();
		double[] param=dsm.getBestParamters();
		System.out.println("estimated value: min="+min+" p="+param[0]+" a="+param[1]);
		System.out.println("PI="+Math.PI+" E="+Math.E);
	}*/
}


//--------------------------------------
//$Log: DownhillSimplexMethod.java,v $
//Revision 1.2  2004/12/02 09:53:52  nakatani
//delete
//
//Revision 1.1  2004/09/18 23:10:18  nakatani
//*** empty log message ***
//
//--------------------------------------
