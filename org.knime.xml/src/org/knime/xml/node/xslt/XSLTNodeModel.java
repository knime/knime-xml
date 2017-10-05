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
 *   12.05.2010 (hofer): created
 */
package org.knime.xml.node.xslt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model for the XSLT node.
 *
 * @author Heiko Hofer
 */
public class XSLTNodeModel extends NodeModel {
    private final XSLTNodeSettings m_settings;

    /**
     * Creates a new model with no input port and one output port.
     */
    public XSLTNodeModel() {
        super(2, 1);
        m_settings = new XSLTNodeSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
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
                setWarningMessage("Auto guessing: using column \""
                        + compatibleCols.get(0) + "\".");
            } else {
                // TODO point to node for converting Data Table to XML
                throw new InvalidSettingsException("No XML "
                        + "column in input table.");
            }
        }
        // validate new column name
        if (null == m_settings.getNewColumn()) {
            // auto-configure
            m_settings.setNewColumn(
                    DataTableSpec.getUniqueColumnName(inSpecs[0],
                            m_settings.getInputColumn() + "_XSLT"));
        }
        if (m_settings.getNewColumn().trim().isEmpty()) {
            throw new InvalidSettingsException("Please set a name for "
                    + "the new column.");
        } else {
            m_settings.setNewColumn(m_settings.getNewColumn().trim());
        }
        // validate settings for the XSLT column
        if (null == m_settings.getXsltColumn()) {
            List<String> compatibleCols = new ArrayList<String>();
            for (DataColumnSpec c : inSpecs[1]) {
                if (c.getType().isCompatible(XMLValue.class)) {
                    compatibleCols.add(c.getName());
                }
            }
            if (compatibleCols.size() == 1) {
                // auto-configure
                m_settings.setXsltColumn(compatibleCols.get(0));
            } else if (compatibleCols.size() > 1) {
                // auto-guessing
                m_settings.setXsltColumn(compatibleCols.get(0));
                setWarningMessage("Auto guessing: using column \""
                        + compatibleCols.get(0) + "\".");
            } else {
                // TODO point to node for converting Data Table to XML
                throw new InvalidSettingsException("No XML "
                        + "column in stylesheet table.");
            }
        }

        ColumnRearranger rearranger = createColumnRearranger(inSpecs[0],
                inSpecs[1], null);
        return new DataTableSpec[]{rearranger.createSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        DataTableSpec inSpec = inData[0].getDataTableSpec();
        DataTableSpec xsltSpec = inData[1].getDataTableSpec();
        ColumnRearranger rearranger = createColumnRearranger(inSpec, xsltSpec,
                inData[1]);
        BufferedDataTable outTable =
                exec.createColumnRearrangeTable(inData[0], rearranger, exec);
        return new BufferedDataTable[]{outTable};
    }

    private ColumnRearranger createColumnRearranger(final DataTableSpec spec,
            final DataTableSpec xsltSpec, final BufferedDataTable xsltData)
            throws InvalidSettingsException {
        // check user settings against input spec here
        String xmlColumn = m_settings.getInputColumn();
        final int xmlIndex = spec.findColumnIndex(xmlColumn);
        if (xmlIndex < 0) {
            throw new InvalidSettingsException(
                    "No such column in input table: " + xmlColumn);
        }
        String newName = m_settings.getNewColumn();
        if ((spec.containsName(newName) && !newName.equals(xmlColumn))
                || (spec.containsName(newName) && newName.equals(xmlColumn)
                        && !m_settings.getRemoveInputColumn())) {
            throw new InvalidSettingsException("Cannot create column "
                    + newName + "since it is already in the input.");
        }
        String xsltColumn = m_settings.getXsltColumn();
        final int xsltIndex = xsltSpec.findColumnIndex(xsltColumn);
        if (xsltIndex < 0) {
            throw new InvalidSettingsException(
                    "No such column in stylesheet table: " + xsltColumn);
        }

        if (null != xsltData && xsltData.getRowCount() <= 0) {
            throw new IllegalStateException("The stylesheet table ist empty");
        }

        ColumnRearranger colRearranger = new ColumnRearranger(spec);
        DataType baseType = m_settings.getOutputIsXML() ? XMLCell.TYPE
                : StringCell.TYPE;
        DataType newCellType = m_settings.getUseFirstStylesheeOnly()
            ? baseType : DataType.getType(ListCell.class, baseType);

        DataColumnSpecCreator appendSpec = new DataColumnSpecCreator(newName,
                newCellType);
        colRearranger.append(new SingleCellFactory(appendSpec.createSpec()) {
            @Override
            public DataCell getCell(final DataRow row) {
                DataCell xmlCell = row.getCell(xmlIndex);
                if (xmlCell.isMissing()) {
                    return DataType.getMissingCell();
                }
                XMLValue xmlValue = (XMLValue) xmlCell;
                DataCell newCell = null;
                try {
                    if (m_settings.getUseFirstStylesheeOnly()) {
                        DataCell xsltCell = xsltData.iterator().next()
                            .getCell(xsltIndex);
                        if (xsltCell.isMissing()) {
                            return DataType.getMissingCell();
                        }
                        XMLValue xsltValue = (XMLValue) xsltCell;
                        DOMSource source = new DOMSource(
                                xmlValue.getDocument());
                        DOMSource stylesheet = new DOMSource(
                                xsltValue.getDocument());
                        TransformerFactory transFact =
                            TransformerFactory.newInstance();
                        Transformer trans;
                            trans = transFact.newTransformer(stylesheet);
                        // this will take precedence over any encoding specified
                        // in the stylesheet
                        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        trans.transform(source, new StreamResult(os));
                        String result = os.toString("UTF-8");
                        if (m_settings.getOutputIsXML()) {
                            newCell = XMLCellFactory.create(result);
                        } else {
                            newCell = new StringCell(result);
                        }
                    } else {
                        List<DataCell> cells = new ArrayList<DataCell>();
                        DOMSource source = new DOMSource(
                                xmlValue.getDocument());
                        TransformerFactory transFact =
                            TransformerFactory.newInstance();
                        for (CloseableRowIterator iter = xsltData.iterator();
                            iter.hasNext();) {
                            DataCell xsltCell = iter.next().getCell(xsltIndex);
                            if (xsltCell.isMissing()) {
                                cells.add(DataType.getMissingCell());
                            }
                            XMLValue xsltValue = (XMLValue) xsltCell;
                            DOMSource stylesheet = new DOMSource(
                                    xsltValue.getDocument());
                            Transformer trans = transFact.newTransformer(
                                    stylesheet);
                            // this will take precedence over any encoding
                            // specified in the stylesheet
                            trans.setOutputProperty(OutputKeys.ENCODING,
                                    "UTF-8");
                            ByteArrayOutputStream os =
                                new ByteArrayOutputStream();
                            trans.transform(source, new StreamResult(os));
                            String result = os.toString("UTF-8");
                            if (m_settings.getOutputIsXML()) {
                                cells.add(XMLCellFactory.create(result));
                            } else {
                                cells.add(new StringCell(result));
                            }
                        }
                        newCell = CollectionCellFactory.createListCell(cells);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                return newCell;
            }
        });
        if (m_settings.getRemoveInputColumn()) {
            colRearranger.remove(xmlIndex);
        }
        return colRearranger;
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
        new XSLTNodeSettings().loadSettingsModel(settings);
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
