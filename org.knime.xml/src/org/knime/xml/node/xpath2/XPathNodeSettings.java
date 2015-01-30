/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   10.02.2011 (hofer): created
 */
package org.knime.xml.node.xpath2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.xml.node.xpath2.ui.XPathNamespaceContext;
import org.w3c.dom.Node;

/**
 *
 * @author Heiko Hofer
 */
public class XPathNodeSettings {
    /**
     * Config key for the xml input column.
     */
    private static final String INPUT_COLUMN = "inputColumn";

    /**
     * Config key for the remove xml input column option.
     */
    private static final String REMOVE_INPUT_COLUMN = "removeInputColumn";

    /**
     * Config key for the name space prefixes.
     */
    private static final String NS_PREFIXES = "nsPrefixes";

    /**
     * Config key for the namespaces.
     */
    private static final String NAMESPACES = "namespaces";

    /**
     * Config key for the use root namespace option.
     */
    private static final String USE_ROOTS_NS = "useRootsNameSpace";

    /**
     * Config key for the root namespace.
     */
    private static final String ROOTS_NS_PREFIX = "rootsNameSpacePrefix";

    /**
     * Config key for the number of xpath queries.
     */
    private static final String NUMBER_OF_QUERIES = "numberOfQueries";

    /**
     * This enum holds all possible output types for XPath 1.0.
     *
     * @author Heiko Hofer
     */
    public enum XPathOutput {
        /** XPath boolean type. */
        Boolean,
        /** XPath numeric type. */
        Double,
        /** XPath numeric (type cast to integer). */
        Integer,
        /** XPath string type. */
        String,
        /** XPath xml node type. */
        Node
    }

    /**
     * This enum holds all possible options for the multiple tag option.
     *
     * @author Tim-Oliver Buchholz
     */
    public enum XPathMultiColOption {
        /**
         * Create a single cell of the selected type. If a tag appears more than once, take only the first appearance.
         */
        SingleCell,

        /**
         * Create a collection cell of the selected type. Add all appearances of this tag to the collection.
         */
        CollectionCell,

        /**
         * Create for each appearance of this tag a new SingleCell (a new column).
         */
        MultipleColumns
    }

    /**
     * XML input column.
     */
    private String m_inputColumn = null;

    /**
     * Remove input column option.
     */
    private boolean m_removeInputColumn = false;

    /**
     * Namespace prefixes.
     */
    private String[] m_nsPrefixes = new String[0];

    /**
     * Namespaces.
     */
    private String[] m_namespaces = new String[0];

    /**
     * Use roots namespace option.
     */
    private boolean m_useRootsNS = true;

    /**
     * Roots namespace prefix.
     */
    private String m_rootsNSPrefix = "dns";

    /**
     * List of {@XPathSettings} holds all xpath queries with the corresponding configuration.
     */
    private ArrayList<XPathSettings> m_xpathQueryList = new ArrayList<XPathSettings>();

    /**
     * Number of different xpath queries.
     */
    private int m_numberOfQueries = 0;

    /**
     * @return the inputColumn
     */
    public String getInputColumn() {
        return m_inputColumn;
    }

    /**
     * @param inputColumn the inputColumn to set
     */
    public void setInputColumn(final String inputColumn) {
        m_inputColumn = inputColumn;
    }

    /**
     * @return the removeInputColumn
     */
    public boolean getRemoveInputColumn() {
        return m_removeInputColumn;
    }

    /**
     * @param removeInputColumn the removeInputColumn to set
     */
    public void setRemoveInputColumn(final boolean removeInputColumn) {
        m_removeInputColumn = removeInputColumn;
    }

    /**
     * @return the nsPrefixes
     */
    public String[] getNsPrefixes() {
        return m_nsPrefixes;
    }

    /**
     * @param nsPrefixes the nsPrefixes to set
     */
    public void setNsPrefixes(final String[] nsPrefixes) {
        m_nsPrefixes = nsPrefixes;
    }

