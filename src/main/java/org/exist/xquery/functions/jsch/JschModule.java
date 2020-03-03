package org.exist.xquery.functions.jsch;

import com.jcraft.jsch.Session;
import org.exist.dom.QName;
import org.exist.xquery.*;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.exist.xquery.FunctionDSL.functionDefs;

/**
 * A very simple example XQuery Library Module implemented
 * in Java.
 */
public class JschModule extends AbstractInternalModule {

    public static final String NAMESPACE_URI = "https://exist-db.org/exist-db/ns/app/jsch";
    public static final String PREFIX = "ssh";
    public static final String RELEASED_IN_VERSION = "eXist-3.6.0";

    // register the functions of the module
    public static final FunctionDef[] functions = functionDefs(
        functionDefs(JschFunctions.class, JschFunctions.signatures)
    );

    public final static String SESSIONS_CONTEXTVAR = "_exist_ssh_sessions";

    public JschModule(final Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "Example Module for eXist-db XQuery";
    }

    @Override
    public String getReleaseVersion() {
        return RELEASED_IN_VERSION;
    }

    public static Session retrieveSession(XQueryContext context, long sessionUID) {
        return ModuleUtils.retrieveObjectFromContextMap(context, JschModule.SESSIONS_CONTEXTVAR, sessionUID);
    }

    public static synchronized long storeSession(XQueryContext context, Session session) {
        return ModuleUtils.storeObjectInContextMap(context, JschModule.SESSIONS_CONTEXTVAR, session);
    }

    /**
     * Resets the Module Context and closes any DB connections for the XQueryContext.
     *
     * @param xqueryContext The XQueryContext
     */
    @Override
    public void reset(XQueryContext xqueryContext, boolean keepGlobals) {
        // reset the module context
        super.reset(xqueryContext, keepGlobals);

        // close any open Sessions
        closeAllSessions(xqueryContext);
    }

    private static void closeAllSessions(XQueryContext context) {
        ModuleUtils.modifyContextMap(context, JschModule.SESSIONS_CONTEXTVAR, new ModuleUtils.ContextMapEntryModifier<Session>() {

            @Override
            public void modify(Map<Long, Session> map) {
                super.modify(map);

                // empty the map
                map.clear();
            }

            @Override
            public void modify(Entry<Long, Session> entry) {
                final Session session = entry.getValue();
                if (session.isConnected()) {
                    session.disconnect();
                }
            }
        });
    }

    static FunctionSignature functionSignature(final String name, final String description,
            final FunctionReturnSequenceType returnType, final FunctionParameterSequenceType... paramTypes) {
        return FunctionDSL.functionSignature(new QName(name, NAMESPACE_URI), description, returnType, paramTypes);
    }

    static FunctionSignature[] functionSignatures(final String name, final String description,
            final FunctionReturnSequenceType returnType, final FunctionParameterSequenceType[][] variableParamTypes) {
        return FunctionDSL.functionSignatures(new QName(name, NAMESPACE_URI), description, returnType, variableParamTypes);
    }

    static class ExpathBinModuleErrorCode extends ErrorCodes.ErrorCode {
        private ExpathBinModuleErrorCode(final String code, final String description) {
            super(new QName(code, NAMESPACE_URI, PREFIX), description);
        }
    }
}
