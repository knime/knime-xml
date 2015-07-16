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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.base.node.preproc.ungroup.UngroupOperation;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.xml.PMMLCell;
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
import org.knime.xml.node.xpath2.CellFactories.XMLSplitCollectionCellFactory;
import org.knime.xml.node.xpath2.CellFactories.XPathCollectionCellFactory;
import org.knime.xml.node.xpath2.CellFactories.XPathMultiColCollectionCellFactory;
import org.knime.xml.node.xpath2.CellFactories.XPathSingleCellFactory;

/**
 * This is the model for the XPath node. It takes an XML column from the input table and performs a XPath query on every
 * cell.
 *
 * @author Tim-Oliver Buchholz, KNIME.com AG, Zurich, Switzerland
 */
final class XPathNodeModel extends SimpleStreamableFunctionNodeModel {

    /**
     * Settings object with all settings.
     */
    private final XPathNodeSettings m_settings;

    /**
     * Indices of {@link XPathSettings}, which will be expanded to multiple columns, in
     * {@link XPathNodeSettings#getXPathQueryList()}.
     */
    private List<Integer> m_multiColPos = new ArrayList<Integer>();

    /**
     * Number of columns in input table.
     */
    private int m_offset;

    /**
     * Indices of {@link XPathSettings}, which will be ungrouped to rows.
     */
    private List<Integer> m_ungroupIndices = new ArrayList<Integer>();

    /**
     * Creates a new model with no input port and one output port.
     */
    public XPathNodeModel() {
        m_settings = new XPathNodeSettings();
    }

    /**
     * Queries with the multiple column option will be read in as collection columns. In a second run we will expand all
     * multiple column collections. {@inheritDoc}
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {

        ColumnRearranger colRearranger = new ColumnRearranger(spec);
        List<XPathSettings> xpathQueries = m_settings.getXPathQueryList();

        m_multiColPos = new ArrayList<Integer>();
        m_ungroupIndices = new ArrayList<Integer>();

        m_offset = spec.getNumColumns();
        if (m_settings.getRemoveInputColumn()) {
            m_offset--;
        }

        int xpsIndex = 0;
        Set<String> colNames = new HashSet<String>();
        for (XPathSettings xps : xpathQueries) {
            xps.setColIndexOfOutputTable(m_offset + xpsIndex);
            xpsIndex++;

            // while we create the first column spec we have to ensure unique column names. therefore
            // we reset the column names during the spec creation.
            String currentName = xps.getNewColumn();
            String specName = XPathNodeSettings.uniqueName(currentName, "", 0, colNames);
            colNames.add(specName);
            xps.setNewColumn(specName);

            XPathMultiColOption multipleTagOption = xps.getMultipleTagOption();
            if (multipleTagOption.equals(XPathMultiColOption.MultipleColumns)) {
                // for one multicol query we add two collection columns
                // first contains all values and the second contains all column names
                // columns will be expanded later
                xpsIndex++;
                colRearranger.append(XPathMultiColCollectionCellFactory.create(spec, m_settings, xps));

                // remember position of multicol query
                m_multiColPos.add(xps.getCurrentColumnIndex());
            } else if (multipleTagOption.equals(XPathMultiColOption.SingleCell)) {
                colRearranger.append(XPathSingleCellFactory.create(spec, m_settings, xps));
            } else if (multipleTagOption.equals(XPathMultiColOption.UngroupToRows)) {
                colRearranger.append(XPathCollectionCellFactory.create(spec, m_settings, xps));
                m_ungroupIndices.add(xps.getCurrentColumnIndex());
            } else {
                colRearranger.append(XPathCollectionCellFactory.create(spec, m_settings, xps));
            }
            // after the spec is created we can reset the column name to the old one.
            // So the disabled column names in the dialog wont change.
            xps.setNewColumn(currentName);
        }

        // remove input column
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

        BufferedDataTable in = inData[0];
        ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
        BufferedDataTable collectedXMLData = exec.createColumnRearrangeTable(in, r, exec.createSubProgress(0.9));

        // TableSpec
        // SingleCol | (1) MultiColValues | (1) MultiColNames | CollectionCol | (2) MultiColValues | (2) MultiColNames
        // SingleCell| CollectionCell     | CollectionCell    | CollectionCell| CollectionCell     | CollectionCell

        DataTableSpec replacedNames = renameSingleNameCells(collectedXMLData);
        BufferedDataTable dataTableWithSingleCellColNames =
            exec.createSpecReplacerTable(collectedXMLData, replacedNames);

        BufferedDataTable ungrouped = dataTableWithSingleCellColNames;
        if (!m_ungroupIndices.isEmpty()) {
            int[] indices = new int[m_ungroupIndices.size()];
            String[] colNames = new String[m_ungroupIndices.size()];
            for (int i = 0; i < m_ungroupIndices.size(); i++) {
                indices[i] = m_ungroupIndices.get(i);
                colNames[i] =
                    dataTableWithSingleCellColNames.getDataTableSpec().getColumnSpec(m_ungroupIndices.get(i)).getName();
            }
            UngroupOperation ugO = new UngroupOperation(false, false, true);
            ugO.setColIndices(indices);
            ugO.setTable(dataTableWithSingleCellColNames);
            ugO.setNewSpec(UngroupOperation.createTableSpec(dataTableWithSingleCellColNames.getDataTableSpec(), true,
                colNames));

            ungrouped = ugO.compute(exec.createSubExecutionContext(0.05));
        }

        if (m_multiColPos.isEmpty()) {
            // no multiple column options ---> return
            return new BufferedDataTable[]{ungrouped};
        } else {
            // expand all multiple column collections
            ColumnRearranger expandColRearranger = insertMultiColumns(ungrouped);

            // TableSpec
            // SingleCol | (1) MC_col1 | (1) MC_col2 | CollectionCol | (2) MC_col1 | (2) MC_col2 | (2) MC_col3
            // SingleCell| SingleCell  | SingleCell  | CollectionCell| SingleCell  | SingleCell  | SingleCell

            return new BufferedDataTable[]{exec.createColumnRearrangeTable(ungrouped, expandColRearranger, exec.createSubProgress(0.05))};
        }
    }

    /**
     * Renames all Single-, Collection- and UngroupCollectionColumns which take their name from an xml element.
     * @param intermediateResult DataTable with all SingleCell columns and Collection columns
     * @return table spec with new unique names.
     */
    private DataTableSpec renameSingleNameCells(final BufferedDataTable intermediateResult) throws InvalidSettingsException {
        int numColumns = intermediateResult.getDataTableSpec().getNumColumns();
        DataTableSpec spec = intermediateResult.getDataTableSpec();
        List<XPathSettings> xpsList = m_settings.getXPathQueryList();

        Set<String> usedNames = new HashSet<String>();
        usedNames.addAll(Arrays.asList(spec.getColumnNames()));
        for (XPathSettings x : xpsList) {
            usedNames.remove(spec.getColumnSpec(x.getCurrentColumnIndex()).getName());
        }

        DataColumnSpec[] updatedColSpecs = new DataColumnSpec[numColumns];
        for (int i = 0; i < numColumns; i++) {
            updatedColSpecs[i] = spec.getColumnSpec(i);
        }

        for (XPathSettings x : xpsList) {
            if (!x.getMultipleTagOption().equals(XPathMultiColOption.MultipleColumns)) {
                if (x.getUseAttributeForColName()) {
                    String name = XPathNodeSettings.uniqueName(x.getColumnNames().get(0), "", 0, usedNames);
                    usedNames.add(name);

                    DataColumnSpecCreator crea = new DataColumnSpecCreator(updatedColSpecs[x.getCurrentColumnIndex()]);
                    crea.setName(name);
                    updatedColSpecs[x.getCurrentColumnIndex()] = crea.createSpec();
                }
            }
        }
        return new DataTableSpec(updatedColSpecs);
    }

