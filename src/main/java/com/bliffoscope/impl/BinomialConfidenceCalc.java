package com.bliffoscope.impl;

/**
 *  http://stackoverflow.com/questions/13059011/is-there-any-python-function-library-for-calculate-binomial-confidence-intervals
 * converted from python to java
 * */

public class BinomialConfidenceCalc {

    public static double binP(double N,double p,double x1,double x2){
        double q = p/(1-p);
        double k = 0.0;
        double v = 1.0;
        double s = 0.0;
        double tot = 0.0;

        while(k<=N){                    
            tot += v;
            if(k >= x1 && k <= x2){                
                s += v;
            }    
            if(tot > Math.pow(10,30)){                    
                s = s/Math.pow(10,30);
                tot = tot/Math.pow(10,30);
                v = v/Math.pow(10,30);
            }
            k += 1;
            v = v*q*(N+1-k)/k;

        }
        return s/tot;
    }


    public static double[] calcBin(double vx,double vN,Double vCL){

        double vTU = (100 - vCL)/2;
        double vTL = vTU;
        double dl = 0.0;
        double vP = vx/vN;
        if(vx==0){            
            dl = 0.0;
        }
        else{
            double v = vP/2;
            double  vsL = 0;
            double vsH = vP;
            double p = vTL/100;

            while((vsH-vsL) > Math.pow(10,-5)){
                if(binP(vN, v, vx, vN) > p){
                    vsH = v;
                    v = (vsL+v)/2;
                }else{
                    vsL = v;
                    v = (v+vsH)/2;
                }
            }
            dl = v;                             
        }

        double ul = 0.0;
        if(vx==vN){            
            ul = 1.0;
        }
        else{

            double v = (1+vP)/2;
            double vsL =vP;
            double vsH = 1;
            double p = vTU/100;
            while((vsH-vsL) > Math.pow(10,-5)){
                if(binP(vN, v, 0, vx) < p){
                    vsH = v;
                    v = (vsL+v)/2;
                }
                else{
                    vsL = v;
                    v = (v+vsH)/2;
                }
            }
            ul = v;
        }
        double dlUl[] = new double[]{dl,ul};
        return dlUl;
    }



}
