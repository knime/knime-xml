/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   10.05.2011 (hofer): created
 */
package org.knime.xml.node.ccombine;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 *
 * @author Heiko Hofer
 */
public class XMLColumnCombinerNodeSettings {
    private static final String NEW_COLUMN = "newColumn";
    private static final String ELEMENT_NAME = "elementName";
    private static final String USE_DATA_BOUND_ELEMENT_NAME =
        "useDataBoundElementName";
    private static final String ELEMENT_NAME_COLUMN = "elementNameColumn";
    private static final String INPUT_COLUMN = "inputColumns";
    private static final String INCLUDE_ALL = "includeAll";
    private static final String REMOVE_SOURCE_COLUMNS = "removeSourceColumns";
    private static final String DATA_BOUND_ATTRIBUTE_NAMES =
        "dataBoundAttributeNames";
    private static final String DATA_BOUND_ATTRIBUTE_VALUES =
        "dataBoundAttributeColumns";
    private static final String ATTRIBUTE_NAMES = "attributeNames";
    private static final String ATTRIBUTE_VALUES = "attributeValues";

    private String m_newColumn = null;
    private String m_elementName = null;
    private boolean m_useDataBoundElementName = false;
    private String m_elementNameColumn = null;
    private String[] m_inputColumns = null;
    private boolean m_includeAll = false;
    private boolean m_removeSourceColumns = false;
    private String[] m_dataBoundAttributeNames = new String[0];
    private String[] m_dataBoundAttributeValues = new String[0];
    private String[] m_attributeNames = new String[0];
    private String[] m_attributeValues = new String[0];


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
     * @return the useDataBoundElementName
     */
    boolean getUseDataBoundElementName() {
        return m_useDataBoundElementName;
    }

    /**
     * @param useDataBoundElementName the useDataBoundElementName to set
     */
    void setUseDataBoundElementName(final boolean useDataBoundElementName) {
        m_useDataBoundElementName = useDataBoundElementName;
    }

    /**
     * @return the removeSourceColumns
     */
    boolean getRemoveSourceColumns() {
        return m_removeSourceColumns;
    }

    /**
     * @param removeSourceColumns the removeSourceColumns to set
     */
    void setRemoveSourceColumns(final boolean removeSourceColumns) {
        m_removeSourceColumns = removeSourceColumns;
    }

    /**
     * @return the elementName
     */
    String getElementName() {
        return m_elementName;
    }

    /**
     * @param elementName the elementName to set
     */
    void setElementName(final String elementName) {
        m_elementName = elementName;
    }

    /**
     * @return the elementNameColumn
     */
    String getElementNameColumn() {
        return m_elementNameColumn;
    }

    /**
     * @param elementNameColumn the elementNameColumn to set
     */
    void setElementNameColumn(final String elementNameColumn) {
        m_elementNameColumn = elementNameColumn;
    }

    /**
     * @return the inputColumns
     */
    String[] getInputColumns() {
        return m_inputColumns;
    }

    /**
     * @param inputColumns the inputColumns to set
     */
    void setInputColumns(final String[] inputColumns) {
        m_inputColumns = inputColumns;
    }

    /**
     * @param includeAll the includeAll to set
     */
    void setIncludeAll(final boolean includeAll) {
        m_includeAll = includeAll;
    }

    /**
     * @return the includeAll
     */
    boolean getIncludeAll() {
        return m_includeAll;
    }

    /**
     * @return the dataBoundAttributeNames
     */
    String[] getDataBoundAttributeNames() {
        return m_dataBoundAttributeNames;
    }

    /**
     * @param dataBoundAttributeNames the dataBoundAttributeNames to set
     */
    void setDataBoundAttributeNames(final String[] dataBoundAttributeNames) {
        m_dataBoundAttributeNames = dataBoundAttributeNames;
    }

    /**
     * @return the dataBoundAttributeValues
     */
    String[] getDataBoundAttributeValues() {
        return m_dataBoundAttributeValues;
    }

    /**
     * @param dataBoundAttributeValues the dataBoundAttributeValues to set
     */
    void setDataBoundAttributeValues(final String[] dataBoundAttributeValues) {
        m_dataBoundAttributeValues = dataBoundAttributeValues;
    }

