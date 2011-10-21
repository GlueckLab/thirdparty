TITLE1 "EXAMPLEB02.SAS--Paired T-test using Difference Scores";
* Equivalent to EXAMPLEB01.SAS ;

PROC IML SYMSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

* Define matrices *;

ESSENCEX = I(1);
SIGMA = {2};		* Variance of Difference of Means *;
BETA = {1};
C = {1};
U = {1};

SIGSCAL= {1};
BETASCAL = DO(0,2.5,0.5);
REPN = { 10 };
OPT_ON = {COLLAPSE};
OPT_OFF= {C U};

RUN POWER;
QUIT;