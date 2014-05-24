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
 *   16.12.2010 (hofer): created
 */
package org.knime.xml.node.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.data.xml.io.LimitedXPathMatcher;
import org.knime.core.data.xml.io.XMLCellReader;
import org.knime.core.data.xml.io.XMLCellReaderFactory;
import org.knime.core.data.xml.util.DefaultNamespaceContext;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeCreationContext;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.util.FileUtil;

/**
 * This is the model for the XML Reader node.
 *
 * @author Heiko Hofer
 */
public class XMLReaderNodeModel extends NodeModel {
    private final XMLReaderNodeSettings m_settings;
    private LimitedXPathMatcher m_xpathExpr;

    /**
     * Creates a new model with no input port and one output port.
     */
    public XMLReaderNodeModel() {
        super(0, 1);
        m_settings = new XMLReaderNodeSettings();
    }

    /**
     * @param context
     *            the node creation context
     */
    XMLReaderNodeModel(final NodeCreationContext context) {
        this();
        m_settings.setFileURL(context.getUrl().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_settings.getFileURL() == null) {
            throw new InvalidSettingsException("No input file selected");
        }
        String loc = m_settings.getFileURL();
        if (loc == null || loc.length() == 0) {
            throw new InvalidSettingsException("No location provided");
        }

        if (loc.startsWith("file:/") || !loc.matches("^[a-zA-Z]+:/.*")) {
            File file = null;
            if (loc.startsWith("file:/")) {
                URL url;
                try {
                    url = new URL(loc);
                } catch (MalformedURLException ex) {
                    throw new InvalidSettingsException("Invalid URL: " + loc,
                            ex);
                }
                try {
                    // can handle file:///c:/Documents%20and%20Settings/...
                    file = new File(url.toURI());
                } catch (Exception e) {
                    // can handle file:///c:/Documents and Settings/...
                    file = new File(url.getPath());
                }
            } else {
                file = new File(loc);
            }

            if (!file.exists()) {
                throw new InvalidSettingsException("File '"
                        + file.getAbsolutePath() + "' does not exist");
            }
            if (!file.isFile()) {
                throw new InvalidSettingsException("'" + file.getAbsolutePath()
                        + "' is a directory");
            }
        }

        if (m_settings.getUseXPathFilter()) {
            if (m_settings.getUseRootsNS()
                    && (m_settings.getRootsNSPrefix() == null || m_settings
                            .getRootsNSPrefix().trim().isEmpty())) {
                throw new InvalidSettingsException(
                        "The prefix of root's default namespace is not set.");
            }
            m_xpathExpr = null;
            // Check for empty prefix
            String[] prefix = m_settings.getNsPrefixes();
            String[] namespaces = m_settings.getNamespaces();
            for (int i = 0; i < prefix.length; i++) {
                if (prefix[i].trim().isEmpty()) {
                    throw new InvalidSettingsException("An empty prefix for "
                            + "namespaces are not allowed in XPath. "
                            + "Please define a valid prefix.");
                }
            }
            // DefaultNamespaceContext does some sanity checking
            NamespaceContext nsContext = new DefaultNamespaceContext(prefix,
                    namespaces);
            try {
                // LimitedXPathMatcher DefaultNamespaceContext does some
                // sanity checking
                LimitedXPathMatcher xpathExpr = new LimitedXPathMatcher(
                        m_settings.getXpath(), nsContext);
                if (m_settings.getUseRootsNS()
                        && Arrays.binarySearch(m_settings.getNsPrefixes(),
                                m_settings.getRootsNSPrefix()) >= 0) {
                    throw new InvalidSettingsException(
                            "The namespace table uses the prefix "
                                    + "reserved for the " + "roots namespace.");
                } else {
                    m_xpathExpr = xpathExpr;
                }
            } catch (InvalidSettingsException e) {
                if (m_settings.getUseRootsNS()) {
                    // try to compile it with roots default prefix
                    createXPathExpr(null);
                    // the xpath compiles with the roots default prefix
                    m_xpathExpr = null;
                } else {
                    throw new InvalidSettingsException(
                            "XPath expression cannot be compiled.", e);
                }
            }
        }

        DataTableSpec spec = createOutSpec();
        return new DataTableSpec[] {spec};
    }

