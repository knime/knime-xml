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
 *   20.01.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2.CellFactories;

import java.util.Iterator;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.container.AbstractCellFactory;

/**
 * This factory inserts the values of a collection accordingly to the column names stored in another collection cell.
 * Values and names have to be in same order and every name has to appear in the new table spec.
 * @author Tim-Oliver Buchholz, KNIME.com AG, Zurich, Switzerland
 */
public class XMLSplitCollectionCellFactory extends AbstractCellFactory {

    private DataColumnSpec[] m_specs;
    private Map<String, Integer> m_reverseColNames;
    private int m_valueColIndex;
    private int m_nameColIndex;

    /**
     * @param specs table specs
     * @param reverseColNames map of column names to there spec position
     * @param valueColIndex index of the value column
     * @param nameColIndex index of the name column
     */
    public XMLSplitCollectionCellFactory(final DataColumnSpec[] specs, final Map<String, Integer> reverseColNames,
        final int valueColIndex, final int nameColIndex) {
        super(true, specs);
        m_specs = specs;
        m_reverseColNames = reverseColNames;
        m_valueColIndex = valueColIndex;
        m_nameColIndex = nameColIndex;
    }

    /**
     * @param specs table specs
     * @param reverseColNames map of column names to there spec postion
     * @param valueColIndex index of the value collection column
     * @param nameColIndex index of the name collection column
     * @return split collection cell factory
     */
    public static XMLSplitCollectionCellFactory create(final DataColumnSpec[] specs,
        final Map<String, Integer> reverseColNames, final int valueColIndex, final int nameColIndex) {
        return new XMLSplitCollectionCellFactory(specs, reverseColNames, valueColIndex, nameColIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        DataCell[] cells = new DataCell[m_specs.length];

        if (!row.getCell(m_valueColIndex).isMissing()) {
            CollectionDataValue valueCollection = (CollectionDataValue)row.getCell(m_valueColIndex);
            CollectionDataValue nameCollection = (CollectionDataValue)row.getCell(m_nameColIndex);

            Iterator<DataCell> valIt = valueCollection.iterator();
            Iterator<DataCell> nameIt = nameCollection.iterator();

            while (valIt.hasNext()) {
                DataCell value = valIt.next();
                DataCell name = nameIt.next();

                Integer index = m_reverseColNames.get(name.toString());
                cells[index] = value;
            }
        }
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) {
                cells[i] = DataType.getMissingCell();
            }
        }

        return cells;
    }
}
