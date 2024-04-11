package interpreter;

import grammar.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import worker.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateVisitor extends firstBaseVisitor<String> {
    private TokenStream tokStream = null;
    private CharStream input=null;

    private Worker worker = new Worker();
    private final Map<String, String> localVars = new HashMap<>();
    private final Map<String, List<String>> localArrays = new HashMap<>();
    private final Map<String, String>globalVars = new HashMap<>();
    private final Map<String, List<String>> globalArrays = new HashMap<>();

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
        int toValue = Integer.parseInt(ctx.from.getText());
        String indexName = null;
        if (ctx.index_name != null){
            indexName = ctx.index_name.getText();
        }

        for (int i = fromValue; i < toValue; i++) {
            if (indexName != null) {
                this.localVars.put(indexName, String.valueOf(i));
            }

            for (firstParser.Expr_fullContext itemCtx: ctx.body) {
                visit(itemCtx);
            }
        }

        if (indexName != null) {
            this.localVars.remove(indexName);
        }

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

        List<String> array = this.globalArrays.get(arrName);
        if (array == null) {
            array = this.localArrays.get(arrName);
        }

        for (int i = 0; i < array.size(); i++) {
            this.localVars.put(itemName, array.get(i));
            if (indexName != null) {
                this.localVars.put(indexName, String.valueOf(i));
            }

            for (firstParser.Expr_fullContext itemCtx: ctx.body) {
                visit(itemCtx);
            }

        }

        this.localVars.remove(itemName);
        if (indexName != null) {
            this.localVars.remove(indexName);
        }

        return "";
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
        //TODO: val to print
        this.worker.setConsoleEnabled(true);
        return "";
    }

    @Override
    public String visitPrintFile(firstParser.PrintFileContext ctx) {
        //TODO
        return super.visitPrintFile(ctx);
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
    public String visitSetVar(firstParser.SetVarContext ctx) {
        String name = ctx.ID().getText();

        if(ctx.global != null) {
            if (!localVars.containsKey(name)
                && !localArrays.containsKey(name)
                && !globalArrays.containsKey(name)) {

                this.globalVars.put(name, visit(ctx.value()));
            }
            return "";
        }

        if (!globalVars.containsKey(name)
                && !localArrays.containsKey(name)
                && !globalArrays.containsKey(name)) {

            this.localVars.put(name, visit(ctx.value()));
        }
        return "";
    }

    @Override
    public String visitSetArr(firstParser.SetArrContext ctx) {
        String name = ctx.ID().getText();

        List<String> items = new ArrayList<>();
        for (firstParser.ValueContext itemCtx: ctx.items) {
            items.add(visit(itemCtx));
        }

        if(ctx.global != null) {
            if (!localVars.containsKey(name)
                    && !globalVars.containsKey(name)
                    && !localArrays.containsKey(name)) {

                this.globalArrays.put(name, items);
            }
            return "";
        }

        if (!localVars.containsKey(name)
                && !globalVars.containsKey(name)
                && !globalArrays.containsKey(name)) {

            this.localArrays.put(name, items);
        }
        return "";
    }

    @Override
    public String visitGetVar(firstParser.GetVarContext ctx) {
        String name = ctx.name.getText();
        Integer number = null;
        if (ctx.index != null)
            number = Integer.parseInt(visit(ctx.index));

        if (number == null && this.localVars.containsKey(name)) {
            return this.localVars.get(name);
        }

        if (number == null && this.globalVars.containsKey(name)) {
            return this.globalVars.get(name);
        }

        if (number != null && this.localArrays.containsKey(name)) {
            return this.localArrays.get(name).get(number);
        }

        if (number != null && this.globalArrays.containsKey(name)) {
            return this.globalArrays.get(name).get(number);
        }

        throw new RuntimeException("Variable " + name + " does not exist or is nor defined correctly");
    }

    @Override
    public String visitStringTok(firstParser.StringTokContext ctx) {
        return ctx.val.getText();
    }

    @Override
    public String visitBlockTok(firstParser.BlockTokContext ctx) {
        //TODO: check
        super.visitBlockTok(ctx);
        this.worker = new Worker();
        this.localVars.clear();
        this.localArrays.clear();

        return "";
    }

}
