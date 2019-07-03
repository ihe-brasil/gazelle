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

package net.ihe.gazelle.EVSclient;

import net.ihe.gazelle.cache.keyvalue.KeyValueCacheManager;
import net.ihe.gazelle.common.application.action.ApplicationCacheManager;
import net.ihe.gazelle.evsclient.connector.api.EVSClientResults;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <b>Class Description : </b>EVSClientResultsWrapper<br>
 * <br>
 *
 * @author Jean-Francois Labbé / IHE-Europe development Project
 * @version 1.0 - 10/03/16
 * @class EVSClientResultsWrapper
 * @package net.ihe.gazelle.EVSclient
 * @see jean-francois.labbe@ihe-europe.net - http://gazelle.ihe.net
 */
public class EVSClientResultsWrapper {

    public static String getValidationStatus(String oid, String validatorUrl, final int objectId) {
        String key = getValStatusKey(oid);
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        return wsCache.getValue(key, new net.ihe.gazelle.cache.CacheUpdater() {
            @Override
            public String getValue(String key, Object... parameters) {
                String status = EVSClientResults.getValidationStatus((String) parameters[0], (String) parameters[1]);
                if (status == null || status.isEmpty() || status.equals("VALIDATION NOT PERFORMED")) {
                    status = "not performed";
                }
                setCacheDate(objectId);
                return status;
            }
        }, oid, validatorUrl);
    }

    public static String getValidationDate(String oid, String validatorUrl, final int objectId) {
        String key = getValDateKey(oid);
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        return wsCache.getValue(key, new net.ihe.gazelle.cache.CacheUpdater() {
            @Override
            public String getValue(String key, Object... parameters) {
                String status = EVSClientResults.getValidationDate((String) parameters[0], (String) parameters[1]);
                if (status == null || status.isEmpty()) {
                    status = "not performed";
                }
                setCacheDate(objectId);
                return status;
            }
        }, oid, validatorUrl);
    }

    public static String getValidationPermanentLink(String oid, String validatorUrl, final int objectId) {
        String key = getValPermLink(oid);
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        return wsCache.getValue(key, new net.ihe.gazelle.cache.CacheUpdater() {
            @Override
            public String getValue(String key, Object... parameters) {
                String status = EVSClientResults.getValidationPermanentLink((String) parameters[0], (String) parameters[1]);
                if (status == null || status.isEmpty()) {
                    status = "not performed";
                }
                setCacheDate(objectId);
                return status;
            }
        }, oid, validatorUrl);
    }

    public static String getLastResultStatusByExternalId(String externalId, String toolOid, String validatorUrl, final int objectId) {
        String key = getLastResultStatusExtIdKey(objectId);
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        return wsCache.getValue(key, new net.ihe.gazelle.cache.CacheUpdater() {
            @Override
            public String getValue(String key, Object... parameters) {
                String status = EVSClientResults.getLastResultStatusByExternalId((String) parameters[0], (String) parameters[1], (String)
                        parameters[2]);
                if (status == null || status.isEmpty()) {
                    status = "not performed";
                }
                setCacheDate(objectId);
                return status;
            }
        }, externalId, toolOid, validatorUrl);
    }

    public static String getLastResultPermanentLinkByExternalId(String externalId, String toolOid, String validatorUrl, final int objectId) {
        String key = getLastResultPermLinkExtIdKey(objectId);
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        return wsCache.getValue(key, new net.ihe.gazelle.cache.CacheUpdater() {
            @Override
            public String getValue(String key, Object... parameters) {
                String permaLink = EVSClientResults.getLastResultPermanentLinkByExternalId((String) parameters[0], (String) parameters[1], (String)
                        parameters[2]);
                if (permaLink == null || permaLink.isEmpty()) {
                    permaLink = "not performed";
                }
                setCacheDate(objectId);
                return permaLink;
            }
        }, externalId, toolOid, validatorUrl);
    }

    public static String getResultOidFromUrl(Map<String, String> urlParams) {
        return EVSClientResults.getResultOidFromUrl(urlParams);
    }

    private static void setCacheDate(int objectId) {
        String key = getDateCacheKey(objectId);
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        wsCache.setValue(key, (new Date()).toString());
    }

    public static void cleanCache(int objectId) {
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        wsCache.removeValue(getDateCacheKey(objectId));
        wsCache.removeValue(getLastResultStatusExtIdKey(objectId));
        wsCache.removeValue(getLastResultPermLinkExtIdKey(objectId));
    }

    private static String getDateCacheKey(int objectId) {
        return "lastValidationCheckDate:" + objectId;
    }

    private static String getLastResultStatusExtIdKey(int objectId) {
        return "lastResultStatusExtId:" + objectId;
    }

    private static String getValDateKey(String oid) {
        return "valDate:" + oid;
    }

    private static String getValPermLink(String oid) {
        return "valPermaLink:" + oid;
    }

    private static String getValStatusKey(String oid) {
        return "valStatus:" + oid;
    }

    private static String getLastResultPermLinkExtIdKey(int objectId) {
        return "lastResultPermLinkExtId:" + objectId;
    }

    public static Date getLastCheck(int objectId) {
        KeyValueCacheManager wsCache = ApplicationCacheManager.getWebServicesCache();
        String value = wsCache.getValue(getDateCacheKey(objectId));
        DateFormat dt = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        Date parse;
        try {
            parse = dt.parse(value);
        } catch (ParseException e) {
            parse = null;
        } catch (NullPointerException e) {
            parse = null;
        }
        return parse;
    }
}
