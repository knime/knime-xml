/*
 * ------------------------------------------------------------------------
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
 *   17.12.2010 (hofer): created
. */
package org.knime.xml.node.xpath2;

import static org.knime.node.impl.description.PortDescription.fixedPort;

import java.io.IOException;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import org.knime.node.impl.description.PortDescription;
import org.xml.sax.SAXException;

/**
 * This is the factory for the XPath node.
 *
 * @author Tim-Oliver Buchholz, KNIME AG, Zurich, Switzerland
 * @author Paul Baernreuther, KNIME GmbH, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
public class XPathNodeFactory extends NodeFactory<XPathNodeModel>
    implements NodeDialogFactory {

    /**
     * Feature flag for webUI configuration dialogs in local AP.
     */
    private static final boolean XPATH_WEBUI_DIALOG = "js".equals(System.getProperty("org.knime.xpath.webuidialog"));

    /**
     * {@inheritDoc}
     */
    @Override
    public XPathNodeModel createNodeModel() {
        return new XPathNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<XPathNodeModel> createNodeView(final int viewIndex, final XPathNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDialog() {
        return true;
    }

    @Override
    public boolean hasNodeDialog() {
        return XPATH_WEBUI_DIALOG;
    }

    private static final String NODE_NAME = "XPath";

    private static final String NODE_ICON = "./xpath.png";

    private static final String SHORT_DESCRIPTION = "Performs XPath queries on a XML column.";

    private static final String FULL_DESCRIPTION = """
            The node takes the XML Documents of the selected column and performs XPath queries on them. The node
                supports XPath 1.0. <h3>XPath Syntax</h3> <h4>Selection Nodes</h4> <table>
                <tr><th>Expression</th><th>Description</th></tr> <tr><td>nodename</td><td>Selects all nodes with the
                name "nodename"</td></tr> <tr><td>/</td><td>Selects from the root node.</td></tr>
                <tr><td>//</td><td>Selects nodes in the document from the current node that match the selection no
                matter where they are </td></tr> <tr><td>.</td><td>Selects the current node</td></tr>
                <tr><td>..</td><td>Selects the parent of the current node</td></tr> <tr><td>@</td><td>Selects
                attributes</td></tr> </table> <h4>Predicates</h4> <table> <tr><th>Path
                Expression</th><th>Result</th></tr> <tr><td>/bookstore/book[1]</td><td>Selects the first book element
                that is the child of the bookstore element.</td></tr> <tr><td>/bookstore/book[last()]</td><td>Selects
                the last book element that is the child of the bookstore element</td></tr>
                <tr><td>/bookstore/book[last()-1]</td><td>Selects the last but one book element that is the child of the
                bookstore element</td></tr> <tr><td>/bookstore/book[position()&lt;3]</td><td>Selects the first two book
                elements that are children of the bookstore element</td></tr> <tr><td>//title[@lang]</td><td>Selects all
                the title elements that have an attribute named lang</td></tr>
                <tr><td>//title[@lang='en']</td><td>Selects all the title elements that have a "lang" attribute with a
                value of "en"</td></tr> <tr><td>/bookstore/book[price&gt;35.00]</td><td>Selects all the book elements of
                the bookstore element that have a price element with a value greater than 35.00</td></tr>
                <tr><td>/bookstore/book[price&gt;35.00]/title</td><td>Selects all the title elements of the book
                elements of the bookstore element that have a price element with a value greater than 35.00</td></tr>
                </table> <p>Syntax description from <a
                href="https://www.w3schools.com/xml/xpath_syntax.asp">w3cschools.com</a>.</p> <p><b>Hint for
                streaming:</b> Node can only be executed in real streamed fashion if every single XPath query is
                configured as follow (see XPath Query Settings):<br /> The column names are fixed (i.e. select 'new
                column name') and either 'Single Cell', 'Collection Cell', or 'Multiple Rows' are selected as 'Multiple
                tag option'.</p>
            """;

    private static final List<PortDescription> INPUT_PORTS =
        List.of(fixedPort("Input Table", "Input table containing at least one XML column."));

    private static final List<PortDescription> OUTPUT_PORTS = List.of(fixedPort("Output Table",
        "The input table with additional columns containing the result of the XPath queries."));

    private static final List<String> KEYWORDS = List.of( //
        "xml parse" //
    );

    @Override
    public NodeDialogPane createNodeDialogPane() {
        if (XPATH_WEBUI_DIALOG) {
            return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
        }
        return new XPathNodeDialog();
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, XPathNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() throws SAXException, IOException, XmlException {
        if (XPATH_WEBUI_DIALOG) {
            return DefaultNodeDescriptionUtil.createNodeDescription( //
                NODE_NAME, //
                NODE_ICON, //
                INPUT_PORTS, //
                OUTPUT_PORTS, //
                SHORT_DESCRIPTION, //
                FULL_DESCRIPTION, //
                List.of(), //
                XPathNodeParameters.class, //
                null, //
                NodeType.Manipulator, //
                KEYWORDS, //
                null //
            );
        }
        return super.createNodeDescription();
    }

    // TODO: Implement KaiNodeInterfaceFactory
    //  @Override
    // public KaiNodeInterface createKaiNodeInterface() {
    //    return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, XPathNodeParameters.class));
    // }
}
