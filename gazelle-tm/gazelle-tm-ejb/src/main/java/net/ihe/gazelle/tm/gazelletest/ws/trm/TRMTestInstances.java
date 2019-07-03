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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by epoiseau on 28/11/2016.
 */
public class TRMTestInstances {

    private static final Logger LOG = LoggerFactory.getLogger(TRMTestInstances.class);


    private Integer count;
    private List<TRMShortTestInstance> instance = new ArrayList<TRMShortTestInstance>();

    public List<TRMShortTestInstance> getInstance() {
        return this.instance;
    }


    public void setInstance(List<TRMShortTestInstance> instance) {
        this.instance = instance;
    }

    public void copyFromTestInstance(List<TestInstance> list) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestInstances");
        }

        this.count = list.size();

        for (TestInstance testInstance : list) {
            if (testInstance != null) {
                TRMShortTestInstance shortTestInstance = new TRMShortTestInstance();
                shortTestInstance.setId(testInstance.getId());
                Status status = testInstance.getLastStatus();
                if (status != null) {
                    shortTestInstance.setLastStatus(status.getKeyword());
                } else {
                    shortTestInstance.setLastStatus("unknown");
                }
                this.instance.add(shortTestInstance);
            }
        }

    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
