/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   05.01.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2;

import java.util.ArrayList;
import java.util.TreeMap;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathMultiColOption;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathOutput;

/**
 *
 * @author tibuch
 */
public class XPathSettings {

    private static final String NEW_COLUMN = "newColumn";

    private static final String XPATH_QUERY = "xpath";

    private static final String RETURN_TYPE = "returnType";

    private static final String MISSING_CELL_ON_EMPTY_STRING = "missingCellOnEmptyString";

    private static final String MISSING_CELL_ON_INF_OR_NAN = "missingCellOnInfinityNaN";

    private static final String VALUE_ON_INF_OR_NAN = "valueOnInfinityOrNaN";

    private static final String DEFAULT_NUMBER = "defaultNumber";

    private static final String MISSING_CELL_ON_EMPTY_SET = "missingCellOnEmptySet";

    private static final String XML_FRAGMENT_NAME = "xmlFragmentName";

    private static final String MULTI_TAG_OPTION = "multiTagOption";

    private static final String ATTRIBUTE_FOR_COL_NAME = "attributeForColName";

    private static final String USE_ATTRIBUTE_FOR_COL_NAME = "useAttributeForColName";

    private String m_xmlFragmentName = "fragment";

    private int m_defaultNumber = 0;

    private String m_columnName = "column";

    private String m_xpathQuery = "/*";

    private XPathOutput m_type = XPathOutput.String;

    private boolean m_missingCellOnEmptySet = true;

    private boolean m_missingCellOnEmptyString = false;

    private boolean m_missingCellOnInfinityOrNaN = true;

    private boolean m_valueOnInfinityOrNaN = false;

    private boolean m_useAttributeForColName = false;

    private XPathMultiColOption m_multiTagOption = XPathMultiColOption.SingleCell;

    private String m_attributeForColName = "name";

    private int m_currentColumnIndex = 0;


    private TreeMap<Integer, String> m_colNameMap = new TreeMap<Integer, String>();


    /**
     * XPathSettings object for a xpath query which selects one tag of many.
     * Ex.: /table/tr/td[1]
     * @param xps setting to copy
     * @param name column name
     */
    public XPathSettings(final XPathSettings xps, final String name) {
        m_xmlFragmentName = xps.getXmlFragmentName();
        m_defaultNumber = xps.getDefaultNumber();
        m_columnName = name;
        m_xpathQuery = xps.getXpathQuery();
        m_type = xps.getReturnType();
        m_missingCellOnEmptySet = xps.getMissingCellOnEmptySet();
        m_missingCellOnEmptyString = xps.getMissingCellOnEmptyString();
        m_missingCellOnInfinityOrNaN = xps.getMissingCellOnInfinityOrNaN();
        m_valueOnInfinityOrNaN = xps.getValueOnInfinityOrNaN();
        m_useAttributeForColName = xps.getUseAttributeForColName();
        m_multiTagOption = xps.getMultipleTagOption();
        m_attributeForColName = xps.getAttributeForColName();
    }

    /**
     * Creates a new XPath settings object.
     */
    public XPathSettings() {

    }

    /**
     * Examples:
     * value query: /root/element
     * name query: /name
     * result: /root/element/name
     *
     * value query: root/element/@attribute
     * name query: ../@anotherAttr
     * result: /root/element/@anotherAttr
     * @param query relative query for column name
     * @return absolute query for column name
     */
    public String buildXPathForColNames(final String query) {
        String q = query;
        int pos = q.indexOf('@');
        if (pos != -1) {
            q = q.substring(0, pos);
        }

        String attributeForColName = m_attributeForColName;
        while (attributeForColName.startsWith("..")) {
            q = q.substring(0, q.lastIndexOf('/'));
            attributeForColName = attributeForColName.substring(3);
        }
        if (!attributeForColName.startsWith("/")) {
            attributeForColName = "/" + attributeForColName;
        }
        q += attributeForColName;
        return q;
    }

    /**
     * @param name Column name
     */
    public void setNewColumn(final String name) {
        m_columnName = name;
    }

    /**
     * @param query XPath query
     */
    public void setXpathQuery(final String query) {
        m_xpathQuery = query;
    }

    /**
     * @param type Return type of XPath query
     */
    public void setReturnType(final XPathOutput type) {
        m_type = type;
    }

    /**
     * @param selected selection
     */
    public void setMissingCellOnEmptySet(final boolean selected) {
        m_missingCellOnEmptySet = selected;
    }

    /**
     * @param selected selection
     */
    public void setMissingCellOnEmptyString(final boolean selected) {
        m_missingCellOnEmptyString = selected;
    }

    /**
     * @param selected selection
     */
    public void setMissingCellOnInfinityOrNaN(final boolean selected) {
        m_missingCellOnInfinityOrNaN = selected;
    }

    /**
     * @param selected selection
     */
    public void setValueOnInfinityOrNaN(final boolean selected) {
        m_valueOnInfinityOrNaN = selected;
    }

    /**
     * @param valueOf default number
     */
    public void setDefaultNumber(final int valueOf) {
        m_defaultNumber = valueOf;
    }

