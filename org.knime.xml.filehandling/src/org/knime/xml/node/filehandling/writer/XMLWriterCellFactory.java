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
 *   26 Jul 2021 (Laurin Siefermann, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.xml.node.filehandling.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.data.xml.io.XMLCellWriterFactory;
import org.knime.filehandling.core.defaultnodesettings.filechooser.writer.FileOverwritePolicy;
import org.knime.filehandling.core.node.table.writer.AbstractMultiTableWriterCellFactory;

/**
 * Factory of the XML Writer node (new file handling).
 *
 * @author Laurin Siefermann, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("rawtypes")
final class XMLWriterCellFactory extends AbstractMultiTableWriterCellFactory<XMLValue> {

    /**
     * Constructor.
     *
     * @param outputColumnsSpecs the spec's of the created columns
     * @param sourceColumnIndex index of source column
     * @param overwritePolicy policy how to proceed when output file exists according to {@link FileOverwritePolicy}
     */
    protected XMLWriterCellFactory(final DataColumnSpec[] outputColumnsSpecs, final int sourceColumnIndex,
        final FileOverwritePolicy overwritePolicy) {
        super(outputColumnsSpecs, sourceColumnIndex, overwritePolicy);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void writeFile(final OutputStream outputStream, final XMLValue value) throws IOException {
        try (var xmlCellWriter = XMLCellWriterFactory.createXMLCellWriter(outputStream)){
            xmlCellWriter.write(value);
        }
    }

    @Override
    protected String getOutputFileExtension(final XMLValue value) {
        return "xml";
    }
}
