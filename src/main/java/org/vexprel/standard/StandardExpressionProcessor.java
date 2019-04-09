package org.vexprel.standard;

import org.vexprel.Expression;
import org.vexprel.ExpressionProcessor;
import org.vexprel.context.ExpressionContext;
import org.vexprel.exptarget.ExpressionTarget;
import org.vexprel.standard.action.ByteCodeGenStandardExpressionActionFactory;

public class StandardExpressionProcessor implements ExpressionProcessor {

    private final StandardExpressionParser parser;
    private final StandardExpressionExecutor executor;


    public StandardExpressionProcessor() {
        super();
        this.parser = new StandardExpressionParser();
        this.executor = new StandardExpressionExecutor(new ByteCodeGenStandardExpressionActionFactory(true));
    }


    @Override
    public Expression parse(final String expression) {
        return this.parser.parse(expression);
    }


    @Override
    public Object execute(final ExpressionContext context, final Expression expression, final ExpressionTarget target) {
        if (expression != null && !(expression instanceof StandardExpression)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot execute expression of class '%s'. This executor can only process " +
                            "instances of '%s'", expression.getClass().getName(), StandardExpression.class.getName()));
        }
        return this.executor.execute(context, (StandardExpression) expression, target);
    }

}