    /**
     * @return the attributeNames
     */
    String[] getAttributeNames() {
        return m_attributeNames;
    }

    /**
     * @param attributeNames the attributeNames to set
     */
    void setAttributeNames(final String[] attributeNames) {
        m_attributeNames = attributeNames;
    }

    /**
     * @return the attributeValues
     */
    String[] getAttributeValues() {
        return m_attributeValues;
    }

    /**
     * @param attributeValues the attributeValues to set
     */
    void setAttributeValues(final String[] attributeValues) {
        m_attributeValues = attributeValues;
    }

    /** Called from dialog when settings are to be loaded.
     * @param settings To load from
     * @param inSpec Input spec
     */
    void loadSettingsDialog(final NodeSettingsRO settings,
            final DataTableSpec inSpec) {
        m_newColumn = settings.getString(NEW_COLUMN, null);
        m_elementName = settings.getString(ELEMENT_NAME, null);
        m_useDataBoundElementName = settings.getBoolean(
                USE_DATA_BOUND_ELEMENT_NAME, false);
        m_elementNameColumn = settings.getString(ELEMENT_NAME_COLUMN, null);
        m_inputColumns = settings.getStringArray(INPUT_COLUMN, (String[])null);
        m_includeAll = settings.getBoolean(INCLUDE_ALL, false);
        m_removeSourceColumns = settings.getBoolean(REMOVE_SOURCE_COLUMNS,
                false);
        m_dataBoundAttributeNames = settings.getStringArray(
                DATA_BOUND_ATTRIBUTE_NAMES, new String[0]);
        m_dataBoundAttributeValues = settings.getStringArray(
                DATA_BOUND_ATTRIBUTE_VALUES, new String[0]);
        m_attributeNames = settings.getStringArray(ATTRIBUTE_NAMES,
                new String[0]);
        m_attributeValues = settings.getStringArray(ATTRIBUTE_VALUES,
                new String[0]);
    }

    /** Called from model when settings are to be loaded.
     * @param settings To load from
     * @throws InvalidSettingsException If settings are invalid.
     */
    void loadSettingsModel(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        m_newColumn = settings.getString(NEW_COLUMN);
        m_elementName = settings.getString(ELEMENT_NAME);
        m_useDataBoundElementName = settings.getBoolean(
                USE_DATA_BOUND_ELEMENT_NAME);
        m_elementNameColumn = settings.getString(ELEMENT_NAME_COLUMN);
        m_inputColumns = settings.getStringArray(INPUT_COLUMN);
        m_includeAll = settings.getBoolean(INCLUDE_ALL);
        m_removeSourceColumns = settings.getBoolean(REMOVE_SOURCE_COLUMNS);
        m_dataBoundAttributeNames = settings.getStringArray(
                DATA_BOUND_ATTRIBUTE_NAMES);
        m_dataBoundAttributeValues = settings.getStringArray(
                DATA_BOUND_ATTRIBUTE_VALUES);
        m_attributeNames = settings.getStringArray(ATTRIBUTE_NAMES);
        m_attributeValues = settings.getStringArray(ATTRIBUTE_VALUES);
    }

    /** Called from model and dialog to save current settings.
     * @param settings To save to.
     */
    void saveSettings(final NodeSettingsWO settings) {
        settings.addString(NEW_COLUMN, m_newColumn);
        settings.addString(ELEMENT_NAME, m_elementName);
        settings.addBoolean(USE_DATA_BOUND_ELEMENT_NAME,
                m_useDataBoundElementName);
        settings.addString(ELEMENT_NAME_COLUMN, m_elementNameColumn);
        settings.addStringArray(INPUT_COLUMN, m_inputColumns);
        settings.addBoolean(INCLUDE_ALL, m_includeAll);
        settings.addBoolean(REMOVE_SOURCE_COLUMNS, m_removeSourceColumns);
        settings.addStringArray(DATA_BOUND_ATTRIBUTE_NAMES,
                m_dataBoundAttributeNames);
        settings.addStringArray(DATA_BOUND_ATTRIBUTE_VALUES,
                m_dataBoundAttributeValues);
        settings.addStringArray(ATTRIBUTE_NAMES, m_attributeNames);
        settings.addStringArray(ATTRIBUTE_VALUES, m_attributeValues);
    }

}