    /**
     * @param text xml fragment name
     */
    public void setXmlFragmentName(final String text) {
        m_xmlFragmentName = text;
    }

    /**
     * @return a row for the summary table
     */
    public Object[] getRow() {
        Object[] row = new Object[3];
        if (m_useAttributeForColName) {
            row[0] = "Value of " + m_xpathQuery + m_attributeForColName;
        } else {
            row[0] = m_columnName;
        }
        row[1] = m_xpathQuery;
        String pre = "";
        if (m_multiTagOption.equals(XPathMultiColOption.SingleCell)) {
            pre = "(SingleCell)";
        } else if (m_multiTagOption.equals(XPathMultiColOption.CollectionCell)) {
            pre = "(CollectionCell)";
        } else {
            pre = "(Multiple columns)";
        }
        row[2] = m_type + pre;
        return row;
    }

    /**
     * @return column name
     */
    public String getNewColumn() {
        return m_columnName;
    }

    /**
     * @return xpath query
     */
    public String getXpathQuery() {
        return m_xpathQuery;
    }

    /**
     * @return type of xpath query
     */
    public XPathOutput getReturnType() {
        return m_type;
    }

    /**
     * @return selection
     */
    public boolean getMissingCellOnEmptySet() {
        return m_missingCellOnEmptySet;
    }

    /**
     * @return selection
     */
    public boolean getMissingCellOnEmptyString() {
        return m_missingCellOnEmptyString;
    }


    /**
     * @return selection
     */
    public boolean getMissingCellOnInfinityOrNaN() {
        return m_missingCellOnInfinityOrNaN;
    }

    /**
     * @return selection
     */
    public boolean getValueOnInfinityOrNaN() {
        return m_valueOnInfinityOrNaN;
    }

    /**
     * @return default number
     */
    public int getDefaultNumber() {
        return m_defaultNumber;
    }

    /**
     * @return XML fragment name
     */
    public String getXmlFragmentName() {
        return m_xmlFragmentName;
    }

    /**
     * Called from dialog when settings are to be loaded.
     *
     * @param settings To load from
     * @param inSpec Input spec
     * @param index index of the xpath query in the xpath query list.
     */
    void loadSettingsDialog(final NodeSettingsRO settings, final DataTableSpec inSpec, final int index) {
        m_columnName = settings.getString(NEW_COLUMN + index, null);
        if (m_columnName == null) {
            // auto-configure
            m_columnName = DataTableSpec.getUniqueColumnName(inSpec, "XML - XPATH");
        }
        m_useAttributeForColName = settings.getBoolean(USE_ATTRIBUTE_FOR_COL_NAME + index, false);
        m_attributeForColName = settings.getString(ATTRIBUTE_FOR_COL_NAME + index, "name");
        m_xpathQuery = settings.getString(XPATH_QUERY + index, "/*");
        m_type = XPathOutput.valueOf(settings.getString(RETURN_TYPE + index, XPathOutput.String.toString()));
        m_missingCellOnEmptySet = settings.getBoolean(MISSING_CELL_ON_EMPTY_SET + index, true);
        m_missingCellOnEmptyString = settings.getBoolean(MISSING_CELL_ON_EMPTY_STRING + index, true);
        m_valueOnInfinityOrNaN = settings.getBoolean(VALUE_ON_INF_OR_NAN + index, false);
        m_defaultNumber = settings.getInt(DEFAULT_NUMBER + index, 0);
        m_missingCellOnInfinityOrNaN = settings.getBoolean(MISSING_CELL_ON_INF_OR_NAN + index, true);
        m_xmlFragmentName = settings.getString(XML_FRAGMENT_NAME + index, "fragment");

        m_multiTagOption =
            XPathMultiColOption.valueOf(settings.getString(MULTI_TAG_OPTION + index,
                XPathMultiColOption.SingleCell.toString()));
    }

    /**
     * Called from model when settings are to be loaded.
     *
     * @param settings To load from
     * @param index index of the xpath query in the xpath query list.
     * @throws InvalidSettingsException If settings are invalid.
     */
    void loadSettingsModel(final NodeSettingsRO settings, final int index) throws InvalidSettingsException {
        m_columnName = settings.getString(NEW_COLUMN + index);
        if (m_columnName == null || m_columnName.trim().isEmpty()) {
            throw new InvalidSettingsException("Please set a name for " + "the new column.");
        }
        m_columnName = m_columnName.trim();

        m_useAttributeForColName = settings.getBoolean(USE_ATTRIBUTE_FOR_COL_NAME + index);
        m_attributeForColName = settings.getString(ATTRIBUTE_FOR_COL_NAME + index);

        m_xpathQuery = settings.getString(XPATH_QUERY + index);
        if (null == m_xpathQuery) {
            throw new InvalidSettingsException("No XPath query defined.");
        }
        m_type = XPathOutput.valueOf(settings.getString(RETURN_TYPE + index));
        if (null == m_type) {
            throw new InvalidSettingsException("No return type defined.");
        }
        m_missingCellOnEmptySet = settings.getBoolean(MISSING_CELL_ON_EMPTY_SET + index);
        m_missingCellOnEmptyString = settings.getBoolean(MISSING_CELL_ON_EMPTY_STRING + index);
        m_valueOnInfinityOrNaN = settings.getBoolean(VALUE_ON_INF_OR_NAN + index);
        m_defaultNumber = settings.getInt(DEFAULT_NUMBER + index);
        m_missingCellOnInfinityOrNaN = settings.getBoolean(MISSING_CELL_ON_INF_OR_NAN + index);
        m_xmlFragmentName = settings.getString(XML_FRAGMENT_NAME + index);
        if ((m_type == XPathOutput.Node) && m_xmlFragmentName.trim().isEmpty()) {
            throw new InvalidSettingsException("The XML fragment name is " + "empty. Please define a valid name.");
        }
        m_multiTagOption = XPathMultiColOption.valueOf(settings.getString(MULTI_TAG_OPTION + index));
        if (null == m_multiTagOption) {
            throw new InvalidSettingsException("No multi tag option defined.");
        }

        m_colNameMap =  new TreeMap<Integer, String>();
    }

