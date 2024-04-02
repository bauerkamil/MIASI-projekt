package compiler;

import grammar.firstLexer;
import interpreter.BaseClass;
import interpreter.ClassType;
import interpreter.Function;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import grammar.firstBaseVisitor;
import grammar.firstParser;

import java.util.ArrayList;
import java.util.List;

public class EmitVisitor extends firstBaseVisitor<ST> {
    private final STGroup stGroup;

    public EmitVisitor(STGroup group) {
        super();
        this.stGroup = group;
    }

    @Override
    protected ST defaultResult() {
        return stGroup.getInstanceOf("deflt");
    }

    @Override
    protected ST aggregateResult(ST aggregate, ST nextResult) {
        if(nextResult!=null)
            aggregate.add("elem",nextResult);
        return aggregate;
    }


    @Override
    public ST visitTerminal(TerminalNode node) {
        return new ST(";Terminal node:<n>").add("n",node.getText());
    }

    @Override
    public ST visitInt_tok(firstParser.Int_tokContext ctx) {
        ST st = stGroup.getInstanceOf("int");
        return st.add("i",ctx.INT().getText());
    }

    @Override
    public ST visitBinOp(firstParser.BinOpContext ctx) {
        ST st = switch (ctx.op.getType()) {
            case firstLexer.ADD -> stGroup.getInstanceOf("add");
            case firstLexer.SUB -> stGroup.getInstanceOf("sub");
            case firstLexer.MUL -> stGroup.getInstanceOf("mul");
            case firstLexer.DIV -> stGroup.getInstanceOf("div");
            default -> null;
        };
        assert st != null;
        return st.add("p1",visit(ctx.l)).add("p2",visit(ctx.r));
    }


    @Override public ST visitInitVar(firstParser.InitVarContext ctx) {
        ST st = stGroup.getInstanceOf("dek");
        return st.add("n",ctx.ID().getText());
    }
    @Override public ST visitAssign(firstParser.AssignContext ctx) {
        ST st = stGroup.getInstanceOf("assign");
        return st.add("id",ctx.ID().getText());
    }

    @Override
    public ST visitGetVar(firstParser.GetVarContext ctx) {
        ST st = stGroup.getInstanceOf("get");
        return st.add("id",ctx.ID().getText());
    }

    @Override
    public ST visitDefineFunc(firstParser.DefineFuncContext ctx) {
        ST st = stGroup.getInstanceOf("def");
        return  st.add("def_name", ctx.def().name.getText())
                .add("body", ctx.def().body);
    }
    @Override public ST visitFunc(firstParser.FuncContext ctx) {
        ST st = stGroup.getInstanceOf("fun");
        return st.add("fun_name", ctx.name.getText());
    }

    @Override
    public ST visitIf_stat(firstParser.If_statContext ctx) {
        ST st = stGroup.getInstanceOf("if_");
        st.add("cond", visit(ctx.cond))
                .add("then_", visit(ctx.then))
                .add("else_", visit(ctx.else_));
        return st;
    }
}
