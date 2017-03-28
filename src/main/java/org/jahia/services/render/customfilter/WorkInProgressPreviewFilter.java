/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.render.customfilter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filer that displays live version of 'work in progress' content in preview
 */
public class WorkInProgressPreviewFilter extends AbstractFilter {

    public static final String PREVIEW_MODE = "previewmode";
    public static final String WORK_IN_PROGRESS = "wip";
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final HttpServletRequest request = renderContext.getRequest();

        if (StringUtils.isEmpty(renderContext.getRequest().getParameter(PREVIEW_MODE)) || !(WORK_IN_PROGRESS.equals(request.getParameter(PREVIEW_MODE)))){
            return null;
        }

        List<String> wipNodes = (List<String>) request.getAttribute("WIP_nodes");
        if (StringUtils.equals(resource.getWorkspace(), "default") && isWorkInProgress(resource.getNode(), wipNodes)) {
            JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession("live", resource.getNode().getSession().getLocale(), resource.getNode().getSession().getFallbackLocale());
            try {
                JCRNodeWrapper n = s.getNode(resource.getNode().getPath());
                chain.pushAttribute(request, "WIP_" + resource.toString(), true);
                if (wipNodes == null) {
                    wipNodes = new ArrayList<String>();
                }
                wipNodes.add(resource.getNode().getPath());
                chain.pushAttribute(request, "WIP_nodes", wipNodes);
                renderContext.setWorkspace("live");
                resource.setNode(n);
                renderContext.getMainResource().setNode(s.getNode(renderContext.getMainResource().getNode().getPath()));
                request.setAttribute("expiration", "0");
                request.setAttribute("workspace", "live");
                request.setAttribute("currentNode", n);
            } catch (PathNotFoundException e) {
                return "";
            }
        }
        return null;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        final HttpServletRequest request = renderContext.getRequest();
        String attrName = "WIP_" + resource.toString();
        Boolean wipResource = (Boolean) request.getAttribute(attrName);
        if (wipResource != null) {
            request.removeAttribute(attrName);
            renderContext.setWorkspace("default");
            List<String> wipNodes = (List<String>) request.getAttribute("WIP_nodes");
            if (wipNodes != null) {
                if (wipNodes.remove(resource.getNode().getPath())) {
                    if (wipNodes.isEmpty()) {
                        request.removeAttribute("WIP_nodes");
                    } else {
                        chain.pushAttribute(request, "WIP_nodes", wipNodes);
                    }
                }
            }
            JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession("default", resource.getNode().getSession().getLocale(), resource.getNode().getSession().getFallbackLocale());
            JCRNodeWrapper n = s.getNode(resource.getNode().getPath());
            resource.setNode(n);
            renderContext.getMainResource().setNode(s.getNode(renderContext.getMainResource().getNode().getPath()));
            request.setAttribute("workspace", "default");
            request.setAttribute("currentNode", n);
        }
        return previousOut;
    }

    private boolean isWorkInProgress(JCRNodeWrapper node, List<String> wipNodes) throws RepositoryException {
        if (wipNodes != null && !wipNodes.isEmpty() && node.getPath().startsWith(wipNodes.get(wipNodes.size() - 1) + "/")) {
            return true;
        }
        Locale locale = node.getSession().getLocale();
        if (node.hasI18N(locale)) {
            final Node i18n = node.getI18N(locale);
            if (i18n.hasProperty("j:workInProgress")){
                return i18n.getProperty("j:workInProgress").getBoolean();
            }
        }
        return node.hasProperty("j:workInProgress") && node.getProperty("j:workInProgress").getBoolean();
    }
}