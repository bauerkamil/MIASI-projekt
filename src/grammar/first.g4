grammar first;

prog:	stat* EOF ;

stat: expr_full #expr_stat
    | block #block_stat
    ;

block : expr_full* '>>' #block_tok
    ;

get_file: FILE '|' path=expr #getFile;

for_block: FOR_ '|'
            (from=INT '|' to=INT '|')?
            (array_name=ID '|')?
            (item_name=ID '|')?
            (index_name=ID '|')?
            expr_full+;

expr_full: expr ';';

expr:
        FOR_ '|' from=INT '|' to=INT '|' (index_name=ID '|')? expr_full+ #forExpr
    |   FOREACH '|' array_name=ID '|' item_name=ID '|' (index_name=ID '|')? expr_full+ #foreachExpr
    |   CALL ('|' num=INT)? #call
    |   OUT '|' CONSOLE ('|' val=expr)? #print_console
    |   OUT '|' get_file #print_file
    |   VERB '|' op=(POST|PUT|GET|DELETE) #setMethod
    |   URL '|' val=expr #setUrl
    |   QUERY '|' key=expr '|' val=expr #setQuery
    |   HEADER '|'  key=expr '|' val=expr #setHeader
    |   BODY '|' val=get_file #setBody
    |   BODY '|' JSON '|'  key=expr '|' val=expr #setBody
    |   VAR '|' (GLOBAL '|')? name=ID '|' expr #setVar
    |   ARRAY '|' (GLOBAL '|')? name=ID '|' '[' items+= expr ('|' items+= expr)* ']' #setArr
    |   '{' ID ('|' index=INT)? '}' #getVar
    |   INT #intTok
    |   ID #stringTok
    |   STRING #stringTok
    ;

VERB:   'verb';
POST:   'post';
PUT:    'put';
GET:    'get';
DELETE: 'delete';

VAR:    'var';
GLOBAL: 'global';
ARRAY:  'array';
URL:    'url';

QUERY:  'query';
HEADER: 'header';
BODY:   'body';
FILE:   'file';
JSON:   'json';

FOR_:   'for';
FOREACH: 'foreach';

OUT:    'output';
CONSOLE: 'console';

CALL:   'call';


//NEWLINE : [\r\n]+ -> skip;
NEWLINE : [\r\n]+ -> channel(HIDDEN);

//WS : [ \t]+ -> skip ;
WS : [ \t]+ -> channel(HIDDEN) ;

ID : [a-zA-Z_][a-zA-Z0-9_]* ;

INT     : [0-9]+;
STRING  : [a-zA-Z0-9_/:.-]+;


COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~'\n'* '\n' -> channel(HIDDEN) ;