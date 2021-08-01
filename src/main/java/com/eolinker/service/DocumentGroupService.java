package com.eolinker.service;

import com.eolinker.pojo.DocumentGroup;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 项目文档分组
 *
 * @author www.eolinker.com 广州银云信息科技有限公司 2015-2018
 * eoLinker是目前全球领先、国内最大的在线API接口管理平台，提供自动生成API文档、API自动化测试、Mock测试、团队协作等功能，旨在解决由于前后端分离导致的开发效率低下问题。
 * 如在使用的过程中有任何问题，欢迎加入用户讨论群进行反馈，我们将会以最快的速度，最好的服务态度为您解决问题。
 * <p>
 * eoLinker AMS开源版的开源协议遵循GPL V3，如需获取最新的eolinker开源版以及相关资讯，请访问:https://www.eolinker.com/#/os/download
 * <p>
 * 官方网站：https://www.eolinker.com/ 官方博客以及社区：http://blog.eolinker.com/
 * 使用教程以及帮助：http://help.eolinker.com/ 商务合作邮箱：market@eolinker.com
 * 用户讨论QQ群：707530721
 * @name eolinker ams open source，eolinker开源版本
 * @link https://www.eolinker.com/
 * @package eolinker
 */
public interface DocumentGroupService {

    public int getUserType(int groupID);

    /**
     * 添加文档分组
     *
     * @param documentGroup
     * @return
     * @throws RuntimeException
     */
    public int addGroup(DocumentGroup documentGroup) throws RuntimeException;

    /**
     * 删除文档分组
     *
     * @param groupID
     * @return
     * @throws RuntimeException
     */
    public int deleteGroup(int groupID) throws RuntimeException;

    /**
     * 获取所有文档列表
     *
     * @param projectID
     * @return
     * @throws RuntimeException
     */
    public Map<String, Object> getGroupList(int projectID) throws RuntimeException;

    /**
     * 修改文档
     *
     * @param statusCode
     * @return
     * @throws RuntimeException
     */
    public int editGroup(DocumentGroup documentGroup) throws RuntimeException;

    /**
     * 修改文档分组列表排序
     *
     * @param projectID
     * @return
     * @throws RuntimeException
     */
    public int sortGroup(int projectID, String orderList) throws RuntimeException;

    /**
     * 导出分组
     *
     * @param groupID
     * @return
     * @throws RuntimeException
     */
    public String exportGroup(HttpServletRequest request, int groupID) throws RuntimeException, IOException;

    /**
     * 导入分组
     *
     * @param projectID
     * @param data
     * @return
     * @throws RuntimeException
     */
    public int importGroup(int projectID, String data) throws RuntimeException;

}
