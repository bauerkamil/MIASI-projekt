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

public class CalculateVisitor extends firstBaseVisitor<String> {
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
    public String visitIf_stat(firstParser.If_statContext ctx) {

        if (visit(ctx.cond).toBoolean()) {
            visit(ctx.then);
        }
        else {
            if(ctx.else_ != null)
                visit(ctx.else_);
        }
        return;
    }
    @Override
    public String visitDefineFunc(firstParser.DefineFuncContext ctx) {
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
        return;
    }

    @Override public String visitCallFunc(firstParser.CallFuncContext ctx) {
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
        return;
    }

    @Override public String visitInitVar(firstParser.InitVarContext ctx) {
        this.variables.newSymbol(ctx.ID().getText());
        return;
    }
    @Override public String visitAssign(firstParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        if (!this.variables.hasSymbol(name)) {
            throw new RuntimeException("Undefined variable");
        }
        this.variables.setSymbol(name, visit(ctx.expr()));
        return;
    }

    @Override
    public String visitGetVar(firstParser.GetVarContext ctx) {
        return this.variables.getSymbol(ctx.getText());
    }

    @Override
    public String visitPrint_stat(firstParser.Print_statContext ctx) {
        var st = ctx.expr();
        var result = visit(st);
        //System.out.printf("|%s=%s|\n", st.getText(), result.toString()); //nie drukuje ukrytych ani pominiętych spacji
        //System.out.printf("|%s=%f|\n", getText(st),  result); //drukuje wszystkie spacje
        System.out.printf("|%s=%s|\n", tokStream.getText(st), result); //drukuje spacje z ukrytego kanału, ale nie ->skip
        return;
    }

}
