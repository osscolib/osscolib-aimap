package org.vexprel;

import org.junit.Test;
import org.vexprel.model.User;
import org.vexprel.standard.StandardExpressionProcessor;

public class VexprelTest {


    @Test
    public void testVexprel01() throws Exception {

        final User user = new User("John", "Apricot");

        final ExpressionProcessor expressionProcessor = new StandardExpressionProcessor();

        final Expression expression = expressionProcessor.parse("name");

        System.out.println(expression);

        final Expression expression2 = expressionProcessor.parse("name.surname");

        System.out.println(expression2);

    }


}
