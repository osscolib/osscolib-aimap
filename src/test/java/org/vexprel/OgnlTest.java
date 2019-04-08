package org.vexprel;

import java.util.HashMap;

import ognl.Ognl;
import ognl.OgnlContext;
import org.junit.Test;
import org.vexprel.model.User;

public class OgnlTest {


    @Test
    public void testOgnl01() throws Exception {

        final User user = new User("John", "Apricot");

        final Object ognlParsed = Ognl.parseExpression("name");

        final OgnlContext ognlContext = new OgnlContext(new HashMap());

        final Object result = Ognl.getValue(ognlParsed, ognlContext, user);

        System.out.println("RESULT WITH OGNL: " + result);

    }


    @Test
    public void testOgnl02() throws Exception {
        final User user = new User("John", "Apricot");
        System.out.println(Ognl.getValue(null, user));
    }

}
