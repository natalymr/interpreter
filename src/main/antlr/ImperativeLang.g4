grammar ImperativeLang;


program
   :  read basicBlock+
   ;

read
   : READ variable (COMA variable)* SEMICOLON
   ;

basicBlock
   : label COLON (assignment)* jump
   ;

assignment
   : variable ASSIGNMENT expression SEMICOLON
   ;

jump
   : GOTO gotoLabel=label SEMICOLON
   | IF left=expression (relop right=expression)? GOTO thenLabel=label ELSE elseLabel=label SEMICOLON
   | RETURN returnedExpression=expression SEMICOLON
   ;

expression
   : signedVariable
   | left=expression (PLUS | MINUS) right=expression
   ;

signedVariable
   : PLUS signedVariable
   | MINUS signedVariable
   | variable
   | number
   ;

label
   : (variable | number)
   ;

number
   : NUMBER
   ;

variable
   : VARIABLE
   ;

relop
   : EQ
   | GT
   | LT
   ;


READ
   : 'read'
   ;


GOTO
   : 'goto'
   ;


RETURN
   : 'return'
   ;


IF
   : 'if'
   ;


ELSE
   : 'else'
   ;


ASSIGNMENT
   : ':='
   ;


PLUS
   : '+'
   ;


MINUS
   : '-'
   ;


TIMES
   : '*'
   ;


DIV
   : '/'
   ;


GT
   : '>'
   ;


LT
   : '<'
   ;


EQ
   : '='
   ;


SEMICOLON
   : ';'
   ;


COLON
   : ':'
   ;


COMA
   : ','
   ;


VARIABLE
   : VALID_ID_START VALID_ID_CHAR*
   ;


NUMBER
   : (SIGN)? UNSIGN_NUMBER
   ;


fragment VALID_ID_START
   : ('a' .. 'z') | ('A' .. 'Z') | '_'
   ;


fragment VALID_ID_CHAR
   : VALID_ID_START | ('0' .. '9')
   ;


fragment UNSIGN_NUMBER
   : ('0' .. '9') + ('.' ('0' .. '9') +)?
   ;


fragment SIGN
   : ('+' | '-')
   ;


WS
   : [ \r\n\t] + -> skip
   ;