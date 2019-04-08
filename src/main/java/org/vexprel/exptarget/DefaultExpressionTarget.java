package org.vexprel.exptarget;

public class DefaultExpressionTarget implements ExpressionTarget {

    private final Object targetObject;


    public DefaultExpressionTarget(final Object targetObject) {
        super();
        this.targetObject = targetObject;
    }

    @Override
    public Object getTargetObject() {
        return this.targetObject;
    }
}
