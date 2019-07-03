package net.ihe.gazelle.form;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.net.MalformedURLException;
import java.net.URL;

@Name("validate")
@Stateless
@GenerateInterface(value = "ValidateLocal")
public class Validate implements ValidateLocal {

    private static final Logger LOG = LoggerFactory.getLogger(Validate.class);

    public boolean url(String url) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("url");
        }
        boolean status = true;

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            status = false;
            FacesMessages.instance().addToControl("assertionRestApi", "The url is not valid");
        }
        return status;

    }
}
