package net.ihe.gazelle.facesMessages;

import org.richfaces.renderkit.html.HtmlMessageRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.context.ResponseWriterWrapper;
import java.io.IOException;

/**
 * Created by jlabbe on 18/03/15.
 */
public class EscapableMessagesRenderer extends HtmlMessageRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(EscapableMessagesRenderer.class);

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("encodeEnd");
        }
        final ResponseWriter originalResponseWriter = context.getResponseWriter();

        try {
            context.setResponseWriter(new ResponseWriterWrapper() {

                @Override
                public ResponseWriter getWrapped() {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getWrapped");
                    }
                    return originalResponseWriter;
                }

                @Override
                public void writeText(Object text, UIComponent component, String property) throws IOException {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("writeText");
                    }
                    String string = String.valueOf(text);
                    String escape = (String) component.getAttributes().get("escape");
                    if (escape != null && !Boolean.valueOf(escape)) {
                        super.write(string);
                    } else {
                        super.writeText(string, component, property);
                    }
                }
            });

            super.encodeEnd(context, component); // Now, render it!
        } finally {
            context.setResponseWriter(originalResponseWriter); // Restore original writer.
        }
    }
}
