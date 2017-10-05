/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   05.05.2011 (hofer): created
 */
package org.knime.xml.node.stringtoxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;

/**
 * NodeModel of the String To XML Node.
 *
 * @author Heiko Hofer
 */
public class StringToXMLNodeModel extends NodeModel {

    /* Node Logger of this class. */
    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(StringToXMLNodeModel.class);

    /**
     * Key for the included columns in the NodeSettings.
     */
    public static final String CFG_INCLUDED_COLUMNS = "include";

    /** The included columns. */
    private final SettingsModelFilterString m_inclCols =
            new SettingsModelFilterString(CFG_INCLUDED_COLUMNS);

    /**
     * Constructor with one inport and one outport.
     */
    public StringToXMLNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        // find indices to work on
        int[] indices = findColumnIndices(inSpecs[0]);
        ConverterFactory converterFac =
                new ConverterFactory(indices, inSpecs[0]);
        ColumnRearranger colre = new ColumnRearranger(inSpecs[0]);
        colre.replace(converterFac, indices);
        DataTableSpec newspec = colre.createSpec();
        return new DataTableSpec[]{newspec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        StringBuilder warnings = new StringBuilder();
        // find indices to work on.
        DataTableSpec inspec = inData[0].getDataTableSpec();
        List<String> inclcols = m_inclCols.getIncludeList();

        if (inclcols.size() == 0) {
            // nothing to convert, let's return the input table.
            setWarningMessage("No columns selected,"
                    + " returning input DataTable.");
            return new BufferedDataTable[]{inData[0]};
        }
        int[] indices = findColumnIndices(inData[0].getSpec());
        ConverterFactory converterFac = new ConverterFactory(indices, inspec);
        ColumnRearranger colre = new ColumnRearranger(inspec);
        colre.replace(converterFac, indices);

        BufferedDataTable resultTable =
                exec.createColumnRearrangeTable(inData[0], colre, exec);
        String errorMessage = converterFac.getErrorMessage();

        if (errorMessage.length() > 0) {
            warnings.append("Problems occurred, see Console messages.\n");
        }
        if (warnings.length() > 0) {
            LOGGER.warn(errorMessage);
            setWarningMessage(warnings.toString());
        }
        return new BufferedDataTable[]{resultTable};
    }

    private int[] findColumnIndices(final DataTableSpec spec)
            throws InvalidSettingsException {
        if (m_inclCols.getIncludeList().isEmpty()
                && m_inclCols.getExcludeList().isEmpty()
                && spec.getNumColumns() > 0) {
            // do auto-configuration
            List<String> toIncl = new ArrayList<String>();
            List<String> toExcl = new ArrayList<String>();
            for (int i = 0; i < spec.getNumColumns(); i++) {
                DataColumnSpec col = spec.getColumnSpec(i);
                if (col.getType().isCompatible(StringValue.class)) {
                    toIncl.add(col.getName());
                } else {
                    toExcl.add(col.getName());
                }
            }
            m_inclCols.setNewValues(toIncl, toExcl, false);
        }

        List<String> inclcols = m_inclCols.getIncludeList();
        StringBuilder warnings = new StringBuilder();
        if (inclcols.size() == 0) {
            warnings.append("No columns selected");
        }
        List<Integer> indicesvec = new ArrayList<Integer>();
        if (m_inclCols.isKeepAllSelected()) {
            for (int i = 0; i < spec.getNumColumns(); i++) {
                DataColumnSpec col = spec.getColumnSpec(i);
                if (col.getType().isCompatible(StringValue.class)) {
                    indicesvec.add(i);
                }
            }
        } else {
            for (int i = 0; i < inclcols.size(); i++) {
                int colIndex = spec.findColumnIndex(inclcols.get(i));
                if (colIndex >= 0) {
                    DataType type = spec.getColumnSpec(colIndex).getType();
                    if (type.isCompatible(StringValue.class)) {
                        indicesvec.add(colIndex);
                    } else {
                        warnings.append("Ignoring column \""
                                        + spec.getColumnSpec(colIndex).getName()
                                        + "\". Wrong type.\n");
                    }
                } else {
                    throw new InvalidSettingsException("Column \""
                            + inclcols.get(i) + "\" not found.");
                }
            }
        }
        if (warnings.length() > 0) {
            setWarningMessage(warnings.toString());
        }
        int[] indices = new int[indicesvec.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indicesvec.get(i);
        }
        return indices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nod does not have internals to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_inclCols.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_inclCols.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_inclCols.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // node does not have internal settings
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // node does not have internal settings
    }

    /**
     * The CellFactory to produce the new converted cells.
     *
     * @author Heiko Hofer
     */
    private class ConverterFactory implements CellFactory {

        /*
         * Column indices to use.
         */
        private final int[] m_colindices;

        /*
         * Original DataTableSpec.
         */
        private final DataTableSpec m_spec;

        /*
         * Error messages.
         */
        private final StringBuilder m_error;

        /**
         *
         * @param colindices the column indices to use.
         * @param spec the original DataTableSpec.
         */
        ConverterFactory(final int[] colindices, final DataTableSpec spec) {
            m_colindices = colindices;
            m_spec = spec;
            m_error = new StringBuilder();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataCell[] getCells(final DataRow row) {
            DataCell[] newcells = new DataCell[m_colindices.length];
            for (int i = 0; i < newcells.length; i++) {
                DataCell dc = row.getCell(m_colindices[i]);
                if (dc.isMissing()) {
                    newcells[i] = DataType.getMissingCell();
                } else {
                    try {
                        newcells[i] = XMLCellFactory.create(dc.toString());
                    } catch (Exception e) {
                        Throwable cause = e;
                        while ((cause.getCause() != cause) && (cause.getCause() != null)) {
                            cause = cause.getCause();
                        }

                        m_error.append("Cell in row:\""
                            + row.getKey().getString()
                            + "\" and column \""
                            + m_spec.getColumnSpec(m_colindices[i]).getName()
                            + "\" could not be parsed: " + cause.getMessage() + " Add missing value.\n");
                        newcells[i] = DataType.getMissingCell();
                    }
                }
            }
            return newcells;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public DataColumnSpec[] getColumnSpecs() {
            DataColumnSpec[] newcolspecs =
                    new DataColumnSpec[m_colindices.length];
            for (int i = 0; i < newcolspecs.length; i++) {
                DataColumnSpec colspec = m_spec.getColumnSpec(m_colindices[i]);
                DataColumnSpecCreator colspeccreator = null;
                // change DataType to XMLCell
                colspeccreator =
                        new DataColumnSpecCreator(colspec.getName(),
                                XMLCell.TYPE);
                newcolspecs[i] = colspeccreator.createSpec();
            }
            return newcolspecs;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setProgress(final int curRowNr, final int rowCount,
                final RowKey lastKey, final ExecutionMonitor exec) {
            exec.setProgress((double)curRowNr / (double)rowCount, "Converting");
        }

        /**
         * Error messages that occur during execution , i.e.
         * NumberFormatException.
         *
         * @return error message
         */
        public String getErrorMessage() {
            return m_error.toString();
        }

    } // end ConverterFactory
}
