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
    private Frame mainFrame;
    private BufferStrategy bufStrat;
    private GraphicsDevice device;

    DisplayMode chooseMode(){
        DisplayMode[] dms=device.getDisplayModes();
        for(int i=0;i<dms.length;i++)
        	System.err.println(dms[i].getWidth()+"x"+dms[i].getHeight()+":"+dms[i].getBitDepth()+","+dms[i].getRefreshRate());
        for(int i=0;i<dms.length;i++){
            if(dms[i].getWidth()==832)
                if(dms[i].getBitDepth()>=16)
                return dms[i];
        }
        return dms[0];
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
        System.out.println(", rendered:" + framesRendered);
        System.exit(0);
    }
    //int disp[] = new int[MAX_WAVES];
    private int framesRendered;

    void render(Graphics g) {
        double y = 0;
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
        if (refOrigin) {
            for (int n = 0; n < MAX_WAVES; n++) {
                if (waves[n] != null) {
                    g.setColor(Color.WHITE);
                    g.drawString("" + (n + 1), (int) waves[n].getX(), (int) waves[n].getY());
                }
            }
        }
	//display time
	g.drawString("Time: "+t,10,10);
        framesRendered++; //debug
    }

    void startRender(WaveDisplayer wd, long sleep){
        RenderThread x=new RenderThread(wd, sleep);
        x.start();
    }

    public static void main(String[] args) {
        WaveDisplayer wd = new WaveDisplayer();
        Wavelet w = new Wavelet(30, 50, 10, 5, 23,330,2000); //x,y,amp,freq,phase
        Wavelet w2 = new Wavelet(100, 350, 40, 63, 0,330,3000);
        Wavelet w3=new Wavelet(500, 350, 20, 6, 40,630,5000);
        w.setDamp(0);
        w2.setDamp(0);
        wd.addWaves(w);
        wd.addWaves(w2);
        wd.addWaves(w3);

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

        wd.startRender(wd, 200);
           //while(wd.framesRendered<200){}
        //wd.unsetupScreen();



    }

    class RenderThread extends Thread {

        WaveDisplayer wd;
        boolean terminate=false;
        long sleepTime;

        void terminate(){
            terminate=true;
        }

        RenderThread(WaveDisplayer z, long sleep) {
            wd = z;
            this.sleepTime=sleep;
            System.err.println("Frame refresh after "+sleepTime+" ms");
        }

        @Override
        public void run() {
            while (terminate == false) {
                if(!wd.bufStrat.contentsLost()){
                //System.err.println("0");
                Graphics g = wd.bufStrat.getDrawGraphics();
                //System.err.println("1");
                wd.render(g);
                //System.err.println("2");
                wd.bufStrat.show();
                //System.err.println("3");
                g.dispose();
                //System.err.println("4");
                wd.incrementTime(1);
                //System.err.println("5");
                }
                try {
                    sleep(sleepTime);
                //k++;
                } catch (InterruptedException ex) {
                    Logger.getLogger(WaveDisplayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            //k++;
            }
        }
    }
}
