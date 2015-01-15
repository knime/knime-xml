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

import org.knime.base.collection.list.split.SplitCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionDataValue;
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

/**
 * This is the model for the XPath node. It takes an XML column from the input table and performs a XPath query on every
 * cell.
 *
 * @author Heiko Hofer
 */
final class XPathNodeModel extends SimpleStreamableFunctionNodeModel {

    private final XPathNodeSettings m_settings;

    private List<Integer> m_multiColPos = new ArrayList<Integer>();

    private RowKey m_firstRowKey;

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

        int offset = spec.getNumColumns();
        if (m_settings.getRemoveInputColumn()) {
            offset--;
        }
        m_multiColPos = new ArrayList<Integer>();
        for (XPathSettings xps : list) {

            XPathMultiColOption multipleTagOption = xps.getMultipleTagOption();
            if (multipleTagOption.equals(XPathMultiColOption.SingleCell)) {
                colRearranger.append(XPathSingleCellFactory.create(spec, m_settings, xps));
            } else {
                if (multipleTagOption.equals(XPathMultiColOption.MultipleColumns)) {
                    m_multiColPos.add(list.indexOf(xps) + offset);
                }
                colRearranger.append(XPathCollectionCellFactory.create(spec, m_settings, xps, m_firstRowKey));
            }
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
        // find first XMLCell in input table which is not missing
        DataRow row = getFirstRow(inData[0].iterator(), inputColIndex);
        XMLValue value = (XMLValue)row.getCell(inputColIndex);
        m_firstRowKey = row.getKey();

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

        // reset column names if at least one column name comes from an attribute.
        if (colNameFromAttribute) {
            // add all column names from input table
            colnames = new HashSet<String>(columnNames);
            m_settings.setNewColumnNames(value, colnames);
        }

        // now execute with all new settings
        BufferedDataTable in = inData[0];
        ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
        BufferedDataTable intermediateResult = exec.createColumnRearrangeTable(in, r, exec);

        intermediateResult = setCollectionElementNamesFromAttributes(intermediateResult, exec);

        if (m_multiColPos.isEmpty()) {
            return new BufferedDataTable[]{intermediateResult};
        } else {
            ColumnRearranger colRearranger = insertMultiColumns(intermediateResult, m_multiColPos);
            return new BufferedDataTable[]{exec.createColumnRearrangeTable(intermediateResult, colRearranger, exec)};
        }
    }

    /**
     * @param it Table row iterator
     * @param index of xml cell in data row
     * @return first datarow with not missing xml cell at {@index}
     * @throws InvalidSettingsException
     */
    private DataRow getFirstRow(final Iterator<DataRow> it, final int index) throws InvalidSettingsException {
        while (it.hasNext()) {
            DataRow row = it.next();
            XMLCell cell = (XMLCell)row.getCell(index);
            if (!cell.isMissing()) {
                return row;
            }
        }
        throw new InvalidSettingsException("All XMLCells in column " + index + "are missing.");
    }

    private ColumnRearranger insertMultiColumns(final BufferedDataTable in, final List<Integer> positions) {
        DataTableSpec spec = in.getDataTableSpec();
        ColumnRearranger colRearranger = new ColumnRearranger(spec);
        String[] names = spec.getColumnNames();

        HashSet<String> colnames = new HashSet<String>();
        for (int j = 0; j < names.length; j++) {
            if (!positions.contains(j)) {
                colnames.add(names[j]);
            }
        }
        int insertAt = positions.get(0);
        for (int i : positions) {

            List<String> elementNames = spec.getColumnSpec(i).getElementNames();
            DataType newCellType = spec.getColumnSpec(i).getType().getCollectionElementType();


            int m = 0;
            for (DataRow row : in) {
                DataCell cell = row.getCell(i);
                if (!cell.isMissing()) {
                    m = Math.max(m, ((CollectionDataValue)cell).size());
                }
            }

            DataColumnSpec[] colSpecs = new DataColumnSpec[m];
            String name = "";
            for (int j = 0; j < m; j++) {
                if (elementNames.size() > j) {
                    name = elementNames.get(j);
                } else {
                    name = "column(#" + j + ")";
                }
                DataColumnSpec dcs =
                    new DataColumnSpecCreator(XPathNodeSettings.uniqueName(name, "", 0, colnames), newCellType)
                        .createSpec();
                colSpecs[j] = dcs;
                colnames.add(dcs.getName());
            }
            SplitCellFactory spf = new SplitCellFactory(i, colSpecs);
            colRearranger.remove(insertAt);
            colRearranger.insertAt(insertAt, spf);
            insertAt += spf.getColumnSpecs().length;
        }
        return colRearranger;
    }

    private BufferedDataTable setCollectionElementNamesFromAttributes(final BufferedDataTable in,
        final ExecutionContext exec) {
        DataTableSpec dataTableSpec = in.getDataTableSpec();
        DataColumnSpec[] specs = new DataColumnSpec[dataTableSpec.getNumColumns()];

        for (int i = 0; i < specs.length; i++) {
            DataColumnSpecCreator dcsc = new DataColumnSpecCreator(dataTableSpec.getColumnSpec(i));
            if (dataTableSpec.getColumnSpec(i).getType().isCollectionType()) {
                dcsc.setElementNames(m_settings.getNextElementNames());
            }
            specs[i] = dcsc.createSpec();
        }

        DataTableSpec newSpec = new DataTableSpec(null, specs);

        return exec.createSpecReplacerTable(in, newSpec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        // Find first XML column
        if (m_settings.getInputColumn() == null) {
            Iterator<DataColumnSpec> it = inSpecs[0].iterator();
            while (it.hasNext()) {
                DataColumnSpec spec = it.next();
                if (spec.getType().equals(XMLCell.TYPE)) {
                    m_settings.setInputColumn(spec.getName());
                    break;
                }
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
