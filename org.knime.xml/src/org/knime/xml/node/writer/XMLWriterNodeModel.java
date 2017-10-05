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
 * ---------------------------------------------------------------------
 *
 * History
 *   17.12.2010 (hofer): created
 */
package org.knime.xml.node.writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.data.xml.io.XMLCellWriter;
import org.knime.core.data.xml.io.XMLCellWriterFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.core.util.PathUtils;

/**
 * This is the model for the XML Writer node. It takes an XML column from the
 * input table and writes each cell into a separate file in the output
 * directory.
 *
 * @author Heiko Hofer
 */
public class XMLWriterNodeModel extends NodeModel {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            XMLWriterNodeModel.class);
    private final XMLWriterNodeSettings m_settings;

    /**
     * Creates a new model with no input port and one output port.
     */
    public XMLWriterNodeModel() {
        super(1, 0);
        m_settings = new XMLWriterNodeSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        StringBuilder warnings = new StringBuilder();

        // validate settings for the directory
        String warning = CheckUtils.checkDestinationDirectory(m_settings.getFolder());
        if (warning != null) {
            warnings.append(warning).append('\n');
        }

        // validate settings for the XML column
        if (null == m_settings.getInputColumn()) {
            List<String> compatibleCols = new ArrayList<String>();
            for (DataColumnSpec c : inSpecs[0]) {
                if (c.getType().isCompatible(XMLValue.class)) {
                    compatibleCols.add(c.getName());
                }
            }
            if (compatibleCols.size() == 1) {
                // auto-configure
                m_settings.setInputColumn(compatibleCols.get(0));
            } else if (compatibleCols.size() > 1) {
                // auto-guessing
                m_settings.setInputColumn(compatibleCols.get(0));
                warnings.append("Auto guessing: using column \"").append(compatibleCols.get(0)).append("\".");
            } else {
                throw new InvalidSettingsException("No XML column in input table. "
                    + "Consider using the 'String to XML' node.");
            }
        }

        if (warnings.length() > 0) {
            setWarningMessage(warnings.toString().trim());
        }

        return new DataTableSpec[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        CheckUtils.checkDestinationDirectory(m_settings.getFolder());

        int max = inData[0].getRowCount();

        URL remoteBaseUrl = FileUtil.toURL(m_settings.getFolder());
        Path localDir = FileUtil.resolveToPath(remoteBaseUrl);

        final int colIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_settings.getInputColumn());

        int count = 0;
        int missingCellCount = 0;
        for (DataRow row : inData[0]) {
            exec.checkCanceled();
            String name = row.getKey() + ".xml";
            exec.setProgress(count++ / (double) max, "Writing " + name + " (" + count + " of " + max + ")");

            DataCell cell = row.getCell(colIndex);
            if (cell.isMissing()) {
                missingCellCount++;
                LOGGER.debug("Skipping row " + row.getKey().getString() + " since the cell is missing.");
            } else {
                Path xmlFile = null;
                URL xmlUrl = null;
                if (localDir != null) {
                    xmlFile = PathUtils.resolvePath(localDir, name);
                    if (!m_settings.getOverwriteExistingFiles() && Files.exists(xmlFile)) {
                        throw new IOException("Output file '" + xmlFile
                            + "' exists and must not be overwritten due to user settings");
                    }
                } else {
                    xmlUrl = new URL(remoteBaseUrl.toString() + "/" + name);
                }

                try (XMLCellWriter xmlCellWriter = XMLCellWriterFactory.createXMLCellWriter(
                    openOutputStream(xmlUrl, xmlFile))) {
                    xmlCellWriter.write((XMLValue)cell);
                }
            }
        }

        if (missingCellCount > 0) {
            setWarningMessage("Skipped " + missingCellCount + " rows due "
                    + "to missing values.");
        }
        return new BufferedDataTable[0];
    }

    private static OutputStream openOutputStream(final URL url, final Path file) throws IOException {
        if (file != null) {
            return new BufferedOutputStream(Files.newOutputStream(file));
        } else {
            return new BufferedOutputStream(FileUtil.openOutputConnection(url, "PUT").getOutputStream());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
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
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        new XMLWriterNodeSettings().loadSettingsModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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
