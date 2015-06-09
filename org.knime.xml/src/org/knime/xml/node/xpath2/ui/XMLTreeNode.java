/*
 * ------------------------------------------------------------------------
 *
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
 *   19.12.2014 (tibuch): created
 */
package org.knime.xml.node.xpath2.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Node for the XML hierarchy tree.
 *
 * @author Tim-Oliver Buchholz, KNIME.com AG, Zurich, Switzerland
 */
public class XMLTreeNode {

    /**
     * List of children.
     */
    private List<XMLTreeNode> m_children;

    /**
     * XML element tag.
     */
    private final String m_tag;

    /**
     * Path to this tag.
     */
    private final String m_path;

    /**
     * Namespace as prefix.
     */
    private final String m_prefix;

    /**
     * Parent node.
     */
    private final XMLTreeNode m_parent;

    /**
     * Linenumber of XML element in RichTextArea.
     */
    private int m_linenumber;

    /**
     * Attribute list.
     */
    private List<String> m_attributes;

    /**
     * Use this constructor for the root element.
     * @param prefix namespace prefix.
     * @param tag name
     */
    public XMLTreeNode(final String prefix, final String tag) {
        if (prefix.isEmpty()) {
            m_tag = tag;
        } else {
            m_tag = prefix + ":" + tag;
        }
        m_prefix = prefix;
        m_attributes = null;
        m_path = "";
        m_linenumber = -1;
        m_parent = null;
        m_children = new ArrayList<XMLTreeNode>();
    }

    /**
     * @param tag XML tag
     * @param attributes XML attributes
     * @param prefix namespace prefix
     * @param path path to this XML tag
     * @param linenumber of this tag in the preview
     * @param parent parent of this tag
     */
    public XMLTreeNode(final String tag, final List<String> attributes, final String prefix, final String path,
        final int linenumber, final XMLTreeNode parent) {
        m_tag = tag;
        m_attributes = attributes;
        if (prefix.isEmpty()) {
            m_path = path + "/" + tag;
        } else {
            m_path = path + "/" + prefix + ":" + tag;
        }
        m_prefix = prefix;
        m_linenumber = linenumber;
        m_children = new ArrayList<XMLTreeNode>();
        m_parent = parent;
    }

    /**
     * @param tag XML element tag
     * @return count of this tag in his namespace
     */
    public int getCountOf(final String tag) {
        int count = 0;
        for (int i = 0; i < m_children.size(); i++) {
            XMLTreeNode n = m_children.get(i);
            String pt;
            if (!n.getPrefix().isEmpty()) {
                pt = n.getPrefix() + ":" + n.getTag();
            } else {
                pt  = n.getTag();
            }
            if (pt.matches(tag + "(\\[[0-9]+\\])*")) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param child the node to add
     * @return added node
     */
    public XMLTreeNode add(final XMLTreeNode child) {
        m_children.add(child);
        return child;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_linenumber;
        result = prime * result + ((m_path == null) ? 0 : m_path.hashCode());
        result = prime * result + ((m_tag == null) ? 0 : m_tag.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        XMLTreeNode other = (XMLTreeNode)obj;
        if (m_children == null) {
            if (other.m_children != null) {
                return false;
            }
        } else if (!m_children.equals(other.m_children)) {
            return false;
        }
        if (m_linenumber != other.m_linenumber) {
            return false;
        }
        if (m_parent == null) {
            if (other.m_parent != null) {
                return false;
            }
        } else if (!m_parent.equals(other.m_parent)) {
            return false;
        }
        if (m_path == null) {
            if (other.m_path != null) {
                return false;
            }
        } else if (!m_path.equals(other.m_path)) {
            return false;
        }
        if (m_tag == null) {
            if (other.m_tag != null) {
                return false;
            }
        } else if (!m_tag.equals(other.m_tag)) {
            return false;
        }
        return true;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return m_tag;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return m_path;
    }

    /**
     * @return the parent
     */
    public XMLTreeNode getParent() {
        return m_parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_tag;
    }

    /**
     * @return the children
     */
    public List<XMLTreeNode> getChildren() {
        return m_children;
    }

    /**
     * @return the linenumber
     */
    public int getLinenumber() {
        return m_linenumber;
    }

    /**
     * Look up an attribute's XML qualified (prefixed) name by index.
     *
     * @param i The attribute index.
     * @return The XML qualified name or null if the index is out of range.
     */
    public String getAttributeName(final int i) {
        if (m_attributes.size() <= i) {
            return null;
        }
        return m_attributes.get(i);
    }

    /**
     * @return namespase prefix
     */
    public String getPrefix() {
        return m_prefix;
    }
}