    /**
     * Expands every collection column which is marked as multiple columns. For every found column name a new column is
     * created. Every value will be matched to its corresponding column.
     *
     * @param in data table with collection cells for values and names
     * @return data table with expanded collection cells
     */
    private ColumnRearranger insertMultiColumns(final BufferedDataTable in) {
        DataTableSpec spec = in.getDataTableSpec();
        ColumnRearranger colRearranger = new ColumnRearranger(spec);
        Set<String> usedColNames = new HashSet<String>();

        for (int i = 0; i < spec.getNumColumns(); i++) {
            if (!(m_multiColPos.contains(i))) {
                usedColNames.add(spec.getColumnSpec(i).getName());
            } else {
                i++;
            }
        }

        int posIndex = 0;
        int offset = 0;
        for (XPathSettings x : m_settings.getXPathQueryList()) {
            if (x.getMultipleTagOption().equals(XPathMultiColOption.MultipleColumns)) {
                List<String> columnNames = x.getColumnNames();
                String[] colNames = new String[columnNames.size()];

                for (int i = 0; i < colNames.length; i++) {
                    String uName = XPathNodeSettings.uniqueName(columnNames.get(i), "", i, usedColNames);
                    colNames[i] = uName;
                    usedColNames.add(uName);
                }

                int pos = m_multiColPos.get(posIndex++);
                DataType[] types = new DataType[colNames.length];
                DataType t = in.getDataTableSpec().getColumnSpec(pos).getType().getCollectionElementType();
                for (int i = 0; i < types.length; i++) {
                    types[i] = t;
                }

                DataColumnSpec[] dcs = DataTableSpec.createColumnSpecs(colNames, types);

                Map<String, Integer> reverseColNames = new HashMap<String, Integer>();

                for (int i = 0; i < columnNames.size(); i++) {
                    reverseColNames.put(columnNames.get(i), i);
                }

                colRearranger.insertAt(offset + pos + 2,
                    XMLSplitCollectionCellFactory.create(dcs, reverseColNames, pos, pos + 1));
                colRearranger.remove(offset + pos + 1, offset + pos);
                offset += dcs.length;

                offset -= 2;
            }
        }
        return colRearranger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec specs = inSpecs[0];

        // Find first XML column
        if (m_settings.getInputColumn() == null) {
            Iterator<DataColumnSpec> it = specs.iterator();
            while (it.hasNext()) {
                DataColumnSpec spec = it.next();
                if (spec.getType().equals(XMLCell.TYPE) || spec.getType().equals(PMMLCell.TYPE)) {
                    m_settings.setInputColumn(spec.getName());
                    break;
                }
            }
        }
        String inputColumn = m_settings.getInputColumn();
        if (inputColumn == null) {
            throw new InvalidSettingsException("No settings available");
        }
        DataColumnSpec inputColSpec = specs.getColumnSpec(inputColumn);
        if (inputColSpec == null) {
            throw new InvalidSettingsException("XML column \"" + inputColumn + "\" is not present in the input table");
        }
        if (!inputColSpec.getType().isCompatible(XMLValue.class)) {
            throw new InvalidSettingsException("XML column \"" + inputColumn + "\" is not of type XML");
        }

        // DataTableSpec could change based on input table and user input
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
