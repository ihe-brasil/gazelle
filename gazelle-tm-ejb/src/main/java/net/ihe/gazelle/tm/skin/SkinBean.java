package net.ihe.gazelle.tm.skin;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.Serializable;

@GenerateInterface("SkinLocal")
public class SkinBean implements Serializable, SkinLocal {

    private static final long serialVersionUID = -8314005855253928009L;

    private static final Logger LOG = LoggerFactory.getLogger(SkinBean.class);

    private static final Color REFERENCE = new Color(40, 117, 255);

    public static final String DEFAULT_COLOR_HTML = getHTMLColor(REFERENCE);

    public static String getHTMLColor(Color newColor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getHTMLColor");
        }
        return "#" + getHex(newColor.getRed()) + getHex(newColor.getGreen()) + getHex(newColor.getBlue());
    }

    private static String getHex(int c) {
        String result = Integer.toHexString(c);
        if (result.length() == 1) {
            return "0" + result;
        } else {
            return result;
        }
    }
}
