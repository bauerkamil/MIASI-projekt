grammar first;

prog:	stat* EOF ;

stat: expr #expr_stat
    | block #block_stat
    | OUT ex=CONSOLE (':'val=expr)? #print_stat
    ;

block : expr #block_single
    | '>>' block* '>>' #block_real
    ;

file: FILE path=expr #getFile;

expr:
        CALL (':' num=INT) #call
    |   VERB op=(POST|PUT|GET|DELETE) #setMethod
    |   URL val=expr #setUrl
    |   QUERY key=expr ':' val=expr #setQuery
    |   HEADER  key=expr ':' val=expr #setHeader
    |   BODY val=file #setBody
    |   BODY JSON  key=expr ':' val=expr #setBody
    | <assoc=right> VAR (GLOBAL global='true')? ID ':' expr ';' #setVar
    |   ARRAY (GLOBAL global='true')? ID ':[' items+= expr (':' items+= expr)* '];' #setArr
    |   '{' ID (':' index=INT)? '}' #getVar
    |   INT #intTok
    |   STRING #stringTok
    ;

VERB:   'verb:';
POST:   'post';
PUT:    'put';
GET:    'get';
DELETE: 'delete';

VAR:    'var:';
GLOBAL: 'global:';
ARRAY:  'array:';
URL:    'url';

QUERY:  'query:';
HEADER: 'header:';
BODY:   'body:';
FILE:   'file:';
JSON:   'json:';

FOR_:   'for:';

OUT:    'output:';
CONSOLE: 'console:';

CALL:   'call';


//NEWLINE : [\r\n]+ -> skip;
NEWLINE : [\r\n]+ -> channel(HIDDEN);

//WS : [ \t]+ -> skip ;
WS : [ \t]+ -> channel(HIDDEN) ;

INT     : [0-9]+ ;
STRING  : [a-zA-Z0-9_]+;

ID : [a-zA-Z_][a-zA-Z0-9_]* ;

COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~'\n'* '\n' -> channel(HIDDEN) ;