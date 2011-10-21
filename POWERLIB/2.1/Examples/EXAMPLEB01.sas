TITLE1 "EXAMPLEB01.SAS--Paired T-test";

PROC IML SYMSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

* Define matrices *;

ESSENCEX = I(1);
SIGMA = {2 1, 1 2};
BETA = {0 1};
C = {1};
U = {1 -1}`;

SIGSCAL= {1};
BETASCAL = DO(0,2.5,0.5);
REPN = { 10 };
OPT_ON = {COLLAPSE};
OPT_OFF= {C U};

RUN POWER;
QUIT;