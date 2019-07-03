/*
 * Copyright 2009 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.tm.gazelletest.action;

import gui.ava.html.image.generator.HtmlImageGenerator;
import net.ihe.gazelle.common.filecache.FileCache;
import net.ihe.gazelle.common.filecache.FileCacheRenderer;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.bean.DiagramGenerator;
import net.ihe.gazelle.tm.gazelletest.model.definition.GazelleLanguageQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestDescription;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author Abderrazek Boufahja > INRIA Rennes IHE development Project
 */

@Name("testSequenceManager")
@Scope(ScopeType.PAGE)
@GenerateInterface("TestSequenceManagerLocal")
public class TestSequenceManager implements TestSequenceManagerLocal, Serializable {

    private static final long serialVersionUID = -1067456282918849503L;

    private static final Logger LOG = LoggerFactory.getLogger(TestSequenceManager.class);

    // methods //////////////////////////////////////////////////////////////////////

    @Override
    public String getPageId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPageId");
        }
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
    }

    public String getLanguageId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLanguageId");
        }
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("languageId");
    }

    @Override
    public Test getTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTest");
        }
        String pageId = this.getPageId();

        Test currentTest = null;
        if (pageId != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            int id = Integer.parseInt(pageId);
            currentTest = em.find(Test.class, id);
        }
        return currentTest;
    }

    private TestInstance getTestInstance() {
        String pageId = this.getPageId();

        TestInstance currentTestInstance = null;
        if (pageId != null) {
            try {
                int id = Integer.parseInt(pageId);
                EntityManager em = EntityManagerService.provideEntityManager();
                currentTestInstance = em.find(TestInstance.class, id);
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
        return currentTestInstance;
    }

    @Override
    public void getTestDescriptionImage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestDescriptionImage");
        }
        try {
            String languageId = getLanguageId();
            if (languageId != null) {
                this.getTestDescriptionImage(getTest(), Integer.parseInt(languageId));
            } else{
                //Default id for English
                this.getTestDescriptionImage(getTest(), 3);
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private void getTestDescriptionImage(Test test, int languageId) {
        String htmlContent = "";
        TestDescription td = null;
        td = getTestDescriptionFromLanguage(test, td, languageId);

        if (td != null) {
            htmlContent = td.getDescription();
            try {
                FileCache.getFile("DescriptionImage_" + test.getKeyword(), htmlContent, new FileCacheRenderer() {
                    @Override
                    public void render(OutputStream out, String value) throws Exception {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("render");
                        }
                        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
                        imageGenerator.loadHtml("<div style=\"width:1000px;font-size:14;\">" + value + "</div>");
                        ImageIO.write(imageGenerator.getBufferedImage(), "PNG", out);
                    }

                    @Override
                    public String getContentType() {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getContentType");
                        }
                        return "image/png";
                    }
                });
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    private TestDescription getTestDescriptionFromLanguage(Test test, TestDescription td, int languageId) {
        List<String> languageDescriptionList = getLanguageDescription(languageId);
        if (languageDescriptionList != null && !languageDescriptionList.isEmpty()) {
            for (String languageDescription : languageDescriptionList) {
                LOG.info("language : " + languageDescription);
                List<TestDescription> ltd = test.getTestDescription();
                for (TestDescription testDescription : ltd) {
                    if (testDescription.getGazelleLanguage().getDescription().equalsIgnoreCase(languageDescription)) {
                        td = testDescription;
                    }
                }
            }
        }
        if (td == null) {
            // By default English language
            List<TestDescription> ltd = test.getTestDescription();
            for (TestDescription testDescription : ltd) {
                if (testDescription.getGazelleLanguage().getKeyword().equals("English")) {
                    td = testDescription;
                }
            }
        }
        return td;
    }

    private List<String> getLanguageDescription(int languageId) {
        GazelleLanguageQuery q = new GazelleLanguageQuery();
        q.id().eq(languageId);
        return q.description().getListDistinctOrdered();
    }

    @Override
    public void getSequenceDiagram() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSequenceDiagram");
        }
        try {
            DiagramGenerator.getTestSequenceDiagram(getTest());
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public void getTISequenceDiagram() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTISequenceDiagram");
        }
        try {
            DiagramGenerator.getTestInstanceSequenceDiagram(getTestInstance());
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    // destructor ///////////////////////////////////////////////////////////////////

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