    /**
     * Called from model and dialog to save current settings.
     *
     * @param settings To save to.
     * @param index index of the xpath query in the xpath query list.
     */
    void saveSettings(final NodeSettingsWO settings, final int index) {
        settings.addString(NEW_COLUMN + index, m_columnName);
        settings.addBoolean(USE_ATTRIBUTE_FOR_COL_NAME + index, m_useAttributeForColName);
        settings.addString(ATTRIBUTE_FOR_COL_NAME + index, m_attributeForColName);
        settings.addString(XPATH_QUERY + index, m_xpathQuery);
        settings.addString(RETURN_TYPE + index, m_type.toString());
        settings.addBoolean(MISSING_CELL_ON_EMPTY_SET + index, m_missingCellOnEmptySet);
        settings.addBoolean(MISSING_CELL_ON_EMPTY_STRING + index, m_missingCellOnEmptyString);
        settings.addBoolean(VALUE_ON_INF_OR_NAN + index, m_valueOnInfinityOrNaN);
        settings.addBoolean(MISSING_CELL_ON_INF_OR_NAN + index, m_missingCellOnInfinityOrNaN);
        settings.addInt(DEFAULT_NUMBER + index, m_defaultNumber);
        settings.addString(XML_FRAGMENT_NAME + index, m_xmlFragmentName);
        settings.addString(MULTI_TAG_OPTION + index, m_multiTagOption.toString());
    }

    /**
     * @param multiTagOption multiple tag option
     */
    public void setMultipleTagOption(final XPathMultiColOption multiTagOption) {
        m_multiTagOption = multiTagOption;
    }

    /**
     * @return multiple tag option
     */
    public XPathMultiColOption getMultipleTagOption() {
        return m_multiTagOption;
    }

    /**
     * @param selected option if attribute value should be used as column name
     */
    public void setUseAttributeForColName(final boolean selected) {
        m_useAttributeForColName = selected;
    }

    /**
     * @return option if attribute value should be used as column name
     */
    public boolean getUseAttributeForColName() {
        return m_useAttributeForColName;
    }

    /**
     * @param attributeForColName XML attribute
     */
    public void setAttributeForColName(final String attributeForColName) {
        m_attributeForColName = attributeForColName;
    }

    /**
     * @return XML attribute
     */
    public String getAttributeForColName() {
        return m_attributeForColName;
    }

    /**
     * @param colNames a column name
     */
    public synchronized void addMultiColName(final ArrayList<StringCell> colNames) {
        for (StringCell colName : colNames) {
            if (!m_colNameMap.containsValue(colName.getStringValue())) {
                m_colNameMap.put(m_colNameMap.size(), colName.getStringValue());
            }
        }
    }

    /**
     * @param string a column name
     * @return false if more than one column name should be inserted
     */
    public synchronized boolean addSingleColname(final String string) {
        if (m_colNameMap.isEmpty()) {
            m_colNameMap.put(m_colNameMap.size(), string);
            return true;
        } else {
            return m_colNameMap.containsValue(string);
        }
    }

    /**
     * @return column names map
     */
    public TreeMap<Integer, String> getColumnNames() {
        return m_colNameMap;
    }

    /**
     * @return the currentColumnIndex
     */
    public int getCurrentColumnIndex() {
        return m_currentColumnIndex;
    }

    /**
     * @param currentColumnIndex the currentColumnIndex to set
     */
    public void setColIndexOfOutputTable(final int currentColumnIndex) {
        m_currentColumnIndex = currentColumnIndex;
    }

    /**
     * @return DataType of this XPath query or null if no type matches
     */
    public DataType getDataCellType() {
        if (m_type.equals(XPathOutput.Boolean)) {
            return BooleanCell.TYPE;
        } else if (m_type.equals(XPathOutput.Double)) {
            return DoubleCell.TYPE;
        } else if (m_type.equals(XPathOutput.Integer)) {
            return IntCell.TYPE;
        } else if (m_type.equals(XPathOutput.String)) {
            return StringCell.TYPE;
        } else if (m_type.equals(XPathOutput.Node)) {
            return XMLCell.TYPE;
        } else {
            return null;
        }
    }
}
