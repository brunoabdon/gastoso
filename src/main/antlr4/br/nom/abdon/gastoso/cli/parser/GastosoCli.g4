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
    id (WS (dia|textArg))? #fatoId
    | subId (WS valor)?    #fatoSubId
    | (dia WS)? textArg    #mkFato
;

contaArgs: 
    id (WS textArg)?
    |textArg
;

contasArgs: textArg;

rmArgs: 
    ('fato'|'conta') WS id
    |'lancamento' WS subId;

gastoGanhoArgs: (dia WS)? textArg WS id WS valor;

transfArgs: (dia WS)? textArg WS id WS id WS valor;

dia:
    HOJE 
    | ONTEM
    | AMANHA
    | DEPOIS_DE_AMANHA
    | ANTE_ONTEM
    | DE_HOJE_A_OITO
    | DE_HOJE_A_QUINZE
    | (DOM | SEG | TER | QUA | QUI | SEX | SAB) (WS varianteMasc)?
;

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
