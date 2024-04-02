       JE MOV A,#1
           ;Terminal node:{
                   MOV A,#1
                   PUSH A
                   MOV A,#1
                   PUSH A
                   MOV A,#1
                   POP B
                   ADD A,B
                   POP B
                   ADD A,B
           ;Terminal node:}
       JMP	label_endif_xx
           ;Terminal node:{
                   MOV A,#3
                   PUSH A
                   MOV A,#2
                   PUSH A
                   MOV A,#1
                   POP B
                   ADD A,B
                   POP B
                   ADD A,B
           ;Terminal node:}
       label_endif_xx:
    ;Terminal node:<EOF>