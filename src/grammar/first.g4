grammar first;

prog:	stat* EOF ;

stat: expr_full #expr_stat
    | block #block_stat
    ;

block : expr_full* '>>' #blockTok
    ;

//get_file: FILE '|' path=expr #getFile;

expr_full: expr ';';

expr:
        FOR_ '|' from=INT '|' to=INT '|' (index_name=ID '|')? expr_full+ #forExpr
    |   FOREACH '|' array_name=ID '|' item_name=ID '|' (index_name=ID '|')? expr_full+ #foreachExpr
    |   CALL ('|' num=expr)? #call
    |   OUT '|' CONSOLE ('|' val=expr)? #printConsole
    |   OUT '|' FILE '|' path=expr #printFile
    |   VERB '|' op=(POST|PUT|GET|DELETE) #setMethod
    |   URL '|' expr #setUrl
    |   QUERY '|' key=expr '|' val=expr #setQuery
    |   HEADER '|'  key=expr '|' val=expr #setHeader
    |   BODY '|' FILE '|' path=expr #setBodyFile
    |   BODY '|' JSON '|'  key=expr '|' val=expr #setBody
    |   VAR '|' (global=GLOBAL '|')? name=ID '|' expr #setVar
    |   ARRAY '|' (global=GLOBAL '|')? name=ID '|' '[' items+= expr ('|' items+= expr)* ']' #setArr
    |   '{' name=ID ('|' index=expr)? '}' #getVar
    |   val=INT #stringTok
    |   val=ID #stringTok
    |   val=STRING #stringTok
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

OUT:    'out';
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