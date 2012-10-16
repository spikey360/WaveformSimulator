package waveformsimulator;

public class Wavelet{

private double x;
private double y;
private double a;
private double f;
private double b;
private double phi;
private double v;
private double lambda;
public double getAmp(){
	return a;
}

public Wavelet(double x, double y, double amp, double freq, double phase, double v, double lambda){
	this.x=x;
	this.y=y;
	this.a=amp;
	this.f=freq;
	this.phi=phase;
	this.v=v;
	this.lambda=lambda;
}

public void setDamp(double x){}

public double getX(){
	return x;
}

public double getY(){
	return y;
}

public double calculateY(double h, double k, double t){
	//h,k used to calc dist from source
	double d=Math.sqrt(Math.pow(h-x,2)+Math.pow(y-k,2));
	double y=getAmp()*Math.sin(2*Math.PI*(v*d-t)/lambda+phi);
	return y;
}

public String toString(){
	return "x:"+x+"y:"+y+"amp:"+a+"freq:"+f+"phase:"+phi+"v:"+v+"lambda:"+lambda;
}

}
