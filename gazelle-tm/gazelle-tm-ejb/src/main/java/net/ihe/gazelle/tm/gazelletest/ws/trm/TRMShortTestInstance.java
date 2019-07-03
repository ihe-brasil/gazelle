/*
 * EVS Client is part of the Gazelle Test Bed
 * Copyright (C) 2006-2016 IHE
 * mailto :eric DOT poiseau AT inria DOT fr
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  This code is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMShortTestInstance {
    private static final Logger LOG = LoggerFactory.getLogger(TRMShortTestInstance.class);

    private Integer id;
    private String lastStatus;

    public void copyFromTestInstance(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestInstance");
        }
        this.id = testInstance.getId();

        Status lastStatus2 = testInstance.getLastStatus();
        if (lastStatus2 != null) {
            this.lastStatus = lastStatus2.getKeyword();
        }

    }

    public Integer getId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getId");
        }
        return id;
    }

    public void setId(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setId");
        }
        this.id = id;
    }

    public String getLastStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLastStatus");
        }
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLastStatus");
        }
        this.lastStatus = lastStatus;
    }


}