    private LimitedXPathMatcher createXPathExpr(final InputStream in)
            throws InvalidSettingsException {
        List<String> nsPrefixes = new ArrayList<String>();
        nsPrefixes.addAll(Arrays.asList(m_settings.getNsPrefixes()));
        if (nsPrefixes.contains(m_settings.getRootsNSPrefix())) {
            throw new InvalidSettingsException(
                    "The namespace table uses the prefix reserved for the "
                            + "roots namespace.");
        }
        nsPrefixes.add(m_settings.getRootsNSPrefix());
        List<String> namespaces = new ArrayList<String>();
        namespaces.addAll(Arrays.asList(m_settings.getNamespaces()));
        if (in == null) {
            // made up a unique namespace for the root, this is for sanity
            // checking of the xpath, only
            String nsTemplate = "roots_ns_";
            int counter = 0;
            String ns = nsTemplate + counter;
            while (namespaces.contains(ns)) {
                counter++;
                ns = nsTemplate + counter;
            }
            namespaces.add(ns);
        } else {
            // read the roots namespace from the input
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            factory.setProperty(
                    XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                    Boolean.TRUE);
            try {
                XMLStreamReader parser = factory.createXMLStreamReader(in);
                while (parser.hasNext()) {
                    int type = parser.getEventType();
                    if (type == XMLStreamConstants.START_ELEMENT) {
                        String rootNSUri = parser.getNamespaceURI();
                        if (rootNSUri != null) {
                            namespaces.add(rootNSUri);
                            break;
                        } else {
                            throw new InvalidSettingsException("The root node "
                                    + "does not have a namesapce URI.");
                        }
                    }
                    parser.next();
                }
            } catch (XMLStreamException e) {
                throw new InvalidSettingsException("The namespace of the root"
                        + "element cannot be read.");
            }
        }

        // DefaultNamespaceContext does some sanity checking
        NamespaceContext nsContext = new DefaultNamespaceContext(
                nsPrefixes.toArray(new String[nsPrefixes.size()]),
                namespaces.toArray(new String[namespaces.size()]));
        // LimitedXPathMatcher DefaultNamespaceContext does some
        // sanity checking
        try {
            return new LimitedXPathMatcher(m_settings.getXpath(), nsContext);
        } catch (InvalidSettingsException e) {
            throw new InvalidSettingsException("XPath query cannot be parsed.",
                    e);
        }

    }

    private DataTableSpec createOutSpec() {
        DataColumnSpecCreator colSpecCreator = new DataColumnSpecCreator("XML",
                XMLCell.TYPE);
        DataTableSpec spec = new DataTableSpec(colSpecCreator.createSpec());
        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        exec.checkCanceled();
        InputStream in = null;
        try {
            DataContainer cont = exec.createDataContainer(createOutSpec());
            int rowCount = 0;
            XMLCellReader reader = null;
            if (m_settings.getUseXPathFilter()) {
                LimitedXPathMatcher xpathMatcher = null;
                if (null != m_xpathExpr) {
                    xpathMatcher = m_xpathExpr;
                } else {
                    in = openInputStream();
                    xpathMatcher = createXPathExpr(in);
                    in.close();
                }
                if (xpathMatcher.rootMatches()) {
                    in = openInputStream();
                    reader = XMLCellReaderFactory.createXMLCellReader(in);
                    DataCell cell = XMLCellFactory.create(reader.readXML());
                    DataRow row = new DefaultRow(RowKey.createRowKey(rowCount),
                            cell);
                    cont.addRowToTable(row);
                    rowCount++;
                    in.close();
                }
                in = openInputStream();
                reader = XMLCellReaderFactory.createXPathXMLCellReader(in,
                        xpathMatcher);
                XMLValue value = reader.readXML();
                while (null != value) {
                    exec.checkCanceled();
                    DataCell cell = XMLCellFactory.create(value);
                    DataRow row = new DefaultRow(RowKey.createRowKey(rowCount),
                            cell);
                    cont.addRowToTable(row);
                    rowCount++;
                    value = reader.readXML();
                }
            } else {
                exec.checkCanceled();
                in = openInputStream();
                reader = XMLCellReaderFactory.createXMLCellReader(in);
                DataCell cell = XMLCellFactory.create(reader.readXML());
                DataRow row = new DefaultRow(RowKey.createRowKey(rowCount),
                        cell);
                cont.addRowToTable(row);
            }
            cont.close();
            DataTable table = cont.getTable();

            BufferedDataTable out = exec.createBufferedDataTable(table, exec);
            return new BufferedDataTable[] {out};
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }
    }

    private InputStream openInputStream() throws IOException,
            InvalidSettingsException {
        String loc = m_settings.getFileURL();
        if (loc == null || loc.length() == 0) {
            throw new InvalidSettingsException("No location provided");
        }
        InputStream in;
        if (loc.matches("^[a-zA-Z]+:/.*")) { // URL style
            URL url;
            try {
                url = new URL(loc);
            } catch (MalformedURLException ex) {
                throw new InvalidSettingsException("Invalid URL: " + loc, ex);
            }
            in = FileUtil.openStreamWithTimeout(url);
        } else {
            File file = new File(loc);
            if (!file.exists()) {
                throw new InvalidSettingsException("No such file: " + loc);
            }
            in = new FileInputStream(file);
        }
        BufferedInputStream bufferedIn = new BufferedInputStream(in);
        bufferedIn.mark(/*readLimit = */ 8 * 1024); // 8kb enough for the header
        try {
            GZIPInputStream gzipIn = new GZIPInputStream(bufferedIn);
            return new BufferedInputStream(gzipIn);
        } catch (IOException ioe) {
            // not gzipped, read uncompressed
            bufferedIn.reset();
            return bufferedIn;
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
        new XMLReaderNodeSettings().loadSettingsModel(settings);
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
