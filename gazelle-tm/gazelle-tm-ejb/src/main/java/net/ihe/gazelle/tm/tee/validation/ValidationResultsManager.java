package net.ihe.gazelle.tm.tee.validation;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgValidationDAO;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMsgValidation;
import net.ihe.gazelle.tm.tee.model.ValidationService;
import net.ihe.gazelle.tm.tee.util.TEEConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages TmStepInstanceMsgValidation Objects.
 * Retrieves them from database and transforms them
 * utilizing the defined XSLTs.
 *
 * @author rizwan.tanoli@aegis.net
 */
@Name(value = "validationResultsManager")
@Scope(ScopeType.PAGE)
public class ValidationResultsManager implements Serializable {

    private static final long serialVersionUID = -2279455047476086225L;
    private static final Logger LOG = LoggerFactory.getLogger(ValidationResultsManager.class);

    public ValidationResultsManager() {

    }

    /**
     * This method transforms the provided TmStepInstanceMsgValidation object
     * into a human readable HTML report. The transformation is done in context
     * of the ValidationService used to generated the TmStepInstanceMsgValidation.
     *
     * @param validationResultMessage
     * @param service
     * @return
     */
    public String transform(TmStepInstanceMsgValidation validationResultMessage, ValidationService service) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("transform");
        }
        return transform(validationResultMessage.getValidationReport(), service);
    }

    /**
     * This method takes the contents and transforms them based on the ValidationService
     * specified. Each ValidationService has it's own specific XSLT that is applied
     * to transform the validationResultContents.
     *
     * @param validationResultContents
     * @param service
     * @return String -representing the transformed contents.
     */
    public String transform(byte[] validationResultContents, ValidationService service) {

        String result = StringUtils.EMPTY;
        if (validationResultContents == null) {
            return result;
        }
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Source xsltTransformations = new StreamSource(getXSLTByValidationService(service));
            Source validationResults = new StreamSource(new ByteArrayInputStream(validationResultContents));

            Transformer validationResultTransformer = factory.newTransformer(xsltTransformations);
            ByteArrayOutputStream transformedValidationReport = new ByteArrayOutputStream();

            validationResultTransformer.transform(validationResults, new StreamResult(transformedValidationReport));

            transformedValidationReport.close();
            result = transformedValidationReport.toString();

        } catch (TransformerConfigurationException e) {
            LOG.error("Error configuring transformer: validationResultTransformer. XSLT being applied for ValidationService: " + service.getKey(), e);
        } catch (TransformerException e) {
            LOG.error("Error occured during transformation. Contents being transformed: " + new String(validationResultContents, Charset.forName
                    ("UTF-8")) + ", validation service: " + service.getKey(), e);
        } catch (IOException e) {
            LOG.error("Error closing ByteOutputStream: transformedValidationReport", e);
        }

        return result;
    }

    private InputStream getXSLTByValidationService(ValidationService service) {
        InputStream xsltContents = null;
        try {
            URL xsltURL = new URL(service.getXsltUrl());
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet requestXslt = new HttpGet(xsltURL.toURI());
            HttpResponse responseXslt = client.execute(requestXslt);

            if (responseXslt.getStatusLine().getStatusCode() == TEEConstants.HTTP_STATUS_CODE_OK) {
                xsltContents = responseXslt.getEntity().getContent();
            } else {
                throw new TestExecutionException("Response returned for XSLT is not valid. HTTP Response Code:" + responseXslt.getStatusLine()
                        .getStatusCode() + " - " + responseXslt.getStatusLine().getReasonPhrase());
            }

        } catch (MalformedURLException e) {
            LOG.error("The XSLT URL is invalid, URL = " + service.getXsltUrl() + ", validationService = " + service.getKey(), e);
        } catch (ClientProtocolException e) {
            LOG.error("The URL has an invalid protocol, URL = " + service.getXsltUrl() + ", validationService = " + service.getKey(), e);
        } catch (IOException e) {
            LOG.error("Accessing URL gave IO exception,  URL = " + service.getXsltUrl() + ", validationService = " + service.getKey(), e);
        } catch (URISyntaxException e) {
            LOG.error("The URL has syntax issue, URL = " + service.getXsltUrl() + ", validationService = " + service.getKey(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return xsltContents;
    }

    public String fetchTransformedValidationMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchTransformedValidationMessage");
        }
        String vrId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("vrId");
        vrId = vrId != null ? vrId : StringUtils.EMPTY;
        if (StringUtils.isEmpty(vrId)) {
            String errorMessage = "Please supply a Validation Record Id to retrieve report.";
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, errorMessage);
            return errorMessage;
        }

        TmStepInstanceMsgValidation validationMessage = (new StepInstanceMsgValidationDAO(EntityManagerService.provideEntityManager()).find(Integer
                .valueOf(vrId).intValue()));
        return transform(validationMessage);

    }

    public List<TmStepInstanceMsgValidation> getValidationsFor(TmStepInstanceMessage message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationsFor");
        }
        List<TmStepInstanceMsgValidation> validations = new ArrayList<TmStepInstanceMsgValidation>();
        validations.addAll(message.getTmStepInstanceMsgValidations());
        return validations;
    }

    /**
     * returns list of validation messages captured for this TmStepInstanceMessage
     *
     * @return
     */
    public String transform(TmStepInstanceMsgValidation curr) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("transform");
        }
        if (curr != null) {
            return transform(curr, curr.getMessageValidationService().getValidationService());
        } else {
            return StringUtils.EMPTY;
        }
    }

}
