// Define a grammar called Hello
grammar Hello;

@members {
int count = 0;
}

command:
    'periodo' PERIODO
    | 'fatos'
    | 'contas' DESCRICAO
    | 'fato' ID
    | 'conta ' ID
    | 'rm' removivel ID
    | 'rm fato' ID(/ID)?
    | 'rm conta' ID;

removivel :
    'fato'
    | 'conta';


DESCRICAO: [a-zA-Z \t\r\n]+;

ID : [0-9]+;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines


/*
r  : 'hello' ID('/'ID)? #HELLOU;     // match keyword hello followed by an identifier
ID : [a-z]+ ;             // match lower-case identifiers


WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

*/