    /**
     * @return the namespaces
     */
    public String[] getNamespaces() {
        return m_namespaces;
    }

    /**
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(final String[] namespaces) {
        m_namespaces = namespaces;
    }

    /**
     * @return the useRootsNS
     */
    public boolean getUseRootsNS() {
        return m_useRootsNS;
    }

    /**
     * @param useRootsNS the useRootsNS to set
     */
    public void setUseRootsNS(final boolean useRootsNS) {
        m_useRootsNS = useRootsNS;
    }

    /**
     * @return the rootsNSPrefix
     */
    public String getRootsNSPrefix() {
        return m_rootsNSPrefix;
    }

    /**
     * @param rootsNSPrefix the rootsNSPrefix to set
     */
    public void setRootsNSPrefix(final String rootsNSPrefix) {
        m_rootsNSPrefix = rootsNSPrefix;
    }

    /**
     * @return the number of xpath queries
     */
    public int getNumberOfQueries() {
        return m_numberOfQueries;
    }

    /**
     * @return the list of all xpath queries.
     */
    public ArrayList<XPathSettings> getXPathQueryList() {
        return m_xpathQueryList;
    }

    /**
     * @param list the list of xpath queries.
     */
    public void setXPathQueryList(final ArrayList<XPathSettings> list) {
        m_xpathQueryList = list;
        m_numberOfQueries = list.size();
    }

