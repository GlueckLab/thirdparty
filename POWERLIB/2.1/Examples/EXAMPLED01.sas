*TITLE1 "EXAMPLED01.SAS--Power Confidence Limit due to varhat";
*TITLE2 "Figure 1 Taylor and Muller, 1995, Amer Statistician, 49, p43-47";

FILENAME OUT01 ".\EXAMPLED01.CGM";

PROC IML WORKSIZE=1000 SYMSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

OPT_OFF = {ALPHA};      *Turn options off;
OPT_ON  = {NOPRINT DS}; *Turn options on;

*Model is Y = XB + E;
*Specify essence of X matrix, the basic design;
ESSENCEX=I(2);  *Balanced two group t test, cell mean coding;
REPN={12};

*Hypothesis, as usual in ANOVA, is grand mean invariant so subtract;
BETA = {0 1}`;
BETASCAL=DO(0,.75,.01);
*Gives power for Beta={0  0}` , {0  .01}` , . . . {0  .75}`;

*Hypothesis is Ho: CBU=Theta0 ;
ALPHA={.01};  *Test size for hypothesis test;
C    ={1 -1}; * U=I and Theta0=0 are defaults; *This is t-test;

SIGMA  = {.068}; *Multivariate notation=Variance in univariate;
SIGSCAL= {1}   ; *Set of covariance matrix multipliers;

*Statements to create confidence limits;
CLTYPE=1;
N_EST=21;        *# Obs for variance estimate;
RANK_EST=1;      *# model df for study giving variance estimate;
ALPHA_CL=.025;   *Lower confidence limit tail size;
ALPHA_CU=.025;   *Upper confidence limit tail size;
RUN POWER; *IML and power code end here.  Power program creates file PWRDT1;
**********************************************************;

*Remainder of code creates plot;
GOPTIONS GSFNAME=OUT01 DEVICE=CGMOF97P 
CBACK=WHITE COLORS=(BLACK) HORIGIN=0IN VORIGIN=0IN
HSIZE=5IN VSIZE=3IN HTEXT=12PT FTEXT=TRIPLEX;

SYMBOL1 I=SPLINE V=NONE L=34 W=2;
SYMBOL2 I=SPLINE V=NONE L= 1 W=2;
SYMBOL3 I=SPLINE V=NONE L=34 W=2;
AXIS1 ORDER=(0 TO 1 BY .2)    W=3 MINOR=NONE MAJOR=(W=2) 
      LABEL=(ANGLE=-90 ROTATE=90);
AXIS2 ORDER=(0 TO .75 BY .25) W=3 MINOR=NONE MAJOR=(W=2)
      LABEL=("Mean Difference, 1/Cr (dL/mg)");

PROC GPLOT DATA=PWRDT1;
PLOT (POWER_L POWER POWER_U)*BETASCAL/ OVERLAY NOFRAME HREF=.5
       VZERO VAXIS=AXIS1 HZERO HAXIS=AXIS2 NOLEGEND;
LABEL POWER_L="Power"  POWER  ="Power"  POWER_U="Power" 
      SIGSCAL="Variance Multiplier";