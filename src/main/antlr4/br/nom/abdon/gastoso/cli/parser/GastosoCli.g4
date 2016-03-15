grammar GastosoCli;

command: WS? lineCommand WS? EOF;

lineCommand: 
    periodo | fato | fatos | conta | contas | rm | gasto | ganho | transf;
    

periodo : 'periodo' (WS periodoDef)?;
fato    : 'fato'    WS fatoArgs;
fatos   : 'fatos';
conta   : 'conta'   WS contaArgs;
contas  : 'contas'  (WS contasArgs)?;
rm      : 'rm'      WS rmArgs;
gasto   : 'gasto'   WS gastoGanhoArgs;
ganho   : 'ganho'   WS gastoGanhoArgs;
transf  : 'transf'  WS transfArgs;


fatoArgs: 
    id                     #fatoId
    | subId (WS valor)?    #fatoSubId
    | (dia WS)? textArg    #mkFato
;

contaArgs: id (WS textArg)?;

contasArgs: textArg;

rmArgs: 
    ('fato'|'conta') WS id
    |'lancamento' WS subId;

gastoGanhoArgs: (dia WS)? textArg WS id WS valor;

transfArgs: (dia WS)? textArg WS id WS id WS valor;

dia: diaSimples
     |diaDaSemana
     |diaDaSemanaPorReferencia
;

diaSimples:
    HOJE 
    | ONTEM
    | AMANHA
    | DEPOIS_DE_AMANHA
    | ANTE_ONTEM
    | DE_HOJE_A_OITO
    | DE_HOJE_A_QUINZE
;

diaDaSemana : DOM | SEG | TER | QUA | QUI | SEX | SAB;

diaDaSemanaPorReferencia : diaDaSemana WS varianteMasc;

mes: mesSimples | mesPorReferencia;

mesSimples: JAN | FEV | MAR | ABR | MAI | JUN | JUL | AGO | SET | OUT | NOV | DEZ;

mesPorReferencia: mesSimples WS varianteMasc;

ano: anoSimples | anoPorReferencia;

anoSimples : INT; 

anoPorReferencia: anoSimples WS varianteMasc;

varianteMasc: QUE_VEM | PASSADO;
varianteFem: QUE_VEM | PASSADA;

periodoDef: periodoSimples | peridoComplexo;

peridoComplexo: 'de' WS periodoSimples WS ATE WS periodoSimples;

periodoSimples :
    dia 
    | periodoSemana
    | mes
    | ano 
    | periodoReferenciado
;

periodoSemana: essaSemana | outraSemana;
periodoReferenciado: essePeriodo | outroPeriodo;

essaSemana: ESSA WS SEMANA;
outraSemana: SEMANA WS varianteFem;

essePeriodo: ESSE WS nomeDePeriodo;
outroPeriodo : nomeDePeriodo WS varianteMasc;

nomeDePeriodo: MES | ANO | SEMESTRE;

textArg : WORD | TEXT;

valor: MENOS? INT CENTAVOS?;
id :INT;
subId: id '/' id;

//lex rules


HOJE: 'hoje';
ONTEM: 'ontem';
AMANHA: 'amanha';
DEPOIS_DE_AMANHA: 'depois de amanha';
ANTE_ONTEM: 'ante-ontem';
DE_HOJE_A_OITO: 'de hoje a oito';
DE_HOJE_A_QUINZE: 'de hoje a quinze';

JAN: 'janeiro' | 'Janeiro' | 'JANEIRO' | 'jan' | 'JAN'; //qnd tiver paciencia, fazer pro outros...
FEV: 'fevereiro';
MAR: 'marco';
ABR: 'abril';
MAI: 'maio';
JUN: 'junho';
JUL: 'julho';
AGO: 'agosto';
SET: 'setembro';
OUT: 'outubro';
NOV: 'novembro';
DEZ: 'dezembro';

DOM: 'Domingo' | 'domingo' | 'dom';
SEG: 'Segunda' | 'segunda' | 'seg';
TER: 'Terca' | 'terca' | 'ter';
QUA: 'Quarta' | 'quarta' | 'qua';
QUI: 'Quinta' | 'quinta' | 'qui';
SEX: 'Sexta' | 'sexta' | 'sex';
SAB: 'Sabado' | 'sabado' | 'sab';

ANO: 'ano';
MES: 'mes';
SEMANA: 'semana';
SEMESTRE: 'semestre';
TRIMESTRE: 'trimestre';
BIMESTRE: 'bimestre';

ESSE: 'esse';
ESSA: 'essa';

QUE_VEM: 'que vem';
PASSADA: 'passada';
PASSADO: 'passado';

ATE: 'a' | 'ate';



fragment ALPHA: [a-zA-Z];
fragment DIGIT: '0'..'9';
fragment ASPAS: '\'' | '"';

INT: DIGIT+;
CENTAVOS: ',' DIGIT DIGIT;
MENOS: '-';
WORD: ALPHA (ALPHA|DIGIT)*;


TEXT: ASPAS (WS | . )+ ASPAS;

WS : [ \t\r\n]+; 