package com.eolinker.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eolinker.pojo.Api;
import com.eolinker.pojo.Partner;
import com.eolinker.service.ApiService;
import com.eolinker.service.ProjectService;
import com.eolinker.service.TestHistoryService;
import com.eolinker.util.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 接口测试控制器
 *
 * @author www.eolinker.com 广州银云信息科技有限公司 2015-2018
 * eoLinker是目前全球领先、国内最大的在线API接口管理平台，提供自动生成API文档、API自动化测试、Mock测试、团队协作等功能，旨在解决由于前后端分离导致的开发效率低下问题。
 * 如在使用的过程中有任何问题，欢迎加入用户讨论群进行反馈，我们将会以最快的速度，最好的服务态度为您解决问题。
 * <p>
 * eoLinker AMS开源版的开源协议遵循GPL
 * V3，如需获取最新的eolinker开源版以及相关资讯，请访问:https://www.eolinker.com/#/os/download
 * <p>
 * 官方网站：https://www.eolinker.com/ 官方博客以及社区：http://blog.eolinker.com/
 * 使用教程以及帮助：http://help.eolinker.com/ 商务合作邮箱：market@eolinker.com
 * 用户讨论QQ群：707530721
 * @name eolinker ams open source，eolinker开源版本
 * @link https://www.eolinker.com/
 * @package eolinker
 */
@Controller
@RequestMapping("/Test")
@Slf4j
public class TestController {

    @Resource
    private TestHistoryService testHistoryService;
    @Resource
    private ProjectService projectService;
    @Resource
    private ApiService apiService;

    /**
     * get请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public Map<String, Object> get(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                   String params, Integer apiID, Integer projectID, int requestType) {
        log.info("---------get start-----------");
        Map<String, Object> map = new HashMap<String, Object>();
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "GET";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = JSONObject.parseObject(params);
            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                if (URL.contains("?")) {
                    URL = URL + "&";
                } else {
                    URL = URL + "?";
                }
                for (String key : paramData.keySet()) {
                    URL += key + "=" + (String) paramData.get(key) + "&";
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
                URL = URL.substring(0, URL.length() - 1);
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", 0);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        log.info("---------get end-----------");
        return map;
    }

    /**
     * get请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Map<String, Object> delete(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                      String params, Integer apiID, Integer projectID, int requestType) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "DELETE";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = JSONObject.parseObject(params);
            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                if (URL.contains("?")) {
                    URL = URL + "&";
                } else {
                    URL = URL + "?";
                }
                for (String key : paramData.keySet()) {
                    URL += key + "=" + (String) paramData.get(key) + "&";
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
                URL = URL.substring(0, URL.length() - 1);
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", 0);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        return map;
    }

    /**
     * post请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public Map<String, Object> post(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                    String params, Integer apiID, Integer projectID, int requestType, String apiMethodType, String apiMethod) {
        Map<String, Object> map = new HashMap<String, Object>();
        log.info("----------post start-------------");
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "POST";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = null;

            /*
             * 判断apiMethod和apiMethodType是否为空，如果为空根据projectID和
             * apiID获取apiMethod和apiMethodType的值
             *
             * apiMethodType:0 Restful, 1 Dispatcher, 2 UploadAction,
             * 				 3 DataGridAction , 4 DataGridEditAction
             */
            if (StringUtils.isEmpty(apiMethod) || StringUtils.isEmpty(apiMethodType)) {
                Api api = apiService.getApiInfo(projectID, apiID);
                log.info("API info: " + JSON.toJSON(api));
                if (api != null) {
                    apiMethod = api.getApiMethod();
                    apiMethodType = api.getApiMethodType();
                    if (StringUtils.isNotEmpty(apiMethodType)) {
                        // Dispatcher
                        if ("1".equals(apiMethodType)) {
                            if (StringUtils.isNotEmpty(apiMethod)) {
                                paramData = new HashMap<>();
                                paramData.put("_METHOD", apiMethod);
                                paramData.put("_DATA", params);
                            } else {
                                // 参数错误 返回
                                map.put("statusCode", "210002");
                            }
                        }
                        // DataGridAction
                        else if ("3".equals(apiMethodType)) {
                            paramData = new HashMap<>();
                            paramData.put("_METHOD", "DataGridExecutor.fetch");
                            JSONObject paramsObject = JSON.parseObject(params);
                            paramsObject.put("_METHOD", apiMethod);
                            paramData.put("_DATA", paramsObject.toJSONString());
                        }
                        // DataGridEditAction
                        else if ("4".equals(apiMethodType)) {
                            paramData = new HashMap<>();
                            paramData.put("_METHOD", "DataGridExecutor.edit");
                            JSONObject paramsObject = JSON.parseObject(params);
                            paramsObject.put("_METHOD", apiMethod);
                            paramData.put("_DATA", paramsObject.toJSONString());
                        }
                    }
                }
            }
            // Restful
            if (StringUtils.isEmpty(apiMethodType) || "0".equals(apiMethodType)) {
                paramData = JSONObject.parseObject(params);
            }
            log.info("params info: " + params);
            log.info("paramData info: " + paramData);

            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                for (String key : paramData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", requestType);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        log.info("post return map :" + JSON.toJSONString(map));
        log.info("----------post end-------------");
        return map;
    }

