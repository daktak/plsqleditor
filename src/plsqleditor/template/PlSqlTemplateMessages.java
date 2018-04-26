package plsqleditor.template;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class PlSqlTemplateMessages extends NLS {

    private static final String BUNDLE_NAME= PlSqlTemplateMessages.class.getName();

    private PlSqlTemplateMessages() {
        // Do not instantiate
    }

    public static String Context_error_cannot_evaluate;

    static {
        NLS.initializeMessages(BUNDLE_NAME, PlSqlTemplateMessages.class);
    }
}