package interpreter;

import SymbolTable.GlobalSymbols;
import SymbolTable.LocalSymbols;
import grammar.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class CalculateVisitor extends firstBaseVisitor<BaseClass> {
    private TokenStream tokStream = null;
    private CharStream input=null;

    private GlobalSymbols<Function> functions;
    private LocalSymbols<BaseClass> variables;
    public CalculateVisitor(CharStream inp) {
        super();
        this.input = inp;
    }

    public CalculateVisitor(TokenStream tok) {
        super();
        this.tokStream = tok;
    }
    public CalculateVisitor(CharStream inp, TokenStream tok) {
        super();
        this.input = inp;
        this.tokStream = tok;
    }
    private String getText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if(input==null) throw new RuntimeException("Input stream undefined");
        return input.getText(new Interval(a,b));
    }
    @Override
    public BaseClass visitIf_stat(firstParser.If_statContext ctx) {
        BaseClass result = null;
        if (visit(ctx.cond).toBoolean()) {
            result = visit(ctx.then);
        }
        else {
            if(ctx.else_ != null)
                result = visit(ctx.else_);
        }
        return result;
    }
    @Override
    public BaseClass visitDefineFunc(firstParser.DefineFuncContext ctx) {
        String name = ctx.def().name.getText();
        this.functions.newSymbol(name);
        Function fun = new Function();
        fun.body = ctx.def().body;
        List<String> args = new ArrayList<>();
        for (Token argToken: ctx.def().args) {
            args.add((argToken.getText()));
        }
        fun.args = args;
        this.functions.setSymbol(name, fun);
        return null;
    }

    @Override public BaseClass visitCallFunc(firstParser.CallFuncContext ctx) {
        Function fun = this.functions.getSymbol(ctx.func().name.getText());

        if (ctx.func().args.size() != fun.args.size()) {
            throw new RuntimeException("wrong number of parameters");
        }
        variables.enterScope();
        int argsSize = fun.args.size();
        for (int i = 0; i < argsSize; i ++) {
            variables.newSymbol(fun.args.get(i));
            variables.setSymbol(fun.args.get(i), visit(ctx.func().args.get(i)));
        }
        visit(fun.body);
        variables.leaveScope();
        return null;
    }

    @Override public BaseClass visitInitVar(firstParser.InitVarContext ctx) {
        this.variables.newSymbol(ctx.ID().getText());
        return null;
    }
    @Override public BaseClass visitAssign(firstParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        if (!this.variables.hasSymbol(name)) {
            throw new RuntimeException("Undefined variable");
        }
        this.variables.setSymbol(name, visit(ctx.expr()));
        return null;
    }

    @Override
    public BaseClass visitGetVar(firstParser.GetVarContext ctx) {
        return this.variables.getSymbol(ctx.getText());
    }

    @Override
    public BaseClass visitPrint_stat(firstParser.Print_statContext ctx) {
        var st = ctx.expr();
        var result = visit(st);
        //System.out.printf("|%s=%s|\n", st.getText(), result.toString()); //nie drukuje ukrytych ani pominiętych spacji
        //System.out.printf("|%s=%f|\n", getText(st),  result); //drukuje wszystkie spacje
        System.out.printf("|%s=%s|\n", tokStream.getText(st), result); //drukuje spacje z ukrytego kanału, ale nie ->skip
        return result;
    }

    @Override
    public BaseClass visitInt_tok(firstParser.Int_tokContext ctx) {
        return new BaseClass(ctx.INT().getText(), ClassType.Int);
    }

    @Override
    public BaseClass visitDouble_tok(firstParser.Double_tokContext ctx) {
        return new BaseClass(ctx.DOU().getText(), ClassType.Double);
    }

    @Override public BaseClass visitBool_tok(firstParser.Bool_tokContext ctx) {
        return new BaseClass(ctx.BOOL().getText(), ClassType.Boolean);
    }

    @Override
    public BaseClass visitPars(firstParser.ParsContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public BaseClass visitBinOp(firstParser.BinOpContext ctx) {
        BaseClass val1 = visit(ctx.l);
        BaseClass val2 = visit(ctx.r);
        if (val1.getType() == ClassType.Boolean || val2.getType() == ClassType.Boolean ) {
            System.err.println("Incompatible type");
            throw new InvalidParameterException();
        }
        boolean isAnyDouble = (val1.getType() == ClassType.Double || val2.getType() == ClassType.Double);
        switch (ctx.op.getType()) {
            case firstLexer.ADD:
                if (isAnyDouble) {
                    return new BaseClass(
                            val1.toDouble() + val2.toDouble(),
                            ClassType.Double
                    );
                } else {
                    return new BaseClass(
                            val1.toInt() + val2.toInt(),
                            ClassType.Int
                    );
                }
            case firstLexer.SUB:
                if (isAnyDouble) {
                    return new BaseClass(
                            val1.toDouble() - val2.toDouble(),
                            ClassType.Double
                    );
                } else {
                    return new BaseClass(
                            val1.toInt() - val2.toInt(),
                            ClassType.Int
                    );
                }
            case firstLexer.MUL:
                if (isAnyDouble) {
                    return new BaseClass(
                            val1.toDouble() * val2.toDouble(),
                            ClassType.Double
                    );
                } else {
                    return new BaseClass(
                            val1.toInt() * val2.toInt(),
                            ClassType.Int
                    );
                }
            case firstLexer.DIV:
                if (val2.toDouble() == 0.0) {
                    System.err.println("Div by zero");
                    throw new ArithmeticException();
                }
                return new BaseClass(
                        val1.toDouble() / val2.toDouble(),
                        ClassType.Double
                );
        }
        return null;
    }


    @Override
    public BaseClass visitLogicOp(firstParser.LogicOpContext ctx) {
        switch (ctx.op.getType()) {
            case firstLexer.EQ, firstLexer.NEQ:
                Boolean eq = visit(ctx.l).equals(visit(ctx.r));
                return new BaseClass(eq, ClassType.Boolean);
            case firstLexer.NOT:
                BaseClass notVal = visit(ctx.r);
                if (notVal.getType() == ClassType.Boolean) {
                    return new BaseClass(!notVal.toBoolean(), ClassType.Boolean);
                } else {
                    System.err.println("Incompatible type");
                    throw new InvalidParameterException();
                }
            case firstLexer.AND, firstLexer.OR:
                BaseClass val1 = visit(ctx.l);
                BaseClass val2 = visit(ctx.r);
                if (val1.getType() != ClassType.Boolean || val2.getType() != ClassType.Boolean) {
                    System.err.println("Incompatible type");
                    throw new InvalidParameterException();
                }
                if (ctx.op.getType() == firstLexer.AND) {
                    return new BaseClass(val1.toBoolean() && val2.toBoolean(), ClassType.Boolean);
                } else {
                    return new BaseClass(val1.toBoolean() || val2.toBoolean(), ClassType.Boolean);
                }

        }
        return null;
    }

}