    /**
     * head请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/head", method = RequestMethod.POST)
    public Map<String, Object> head(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                    String params, Integer apiID, Integer projectID, int requestType) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "HEAD";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = JSONObject.parseObject(params);
            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                for (String key : paramData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", requestType);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        return map;
    }

    /**
     * patch请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/patch", method = RequestMethod.POST)
    public Map<String, Object> patch(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                     String params, Integer apiID, Integer projectID, int requestType) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "PATCH";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = JSONObject.parseObject(params);
            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                for (String key : paramData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", requestType);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        return map;
    }

    /**
     * put请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/put", method = RequestMethod.POST)
    public Map<String, Object> put(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                   String params, Integer apiID, Integer projectID, int requestType) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "PUT";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = JSONObject.parseObject(params);
            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                for (String key : paramData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", requestType);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        return map;
    }

    /**
     * options请求测试
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/options", method = RequestMethod.POST)
    public Map<String, Object> options(HttpServletRequest request, int apiProtocol, String URL, String headers,
                                       String params, Integer apiID, Integer projectID, int requestType) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (URL == null || URL.equals("") || URL.length() < 0) {
            map.put("statusCode", "210001");
        } else if (projectID == null || projectID < 1) {
            map.put("statusCode", "210002");
        } else if (apiID == null || apiID < 1) {
            map.put("statusCode", "210003");
        } else {
            String method = "OPTIONS";
            String URI = URL;
            Map<String, Object> headerData = JSONObject.parseObject(headers);
            List<Map<String, String>> headerList = new ArrayList<Map<String, String>>();
            if (headerData != null && !headerData.isEmpty()) {
                for (String key : headerData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("name", key);
                    data.put("value", (String) headerData.get(key));
                    headerList.add(data);
                }
            }
            Map<String, Object> paramData = JSONObject.parseObject(params);
            List<Map<String, String>> paramList = new ArrayList<Map<String, String>>();
            if (paramData != null && !paramData.isEmpty()) {
                for (String key : paramData.keySet()) {
                    Map<String, String> data = new HashMap<>();
                    data.put("key", key);
                    data.put("value", (String) paramData.get(key));
                    paramList.add(data);
                }
            }
            String completeURL = "";
            if (apiProtocol == 0) {
                completeURL = "http://" + URL;
            } else {
                completeURL = "https://" + URL;
            }
            if (completeURL == null || completeURL.equals("") || completeURL.length() <= 0) {
                map.put("statusCode", "210001");
                return map;
            }
            Proxy proxy = new Proxy();
            Map<String, Object> result = proxy.proxyToDesURL(method, completeURL, headerList, paramList);
            if (result != null && !result.isEmpty()) {
                Map<String, Object> requestInfo = new HashMap<String, Object>();
                requestInfo.put("apiProtocol", apiProtocol);
                requestInfo.put("method", method);
                requestInfo.put("URL", URI);
                requestInfo.put("headers", headerList);
                requestInfo.put("requestType", requestType);
                requestInfo.put("params", paramList);
                Map<String, Object> resultInfo = new HashMap<String, Object>();
                resultInfo.put("headers", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("headers"));
                resultInfo.put("body", ((JSONObject) JSONObject.toJSON(result.get("testResult"))).get("body"));
                resultInfo.put("httpCode", result.get("testHttpCode"));
                resultInfo.put("testDeny", result.get("testDeny"));
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                try {
                    date = dateFormat.parse(result.get("testTime").toString());
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Timestamp testTime = new Timestamp(date.getTime());
                Integer testID = testHistoryService.addTestHistory(projectID, apiID,
                        JSONObject.toJSON(requestInfo).toString(), JSONObject.toJSON(resultInfo).toString(), testTime);
                if (testID != null) {
                    map.put("statusCode", "000000");
                    map.put("testHttpCode", result.get("testHttpCode"));
                    map.put("testDeny", result.get("testDeny"));
                    map.put("testResult", result.get("testResult"));
                    map.put("testID", testID);
                }
            }

        }
        return map;
    }

    /**
     * 删除测试记录
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/deleteTestHistory", method = RequestMethod.POST)
    public Map<String, Object> deleteTestHistory(HttpServletRequest request, Integer testID, Integer projectID) {
        Map<String, Object> map = new HashMap<String, Object>();
        HttpSession session = request.getSession(true);
        Integer userID = (Integer) session.getAttribute("userID");
        Partner partner = projectService.getProjectUserType(userID, projectID);
        if (partner == null || partner.getUserType() < 0 || partner.getUserType() > 2) {
            map.put("statusCode", "100002");
        } else {
            boolean result = testHistoryService.deleteTestHistory(projectID, userID, testID);
            if (result) {
                map.put("statusCode", "000000");
            } else {
                map.put("statusCode", "210000");
            }
        }
        return map;
    }

    /**
     * 清空测试记录
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/deleteAllTestHistory", method = RequestMethod.POST)
    public Map<String, Object> deleteAllTestHistory(HttpServletRequest request, Integer apiID, Integer projectID) {
        Map<String, Object> map = new HashMap<String, Object>();
        HttpSession session = request.getSession(true);
        Integer userID = (Integer) session.getAttribute("userID");
        Partner partner = projectService.getProjectUserType(userID, projectID);
        if (partner == null || partner.getUserType() < 0 || partner.getUserType() > 2) {
            map.put("statusCode", "100002");
        } else {
            boolean result = testHistoryService.deleteAllTestHistory(projectID, userID, apiID);
            if (result) {
                map.put("statusCode", "000000");
            } else {
                map.put("statusCode", "210000");
            }
        }
        return map;
    }

    /**
     * 添加测试记录
     *
     * @param request
     * @param project
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/addTestHistory", method = RequestMethod.POST)
    public Map<String, Object> addTestHistory(HttpServletRequest request, Integer apiID, String requestInfo,
                                              String resultInfo) {
        Map<String, Object> map = new HashMap<String, Object>();
        HttpSession session = request.getSession(true);
        Integer userID = (Integer) session.getAttribute("userID");
        Integer projectID = apiService.getProjectID(apiID);
        Partner partner = projectService.getProjectUserType(userID, projectID);
        if (partner == null || partner.getUserType() < 0 || partner.getUserType() > 2) {
            map.put("statusCode", "100002");
        } else {
            Date date = new Date();
            Timestamp testTime = new Timestamp(date.getTime());
            Integer result = testHistoryService.addTestHistory(projectID, apiID, requestInfo, resultInfo, testTime);
            if (result != null) {
                map.put("statusCode", "000000");
                map.put("testID", result);
            } else {
                map.put("statusCode", "210000");
            }
        }
        return map;
    }
}
