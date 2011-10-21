*TITLE1 "EXAMPLEA02.SAS--Two sample T-test Plot";

FILENAME OUT01 ".\EXAMPLEA02.CGM";

*************************************************************;
* Performs power calculations for a two sample T test,       ;
* replicating the plots in "Increasing scientific power with ;
* statistical power", by K.E. Muller and V.A. Benignus,      ;
* Neurotoxicology and Teratology, vol 14, May-June, 1992     ;
*************************************************************;

PROC IML SYMSIZE=1000 WORKSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

* Define inputs to power program;

SIGMA = {1};
SIGSCAL= {0.32 1.00 2.05};

ESSENCEX = I(2);
REPN = { 10 };

BETA = {0 1}`;
BETASCAL = DO(0, 2.5, 0.05);
C = {1 -1};

OPT_ON = {DS NOPRINT};

RUN POWER;

*The DS option creates a SAS file with the power calculation results.;
*Since no name for the file was specified, WORK.PWRDT1 is created.;

QUIT;

PROC CONTENTS DATA=PWRDT1;
RUN;

* Create Plots *;

PROC SORT DATA=PWRDT1
          OUT=ONE;
   BY BETASCAL SIGSCAL;

* create file for power curves of varying VARIANCE *;

PROC TRANSPOSE DATA=ONE OUT=TWO PREFIX=SIGPWR;
VAR POWER;
BY BETASCAL;

* create ANNOTATE dataset and assign symbols for labeling plots *;

DATA LABELS (KEEP= X Y XSYS YSYS TEXT STYLE SIZE);
LENGTH   TEXT $ 5  STYLE $ 8;
XSYS="2"; YSYS="2";

X=.26;  Y=.95; TEXT="s";     STYLE="cgreek"; *SIZE=1.0; OUTPUT;
X=.31;  Y=.97; TEXT="2";     STYLE="TRIPLEX"; SIZE=.75; OUTPUT;
X=.50;  Y=.95; TEXT="=0.32"; STYLE="TRIPLEX"; SIZE=1.0; OUTPUT;
X=.76;  Y=.70; TEXT="s";     STYLE="cgreek";  SIZE=1.0; OUTPUT;
X=.81;  Y=.72; TEXT="2";     STYLE="TRIPLEX"; SIZE=.75; OUTPUT;
X=1.00; Y=.70; TEXT="=1.00"; STYLE="TRIPLEX"; SIZE=1.0; OUTPUT;
X=1.01; Y=.15; TEXT="s";     STYLE="cgreek";  SIZE=1.0; OUTPUT;
X=1.06; Y=.17; TEXT="2";     STYLE="TRIPLEX"; SIZE=.75; OUTPUT;
X=1.25; Y=.15; TEXT="=2.05"; STYLE="TRIPLEX"; SIZE=1.0; OUTPUT;
RUN;

* The plot will be saved to this file for future inclusion in a document.*;
GOPTIONS GSFNAME=OUT01 DEVICE=CGMOF97P 
CBACK=WHITE COLORS=(BLACK) HORIGIN=0IN VORIGIN=0IN
HSIZE=5IN VSIZE=3IN HTEXT=12PT FTEXT=TRIPLEX;

SYMBOL1 I=JOIN V=NONE L=34 W=1.0;
SYMBOL2 I=JOIN V=NONE L=1  W=1.0;
SYMBOL3 I=JOIN V=NONE L=34 W=1.0;
AXIS1 ORDER=(0 TO 1 BY .1)  W=1.5 MINOR=NONE MAJOR=(W=1.5) 
      LABEL=(ANGLE=-90 ROTATE=90);
AXIS2 ORDER=(0 TO 2.5 BY .5) W=1.5 MINOR=NONE MAJOR=(W=1.5); 

* The plot overlays power curves for three different variances *;
PROC GPLOT DATA=TWO;
PLOT SIGPWR1*BETASCAL=1
     SIGPWR2*BETASCAL=2
     SIGPWR3*BETASCAL=3/OVERLAY VAXIS=AXIS1 HAXIS=AXIS2 ANNOTATE=LABELS;
LABEL SIGPWR1="Power"  SIGPWR2="Power"  SIGPWR3="Power"
      BETASCAL="Mean Difference";
RUN;
QUIT;