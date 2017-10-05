/*
 * ------------------------------------------------------------------------
 *
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
 *   19.12.2014 (tibuch): created
 */
package org.knime.xml.node.xpath2.ui;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class uses the SAXParser to create a XML hierarchy tree.
 *
 * @author Tim-Oliver Buchholz, KNIME.com AG Zurich
 */
public class SaxHandler extends DefaultHandler {

    /**
     * Current node in the XML hierarchy.
     */
    private XMLTreeNode m_currentNode;

    /**
     * The document locator {@link Locator}.
     */
    private Locator m_locator;

    /**
     * Namespace keys.
     */
    private List<String> m_keys;

    /**
     * Namespace URIs.
     */
    private List<String> m_values;

    /**
     * Indicate if namespace has changed.
     */
    private boolean m_namespaceHasChanged = false;

    /**
     * @param root XML hierarchy tree root
     * @param collectNS collect namespace information
     */
    public SaxHandler(final XMLTreeNode root, final boolean collectNS) {
        m_currentNode = root;
        m_keys = new ArrayList<String>();
        m_values = new ArrayList<String>();
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        m_locator = locator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
        throws SAXException {
        String localQNmae = qName;
        List<String> attr = new ArrayList<String>();

        for (int i = 0; i < attributes.getLength(); i++) {
            attr.add(attributes.getQName(i));
        }

        String ns = "";
        if (!uri.isEmpty()) {
            if (m_values.contains(uri)) {
                ns = m_keys.get(m_values.indexOf(uri));
            } else {
                int i = 0;
                ns = "dns";
                while (m_keys.contains(ns)) {
                    ns = "dns" + i;
                    i++;
                }
            }
            if (localName.equals(localQNmae)) {
                //ns = "dns"
                localQNmae = ns + ":" + localQNmae;
            } else {
                ns = localQNmae.substring(0, localQNmae.indexOf(':'));
            }
            if (!m_keys.contains(ns)) {
                m_keys.add(ns);
                m_values.add(uri);
            }
        }

        int m = m_currentNode.getCountOf(localQNmae);
        String p = localName;
        if (m > 0) {
            m++;
            p += "[" + m + "]";
        }

        XMLTreeNode newChild =
            new XMLTreeNode(p, attr, ns, m_currentNode.getPath(), m_locator.getLineNumber(), m_currentNode);
        m_currentNode = m_currentNode.add(newChild);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        m_currentNode = m_currentNode.getParent();
    }

    /**
     * Get the namespace prefixes.
     * @return namespace prefixes
     */
    public String[] getKeys() {
        return m_keys.toArray(new String[m_keys.size()]);
    }

    /**
     * Get the namespace URIs.
     * @return namespace uris
     */
    public String[] getValues() {
        return m_values.toArray(new String[m_values.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fatalError(final SAXParseException arg0) throws SAXException {
        if (arg0.getMessage().equals("Premature end of file.")
            || arg0.getMessage().equals("XML document structures must start and end within the same entity.")) {
            // thrown if the preview string is not a complete xml file
            throw new StopParsingException();
        }
        super.fatalError(arg0);
    }

    /**
     * @return namespace has changed
     */
    public boolean getNamespaceHasChanged() {
        return m_namespaceHasChanged;
    }
}
