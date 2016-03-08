grammar GastosoCli;

command: WS? lineCommand WS? EOF;

lineCommand: 
    'periodo' (WS periodo)?
    |'fato' WS fatoArgs
    |'fatos'
    |'conta' WS contaArgs
    |'contas' (WS contasArgs)?
    |'rm' WS rmArgs
    |('gasto'|'ganho') WS gastoGanhoArgs
    |'transf' WS transfArgs
;

fatoArgs: 
    id
    | subId (WS valor)?
    | (dia WS)? textArg;

contaArgs: id (WS textArg)?;

contasArgs: textArg;

rmArgs: 
    'conta' WS id
    |'fato' WS (id | subId);

gastoGanhoArgs: (dia WS)? textArg WS id WS valor;

transfArgs: (dia WS)? textArg WS id WS id WS valor;


dia: 'hoje' | 'ontem' | 'amanha' | 'depois de amanha' | 'ante-ontem'
    |diaDaSemana (WS ('que vem'|'passado'|'passada'))?
    |'de hoje a oito' | 'de hoje a quinze'
;

diaDaSemana : 'domingo' | 'segunda' | 'terca' | 'quarta' | 'quinta' | 'sexta' | 'sabado';

mes: 'janeiro' | 'fevereiro' | 'marco' | 'abril' | 'maio' | 'junho' | 'julho' | 'agosto' | 'setembro' | 'outubro' | 'novembro' | 'dezembro';

ano : INT; 

periodo:
  periodoSimples
  |'de' WS periodoSimples WS ('a' | 'ate') WS periodoSimples;

periodoSimples : 
    dia 
    | mes (WS ('que vem'|'passado'|'passada'))?
    | ano 
    | diaDaSemana (WS ('que vem'|'passado'|'passada'))
    | ('semana'|'mes'|'ano'|'semestre'|'trimestre'|'bimestre') WS ('que vem'|'passado'|'passada')
    | 'essa semana' | 'esse ' ('mes'|'ano'|'semestre'|'trimestre'|'bimestre') ;

textArg : WORD | TEXT;

valor: '-'? INT CENTAVOS?;
id:INT;
subId: id '/' id;

//lex rules

fragment ALPHA: [a-zA-Z];
fragment DIGIT: '0'..'9';
fragment ASPAS: '\'' | '"';

INT: DIGIT+;
CENTAVOS: ',' DIGIT DIGIT;

WORD: ALPHA (ALPHA|DIGIT)*;

TEXT: ASPAS (WS | ALPHA| DIGIT)+ ASPAS;

WS : [ \t\r\n]+; 