/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
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
    private static final String MISSING_CELL_ON_FALSE = "missingCellOnFalse";
    private static final String MISSING_CELL_ON_EMPTY_STRING =
        "missingCellOnEmptyString";
    private static final String MISSING_CELL_ON_INF_OR_NAN =
        "missingCellOnInfinityNaN";
    private static final String VALUE_ON_INF_OR_NAN =
        "valueOnInfinityOrNaN";
    private static final String DEFAULT_NUMBER = "defaultNumber";
    private static final String MISSING_CELL_ON_EMPTY_SET =
        "missingCellOnEmptySet";
    private static final String XML_FRAGMENT_NAME = "xmlFragmentName";


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
        /** XPath numeric (type cast to integer). */
        Integer,
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
    private boolean m_missingCellOnFalse = false;
    private boolean m_missingCellOnEmptyString = true;
    private boolean m_missingCellOnInfinityOrNaN = true;
    private boolean m_valueOnInfinityOrNaN = false;
    private double m_defaultNumber = 0.0;
    private boolean m_missingCellOnEmptySet = true;
    private String m_xmlFragmentName = "fragment";

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

    /**
     * @return the missingCellOnFalse
     */
    boolean getMissingCellOnFalse() {
        return m_missingCellOnFalse;
    }

    /**
     * @param missingCellOnFalse the missingCellOnFalse to set
     */
    void setMissingCellOnFalse(final boolean missingCellOnFalse) {
        m_missingCellOnFalse = missingCellOnFalse;
    }

    /**
     * @return the missingCellOnEmptyString
     */
    boolean getMissingCellOnEmptyString() {
        return m_missingCellOnEmptyString;
    }

    /**
     * @param missingCellOnEmptyString the missingCellOnEmptyString to set
     */
    void setMissingCellOnEmptyString(final boolean missingCellOnEmptyString) {
        m_missingCellOnEmptyString = missingCellOnEmptyString;
    }


    /**
     * @return the missingCellOnInfinityOrNaN
     */
    boolean getMissingCellOnInfinityOrNaN() {
        return m_missingCellOnInfinityOrNaN;
    }

    /**
     * @param missingCellOnInfinityOrNaN the missingCellOnInfinityOrNaN to set
     */
    void setMissingCellOnInfinityOrNaN(
            final boolean missingCellOnInfinityOrNaN) {
        m_missingCellOnInfinityOrNaN = missingCellOnInfinityOrNaN;
    }

    /**
     * @return the zeroOnInfinityOrNaN
     */
    boolean getValueOnInfinityOrNaN() {
        return m_valueOnInfinityOrNaN;
    }

    /**
     * @param zeroOnInfinityOrNaN the zeroOnInfinityOrNaN to set
     */
    void setValueOnInfinityOrNaN(final boolean zeroOnInfinityOrNaN) {
        m_valueOnInfinityOrNaN = zeroOnInfinityOrNaN;
    }

    /**
     * @return the defaultNumber
     */
    double getDefaultNumber() {
        return m_defaultNumber;
    }

    /**
     * @param defaultNumber the defaultNumber to set
     */
    void setDefaultNumber(final double defaultNumber) {
        m_defaultNumber = defaultNumber;
    }


    /**
     * @return the missingCellOnEmptySet
     */
    boolean getMissingCellOnEmptySet() {
        return m_missingCellOnEmptySet;
    }

    /**
     * @param missingCellOnEmptySet the missingCellOnEmptySet to set
     */
    void setMissingCellOnEmptySet(final boolean missingCellOnEmptySet) {
        m_missingCellOnEmptySet = missingCellOnEmptySet;
    }

    /**
     * @return the xmlFragmentName
     */
    String getXmlFragmentName() {
        return m_xmlFragmentName;
    }

    /**
     * @param xmlFragmentName the xmlFragmentName to set
     */
    void setXmlFragmentName(final String xmlFragmentName) {
        m_xmlFragmentName = xmlFragmentName;
    }

    /** Called from dialog when settings are to be loaded.
     * @param settings To load from
     * @param inSpec Input spec
     */
    void loadSettingsDialog(final NodeSettingsRO settings,
            final DataTableSpec inSpec) {
        m_inputColumn = settings.getString(INPUT_COLUMN, null);
        m_newColumn = settings.getString(NEW_COLUMN, null);
        if (m_newColumn == null) {
            // auto-configure
            m_newColumn = DataTableSpec.getUniqueColumnName(inSpec,
                            "XML - XPATH");
        }
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN, false);
        m_xpathQuery = settings.getString(XPATH_QUERY, "/*");
        m_returnType =
            XPathOutput.valueOf(settings.getString(RETURN_TYPE,
                    XPathOutput.NodeSet.toString()));
        m_nsPrefixes = settings.getStringArray(NS_PREFIXES, new String[0]);
        m_namespaces = settings.getStringArray(NAMESPACES, new String[0]);
        m_useRootsNS = settings.getBoolean(USE_ROOTS_NS, true);
        m_rootsNSPrefix = settings.getString(ROOTS_NS_PREFIX, "dns");
        m_missingCellOnEmptySet = settings.getBoolean(
                MISSING_CELL_ON_EMPTY_SET, true);
        m_missingCellOnEmptyString = settings.getBoolean(
                MISSING_CELL_ON_EMPTY_STRING, true);
        m_missingCellOnFalse = settings.getBoolean(
                MISSING_CELL_ON_FALSE, false);
        m_valueOnInfinityOrNaN = settings.getBoolean(
                VALUE_ON_INF_OR_NAN, false);
        m_defaultNumber = settings.getDouble(DEFAULT_NUMBER, 0.0);
        m_missingCellOnInfinityOrNaN = settings.getBoolean(
                MISSING_CELL_ON_INF_OR_NAN,
                true);
        m_xmlFragmentName = settings.getString(XML_FRAGMENT_NAME, "fragment");


    }

    /** Called from model when settings are to be loaded.
     * @param settings To load from
     * @throws InvalidSettingsException If settings are invalid.
     */
    void loadSettingsModel(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        m_inputColumn = settings.getString(INPUT_COLUMN);
        m_newColumn = settings.getString(NEW_COLUMN);
        if (m_newColumn == null || m_newColumn.trim().isEmpty()) {
            throw new InvalidSettingsException("Please set a name for "
                    + "the new column.");
        }
        m_newColumn = m_newColumn.trim();
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN);
        m_xpathQuery = settings.getString(XPATH_QUERY);
        if (null == m_xpathQuery) {
            throw new InvalidSettingsException("No XPath query defined.");
        }
        m_returnType = XPathOutput.valueOf(settings.getString(RETURN_TYPE));
        if (null == m_returnType) {
            throw new InvalidSettingsException("No return type defined.");
        }
        m_nsPrefixes = settings.getStringArray(NS_PREFIXES);
        m_namespaces = settings.getStringArray(NAMESPACES);
        // default is false to keep backward compatibility
        m_useRootsNS = settings.getBoolean(USE_ROOTS_NS, false);
        m_rootsNSPrefix = settings.getString(ROOTS_NS_PREFIX, "dns");
        if (m_useRootsNS
            && (m_rootsNSPrefix == null || m_rootsNSPrefix.trim().isEmpty())) {
            throw new InvalidSettingsException(
                    "The prefix of root's default namespace is not set.");
        }
        // default is false to keep backward compatibility
        m_missingCellOnEmptySet = settings.getBoolean(
                MISSING_CELL_ON_EMPTY_SET, false);
        // default is false to keep backward compatibility
        m_missingCellOnEmptyString = settings.getBoolean(
                MISSING_CELL_ON_EMPTY_STRING, false);
        // default is false to keep backward compatibility
        m_missingCellOnFalse = settings.getBoolean(
                MISSING_CELL_ON_FALSE, false);
        // For return type number the backward compatiblity is broken.
        // A backward compatible setting
        // is not possible since NaN and inf values are discouraged in KNIME
        m_valueOnInfinityOrNaN = settings.getBoolean(
                VALUE_ON_INF_OR_NAN, false);
        m_defaultNumber = settings.getDouble(DEFAULT_NUMBER, 0.0);
        // default is false to keep backward compatibility
        m_missingCellOnInfinityOrNaN = settings.getBoolean(
                MISSING_CELL_ON_INF_OR_NAN,
                true);
        m_xmlFragmentName = settings.getString(XML_FRAGMENT_NAME, "fragment");
        if ((m_returnType == XPathOutput.Node
                || m_returnType == XPathOutput.NodeSet)
                && m_xmlFragmentName.trim().isEmpty()) {
            throw new InvalidSettingsException("The XML fragment name is "
                    + "empty. Please define a valid name.");
        }
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
        settings.addBoolean(MISSING_CELL_ON_EMPTY_SET,
                m_missingCellOnEmptySet);
        settings.addBoolean(MISSING_CELL_ON_EMPTY_STRING,
                m_missingCellOnEmptyString);
        settings.addBoolean(MISSING_CELL_ON_FALSE, m_missingCellOnFalse);
        settings.addBoolean(VALUE_ON_INF_OR_NAN, m_valueOnInfinityOrNaN);
        settings.addBoolean(MISSING_CELL_ON_INF_OR_NAN,
                m_missingCellOnInfinityOrNaN);
        settings.addDouble(DEFAULT_NUMBER, m_defaultNumber);
        settings.addString(XML_FRAGMENT_NAME, m_xmlFragmentName);

    }

}
