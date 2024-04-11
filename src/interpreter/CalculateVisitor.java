package interpreter;

import grammar.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import variable_manager.VariableManager;
import worker.Worker;

import java.util.ArrayList;
import java.util.List;

public class CalculateVisitor extends firstBaseVisitor<String> {
    private TokenStream tokStream = null;
    private CharStream input=null;

    private final VariableManager variableManager = new VariableManager();
    private Worker worker = new Worker(variableManager);

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

    //TODO: save return from call to variable??
    @Override
    public String visitForExpr(firstParser.ForExprContext ctx) {
        int fromValue = Integer.parseInt(ctx.from.getText());
        int toValue = Integer.parseInt(ctx.to.getText());
        String indexName = null;
        if (ctx.index_name != null){
            indexName = ctx.index_name.getText();
        }

        for (int i = fromValue; i < toValue; i++) {
            if (indexName != null) {
                this.variableManager.putVar(indexName, String.valueOf(i), false);
            }

            for (firstParser.Expr_fullContext itemCtx: ctx.body) {
                visit(itemCtx);
            }
        }
//
//        if (indexName != null) {
//            this.localVars.remove(indexName);
//        }

        return "";
    }

    @Override
    public String visitForeachExpr(firstParser.ForeachExprContext ctx) {
        String arrName = ctx.array_name.getText();
        String itemName = ctx.item_name.getText();
        String indexName = null;
        if (ctx.index_name != null){
            indexName = ctx.index_name.getText();
        }

        List<String> array = this.variableManager.getArray(arrName);

        for (int i = 0; i < array.size(); i++) {
            this.variableManager.putVar(itemName, array.get(i), false);

            if (indexName != null) {
                this.variableManager.putVar(indexName, String.valueOf(i), false);
            }

            for (firstParser.Expr_fullContext itemCtx: ctx.body) {
                visit(itemCtx);
            }

        }
//
//        this.localVars.remove(itemName);
//        if (indexName != null) {
//            this.localVars.remove(indexName);
//        }

        return "";
    }

    @Override
    public String visitPrint(firstParser.PrintContext ctx) {
        var st = ctx.value();
        var result = visit(st);
        System.out.printf("> %s\n", result);
        return result;
    }

    @Override
    public String visitCall(firstParser.CallContext ctx) {
        int number = 1;
        if (ctx.num != null)
            number = Integer.parseInt(visit(ctx.num));

        for(int i=0; i<number; i++){
            worker.sendRequest();
        }

        return "";
    }

    @Override
    public String visitPrintConsole(firstParser.PrintConsoleContext ctx) {
        this.worker.setConsoleEnabled(true);
        return "";
    }

    @Override
    public String visitPrintFile(firstParser.PrintFileContext ctx) {
        this.worker.setOutputFile(visit(ctx.path));
        return "";
    }

    @Override
    public String visitSetMethod(firstParser.SetMethodContext ctx) {
        this.worker.setMethod(ctx.op.getText().toUpperCase());
        return "";
    }

    @Override
    public String visitSetUrl(firstParser.SetUrlContext ctx) {
        this.worker.setUrl(visit(ctx.value()));
        return "";
    }

    @Override
    public String visitSetQuery(firstParser.SetQueryContext ctx) {
        this.worker.updateQuery(visit(ctx.key), visit(ctx.val));
        return "";
    }

    @Override
    public String visitSetHeader(firstParser.SetHeaderContext ctx) {
        this.worker.updateHeader(visit(ctx.key), visit(ctx.val));
        return "";
    }

    @Override
    public String visitSetBody(firstParser.SetBodyContext ctx) {
        this.worker.updateRequestBody(visit(ctx.key), visit(ctx.val));
        return "";
    }

    @Override
    public String visitSetBodyFile(firstParser.SetBodyFileContext ctx) {
        this.worker.readRequestBody(visit(ctx.path));
        return "";
    }

    @Override
    public String visitSetResponseVar(firstParser.SetResponseVarContext ctx) {
        this.worker.updateResponseVariables(visit(ctx.response_key), ctx.var_name.getText());
        return "";
    }

    @Override
    public String visitSetVar(firstParser.SetVarContext ctx) {
        String name = ctx.ID().getText();
        String value = visit(ctx.value());

        this.variableManager.putVar(name, value, ctx.global != null);

        return "";
    }

    @Override
    public String visitSetArr(firstParser.SetArrContext ctx) {
        String name = ctx.ID().getText();

        List<String> items = new ArrayList<>();
        for (firstParser.ValueContext itemCtx: ctx.items) {
            items.add(visit(itemCtx));
        }

        this.variableManager.putArray(name, items, ctx.global != null);

        return "";
    }

    @Override
    public String visitGetVar(firstParser.GetVarContext ctx) {
        String name = ctx.name.getText();
        Integer number = null;
        if (ctx.index != null)
            number = Integer.parseInt(visit(ctx.index));

        return this.variableManager.getVar(name, number);
    }

    @Override
    public String visitStringTok(firstParser.StringTokContext ctx) {
        return ctx.val.getText().replace("__", " ");
    }

    @Override
    public String visitConcat(firstParser.ConcatContext ctx) {
        return visit(ctx.left) + visit(ctx.right);
    }

    @Override
    public String visitClearBlock(firstParser.ClearBlockContext ctx) {
        this.worker = new Worker(this.variableManager);
        this.variableManager.clearLocalVars();

        return "";
    }

}
