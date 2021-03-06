*TITLE1 "EXAMPLEE01.SAS--Power as a fct of Sample Size and Mean Dif";

FILENAME OUT01 ".\EXAMPLEE01.CGM";

PROC IML WORKSIZE=1000 SYMSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

OPT_OFF = {WARN ALPHA};
OPT_ON={NOPRINT DS};
ROUND=6;
ALPHA={.01};
SIGMA = {.068};  SIGSCAL=  {1} ;
BETA = { 0    1}`;
C = {-1 1};
ESSENCEX=I(2);
REPN=DO(3,18,3);
BETASCAL=DO(0,.75,.05);
RUN POWER;

%MACRO GRAPH(TILT,ROTATE);
TITLE1;
PROC G3D DATA=ONE GOUT=OUT01;
PLOT BETASCAL*TOTAL_N=POWER/
     ZMIN=0 ZMAX=1.0 ZTICKNUM=6   YTICKNUM=4    XTICKNUM=6   SIDE;
LABEL TOTAL_N ="N"    BETASCAL="Delta"      POWER   ="Power" ;
RUN;
%MEND;

PROC G3GRID DATA=PWRDT1 OUT=ONE;
      GRID  BETASCAL*TOTAL_N=POWER /SPLINE NAXIS1=16 NAXIS2=11 ;

GOPTIONS GSFNAME=OUT01 DEVICE=CGMOF97P 
CBACK=WHITE COLORS=(BLACK) HORIGIN=0IN VORIGIN=0IN
HSIZE=5IN VSIZE=3IN HTEXT=12PT FTEXT=TRIPLEX;

%GRAPH(90, 300);