*TITLE1 "EXAMPLEI01.SAS--Power for Region by Gender interaction with 5 age groups";

LIBNAME IN01 "..\DATA\";

PROC IML SYMSIZE=2000 WORKSIZE=2000;
%INCLUDE "..\IML\POWERLIB203.IML"/NOSOURCE2;

OPT_ON = {GG HF BOX UN TOTAL_N ORTHU NOPRINT};
OPT_OFF={WARN};

USE IN01.P0104;
READ ALL VAR {ANT LEFT POST RIGHT} WHERE(_TYPE_ = "COV") INTO INSIGMA;	*Importing covariance matrix of data*;

*Rounding and viewing covariance matrix*;

RNM={ANT LEFT POST RIGHT};
ALPHA = .05/6;
SIGMA = ROUND(INSIGMA,.0001);
PRINT SIGMA[FORMAT=11.5 COLNAME=RNM];

*Define input matrices*;

ESSENCEX = I(10);
REPN ={10};

P = 4;
Q = 2;
BETARG= J(2,4, 3.2) + {.30}#({ -1  0 1  0,
                               -1  0 1  0}) ;	*Pattern of means for Gender by Region*;
PRINT BETARG[COLNAME=RNM];
BETASCAL={1};

C = {1 -1} @ J(1,5,1);

GROUP={F1 F2 F3 F4 F5  M1 M2 M3 M4 M5};
REGION = {1,2,3,4};
RUN UPOLY1(REGION,"REGION",U1,REGU);
U=U1;

DO DELTA=0 TO .10 BY .02;
  BETARGD=BETARG + (J(2,2,0)||(DELTA//(-DELTA))||J(2,1,0));	*Creation of Beta matrix based on varying Gender differences*;
  BETA= BETARGD @ J(5,1,1) ;					*Final Beta matrix with age groups added*;
  PRINT  / DELTA BETA[COLNAME=RNM ROWNAME=GROUP];
  RUN POWER;
  HOLDALL=HOLDALL//_HOLDPOWER;
END;

PRINT HOLDALL[COLNAME=_HOLDPOWERLBL];				*Power calculations*;

QUIT;