    /**
     * Called from dialog when settings are to be loaded.
     *
     * @param settings To load from
     * @param inSpec Input spec
     */
    void loadSettingsDialog(final NodeSettingsRO settings, final DataTableSpec inSpec) {
        m_inputColumn = settings.getString(INPUT_COLUMN, null);
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN, false);
        m_nsPrefixes = settings.getStringArray(NS_PREFIXES, new String[0]);
        m_namespaces = settings.getStringArray(NAMESPACES, new String[0]);
        m_useRootsNS = settings.getBoolean(USE_ROOTS_NS, true);
        m_rootsNSPrefix = settings.getString(ROOTS_NS_PREFIX, "dns");
        m_numberOfQueries = settings.getInt(NUMBER_OF_QUERIES, 1);
        m_xpathQueryList = new ArrayList<XPathSettings>(m_numberOfQueries);
        for (int i = 0; i < m_numberOfQueries; i++) {
            XPathSettings xps = new XPathSettings();
            xps.loadSettingsDialog(settings, inSpec, i);
            m_xpathQueryList.add(xps);
        }
    }

    /**
     * Called from model when settings are to be loaded.
     *
     * @param settings To load from
     * @throws InvalidSettingsException If settings are invalid.
     */
    void loadSettingsModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_inputColumn = settings.getString(INPUT_COLUMN);
        m_removeInputColumn = settings.getBoolean(REMOVE_INPUT_COLUMN);
        m_nsPrefixes = settings.getStringArray(NS_PREFIXES);
        m_namespaces = settings.getStringArray(NAMESPACES);
        m_useRootsNS = settings.getBoolean(USE_ROOTS_NS);
        m_rootsNSPrefix = settings.getString(ROOTS_NS_PREFIX, "dns");
        if (m_useRootsNS && (m_rootsNSPrefix == null || m_rootsNSPrefix.trim().isEmpty())) {
            throw new InvalidSettingsException("The prefix of root's default namespace is not set.");
        }
        m_numberOfQueries = settings.getInt(NUMBER_OF_QUERIES);
        m_xpathQueryList = new ArrayList<XPathSettings>(m_numberOfQueries);
        for (int i = 0; i < m_numberOfQueries; i++) {
            XPathSettings xps = new XPathSettings();
            xps.loadSettingsModel(settings, i);
            m_xpathQueryList.add(xps);
        }
    }

    /**
     * Called from model and dialog to save current settings.
     *
     * @param settings To save to.
     */
    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(INPUT_COLUMN, m_inputColumn);
        settings.addBoolean(REMOVE_INPUT_COLUMN, m_removeInputColumn);
        settings.addStringArray(NS_PREFIXES, m_nsPrefixes);
        settings.addStringArray(NAMESPACES, m_namespaces);
        settings.addBoolean(USE_ROOTS_NS, m_useRootsNS);
        settings.addString(ROOTS_NS_PREFIX, m_rootsNSPrefix);
        settings.addInt(NUMBER_OF_QUERIES, m_numberOfQueries);
        for (int i = 0; i < m_xpathQueryList.size(); i++) {
            m_xpathQueryList.get(i).saveSettings(settings, i);
        }
    }

    /**
     * Creates a name which is not contained in names.
     *
     * @param name new column name
     * @param suffix the suffix
     * @param i index for the suffix
     * @param names all current used names
     * @return unique column name
     */
    public static String uniqueName(final String name, final String suffix, final int i, final HashSet<String> names) {
        String n = name + suffix;
        if (names.contains(n)) {
            return uniqueName(name, "(#" + i + ")", i + 1, names);
        }
        return n;
    }

    /**
     * @param xmlValue XML value from input cell
     * @param query xpath query
     * @return xpath expression
     * @throws InvalidSettingsException thrown if xpath expression is invalid
     */
    public XPathExpression createXPathExpr(final XMLValue xmlValue, final String query)
            throws InvalidSettingsException {
        List<String> nsPrefixes = new ArrayList<String>();
        nsPrefixes.addAll(Arrays.asList(getNsPrefixes()));
        if (nsPrefixes.contains(getRootsNSPrefix())) {
            throw new InvalidSettingsException("The namespace table uses the prefix reserved for the "
                + "roots namespace.");
        }
        nsPrefixes.add(getRootsNSPrefix());
        List<String> namespaces = new ArrayList<String>();
        namespaces.addAll(Arrays.asList(getNamespaces()));
        if (xmlValue == null) {
            String nsTemplate = "roots_ns_";
            int counter = 0;
            String ns = nsTemplate + counter;
            while (namespaces.contains(ns)) {
                counter++;
                ns = nsTemplate + counter;
            }
            namespaces.add(ns);
        } else {
            Node root = xmlValue.getDocument().getFirstChild();
            while (root.getNodeType() != Node.ELEMENT_NODE) {
                root = root.getNextSibling();
            }
            String rootNSUri = root.getNamespaceURI();
            if (rootNSUri != null) {
                namespaces.add(rootNSUri);
            } else {
                throw new InvalidSettingsException("The root node does not have a namesapce URI.");
            }
        }

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new XPathNamespaceContext(nsPrefixes.toArray(new String[nsPrefixes.size()]),
            namespaces.toArray(new String[namespaces.size()])));
        try {
            return xpath.compile(query);
        } catch (XPathExpressionException e) {
            throw new InvalidSettingsException("XPath query cannot be parsed.", e);
        }
    }

    /**
     * @param query the xpath query
     * @return xpathexpression
     * @throws InvalidSettingsException if query is invalid
     *
     */
    public XPathExpression initXPathExpression(final String query) throws InvalidSettingsException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new XPathNamespaceContext(getNsPrefixes(), getNamespaces()));
        try {
            XPathExpression xpathExpr = xpath.compile(query);

            if (getUseRootsNS()
                && Arrays.binarySearch(getNsPrefixes(), getRootsNSPrefix()) >= 0) {
                throw new InvalidSettingsException("The namespace table uses the prefix " + "reserved for the "
                    + "roots namespace.");
            } else {
                return xpathExpr;
            }
        } catch (XPathExpressionException e) {
            if (getUseRootsNS()) {
                // try to compile it with roots default prefix
                createXPathExpr(null, query);
                // the xpath compiles with the roots default prefix
                return null;
            } else {
                throw new InvalidSettingsException("XPath expression cannot be compiled.", e);
            }
        }
    }
}
