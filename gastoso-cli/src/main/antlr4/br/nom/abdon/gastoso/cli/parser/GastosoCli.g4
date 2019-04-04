grammar GastosoCli;

command: WS? lineCommand WS? EOF;

lineCommand: 
      MKF WS textArg (WS dia)? (WS conta (WS conta)? WS valor)?
    | MKC WS textArg
    | RMF WS id
    | RMC WS conta
    | LN  WS id WS WORD
    | RML WS WORD
    | (ENTRADA|SAIDA) WS conta WS valor
    | DIA WS dia
    | DESC WS textArg
    | NULL WS conta
    | SAVE
    | CANCEL
    | LS ((WS CONTAS) | ((WS conta)? (WS periodo)?))
    | MVC WS id WS textArg
    | MVF WS id (WS dia)? (WS textArg)?
    | CD WS (id|PARENT)
    | PERIODO (WS periodo)?
    ;

conta: id | WORD;

dia:
    HOJE 
    | ONTEM
    | AMANHA
    | DEPOIS_DE_AMANHA
    | ANTE_ONTEM
    | DE_HOJE_A_OITO
    | DE_HOJE_A_QUINZE
    | (DOM | SEG | TER | QUA | QUI | SEX | SAB) (WS varianteMasc)?
    | mesISO '/' INT
;

mes: (JAN | FEV | MAR | ABR | MAI | JUN | JUL | AGO | SET | OUT | NOV | DEZ) (WS varianteMasc)?
     | mesISO;

mesISO: INT '/' INT;

ano: INT | ANO (WS varianteMasc);

varianteMasc: QUE_VEM | PASSADO;
varianteFem: QUE_VEM | PASSADA;

periodo: periodoSimples | peridoComplexo;

peridoComplexo: DE WS periodoSimples WS ATE WS periodoSimples;

periodoSimples :
    dia 
    | periodoSemana
    | mes
    | ano 
    | periodoReferenciado
;

periodoSemana: ESSA WS SEMANA | SEMANA WS varianteFem;

periodoReferenciado: 
    ESSE WS nomeDePeriodo
    | nomeDePeriodo WS varianteMasc;

nomeDePeriodo: MES | ANO | SEMESTRE;

textArg : WORD | TEXT;

valor: MENOS? INT CENTAVOS?;
id :INT;

//lex rules
MKF: 'mkf';
MKC: 'mkc';
RMF: 'rmf';
RMC: 'rmc';
LN: 'ln';
RML: 'rml';
ENTRADA: 'in';
SAIDA: 'out';
NULL: 'null';
LS: 'ls';
MVF: 'mvf';
MVC: 'mvc';
CD: 'cd';
SAVE: 'save';
CANCEL: 'cancel';

PARENT: '..';
CONTAS: 'contas';
DIA: 'dia';
DESC: 'desc';

HOJE: 'hoje';
ONTEM: 'ontem';
AMANHA: 'amanha';
DEPOIS_DE_AMANHA: 'depois de amanha';
ANTE_ONTEM: 'ante-ontem';
DE_HOJE_A_OITO: 'de hoje a oito';
DE_HOJE_A_QUINZE: 'de hoje a quinze';
PERIODO: 'periodo';

JAN: 'janeiro'   | 'Janeiro'   | 'JANEIRO'   | 'jan' | 'JAN';
FEV: 'fevereiro' | 'Fevereiro' | 'FEVEREIRO' | 'fev' | 'FEV';
MAR: 'marco'     | 'Marco'     | 'MARCO'     | 'mar' | 'MAR' | 'Março' | 'MARÇO' ;
ABR: 'abril'     | 'Abril'     | 'ABRIL'     | 'abr' | 'ABR';
MAI: 'maio'      | 'Maio'      | 'MAIO'      | 'mai' | 'MAI';
JUN: 'junho'     | 'Junho'     | 'JUNHO'     | 'jun' | 'JUN';
JUL: 'julho'     | 'Julho'     | 'JULHO'     | 'jul' | 'JUL';
AGO: 'agosto'    | 'Agosto'    | 'AGOSTO'    | 'ago' | 'AGO';
SET: 'setembro'  | 'Setembro'  | 'SETEMBRO'  | 'set' | 'SET';
OUT: 'outubro'   | 'Outubro'   | 'OUTUBRO'   | 'out' | 'OUT';
NOV: 'novembro'  | 'Novembro'  | 'NOVEMBRO'  | 'nov' | 'NOV';
DEZ: 'dezembro'  | 'Dezembro'  | 'DEZEMBRO'  | 'dez' | 'DEZ';

DOM: 'Domingo' | 'domingo' | 'dom' | 'DOM'  | 'DOMINGO';
SEG: 'Segunda' | 'segunda' | 'seg' | 'SEG'  | 'SEGUNDA';
TER: 'Terca'   | 'terca'   | 'ter' | 'TER'  | 'TERCA'   | 'Terça'  | 'terça' | 'TERÇA' ;
QUA: 'Quarta'  | 'quarta'  | 'qua' | 'QUA'  | 'QUARTA';
QUI: 'Quinta'  | 'quinta'  | 'qui' | 'QUI'  | 'QUINTA';
SEX: 'Sexta'   | 'sexta'   | 'sex' | 'SEX'  | 'SEXTA';
SAB: 'Sabado'  | 'sabado'  | 'sab' | 'SAB'  | 'SABADO'  | 'Sábado' | 'sábado' |'SÁBADO';

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

DE: 'de' | 'desde';
ATE: 'a' | 'ate' | 'pra';

fragment ALPHA: [a-zA-Z];
fragment DIGIT: '0'..'9';
fragment ASPAS: '\'' | '"';

INT: DIGIT+;
CENTAVOS: ',' DIGIT DIGIT;
MENOS: '-';
WORD: ALPHA (ALPHA|DIGIT)*;

TEXT: ASPAS (W | . )+? ASPAS;

WS : W+; 

W: [ \t\r\n];