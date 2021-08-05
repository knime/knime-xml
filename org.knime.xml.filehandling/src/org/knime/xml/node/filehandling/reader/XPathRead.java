/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   28 Jun 2021 Moditha Hewasinghage: created
 */
package org.knime.xml.node.filehandling.reader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataValue;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.data.xml.io.LimitedXPathMatcher;
import org.knime.core.data.xml.io.XMLCellReader;
import org.knime.core.data.xml.io.XMLCellReaderFactory;
import org.knime.core.data.xml.util.DefaultNamespaceContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.filehandling.core.connections.FSPath;
import org.knime.filehandling.core.node.table.reader.config.TableReadConfig;
import org.knime.filehandling.core.node.table.reader.randomaccess.RandomAccessible;
import org.knime.filehandling.core.util.CompressionAwareCountingInputStream;
import org.w3c.dom.Document;

/**
 *
 * Common lass for XML reading
 *
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 *
 */
final class XPathRead extends XMLRead {

    private final XMLCellReader m_reader;

    private final Path m_path;

    private final LimitedXPathMatcher m_xpathMatcher;

    private final boolean m_useRootNamespace;

    private final String[] m_namespacePrefixes;

    private final String[] m_namespaces;

    private final String m_rootNamespacePrefix;

    /**
     * Constructor.
     *
     * @param path the {@link Path} to the file
     * @param config the {@link TableReadConfig} of the node
     * @throws IOException
     * @throws InvalidSettingsException
     * @throws ParserConfigurationException
     * @throws XMLStreamException
     */
    XPathRead(final FSPath path, final TableReadConfig<XMLReaderConfig> config) throws IOException {
        super(path, config);

        m_useRootNamespace = m_xmlReaderConfig.useRootNamespace();
        m_namespacePrefixes = m_xmlReaderConfig.getNamespacePrefixes();
        m_namespaces = m_xmlReaderConfig.getNamespaces();
        m_rootNamespacePrefix = m_xmlReaderConfig.getRootNamespacePrefix();
        m_path = path;

        performSanityChecks();

        m_xpathMatcher = createXPathMatcher();

        try {
            m_reader = createXMLReader();
        } catch (ParserConfigurationException | XMLStreamException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Reads and returns the next row
     */
    @SuppressWarnings("resource")
    @Override
    public RandomAccessible<DataValue> next() throws IOException {
        m_linesRead++;

        final XMLValue<Document> value = m_reader.readXML();
        if (value != null) {
            return createRandomAccessible(XMLCellFactory.create(value.getDocumentSupplier().get()));
        } else {
            return null;
        }
    }

    /**
     * Creates a {@link CompressionAwareCountingInputStream}.
     *
     * @param path the {@link Path} to a file
     * @return a {@link CompressionAwareCountingInputStream}
     * @throws IOException
     */
    private CompressionAwareCountingInputStream createInputStream() throws IOException {
        return new CompressionAwareCountingInputStream(m_path);
    }

    private void performSanityChecks() throws IOException {
        checkForEmptyPrefixes();
        checkNamespacePrefixesLength();
        if (m_useRootNamespace) {
            checkForEmptyRootNamespace();
            checkForUniqueRootPrefix();
            validateXPathUsingRootPrefix();
        }
    }

    private void checkForUniqueRootPrefix() throws IOException {
        if (Arrays.binarySearch(m_namespacePrefixes, m_rootNamespacePrefix) >= 0) {
            throw new IOException("The namespace table uses the prefix reserved for the root element's namespace.");
        }
    }

    private void checkNamespacePrefixesLength() throws IOException {
        if (m_namespaces.length != m_namespacePrefixes.length) {
            throw new IOException("Each namespace prefix must have a corresponding namespace");
        }
    }

    private void checkForEmptyPrefixes() throws IOException {
        for (var i = 0; i < m_namespacePrefixes.length; i++) {
            if (StringUtils.isBlank(m_namespacePrefixes[i])) {
                throw new IOException(
                    "An empty prefix for namespaces is not allowed in XPath. Please define a valid prefix.");
            }
        }
    }

    private void checkForEmptyRootNamespace() throws IOException {
        if (m_rootNamespacePrefix == null || StringUtils.isBlank(m_rootNamespacePrefix)) {
            throw new IOException("The prefix of root element's default namespace is not set.");
        }
    }

    private LimitedXPathMatcher createXPathMatcher() throws IOException {
        final NamespaceContext nsContext;
        if (!m_useRootNamespace) {
            nsContext = new DefaultNamespaceContext(m_namespacePrefixes, m_namespaces);
        } else { // adding the root namespace into the context
            final List<String> nsPrefixes = new ArrayList<>(Arrays.asList(m_namespacePrefixes));
            final List<String> namespaces = new ArrayList<>(Arrays.asList(m_namespaces));
            nsPrefixes.add(m_rootNamespacePrefix);
            getRootNameSpace(namespaces);
            nsContext = new DefaultNamespaceContext(nsPrefixes.toArray(new String[nsPrefixes.size()]),
                namespaces.toArray(new String[namespaces.size()]));
        }
        try {
            return new LimitedXPathMatcher(m_xmlReaderConfig.getXPath(), nsContext);
        } catch (InvalidSettingsException e) {
            throw new IOException("XPath query cannot be parsed.", e);
        }

    }

    @SuppressWarnings("unused")
    private void validateXPathUsingRootPrefix() throws IOException {
        final List<String> nsPrefixes = new ArrayList<>(Arrays.asList(m_namespacePrefixes));
        final List<String> namespaces = new ArrayList<>(Arrays.asList(m_namespaces));

        nsPrefixes.add(m_rootNamespacePrefix);
        // made up a unique namespace for the root, this is for sanity
        // checking of the xpath, only
        final var nsTemplate = "roots_ns_";
        var counter = 0;
        String ns = nsTemplate + counter;
        while (namespaces.contains(ns)) {
            counter++;
            ns = nsTemplate + counter;
        }
        namespaces.add(ns);

        // DefaultNamespaceContext does some sanity checking
        final NamespaceContext nsContext = new DefaultNamespaceContext(
            nsPrefixes.toArray(new String[nsPrefixes.size()]), namespaces.toArray(new String[namespaces.size()]));
        // LimitedXPathMatcher DefaultNamespaceContext does some
        // sanity checking
        try {
            new LimitedXPathMatcher(m_xmlReaderConfig.getXPath(), nsContext);
        } catch (InvalidSettingsException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Picking the namespace of the root element by reading the file.
     *
     * @param namespaces
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    private void getRootNameSpace(final List<String> namespaces) throws FactoryConfigurationError, IOException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

        XMLStreamReader parser = null;
        try (final var inputStreamTmp = createInputStream()) {
            parser = factory.createXMLStreamReader(inputStreamTmp);
            extractRootNamespace(namespaces, parser);
        } catch (XMLStreamException e) {
            throw new IOException("The namespace of the root element cannot be read.", e);
        } finally {
            if (parser != null) {
                try {
                    parser.close();
                } catch (XMLStreamException e) {// NOSONAR
                    // Do nothing
                }
            }
        }
    }

    private static void extractRootNamespace(final List<String> namespaces, final XMLStreamReader parser)
        throws XMLStreamException, IOException {

        while (parser.hasNext()) {
            final int type = parser.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                final String rootNSUri = parser.getNamespaceURI();
                if (rootNSUri != null) {
                    namespaces.add(rootNSUri);
                    break;
                } else {
                    throw new IOException("The root node does not have a namespace URI.");
                }
            }
            parser.next();
        }
    }

    private XMLCellReader createXMLReader() throws ParserConfigurationException, XMLStreamException {
        if (m_xpathMatcher.rootMatches()) {
            return XMLCellReaderFactory.createXMLCellReader(m_compressionAwareStream);
        } else {
            return XMLCellReaderFactory.createXPathXMLCellReader2(m_compressionAwareStream, m_xpathMatcher);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        m_reader.close();
    }
}
