package org.exist.xquery.functions.jsch;


import org.exist.EXistException;
import org.exist.security.PermissionDeniedException;
import org.exist.test.ExistEmbeddedServer;
import org.exist.xquery.XPathException;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class JschModuleTest {

    @ClassRule
    public static ExistEmbeddedServer existEmbeddedServer = new ExistEmbeddedServer(false, true);

    @Test
    public void helloWorld() throws XPathException, PermissionDeniedException, EXistException {
        assertTrue(true);
    }
}
