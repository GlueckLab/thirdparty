*TITLE1 "EXAMPLEL01.SAS--one sided Power Confidence Limit due to varhat";

/* Test whether specifying
	ALPHA_CL=.05;   *Lower confidence limit tail size;
	ALPHA_CU=  0;   *Upper confidence limit tail size;
   works correctly  */

FILENAME OUT01 ".\&PROG..EMF";

PROC IML WORKSIZE=1000 SYMSIZE=2000;
%INCLUDE "..\IML\POWERLIB203.IML" / NOSOURCE;

OPT_OFF = {ALPHA WARN  SIGSCAL TOTAL_N  CLTYPE  ALPHA_CL ALPHA_CU};
OPT_ON  = { DS};

ESSENCEX=I(2);  *Balanced two group t test, cell mean coding;
REPN={12};

BETA = {0 1}`;
BETASCAL=DO(0,.75,.005);

ALPHA={.01};  *Test size for hypothesis test;
C    ={1 -1}; * U=I and Theta0=0 are defaults; *This is t-test;

SIGMA  = {.068}; *Multivariate notation=Variance in univariate;
SIGSCAL= {1}   ;

*Statements to create confidence limits;
CLTYPE=1;
N_EST=24;        *# Obs for variance estimate;
RANK_EST=2;      *# model df for study giving variance estimate;
ALPHA_CL=0.05;   *Lower confidence limit tail size;
ALPHA_CU=  0;   *Upper confidence limit tail size;
RUN POWER;
**********************************************************;

*Remainder of code creates plot;
GOPTIONS GSFNAME=OUT01 DEVICE=emf 
CBACK=WHITE COLORS=(BLACK) HORIGIN=0IN VORIGIN=0IN
HSIZE=6IN VSIZE=4IN HTEXT=24PT FTEXT=ZAPF;

SYMBOL1 I=SPLINE V=NONE L=34 W=4;
SYMBOL2 I=SPLINE V=NONE L= 1 W=4;
SYMBOL3 I=SPLINE V=NONE L=34 W=4;
AXIS1 ORDER=(0 TO 1 BY .2)    W=6 MINOR=NONE MAJOR=(W=2) 
      LABEL=(ANGLE=-90 ROTATE=90);
AXIS2 ORDER=(0 TO .75 BY .25) W=6 MINOR=NONE MAJOR=(W=2)
      LABEL=NONE;
*      LABEL=("Mean Difference, 1/Cr (dL/mg)");

PROC GPLOT DATA=PWRDT1;
PLOT (POWER_L POWER POWER_U)*BETASCAL/ OVERLAY NOFRAME HREF=.5
       VZERO VAXIS=AXIS1 HZERO HAXIS=AXIS2 NOLEGEND;
LABEL POWER_L="Power"  POWER  ="Power"  POWER_U="Power";
run; quit;