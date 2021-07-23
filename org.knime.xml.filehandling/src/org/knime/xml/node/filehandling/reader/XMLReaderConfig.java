/*
 * ------------------------------------------------------------------------
 *
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 * ---------------------------------------------------------------------
 *
 * History
 *   28 Jun 2021 (Moditha Hewasinghage, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.xml.node.filehandling.reader;

import org.knime.filehandling.core.node.table.reader.config.ReaderSpecificConfig;

/**
 * {@link ReaderSpecificConfig} for the XML reader node.
 * 
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 */
final class XMLReaderConfig implements ReaderSpecificConfig<XMLReaderConfig> {

    private String m_columnName = "XML";

    private boolean m_useXPath = false;

    private String m_xPath = "";

    private String[] m_namespacePrefixes = new String[0];

    private String[] m_namespaces = new String[0];

    private boolean m_useRootNamespace = true;

    private String m_rootNamespacePrefix = "dns";

    private boolean m_failIfNotFound = false;
    
    /**
     * Constructor.
     */
    XMLReaderConfig() {
    }

    private XMLReaderConfig(final XMLReaderConfig toCopy) {
        setColumnName(toCopy.getColumnName());
        setUseXPath(toCopy.useXPath());
        setXPath(toCopy.getXPath());
        setNamespaces(toCopy.getNamespaces());
        setNamespacePrefixes(toCopy.getNamespacePrefixes());
        setUseRootNamespace(toCopy.useRootNamespace());
        setRootNamespacePrefix(toCopy.getRootNamespacePrefix());
        setFailIfNotFound(toCopy.failIfNotFound());
    }

    @Override
    public XMLReaderConfig copy() {
        return new XMLReaderConfig(this);
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return m_columnName;
    }

    /**
     * @param columnName
     *            the columnName to set
     */
    public void setColumnName(final String columnName) {
        m_columnName = columnName;
    }

    /**
     * @return the useXPath
     */
    public boolean useXPath() {
        return m_useXPath;
    }

    /**
     * @param useXPath
     *            the useXPath to set
     */
    public void setUseXPath(final boolean useXPath) {
        m_useXPath = useXPath;
    }

    /**
     * @return the xPath
     */
    public String getXPath() {
        return m_xPath;
    }

    /**
     * @param xPath
     *            the xPath to set
     */
    public void setXPath(final String xPath) {
        m_xPath = xPath;
    }

    /**
     * @return the nsPrefixes
     */
    public String[] getNamespacePrefixes() {
        return m_namespacePrefixes;
    }

    /**
     * @param nsPrefixes
     *            the nsPrefixes to set
     */
    public void setNamespacePrefixes(final String[] nsPrefixes) {
        m_namespacePrefixes = nsPrefixes;
    }

    /**
     * @return the namespaces
     */
    public String[] getNamespaces() {
        return m_namespaces;
    }

    /**
     * @param namespaces
     *            the namespaces to set
     */
    public void setNamespaces(final String[] namespaces) {
        m_namespaces = namespaces;
    }

    /**
     * @return the useRootsNS
     */
    boolean useRootNamespace() {
        return m_useRootNamespace;
    }

    /**
     * @param useRootNamespace
     *            the useRootsNS to set
     */
    void setUseRootNamespace(final boolean useRootNamespace) {
        m_useRootNamespace = useRootNamespace;
    }

    /**
     * @return the rootsNSPrefix
     */
    String getRootNamespacePrefix() {
        return m_rootNamespacePrefix;
    }

    /**
     * @param rootNamespacePrefix
     *            the rootsNSPrefix to set
     */
    void setRootNamespacePrefix(final String rootNamespacePrefix) {
        m_rootNamespacePrefix = rootNamespacePrefix;
    }

    /**
     * @return the failIfNotFound
     */
    public boolean failIfNotFound() {
        return m_failIfNotFound;
    }

    /**
     * @param failIfNotFound
     *            the failIfNotFound to set
     */
    public void setFailIfNotFound(final boolean failIfNotFound) {
        m_failIfNotFound = failIfNotFound;
    }

}
