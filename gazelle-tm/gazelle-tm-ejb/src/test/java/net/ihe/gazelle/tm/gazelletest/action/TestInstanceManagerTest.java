/*
 * Copyright 2016 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.EVSclient.EVSClientResultsWrapper;
import net.ihe.gazelle.cache.keyvalue.KeyValueCacheManager;
import net.ihe.gazelle.common.application.action.ApplicationCacheManager;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <b>Class Description : </b>TestInstanceManagerTest<br>
 * <br>
 *
 * @author Jean-Francois Labbé / IHE-Europe development Project
 * @version 1.0 - 09/03/16
 * @class TestInstanceManagerTest
 * @package net.ihe.gazelle.tm.gazelletest.action
 * @see jean-francois.labbe@ihe-europe.net - http://gazelle.ihe.net
 */
@Ignore
public class TestInstanceManagerTest {

    @Test
    public void EvsClientWrapper() {
        int id = 1;
        String proxyId = "1451649";
        String toolOid = "1.3.6.1.4.1.12559.11.1.6";
        String url = "https://gazelle.ihe.net/EVSClient";
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        assertEquals(0, wsCache.size());

        assertEquals("PASSED", EVSClientResultsWrapper.getLastResultStatusByExternalId(proxyId, toolOid, url, id));

        assertEquals("PASSED", EVSClientResultsWrapper.getLastResultStatusByExternalId(proxyId, toolOid, url, id));
        assertEquals(2, wsCache.size());

        assertEquals("https://gazelle.ihe.net/EVSClient/hl7v2Result.seam?&oid=1.3.6.1.4.1.12559.11.1.2.1.4.438765", EVSClientResultsWrapper
                .getLastResultPermanentLinkByExternalId(proxyId, toolOid, url, id));
        assertEquals(3, wsCache.size());
    }
}