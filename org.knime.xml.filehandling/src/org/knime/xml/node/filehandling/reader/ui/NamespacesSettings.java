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
 *   2021-05-15 (Alexander Bondaletov): created
 */
package org.knime.xml.node.filehandling.reader.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;

/**
 * Backing settings for the namespace table.
 *
 * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
 */
public class NamespacesSettings {

    private final List<NamespaceEntry> m_namespaces;

    private final CopyOnWriteArrayList<ChangeListener> m_listeners;

    /**
     * Constructor.
     */
    public NamespacesSettings() {
        m_namespaces = new ArrayList<>();
        m_listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * @return the namespaces
     */
    public List<NamespaceEntry> getNamespaces() {
        return m_namespaces;
    }

    /**
     * Add new empty namespace entry.
     */
    public void addNewNamespace() {
        add(new NamespaceEntry("", ""));
    }

    private void add(final NamespaceEntry ns) {
        m_namespaces.add(ns);
        notifyChangeListeners();
    }

    /**
     * Removes the host entry.
     *
     * @param idx
     *            Index of the entry to remove
     */
    public void remove(final int idx) {
        m_namespaces.remove(idx);
        notifyChangeListeners();
    }

    /**
     * Removes all of the host entries.
     */
    public void removeAll() {
        m_namespaces.clear();
        notifyChangeListeners();
    }

    /**
     *
     * @return namespaces
     */
    public String[] getNamespacesArray() {
        return m_namespaces.stream().map(NamespaceEntry::getNamespace).toArray(String[]::new);
    }

    /**
     *
     * @return prefixes
     */
    public String[] getPrefixesArray() {
        return m_namespaces.stream().map(NamespaceEntry::getPrefix).toArray(String[]::new);
    }

    /**
     * Notifies all registered listeners about a new model content. Call this,
     * whenever the value in the model changes!
     */
    protected void notifyChangeListeners() {
        for (ChangeListener l : m_listeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * Validates consistency of the settings.
     *
     * @throws InvalidSettingsException
     */
    public void validate() throws InvalidSettingsException {
        for (NamespaceEntry h : m_namespaces) {
            h.validate();
        }
    }

    /**
     * @author Moditha Hewasinghage, KNIME GmbH, Berlin, Germany
     */
    class NamespaceEntry {
        private String m_prefix;
        private String m_namespace;

        /**
         * @param prefix
         * @param namespace
         */
        public NamespaceEntry(final String prefix, final String namespace) {
            m_prefix = prefix;
            m_namespace = namespace;
        }

        /**
         * @return the prefix
         */
        public String getPrefix() {
            return m_prefix;
        }

        /**
         * @param prefix
         *            the prefix to set
         */
        public void setPrefix(final String prefix) {
            m_prefix = prefix;
            notifyChangeListeners();
        }

        /**
         *
         * @return namespace
         */
        public String getNamespace() {
            return m_namespace;
        }

        /**
         *
         * @param namespace
         */
        public void setNamespace(final String namespace) {
            m_namespace = namespace;
            notifyChangeListeners();
        }

        /**
         * Validates the prefix field.
         *
         * @throws InvalidSettingsException
         */
        public void validatePrefix() throws InvalidSettingsException {
            if (m_prefix == null || m_prefix.isEmpty()) {
                throw new InvalidSettingsException("Prefix cannot be empty");
            }
        }

        /**
         * Validates the namespace field.
         *
         * @throws InvalidSettingsException
         */
        public void validateNamespace() throws InvalidSettingsException {
            if (m_prefix == null || StringUtils.isEmpty(m_prefix)) {
                throw new InvalidSettingsException("Namespace cannot be empty");
            }
        }

        /**
         * Validates the host and port fields.
         *
         * @throws InvalidSettingsException
         */
        public void validate() throws InvalidSettingsException {
            validatePrefix();
            validateNamespace();
        }

    }

    /**
     * Adds a new {@link ChangeListener}.
     *
     * @param l The listener to add.
     */
    public void addChangeListener(final ChangeListener l) {
        if (!m_listeners.contains(l)) {
            m_listeners.add(l);
        }
    }

    /**
     * Initializes the prefix/namespace mapping table.
     *
     * @param namespacePrefixes Array of prefixes.
     * @param namespaces Array of namespaces (corresponding to prefixes).
     */
    public void setTableData(final String[] namespacePrefixes, final String[] namespaces) {
        m_namespaces.clear();
        for (var i = 0; i < namespacePrefixes.length; i++) {
            add(new NamespaceEntry(namespacePrefixes[i], namespaces[i]));
        }
    }
}
