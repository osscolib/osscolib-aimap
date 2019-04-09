package org.vexprel.standard.step;

public class ObjectPropertyExpressionStep implements StandardExpressionStep {

    private final String propertyName;
    private final String getterMethodName;


    public ObjectPropertyExpressionStep(final String propertyName) {
        super();
        if (propertyName == null || propertyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Property name cannot be null or empty");
        }
        this.propertyName = propertyName;
        this.getterMethodName = "get" + (Character.toUpperCase(propertyName.charAt(0))) + propertyName.substring(1);
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public String getGetterMethodName() {
        return this.getterMethodName;
    }

    @Override
    public String getStringRepresentation() {
        return this.propertyName;
    }


    @Override
    public String toString() {
        return String.format("(ObjectProperty: '%s')",this.propertyName);
    }

}
