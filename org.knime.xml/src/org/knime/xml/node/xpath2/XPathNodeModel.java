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
import org.knime.core.data.DataRow;
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
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableFunction;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
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
final class XPathNodeModel extends NodeModel {

    /**
     * Settings object with all settings.
     */
    private final XPathNodeSettings m_settings;

    /**
     * Intermediate table created when the node is run in streamed fashion but is not streamable. Table needs to be kept
     * in order to transfer it between the {@link StreamableOperator#runIntermediate(PortInput[], ExecutionContext)} and
     * {@link StreamableOperator#runFinal(PortInput[], PortOutput[], ExecutionContext)}-method calls.
     */
    private BufferedDataTable m_table;

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
        super(1,1);
        m_settings = new XPathNodeSettings();
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

        return new DataTableSpec[]{createFinalOutSpec(inSpecs[0])};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        return new BufferedDataTable[]{executeInternal(inData[0], exec)};
    }

    /**
     * Internal execution shared by {@link #execute(BufferedDataTable[], ExecutionContext)} and
     * {@link StreamableOperator#runIntermediate(PortInput[], ExecutionContext)}.
     *
     * @param inData input table
     * @param exec
     * @return final output table
     * @throws Exception
     */
    private BufferedDataTable executeInternal(final BufferedDataTable inData, final ExecutionContext exec)
        throws Exception {
        BufferedDataTable intermediate = null;
        if (isOutSpecKnown()) {
            //if the outspec is known, the query execution and the (optional) ungroup to rows (if desired) can be done on the fly
            RowInput in = new DataTableRowInput(inData);
            BufferedDataTableRowOutput out =
                new BufferedDataTableRowOutput(exec.createDataContainer(createKnownOutSpec(in.getDataTableSpec())));
            executeQueriesAndUngroupToRowsOnTheFly(in, out, exec.createSubExecutionContext(.95), inData.size());
            intermediate = out.getDataTable();
        } else {
            //otherwise the xml-queries have to be executed first before (optional) the ungroup to rows can be done
            intermediate = executeQueriesAndUngroupToRows(inData, exec.createSubExecutionContext(.95));
        }
        //return the table and (optionally) ungroup the result to columns
        return executeUngroupToColumns(intermediate, exec.createSubExecutionContext(0.05));
    }

    /**
     * Performs the actual execution, i.e. executing the queries and (optionally) ungrouping them to rows.
     * It does it on the fly, i.e. directly ungrouping collections-cells after their creation. This is only
     * possible, if column names are not determined by xml attributes (i.e. not depending on the input data).
     *
     * @param in in data
     * @param out out data
     * @param exec execution context to create new tables and log the progress
     * @param rowCount number of rows in table to log the progress, if <code>-1</code> no progress will be logged
     * @throws Exception
     */
    private void executeQueriesAndUngroupToRowsOnTheFly(final RowInput in, final RowOutput out, final ExecutionContext exec,
        final long rowCount) throws Exception {

        // TableSpec
        // SingleCol | (1) MultiColValues | (1) MultiColNames | CollectionCol | (2) MultiColValues | (2) MultiColNames
        // SingleCell| CollectionCell     | CollectionCell    | CollectionCell| CollectionCell     | CollectionCell

        DataTableSpec inSpec = in.getDataTableSpec();
        UngroupOperation ungroup = null;
        if (!m_ungroupIndices.isEmpty()) {
            int[] indices = new int[m_ungroupIndices.size()];
            for (int i = 0; i < m_ungroupIndices.size(); i++) {
                indices[i] = m_ungroupIndices.get(i);
            }
            ungroup = new UngroupOperation(false, false, true);
            ungroup.setColIndices(indices);
        }

        StreamableFunction fct = createColumnRearranger(inSpec).createStreamableFunction();
        fct.init(exec);
        try {
            DataRow inputRow;
            long index = 0;
            while ((inputRow = in.poll()) != null) {
                DataRow newRow = fct.compute(inputRow);
                if (ungroup != null) {
                    ungroup.compute(new SingleRowInput(newRow), out, exec, rowCount);
                } else {
                    out.push(newRow);
                }
                final long i = ++index;
                final DataRow r = inputRow;
                exec.setMessage(() -> String.format("Row %d (\"%s\"))", i, r.getKey()));
            }
        } finally {
            fct.finish();
            in.close();
            out.close();
        }

    }

    /**
     * This method is used when the on the fly execution (queries execution and ungrouping to rows) is not possible
     * (i.e. {@link #executeQueriesAndUngroupToRowsOnTheFly(RowInput, RowOutput, ExecutionContext, long, boolean)}. The
     * entire xml-query table is created first. In a second run the table is ungrouped into rows (if necessary and
     * configured so).
     *
     * @param inData
     * @return the result table
     * @throws Exception
     */
    private BufferedDataTable executeQueriesAndUngroupToRows(final BufferedDataTable in, final ExecutionContext exec)
        throws Exception {
        ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
        BufferedDataTable collectedXMLData = exec.createColumnRearrangeTable(in, r, exec);

        // TableSpec
        // SingleCol | (1) MultiColValues | (1) MultiColNames | CollectionCol | (2) MultiColValues | (2) MultiColNames
        // SingleCell| CollectionCell     | CollectionCell    | CollectionCell| CollectionCell     | CollectionCell

        DataTableSpec replacedNames = renameSingleNameCells(collectedXMLData.getDataTableSpec());
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
            ugO.setNewSpec(
                UngroupOperation.createTableSpec(dataTableWithSingleCellColNames.getDataTableSpec(), true, colNames));

            ungrouped = ugO.compute(exec.createSubExecutionContext(0.05));
        }
        return ungrouped;
    }

    /**
     * Performs the ungroup to column operation, if configured so.
     *
     * @param intermediate the result table of the intermediate execution, i.e. TODO
     * @param exec to log the progress
     * @return the result table
     * @throws CanceledExecutionException
     */
    private BufferedDataTable executeUngroupToColumns(final BufferedDataTable intermediate,
        final ExecutionContext exec) throws CanceledExecutionException {
        if (m_multiColPos.isEmpty()) {
            // no multiple column options ---> return
            return intermediate;
        } else {
            // expand all multiple column collections
            ColumnRearranger expandColRearranger = insertMultiColumns(intermediate);

            // TableSpec
            // SingleCol | (1) MC_col1 | (1) MC_col2 | CollectionCol | (2) MC_col1 | (2) MC_col2 | (2) MC_col3
            // SingleCell| SingleCell  | SingleCell  | CollectionCell| SingleCell  | SingleCell  | SingleCell

            return exec.createColumnRearrangeTable(intermediate, expandColRearranger, exec);
        }
    }



    /*---------------------------- Streaming API methods --------------------------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public InputPortRole[] getInputPortRoles() {
        return new InputPortRole[]{
            isOutSpecKnown() ? InputPortRole.NONDISTRIBUTED_STREAMABLE : InputPortRole.NONDISTRIBUTED_NONSTREAMABLE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        return new OutputPortRole[]{isOutSpecKnown() ? OutputPortRole.DISTRIBUTED : OutputPortRole.NONDISTRIBUTED};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean iterate(final StreamableOperatorInternals internals) {
        return !isOutSpecKnown() && m_table == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObjectSpec[] computeFinalOutputSpecs(final StreamableOperatorInternals internals,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        if (isOutSpecKnown()) {
            return configure(inSpecs);
        } else {
            return new PortObjectSpec[]{m_table.getDataTableSpec()};
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        if (isOutSpecKnown()) {
            return new StreamableOperator() {

                @Override
                public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec) throws Exception {
                    RowInput in = (RowInput) inputs[0];
                    RowOutput out = (RowOutput) outputs[0];
                    executeQueriesAndUngroupToRowsOnTheFly(in, out, exec, -1);
                }
            };
        } else {
            NodeLogger.getLogger(this.getClass())
                .warn("Is NOT executed in streamed fashion. Please check the node's configuration.");
            return new StreamableOperator() {

                @Override
                public void runIntermediate(final PortInput[] inputs, final ExecutionContext exec) throws Exception {
                    //workaround, otherwise it will result in an IllegalThreadStateException
                    KNIMEConstants.GLOBAL_THREAD_POOL
                        .submit(() -> m_table =
                            executeInternal((BufferedDataTable)((PortObjectInput)inputs[0]).getPortObject(), exec))
                        .get();
                }

                @Override
                public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
                    throws Exception {
                    //transfer the already created result table
                    ((RowOutput)outputs[0]).setFully(m_table);
                    m_table = null;
                }
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MergeOperator createMergeOperator() {
        //overwriting the #finishStreamableExecution-method requires this method to be overridden, too
        return new MergeOperator() {

            @Override
            public StreamableOperatorInternals mergeIntermediate(final StreamableOperatorInternals[] operators) {
                return null;
            }

            @Override
            public StreamableOperatorInternals mergeFinal(final StreamableOperatorInternals[] operators) {
                return operators[0];
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishStreamableExecution(final StreamableOperatorInternals internals, final ExecutionContext exec,
        final PortOutput[] output) throws Exception {
        if (!isOutSpecKnown()) {
            setWarningMessage("Node wasn't executed in streamed fashion due to its configuration.");
        }
    }


    /* ----------------------- Helper Methods -------------------------- */


    /**
     * Queries with the multiple column option will be read in as collection columns. In a second run we will expand all
     * multiple column collections.
     * @param spec data table spec of the input table
     * @return the column rearranger used to do most of the work
     * @throws InvalidSettingsException
     */
    private ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {

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
     * Create the out spec to the point where its know, also including the ungroup row operation.
     *
     * @param inSpec
     * @return the output spec
     * @throws InvalidSettingsException
     */
    private DataTableSpec createKnownOutSpec(final DataTableSpec inSpec) throws InvalidSettingsException {
        DataTableSpec initialOutSpec = renameSingleNameCells(createColumnRearranger(inSpec).createSpec());

        if (!m_ungroupIndices.isEmpty()) {
            int[] indices = new int[m_ungroupIndices.size()];
            String[] colNames = new String[m_ungroupIndices.size()];
            for (int i = 0; i < m_ungroupIndices.size(); i++) {
                indices[i] = m_ungroupIndices.get(i);
                colNames[i] = initialOutSpec.getColumnSpec(m_ungroupIndices.get(i)).getName();
            }
            return UngroupOperation.createTableSpec(initialOutSpec, true, colNames);
        } else {
            return initialOutSpec;
        }
    }


    /**
     * Creates the final out spec.
     *
     * @return the out spec, potentially <code>null</code> if cannot be predetermined (e.g. when column names are taken
     *         from the incoming xml data)
     * @throws InvalidSettingsException
     */
    private DataTableSpec createFinalOutSpec(final DataTableSpec inSpec) throws InvalidSettingsException {
        if (isOutSpecKnown()) {
            return createKnownOutSpec(inSpec);
        } else {
            return null;
        }
    }

    /**
     * Checks whether the out spec can be predetermined based on the current settings. The out spec can only be
     * predetermined if there no xpath-query whose result needs to be column-ungrouped or whose attribute values specify the
     * column names.
     *
     * If the output spec is know the node can be executed in real streamed fashion.
     *
     * @return <code>true</code> if the out spec can be predetermined
     */
    private boolean isOutSpecKnown() {
        return m_settings.getXPathQueryList().stream().allMatch(xps -> {
            return !xps.getUseAttributeForColName()
                && (xps.getMultipleTagOption().equals(XPathMultiColOption.SingleCell)
                    || xps.getMultipleTagOption().equals(XPathMultiColOption.CollectionCell)
                    || xps.getMultipleTagOption().equals(XPathMultiColOption.UngroupToRows));
        });
    }

    /**
     * Renames all Single-, Collection- and UngroupCollectionColumns which take their name from an xml element.
     * @param intermediateResultSpec datatable-specs with all SingleCell columns and Collection columns
     * @return table spec with new unique names.
     */
    private DataTableSpec renameSingleNameCells(final DataTableSpec intermediateResultSpec) throws InvalidSettingsException {
        int numColumns = intermediateResultSpec.getNumColumns();
        DataTableSpec spec = intermediateResultSpec;
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


    /* ------------------ load/save --------------------*/

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
        m_table = null;
    }

    /**
     * {@link RowInput} wrapping just one single row.
     *
     */
    private class SingleRowInput extends RowInput {

        private DataRow m_row;

        /**
         *
         */
        public SingleRowInput(final DataRow row) {
            m_row = row;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataTableSpec getDataTableSpec() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataRow poll() throws InterruptedException {
            DataRow r = m_row;
            m_row = null;
            return r;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            //
        }

    }
}
