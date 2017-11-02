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
 *   20.01.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2.CellFactories;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;

/**
 * Abstract class which evaluates a XPhat node list.
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich, Switzerland
 * @param <T> type of input
 */
public abstract class NodeListReader<T> {
    private List<T> m_values;

    /**
     * Adds all xpath query results to the values list.
     * @param nodes node list
     */
    public NodeListReader(final NodeList nodes) {
        m_values = new ArrayList<T>();

        if (!(nodes.getLength() == 0)) {
            for (int i = 0; i < nodes.getLength(); i++) {
                String str = nodes.item(i).getTextContent();
                m_values.add(parse(str));

            }
        }
    }

    /**
     * @param string text content of a xpath node
     * @return parsed string of type T
     */
    public abstract T parse(String string);

    /**
     * @return list of all parsed values
     */
    public List<T> getValues() {
        return m_values;
    }
}
