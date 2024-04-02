grammar first;

prog:	stat* EOF ;

stat: expr #expr_stat
    | def #defineFunc
    | IF_kw '(' cond=expr ')' then=block  ('else' else=block)? #if_stat
    | '>' expr #print_stat
    ;

block : stat #block_single
    | '{' block* '}' #block_real
    ;

def : name=ID '(' (args+= ID (',' args+=ID)* )')' body=block ;

func : name=ID '(' (args+= expr (',' args+= expr)* ) ')' ;

expr:
        l=expr op=(EQ|NEQ) r=expr #logicOp
    |   l=expr op=(MUL|DIV) r=expr #binOp
    |	l=expr op=(ADD|SUB) r=expr #binOp
    |   op=NOT r=expr #logicOp
    |   l=expr op=AND r=expr #logicOp
    |   l=expr op=OR r=expr #logicOp
    |	INT #int_tok
    |   DOU #double_tok
    |   BOOL #bool_tok
    |	op='D(' expr ')' #pars
    |	op='I(' expr ')' #pars
    |   (INIT) ID #initVar
    | <assoc=right> ID '=' expr # assign
    |   ID #getVar
    |   func #callFunc
    ;

IF_kw : 'if' ;

DIV : '/' ;

MUL : '*' ;

SUB : '-' ;

ADD : '+' ;

OR : 'or';

AND : 'and';

NOT : 'not';

EQ : '==';

NEQ : '!=';

INIT: 'var';

//NEWLINE : [\r\n]+ -> skip;
NEWLINE : [\r\n]+ -> channel(HIDDEN);

//WS : [ \t]+ -> skip ;
WS : [ \t]+ -> channel(HIDDEN) ;

INT     : [0-9]+ ;
DOU : [0-9]+'.'[0-9]+;
BOOL : 'true'|'false';


ID : [a-zA-Z_][a-zA-Z0-9_]* ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~'\n'* '\n' -> channel(HIDDEN) ;