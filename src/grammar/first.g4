grammar first;

prog:	stat* EOF ;

stat: expr_full #exprStat
    | '>>' #clearBlock
    ;

//get_file: FILE '|' path=expr #getFile;

expr_full: expr ';';

expr:
        FOR_ '|' from=INT '|' to=INT '|' (index_name=ID '|')? body+=expr_full (body+=expr_full)* #forExpr
    |   FOREACH '|' array_name=ID '|' item_name=ID '|' (index_name=ID '|')? body+=expr_full (body+=expr_full)* #foreachExpr
    |   PRINT '|' value #print
    |   CALL ('|' num=value)? #call
    |   OUT '|' CONSOLE ('|' val=value)? #printConsole
    |   OUT '|' FILE '|' path=value #printFile
    |   VERB '|' op=(POST|PUT|GET|DELETE) #setMethod
    |   URL '|' value #setUrl
    |   QUERY '|' key=value '|' val=value #setQuery
    |   HEADER '|'  key=value '|' val=value #setHeader
    |   BODY '|' FILE '|' path=value #setBodyFile
    |   BODY '|' JSON '|'  key=value '|' val=value #setBody
    |   RESPONSE '|' response_key=value '|' var_name=ID #setResponseVar
    |   VAR '|' (global=GLOBAL '|')? name=ID '|' value #setVar
    |   ARRAY '|' (global=GLOBAL '|')? name=ID '|' '[' items+= value ('|' items+= value)* ']' #setArr
    ;

value:
        '{' name=ID ('|' index=value)? '}' #getVar
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
RESPONSE: 'response';

FOR_:   'for';
FOREACH: 'foreach';

PRINT:  'print';

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