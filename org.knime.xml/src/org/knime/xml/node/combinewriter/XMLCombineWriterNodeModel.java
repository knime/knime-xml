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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   17.12.2010 (hofer): created
 */
package org.knime.xml.node.combinewriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.data.xml.io.XMLCellWriter;
import org.knime.core.data.xml.io.XMLCellWriterFactory;
import org.knime.core.data.xml.util.DefaultNamespaceContext;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model for the XML combine Writer node.
 * It takes an XML column from the input table combines it to a single
 * document and writes the document to a file.
 *
 * @author Heiko Hofer
 */
public class XMLCombineWriterNodeModel extends NodeModel {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            XMLCombineWriterNodeModel.class);
    private final XMLCombineWriterNodeSettings m_settings;

    /**
     * Creates a new model with no input port and one output port.
     */
    public XMLCombineWriterNodeModel() {
        super(1, 0);
        m_settings = new XMLCombineWriterNodeSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        // validate settings for the directory
        if (m_settings.getOutputFile() == null) {
            throw new InvalidSettingsException("No output file selected");
        }

        File file = new File(m_settings.getOutputFile());
        if (file.isDirectory()) {
            throw new InvalidSettingsException("'" + file.getAbsolutePath()
                    + "' is a directory");
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
                setWarningMessage("Auto guessing: using column \""
                        + compatibleCols.get(0) + "\".");
            } else {
                // TODO point to node for converting Data Table to XML
                throw new InvalidSettingsException("No XML "
                        + "column in input table.");
            }
        }

        if (m_settings.getRootElement().isEmpty()) {
            throw new InvalidSettingsException("The root element is "
                    + "empty. Please define a root element.");
        }
        // createRootAttributes(..) does some validity checking concerning
        // namespace definition
        Map<QName, String> rootAttributes = createRootAttributes(
                m_settings.getAttributeNames(),
                m_settings.getAttributeValues());
        // createRootAttributes(..) does some validity checking concerning
        // namespace definition
        createRootElement(m_settings.getRootElement(),
                rootAttributes);

        return new DataTableSpec[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        exec.checkCanceled();
        double max = inData[0].getRowCount();
        int count = 0;
        int missingCellCount = 0;

        final int colIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_settings.getInputColumn());

        File xmlFile = new File(m_settings.getOutputFile());

        if (!m_settings.getOverwriteExistingFiles() && xmlFile.exists()) {
            throw new IOException("File '" + xmlFile.getAbsolutePath()
                    + "' already exists");
        }

        Map<QName, String> rootAttributes = createRootAttributes(
                m_settings.getAttributeNames(),
                m_settings.getAttributeValues());
        QName rootElement = createRootElement(m_settings.getRootElement(),
                rootAttributes);

        XMLCellWriter xmlCellWriter =
            XMLCellWriterFactory.createXMLMultiCellWriter(
                new FileOutputStream(xmlFile), rootElement, rootAttributes);

        for (DataRow row : inData[0]) {
            exec.checkCanceled();
            exec.setProgress(count / max, "Writing row " + count + " ("
                    + max + ")");
            DataCell cell = row.getCell(colIndex);
            if (!cell.isMissing()) {
                xmlCellWriter.write((XMLValue)cell);
            } else {
                missingCellCount++;
                LOGGER.debug("Skip row " + row.getKey().getString()
                        + " since the cell is a missing data cell.");
            }
            count++;
        }
        xmlCellWriter.close();

        if (missingCellCount > 0) {
            setWarningMessage("Skipped " + missingCellCount + " rows due "
                    + "to missing values.");
        }
        return new BufferedDataTable[0];
    }


    /**
     * Create the list of attributes for the root element.
     *
     * @param attributeNames the names of the attributes
     * @param attributeValues the values of the attributes
     * @return a map of the attributes qualified name to their value
     */
    private Map<QName, String> createRootAttributes(
            final String[] attributeNames,
            final String[] attributeValues) {
        Map<QName, String> attrs = new HashMap<QName, String>();
        DefaultNamespaceContext nsContext = createNameSpaceContext(
                attributeNames, attributeValues);
        for (int i = 0; i < attributeNames.length; i++) {
            String qname = attributeNames[i];
            int colon = qname.indexOf(':');
            String prefix = colon > -1
                ? qname.substring(0, colon)
                : XMLConstants.DEFAULT_NS_PREFIX;
            String localName = colon > -1
                ? qname.substring(colon + 1)
                : qname;
            String nsURI = prefix.isEmpty()
                ? XMLConstants.NULL_NS_URI
                : nsContext.getNamespaceURI(prefix);
            if (null == nsURI) {
                throw new IllegalArgumentException("Please specify a "
                        + "namespace for the prefix: \"" + prefix
                        + "\" on the root element.");
            }
            attrs.put(new QName(nsURI, localName, prefix),
                    attributeValues[i]);
        }

        return attrs;
    }

    /** Create a namespace context for the root element. */
    private DefaultNamespaceContext createNameSpaceContext(
            final String[] attributeNames,
            final String[] attributeValues) {
        List<String> prefixes = new ArrayList<String>();
        List<String> namespaces = new ArrayList<String>();

        for (int i = 0; i < attributeNames.length; i++) {
            String qname = attributeNames[i];
            if (qname.equals("xmlns") || qname.startsWith("xmlns:")) {
                int colon = qname.indexOf(':');
                String prefix = colon > -1
                    ? qname.substring(colon + 1)
                    : "";
                if (prefixes.contains(prefix)) {
                    String msg = null == prefix
                            ? "The attributes of the root element contains "
                              + "more than one default name space definition."
                            : "The namespace prefix \"" + prefix
                              + "\" is definied more than one time "
                              + "for the root element";
                    throw new IllegalArgumentException(msg);
                }
                prefixes.add(prefix);
                namespaces.add(attributeValues[i]);
            }
        }


        return new DefaultNamespaceContext(
                prefixes.toArray(new String[prefixes.size()]),
                namespaces.toArray(new String[prefixes.size()]));
    }

    /**
     * Creates the QName object for the root element.
     */
    private QName createRootElement(final String qname,
            final Map<QName, String> rootAttributes) {
        // get namespace definitions from the attributes
        Map<String, String> nsMap = new HashMap<String, String>();
        for (QName attrQName : rootAttributes.keySet()) {
            String attrPrefix = attrQName.getPrefix();
            String attrLocalName = attrQName.getLocalPart();

            if (null == attrPrefix || attrPrefix.isEmpty()) {
                if (attrLocalName.equals("xmlns")) {
                    // default namespace definition
                    nsMap.put(XMLConstants.DEFAULT_NS_PREFIX,
                            rootAttributes.get(attrQName));
                }
            } else {
                if (attrPrefix.equals("xmlns")) {
                    // namespace definition
                    nsMap.put(attrLocalName, rootAttributes.get(attrQName));
                }
            }
        }
        // Analyze given qualified name for the root element
        int colon = qname.indexOf(':');
        String prefix = colon > -1
            ? qname.substring(0, colon)
            : XMLConstants.DEFAULT_NS_PREFIX;
        String localName = colon > -1
            ? qname.substring(colon + 1)
            : qname;

        String nsURI = null;
        if (nsMap.containsKey(prefix)) {
            nsURI = nsMap.get(prefix);
        } else {
            if (prefix.isEmpty()) {
                nsURI = XMLConstants.NULL_NS_URI;
            } else {
                throw new IllegalArgumentException("Please specify a "
                        + "namespace for the prefix: \"" + prefix
                        + "\" on the root element.");
            }
        }

        return new QName(nsURI, localName, prefix);
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
        new XMLCombineWriterNodeSettings().loadSettingsModel(settings);
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
