package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.tm.gazelletest.model.definition.ContextualInformation;
import net.ihe.gazelle.tm.gazelletest.model.definition.Path;
import net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author abderrazek boufahja
 */
public class Simulator2GazelleClassConverter {

    public static ContextualInformation convertContextualInformation(
            net.ihe.gazelle.simulator.ws.ContextualInformation inContextualInformation) {
        if (inContextualInformation != null) {
            ContextualInformation ci = new ContextualInformation();
            ci.setLabel(inContextualInformation.getLabel());
            ci.setPath(new Path());
            ci.getPath().setDescription(inContextualInformation.getPath().getDescription());
            ci.getPath().setKeyword(inContextualInformation.getPath().getKeyword());
            ci.getPath().setType(inContextualInformation.getPath().getType());
            ci.setValue(inContextualInformation.getValue());
            return ci;
        }
        return null;
    }

    public static ContextualInformationInstance convertContextualInformationInstance(
            net.ihe.gazelle.simulator.ws.ContextualInformationInstance inContextualInformationInstance) {
        if (inContextualInformationInstance != null) {
            ContextualInformationInstance cii = new ContextualInformationInstance();
            if (inContextualInformationInstance.getContextualInformation() != null) {
                ContextualInformation ci = convertContextualInformation(inContextualInformationInstance
                        .getContextualInformation());
                cii.setContextualInformation(ci);
            }
            cii.setValue(inContextualInformationInstance.getValue());
            return cii;
        }
        return null;
    }

    public static List<ContextualInformationInstance> convertListContextualInformationInstance(
            List<net.ihe.gazelle.simulator.ws.ContextualInformationInstance> inListContextualInformationInstance) {
        if (inListContextualInformationInstance != null) {
            List<ContextualInformationInstance> lcii = new ArrayList<ContextualInformationInstance>();
            for (net.ihe.gazelle.simulator.ws.ContextualInformationInstance inContextualInformationInstance : inListContextualInformationInstance) {
                ContextualInformationInstance cii = convertContextualInformationInstance(inContextualInformationInstance);
                lcii.add(cii);
            }
            return lcii;
        }
        return null;
    }

    public static ContextualInformationInstance[] convertListContextualInformationInstanceToArray(
            List<net.ihe.gazelle.simulator.ws.ContextualInformationInstance> inListContextualInformationInstance) {
        List<ContextualInformationInstance> lcii = convertListContextualInformationInstance(inListContextualInformationInstance);
        ContextualInformationInstance[] tcii = null;
        if (lcii != null) {
            tcii = lcii.toArray(new ContextualInformationInstance[lcii.size()]);
        }
        return tcii;
    }

}
