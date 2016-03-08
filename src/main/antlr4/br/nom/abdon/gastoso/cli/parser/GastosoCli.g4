grammar GastosoCli;


r: 'fato' WS fatoArgs WS? EOF;

fatoArgs: 
    id
    |subId WS valor?;

valor: NUM (',' DIGIT DIGIT)?;

textArg : WORD | TEXT;

command: TEXT;
//'fato' | 'fatos' | 'conta' | 'contas';


id:NUM;
subId: id '/' id;


fragment ALPHA: [a-zA-Z];
fragment DIGIT: '0'..'9';
fragment ASPAS: '\'' | '"';

NUM: DIGIT+;
WORD: ALPHA (ALPHA|DIGIT)*;

TEXT: ASPAS (WS | ALPHA| DIGIT)+ ASPAS;

WS : [ \t\r\n]+; 

