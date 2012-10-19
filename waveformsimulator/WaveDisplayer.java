/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package waveformsimulator;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//import wavelet.Wavelet;

/**
 *
 * @author Riju
 */
public class WaveDisplayer {

    private static final int MAX_WAVES = 5,  RGB_MAX = 255,  BUFFERS = 3; //to ensure optimal performance
    private Wavelet[] waves;
    private int c;
    private double incTime;
    private double t;
    private boolean refOrigin = true;

    private static boolean maxdisp=false;
    private static boolean scsaver=false;
    private static String filename="wavelets.cnf";
    private static double timeInc=100;
    private static long sleep=0;
    private static int maxframe=-1;
    
    private Frame mainFrame;
    private BufferStrategy bufStrat;
    private GraphicsDevice device;
    private int framesRendered;
    private RenderThread rt;
    private int renderWidth;
    private int renderHeight;
    
    private int logline=1;
    private int logspacingPix=20;
    private int logx=10;
    
    public void addWaves(Wavelet w) {
        if (c < MAX_WAVES - 1) {
            waves[c++] = w;
        }
        calcMaxAmp();
    //  System.out.println(maxAmp);
    }

    public void incrementTime(double tx) {
        t += tx;
    }

    public WaveDisplayer() {
        waves = new Wavelet[MAX_WAVES];
        setupScreen();
    }
    double maxAmp;

    private void calcMaxAmp() {
        for (int i = 0; i < MAX_WAVES; i++) {
            if (waves[i] != null) {
                maxAmp += waves[i].getAmp();
            }
        }
    }

    private Color getScaledColor(double y, Color low, Color high) {
        //just return the Color
        int r = (int) (colorScale(y) * (high.getRed() - low.getRed()));

        int g = (int) (colorScale(y) * (high.getGreen() - low.getGreen()));
        int b = (int) (colorScale(y) * (high.getBlue() - low.getBlue()));

        Color newcolor = new Color(Math.abs(r), Math.abs(g), Math.abs(b));

        return newcolor;
    }

    private int colorScale(double y) {
        int k = (int) ((y / maxAmp) * RGB_MAX);
        // System.err.println("y:"+y+",k:"+k+"y/maxAmp:"+(y/maxAmp));
        return k;
    }
    

    DisplayMode chooseMode(){
        DisplayMode[] dms=device.getDisplayModes();
        for(int i=0;i<dms.length;i++)
        	System.err.println(dms[i].getWidth()+"x"+dms[i].getHeight()+":"+dms[i].getBitDepth()+","+dms[i].getRefreshRate());
        DisplayMode d=dms[0];
        
        if(maxdisp){
        	int mw=0; int c=0;
        	for(int i=0;i<dms.length;i++){
        		if(mw<dms[i].getWidth()){
        				c=i;
        				mw=dms[i].getWidth();
        			}
        	}
        	d=dms[c];
        	}else{
        		for(int i=0;i<dms.length;i++){
         		   if(dms[i].getWidth()==832)
         		       d= dms[i];
        		}
        	}
        System.err.println("Chosen mode: "+(renderWidth=d.getWidth())+"x"+(renderHeight=d.getHeight()));
        return d;
    }

