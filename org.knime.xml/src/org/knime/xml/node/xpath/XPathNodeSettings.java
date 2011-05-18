/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   10.02.2011 (hofer): created
 */
package org.knime.xml.node.xpath;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 *
 * @author Heiko Hofer
 */
public class XPathNodeSettings {
    private static final String INPUT_COLUMN = "inputColumn";
    private static final String NEW_COLUMN = "newColumn";
    private static final String REMOVE_INPUT_COLUMN = "removeInputColumn";
    private static final String XPATH_QUERY = "xpath";
    private static final String RETURN_TYPE = "returnType";
    private static final String NS_PREFIXES = "nsPrefixes";
    private static final String NAMESPACES = "namespaces";
    private static final String USE_ROOTS_NS = "useRootsNameSpace";
    private static final String ROOTS_NS_PREFIX = "rootsNameSpacePrefix";

    /**
     * This enum holds all possible output types for XPath 1.0.
     *
     * @author Heiko Hofer
     */
    public enum XPathOutput {
        /** XPath boolean type. */
        Boolean,
        /** XPath numeric type. */
        Number,
        /** XPath string type. */
        String,
        /** XPath xml node type. */
        Node,
        /** XPath collection of xml nodes type. */
        NodeSet;
    }

    private String m_inputColumn = null;
    private String m_newColumn = null;
    private boolean m_removeInputColumn = false;
    private XPathOutput m_returnType = XPathOutput.NodeSet;
    private String m_xpathQuery = "/*";
    private String[] m_nsPrefixes = new String[0];
    private String[] m_namespaces = new String[0];
    private boolean m_useRootsNS = true;
    private String m_rootsNSPrefix = "dns";

    /**
     * @return the inputColumn
     */
    String getInputColumn() {
        return m_inputColumn;
    }

    /**
     * @param inputColumn the inputColumn to set
     */
    void setInputColumn(final String inputColumn) {
        m_inputColumn = inputColumn;
    }

    /**
     * @return the newColumn
     */
    String getNewColumn() {
        return m_newColumn;
    }

    /**
     * @param newColumn the newColumn to set
     */
    void setNewColumn(final String newColumn) {
        m_newColumn = newColumn;
    }

    /**
     * @return the removeInputColumn
     */
    boolean getRemoveInputColumn() {
        return m_removeInputColumn;
    }

    /**
     * @param removeInputColumn the removeInputColumn to set
     */
    void setRemoveInputColumn(final boolean removeInputColumn) {
        m_removeInputColumn = removeInputColumn;
    }

    /**
     * @return the xpathQuery
     */
    String getXpathQuery() {
        return m_xpathQuery;
    }

    /**
     * @param xpathQuery the xpathQuery to set
     */
    void setXpathQuery(final String xpathQuery) {
        m_xpathQuery = xpathQuery;
    }

    /**
     * @return the returnType
     */
    XPathOutput getReturnType() {
        return m_returnType;
    }

    /**
     * @param returnType the returnType to set
     */
    void setReturnType(final XPathOutput returnType) {
        m_returnType = returnType;
    }

    /**
     * @return the nsPrefixes
     */
    String[] getNsPrefixes() {
        return m_nsPrefixes;
    }

    /**
     * @param nsPrefixes the nsPrefixes to set
     */
    void setNsPrefixes(final String[] nsPrefixes) {
        m_nsPrefixes = nsPrefixes;
    }

    /**
     * @return the namespaces
     */
    String[] getNamespaces() {
        return m_namespaces;
    }

    /**
     * @param namespaces the namespaces to set
     */
    void setNamespaces(final String[] namespaces) {
        m_namespaces = namespaces;
    }

    /**
     * @return the useRootsNS
     */
    boolean getUseRootsNS() {
        return m_useRootsNS;
    }

    /**
     * @param useRootsNS the useRootsNS to set
     */
    void setUseRootsNS(final boolean useRootsNS) {
        m_useRootsNS = useRootsNS;
    }

    /**
     * @return the rootsNSPrefix
     */
    String getRootsNSPrefix() {
        return m_rootsNSPrefix;
    }

    /**
     * @param rootsNSPrefix the rootsNSPrefix to set
     */
    void setRootsNSPrefix(final String rootsNSPrefix) {
        m_rootsNSPrefix = rootsNSPrefix;
    }

    /** Called from dialog when settings are to be loaded.
     * @param settings To load from
     * @param inSpec Input spec
     */
    void loadSettingsDialog(final NodeSettingsRO settings,
            final DataTableSpec inSpec) {
        m_inputColumn = settings.getString(INPUT_COLUMN, null);
        m_newColumn = settings.getString(NEW_COLUMN, null);
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN, false);
        m_xpathQuery = settings.getString(XPATH_QUERY, "/*");
        m_returnType =
            XPathOutput.valueOf(settings.getString(RETURN_TYPE,
                    XPathOutput.NodeSet.toString()));
        m_nsPrefixes = settings.getStringArray(NS_PREFIXES, new String[0]);
        m_namespaces = settings.getStringArray(NAMESPACES, new String[0]);
        m_useRootsNS = settings.getBoolean(USE_ROOTS_NS, true);
        m_rootsNSPrefix = settings.getString(ROOTS_NS_PREFIX, "dns");

    }

    /** Called from model when settings are to be loaded.
     * @param settings To load from
     * @throws InvalidSettingsException If settings are invalid.
     */
    void loadSettingsModel(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        m_inputColumn = settings.getString(INPUT_COLUMN);
        m_newColumn = settings.getString(NEW_COLUMN);
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN);
        m_xpathQuery = settings.getString(XPATH_QUERY);
        m_returnType = XPathOutput.valueOf(settings.getString(RETURN_TYPE));
        m_nsPrefixes = settings.getStringArray(NS_PREFIXES);
        m_namespaces = settings.getStringArray(NAMESPACES);
        // default is false to keep backward compatibility
        m_useRootsNS = settings.getBoolean(USE_ROOTS_NS, false);
        m_rootsNSPrefix = settings.getString(ROOTS_NS_PREFIX, "dns");
    }

    /** Called from model and dialog to save current settings.
     * @param settings To save to.
     */
    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(INPUT_COLUMN, m_inputColumn);
        settings.addString(NEW_COLUMN, m_newColumn);
        settings.addBoolean(REMOVE_INPUT_COLUMN, m_removeInputColumn);
        settings.addString(XPATH_QUERY, m_xpathQuery);
        settings.addString(RETURN_TYPE, m_returnType.toString());
        settings.addStringArray(NS_PREFIXES, m_nsPrefixes);
        settings.addStringArray(NAMESPACES, m_namespaces);
        settings.addBoolean(USE_ROOTS_NS, m_useRootsNS);
        settings.addString(ROOTS_NS_PREFIX, m_rootsNSPrefix);
    }

}