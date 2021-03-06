TITLE1 "EXAMPLEH01.SAS-two within factors, UPOLY2, no between, vary SIGMA";
TITLE2 "From Coffey C.S. and Muller K.E. (2003) Properties of ";
TITLE3 "internal pilots with the univariate approach to repeated measures";
TITLE4 "Statistics in Medicine, 22(15), in press.";

PROC IML SYMSIZE=1000 WORKSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

ALPHA=.04;
OPT_ON={NOPRINT  GG HF BOX TOTAL_N  UCDF UMETHOD};
OPT_OFF={WARN  ALPHA  BETASCAL  HLT PBT WLK };
ROUND=2;

BETASCAL=1;
THETA={.25}#{.5 1 -1 .5}; * =Theta(cr) from 1st sentence *after* 
                          *  equation 7, Coffey and Muller (2003); 

*Following from Table II in Coffey and Muller (2003);
VARSTARE={.47960 .01000 .01000 .01000}; *epsilon ~ .28; 
VARSTARF={.34555 .06123 .05561 .04721}; *epsilon ~ .50;
VARSTARG={.23555 .17123 .05561 .04721}; *epsilon ~ .72;
VARSTARH={.12740 .12740 .12740 .12740}; *epsilon = 1;
VARSTAR=VARSTARE//VARSTARF//VARSTARG//VARSTARH;

SIGSCAL={0.50 1.00 2.00}; * <=> gamma in Coffey and Muller (2003);

*log base 2 spacing Clip (2,4,16) and Region(2,8,32);
*Get orthonormal U matrices;
RUN UPOLY2({1 2 4},"A", {1 3 5},"B",
            UA,NMA,    UB,NMB,  UAB ,NMAB);
U=UAB;
C=1;

ESSENCEX={1};
REPN={20};

  DO IVAR=1 TO 4 BY 1;
  SIGSTAR=DIAG(VARSTAR[IVAR,*]);

  SIGMA=U*SIGSTAR*U`;  *1st paragraph in section 2.4, Coffey and Muller 2003;
  BETA=THETA*U`;       *1st paragraph in section 2.4, Coffey and Muller 2003; 

    DO VERSION=1 TO 2 BY 1;  *POWERLIB version;
    UCDF=J(4,1,VERSION);
    UMETHOD=J(2,1,VERSION);
    RUN POWER;
    HOLDALL=HOLDALL//_HOLDPOWER;
    END;
  END;

CREATE ONE VAR _HOLDPOWERLBL;
APPEND FROM HOLDALL;


PROC SORT DATA=ONE OUT=TWO;
BY UCDF_GG UMETHOD_GG SIGSCAL EPSILON;

PROC PRINT DATA=TWO UNIFORM NOOBS;
BY UCDF_GG UMETHOD_GG  UCDF_HF UMETHOD_HF UCDF_BOX TOTAL_N;
PAGEBY UCDF_GG;
TITLE5 "All data in file";

PROC PRINT DATA=TWO(RENAME=(SIGSCAL=GAMMA)) UNIFORM NOOBS;
VAR EPSILON GAMMA  POWER_GG POWER_HF ; 
BY UCDF_GG UMETHOD_GG  UCDF_HF UMETHOD_HF UCDF_BOX TOTAL_N;
PAGEBY UCDF_GG;
TITLE5 "Version 2 far more accurate for Table III, Coffey and Muller (2003)";
