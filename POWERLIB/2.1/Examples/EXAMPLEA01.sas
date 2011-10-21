TITLE1 "EXAMPLEA01.SAS--Two sample T-test";

***************************************************************;
* Perform power calculations for a two sample T test,          ;
* replicating the results in "Increasing scientific power with ;
* statistical power", by K.E. Muller and V.A. Benignus,        ;
* Neurotoxicology and Teratology, vol 14, May-June, 1992       ;
* The code reports power for a limited number of predicted     ;
* differences in means, compared to the number of values       ;
* needed for plotting.  Code for plot is in ExampleA02.sas     ;
***************************************************************;

PROC IML SYMSIZE=1000 WORKSIZE=2000;
%INCLUDE "..\IML\POWERLIB202.IML"/NOSOURCE2;

* Define inputs to power program;

SIGMA = {1};
SIGSCAL= {0.32 1.00 2.05};

ESSENCEX = I(2);
REPN = { 10 };

BETA = {0 1}`;
BETASCAL = DO(0,2.5,0.5);
C = {1 -1};

OPT_OFF= {C U};

RUN POWER;