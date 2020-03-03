package org.exist.xquery.functions.jsch;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.exist.dom.QName;
import org.exist.dom.memtree.DocumentImpl;
import org.exist.dom.memtree.MemTreeBuilder;
import org.exist.xquery.*;
import org.exist.xquery.value.*;

import java.util.Optional;

import static org.exist.xquery.FunctionDSL.*;
import static org.exist.xquery.functions.jsch.JschModule.functionSignature;

/**
 * Some very simple XQuery example functions implemented
 * in Java.
 */
public class JschFunctions extends BasicFunction {

    protected static final FunctionReturnSequenceType RETURN_TYPE = new FunctionReturnSequenceType(Type.LONG, Cardinality.ZERO_OR_ONE, "an xs:long representing the connection handle");

    protected static final FunctionReturnSequenceType ASSIGNED_PORT_RETURN_TYPE = new FunctionReturnSequenceType(Type.INT, Cardinality.ZERO_OR_ONE, "The assigned local port");

    protected static final FunctionParameterSequenceType SSH_PASSWORD_PARAM = new FunctionParameterSequenceType("password", Type.STRING, Cardinality.EXACTLY_ONE, "The SSH password");

    protected static final FunctionParameterSequenceType SSH_USERNAME_PARAM = new FunctionParameterSequenceType("username", Type.STRING, Cardinality.EXACTLY_ONE, "The SSH username");

    protected static final FunctionParameterSequenceType SSH_HOST_PARAM = new FunctionParameterSequenceType("host", Type.STRING, Cardinality.EXACTLY_ONE, "The SSH session hostname");

    protected static final FunctionParameterSequenceType SSH_LOCAL_PORT_PARAM = new FunctionParameterSequenceType("local-port", Type.INT, Cardinality.EXACTLY_ONE, "The port forwarding local port number");

    protected static final FunctionParameterSequenceType SSH_REMOTE_HOST_PARAM = new FunctionParameterSequenceType("remote-host", Type.STRING, Cardinality.EXACTLY_ONE, "The port forwarding remote hostname");

    protected static final FunctionParameterSequenceType SSH_REMOTE_PORT_PARAM = new FunctionParameterSequenceType("remote-port", Type.INT, Cardinality.EXACTLY_ONE, "The port forwarding remote port number");

    protected static final FunctionParameterSequenceType SSH_SESSION_HANDLE_PARAM = new FunctionParameterSequenceType("handle", Type.LONG, Cardinality.EXACTLY_ONE, "The SSH session handle");

    public final static FunctionSignature[] signatures = {
            new FunctionSignature(
                    new QName("get-session", JschModule.NAMESPACE_URI, JschModule.PREFIX),
                    "Opens an SSH session",
                    new SequenceType[]{SSH_HOST_PARAM, SSH_USERNAME_PARAM, SSH_PASSWORD_PARAM},
                    RETURN_TYPE),

            new FunctionSignature(
                    new QName("forward-port", JschModule.NAMESPACE_URI, JschModule.PREFIX),
                    "Prepares a SQL statement against a SQL db using the connection indicated by the connection handle.",
                    new SequenceType[]{SSH_SESSION_HANDLE_PARAM, SSH_LOCAL_PORT_PARAM, SSH_REMOTE_HOST_PARAM, SSH_REMOTE_PORT_PARAM},
                    ASSIGNED_PORT_RETURN_TYPE
            )
    };

    public JschFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(final Sequence[] args, final Sequence contextSequence) throws XPathException {
        switch (getName().getLocalPart()) {

            case "get-session":
                return getSession(args, contextSequence);

            case "forward-port":
                return forwardPort(args, contextSequence);

            default:
                throw new XPathException(this, "No function: " + getName() + "#" + getSignature().getArgumentCount());
        }
    }

    private Sequence getSession(final Sequence[] args, final Sequence contextSequence) throws XPathException {

        String hostName = args[0].getStringValue();
        String userName = args[1].getStringValue();
        String password = args[2].getStringValue();
        Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            final JSch jsch = new JSch();
            session = jsch.getSession(userName, hostName, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();
            return new IntegerValue(JschModule.storeSession(context, session));
        } catch (JSchException e) {
            throw new XPathException(this, e.getMessage(), e);
        }
    }

    private Sequence forwardPort(final Sequence[] args, final Sequence contextSequence) throws XPathException {

        long sessionUID = ((IntegerValue) args[0].itemAt(0)).getLong();
        Session session = JschModule.retrieveSession(context, sessionUID);
        int localPort = ((IntegerValue) args[1].itemAt(0)).getInt();
        String remoteHostName = args[2].getStringValue();
        int remotePort = ((IntegerValue) args[3].itemAt(0)).getInt();
        int assignedPort;
        try {
            assignedPort = session.setPortForwardingL(localPort, remoteHostName, remotePort);

            return new IntegerValue(assignedPort);
        } catch (JSchException e) {
            throw new XPathException(this, e.getMessage(), e);
        }
    }

}