    void setupScreen() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        device = env.getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        mainFrame = new Frame(gc);
        mainFrame.setUndecorated(true);
        mainFrame.setIgnoreRepaint(true);
        device.setFullScreenWindow(mainFrame);
        if (device.isDisplayChangeSupported()) {
            device.setDisplayMode(chooseMode()); //16-bit colors
        }
        //Rectangle bounds=mainFrame.getBounds();
        mainFrame.createBufferStrategy(BUFFERS);
        bufStrat = mainFrame.getBufferStrategy();
    //now frame is properly set up, mainFrame can be safely used

    }

    void unsetupScreen() {
        device.setFullScreenWindow(null);
        System.out.println("Frames rendered:" + (framesRendered-1));
        System.exit(0);
    }
    
    public void finalize(){
    	unsetupScreen();
    }
    
    //int disp[] = new int[MAX_WAVES];
    

    void render(Graphics g) {
        double y = 0;
        //g.clearRect(0,0,100,100);
        //render each pixel
        for (int h = 0; h < mainFrame.getBounds().getWidth(); h++) {
            for (int k = 0; k < mainFrame.getBounds().getHeight(); k++) {
                //calculate y here
                y = 0;
                for (int i = 0; i < MAX_WAVES; i++) {
                    if (waves[i] != null) {
                        // System.err.println("i:"+i+":"+waves[i].calculateY(h, k, t)); //
                        y += waves[i].calculateY(h, k, t);
                    }
                }
                if (y >= 0) {
                    //g.setColor(getScaledColor(y, Color.BLACK, Color.BLUE));
                    g.setColor(new Color(0, 0, 128 + Math.abs(colorScale(y)) / 2));
                    g.drawLine(h, k, h, k);
                }
                if (y < 0) {
                    //g.setColor(getScaledColor(y, Color.GREEN, Color.BLACK));
                    g.setColor(new Color(0, 0, 128 - Math.abs(colorScale(y) / 2)));
                    g.drawLine(h, k, h, k);
                }


            }
        }
        if (refOrigin && !scsaver) {
            for (int n = 0; n < MAX_WAVES; n++) {
                if (waves[n] != null) {
                    g.setColor(Color.WHITE);
                    g.drawString("" + (n + 1), (int) waves[n].getX(), (int) waves[n].getY());
                }
            }
        }
        logline=1; logx=10; logspacingPix=10;
	//display time
	framesRendered++; //debug
	if(!scsaver){
	g.drawString("Frame: "+framesRendered+" / "+ maxframe,logx,(logline++)*logspacingPix);
	g.drawString("Rendering @ "+renderWidth+"x"+renderHeight,logx,(logline++)*logspacingPix);
	}
	incrementTime(timeInc);
    }

    void startRender(WaveDisplayer wd, long sleep){
        rt=new RenderThread(wd, sleep);
        rt.start();
    }

    public static void main(String[] args) {
    	System.out.println("WaveSimulator - spikey360\nspikey360@yahoo.co.in");
    	for(int i=0;i<args.length;i++){
    		if(args[i].equals("-maxdisp") || args[i].equals("-md")){
    			maxdisp=true;
    			}
    		if(args[i].equals("-f")){
    			if((i+1)<args.length){
    				filename=args[i+1];
    				}
    			else{
    				System.err.println("Filename not supplied");
    				return;
    				}
    		}
    		if(args[i].equals("-t")){
    			if((i+1)<args.length){
    				timeInc=Double.parseDouble(args[i+1]);
    				}
    			else{
    				System.err.println("Time increment invalid");
    				}
    		}
    		if(args[i].equals("-s")){
    			if((i+1)<args.length){
    				sleep=Long.parseLong(args[i+1]);
    				}
    			else{
    				System.err.println("Invalid sleep time");
    				}
    		}
    		if(args[i].equals("-mf")){
    			if((i+1)<args.length){
    				maxframe=Integer.parseInt(args[i+1]);
    				}
    			else{
    				System.err.println("Invalid frame limit");
    				}
    		}
    		if(args[i].equals("-ss")){
    			scsaver=true;
    			
    		}
    		if(args[i].equals("-help") || args[i].equals("-h")){
    			System.out.println("Usage\nWaveDisplayer <options>\n-maxdisp,-md\tMaximum display resolution\n-f <filename>\tUse <filename> for wavelets data\n-help,-h\tHelp\n-t <time>\tTime to increment for waves\n-s <timeInMillis> Time to sleep after each frame\n-mf <number>\tNumber of frames to render\n-ss\tScreen saver mode (overrides -mf)");
    			return;
    		}
    	}
    	if(scsaver)
    		maxframe=-1;
    		
    		
        WaveDisplayer wd = new WaveDisplayer();
/*        Wavelet w = new Wavelet(30, 50, 10, 5, 23,330,2000); //x,y,amp,freq,phase
        Wavelet w2 = new Wavelet(100, 350, 40, 63, 0,330,3000);
        Wavelet w3=new Wavelet(500, 350, 20, 6, 40,630,5000);
        w.setDamp(0);
        w2.setDamp(0);
        wd.addWaves(w);
        wd.addWaves(w2);
        wd.addWaves(w3);*/
        try{
        BufferedReader in=new BufferedReader(new InputStreamReader(WaveDisplayer.class.getResourceAsStream(filename)));
        String s=";";
        while(!(s=in.readLine()).equals("")){
        	String params[]=s.split(" ");
        	
        	double x=Double.parseDouble(params[0]);
        	double y=Double.parseDouble(params[1]);
        	double amp=Double.parseDouble(params[2]);
        	double freq=Double.parseDouble(params[3]);
        	double phase=Double.parseDouble(params[4]);
        	double v=Double.parseDouble(params[5]);
        	double lambda=Double.parseDouble(params[6]);
        	Wavelet w=new Wavelet(x,y,amp,freq,phase,v,lambda);
        	System.out.println(w);
        	wd.addWaves(w);
        }
	}
	catch(FileNotFoundException e){
		e.printStackTrace();
		System.err.println(filename+" not found");
		wd.unsetupScreen();
		return;
	}
	catch(IOException e){
		e.printStackTrace();
	}
	catch(Exception e){
		e.printStackTrace();
	}
//        for (double f = 0; f < 100; f+=33.3333) {
//            for(int i=0;i<32;i++){
//            Graphics g = wd.bufStrat.getDrawGraphics();
//            wd.render(g);
//            wd.bufStrat.show();
//            g.dispose();
//            wd.incrementTime(0.01);
//            try {
//                Thread.sleep((int)f);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(WaveDisplayer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        }
        //int k = 0;

//

        wd.startRender(wd, sleep);
        while(!wd.rt.isTerminated());
        
	
    }

    class RenderThread extends Thread {

        WaveDisplayer wd;
        boolean terminate=false;
        long sleepTime;

        void terminate(){
            terminate=true;
        }
        
        public boolean isTerminated(){
        	return terminate;
        }

        RenderThread(WaveDisplayer z, long sleep) {
            wd = z;
            this.sleepTime=sleep;
            System.err.println("Frame refresh after "+sleepTime+" ms");
        }

        @Override
        public void run() {
            while (wd.framesRendered!=wd.maxframe) {
                if(!wd.bufStrat.contentsLost()){
                
                Graphics g = wd.bufStrat.getDrawGraphics();
                
                wd.render(g);
                
                wd.bufStrat.show();
                
                g.dispose();
                
                //wd.incrementTime(100); //done in render() function
                
                }
                try {
                    sleep(sleepTime);
                //k++;
                } catch (InterruptedException ex) {
                    Logger.getLogger(WaveDisplayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            //k++;
            }
            terminate();
            wd.unsetupScreen();
        }
    }
}
