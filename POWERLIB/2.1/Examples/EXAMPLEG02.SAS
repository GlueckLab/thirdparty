TITLE1 "EXAMPLEG02.SAS--UPOLY3 for contrasts";
TITLE2 "factorial design, repeated measures: A, B, C between, D, E, F within";

PROC IML SYMSIZE=1000 WORKSIZE=2000;
RESET FUZZ NOAUTONAME FW=6 LINESIZE=80;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

ALPHA=.05;

*Choose dimensions of design;
GA=3; *=# groups for between factor A;
GB=3; *=# groups for between factor B;
GC=3; *=# groups for between factor C;
TD=3; *=#Times for within factor D;
TE=3; *=#Times for within factor E;
TF=3; *=#Times for within factor F;

P=TD#TE#TF;
Q=GA#GB#GC;
ESSENCEX= I(Q);
BETA=J(Q,P,0);
BETA[1,1]=1;

SIGMA=DIAG(DO(1,P,1));*Variances are 1,2,3,...p;

*Get orthonormal U matrices;
CALL UPOLY3 ( (1:TD),"D",  (1:TE),"E",  (1:TF),"F",
		              UD,UDLBL,    UE,UELBL,    UF,UFLBL, 
             UDE,UDELBL,  UDF,UDFLBL,  UEF,UEFLBL,  UDEF,UDEFLBL );
 
*Get orthonormal C matrices;
CALL UPOLY3 ((1:GA),"A" , (1:GB),"B" , (1:GC),"C",
		             U1,CALBL,   U2,CBLBL,      U3,CCLBL,
           U12,CABLBL, U13,CACLBL, U23,CBCLBL,  U123,CABCLBL);

BETASCAL={9 18 27};
ROUND=4;
OPT_ON={NOPRINT  GG HF UN  PBT HLT WLK};
OPT_OFF={WARN SIGSCAL ALPHA};
BUG=" ";

C=U1`;
U=UD;
  DO REPN=2 TO 12 BY 2;
  RUN POWER;
  HOLDA=HOLDA//_HOLDPOWER;
  END;
PRINT / "AxD";
PRINT HOLDA[COLNAME=_HOLDPOWERLBL ROWNAME=BUG];

C=U12`;
U=UDE;
  DO REPN=2 TO 12 BY 2;
  RUN POWER;
  HOLDABDE=HOLDABDE//_HOLDPOWER;
  END;
PRINT / "AxB x DxE Interaction";
PRINT HOLDABDE[COLNAME=_HOLDPOWERLBL ROWNAME=BUG];

C=U123`;
U=UDEF;
  DO REPN=2 TO 12 BY 2;
  RUN POWER;
  HABCDEF=HABCDEF//_HOLDPOWER;
  END;
PRINT / "AxBxC x DxExF Interaction";
PRINT HABCDEF[COLNAME=_HOLDPOWERLBL ROWNAME=BUG];