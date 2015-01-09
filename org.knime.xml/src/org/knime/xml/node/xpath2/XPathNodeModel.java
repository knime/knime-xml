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
 * ---------------------------------------------------------------------
 *
 * History
 *   17.12.2010 (hofer): created
 */
package org.knime.xml.node.xpath2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.xml.node.xpath2.XPathNodeSettings.XPathMultiColOption;
import org.w3c.dom.NodeList;

/**
 * This is the model for the XPath node. It takes an XML column from the input table and performs a XPath query on every
 * cell.
 *
 * @author Heiko Hofer
 */
final class XPathNodeModel extends SimpleStreamableFunctionNodeModel {

    private final XPathNodeSettings m_settings;

    /**
     * Creates a new model with no input port and one output port.
     */
    public XPathNodeModel() {

        m_settings = new XPathNodeSettings();
    }

    /** {@inheritDoc} */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        if (m_settings.getInputColumn() == null) {
            for (int i = 0; i < spec.getNumColumns(); i++) {
                if (spec.getColumnSpec(i).getType().equals(XMLCell.TYPE)) {
                    m_settings.setInputColumn(spec.getColumnSpec(i).getName());
                    break;
                }
            }
        }
        String inputColumn = m_settings.getInputColumn();
        if (inputColumn == null) {
            throw new InvalidSettingsException("No settings available");
        }
        DataColumnSpec inputColSpec = spec.getColumnSpec(inputColumn);
        if (inputColSpec == null) {
            throw new InvalidSettingsException("XML column \"" + inputColumn + "\" is not present in the input table");
        }
        if (!inputColSpec.getType().isCompatible(XMLValue.class)) {
            throw new InvalidSettingsException("XML column \"" + inputColumn + "\" is not of type XML");
        }
        ColumnRearranger colRearranger = new ColumnRearranger(spec);
        ArrayList<XPathSettings> list = m_settings.getXPathQueryList();

        for (XPathSettings xps : list) {
            colRearranger.append(XPathCellFactory.create(spec, m_settings, xps));
        }

        if (m_settings.getRemoveInputColumn()) {
            String xmlColumn = m_settings.getInputColumn();
            int xmlIndex = spec.findColumnIndex(xmlColumn);
            colRearranger.remove(xmlIndex);
        }
        return colRearranger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        int inputColIndex = inData[0].getSpec().findColumnIndex(m_settings.getInputColumn());
        boolean colNameFromAttribute = false;

        List<String> columnNames = Arrays.asList(inData[0].getSpec().getColumnNames());
        // add all column names from input table
        HashSet<String> colnames = new HashSet<String>(columnNames);

        // add all column names from single cell or collection cell columns
        // do not change user defined column names
        for (XPathSettings xps : m_settings.getXPathQueryList()) {
            // if only one column name comes from an attribute we have to reset all column names later on
            colNameFromAttribute = colNameFromAttribute || xps.getUseAttributeForColName();
            if (!xps.getMultipleTagOption().equals(XPathMultiColOption.MultipleColumns)) {
                colnames.add(xps.getNewColumn());
            }
        }

        m_settings.setXPathQueryList(expandMultiColOption(inData, inputColIndex, colnames));

        // find first XMLCell in input table which is not missing
        Iterator<DataRow> it = inData[0].iterator();
        XMLValue value = null;
        while (it.hasNext()) {
            XMLCell cell = (XMLCell)it.next().getCell(inputColIndex);
            if (!cell.isMissing()) {
                value = cell;
                break;
            }
        }

        // reset column names if at least one column name comes from an attribute.
        if (colNameFromAttribute) {
            // add all column names from input table
            colnames = new HashSet<String>(columnNames);
            m_settings.setNewColumnNames(value, colnames);
        }

        // now execute with all new settings
        return super.execute(inData, exec);
    }

    /**
     * Expands all MultiColumn option XPathSettings to SingleCell option XPathSettings.
     * @param inData input data table
     * @param inputColIndex column index of xml input column
     * @param colnames column names of all user defined columns and all columns from the input data table
     * @return List which only contains single-/collectioncell XPathSettings
     * @throws XPathExpressionException the thrown XPathExpressionException
     */
    private ArrayList<XPathSettings> expandMultiColOption(final BufferedDataTable[] inData, final int inputColIndex,
        final HashSet<String> colnames) throws XPathExpressionException {

        ArrayList<XPathSettings> newList = new ArrayList<XPathSettings>();
        for (int i = 0; i < m_settings.getXPathQueryList().size(); i++) {
            XPathSettings xps = m_settings.getXPathQueryList().get(i);

            if (xps.getMultipleTagOption().equals(XPathMultiColOption.MultipleColumns)) {
                // get xpath expression for this xml tag
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                XPathExpression xpathExpr = xpath.compile(xps.getXpathQuery());

                // iterate over all rows and determine max row width
                int m = 0;
                for (DataRow r : inData[0]) {

                    DataCell c = r.getCell(inputColIndex);

                    if (!c.isMissing()) {
                        XMLValue xmlValue = (XMLValue)c;

                        Object result = xpathExpr.evaluate(xmlValue.getDocument(), XPathConstants.NODESET);

                        NodeList nodes = (NodeList)result;
                        m = Math.max(m, nodes.getLength());
                    }
                }

                // add as man XPathSettings with SingleCell Option as possible based on input table
                for (int j = 0; j < m; j++) {
                    String name = xps.getNewColumn();
                    name = m_settings.uniqueName(name, "", 0, colnames);
                    XPathSettings x = new XPathSettings(xps, j + 1, name, XPathMultiColOption.SingleCell);
                    newList.add(x);
                    colnames.add(name);
                }
            } else {
                // if SingleCell or CollectionCell just add to newList
                newList.add(xps);
            }
        }
        return newList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        // Find first XML column
        Iterator<DataColumnSpec> it = inSpecs[0].iterator();
        while (it.hasNext()) {
            DataColumnSpec spec = it.next();
            if (spec.getType().equals(XMLCell.TYPE)) {
                m_settings.setInputColumn(spec.getName());
                break;
            }
        }

        if (m_settings.getInputColumn() == null) {
            throw new InvalidSettingsException("No XML column available.");
        }

        // DataTableSpec could alter based on input table and user input
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // no internals
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // no internals
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new XPathNodeSettings().loadSettingsModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettingsModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // no internals
    }

}
