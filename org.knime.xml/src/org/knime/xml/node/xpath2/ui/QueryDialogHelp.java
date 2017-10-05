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
 *   02.03.2015 (tibuch): created
 */
package org.knime.xml.node.xpath2.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLEditorKit;

import org.knime.core.node.NodeLogger;

/**
 *
 * @author Tim-Oliver Buchholz, KNIME.com AG, Zurich, Switzerland
 */
public class QueryDialogHelp extends JDialog {
    private Component m_parent;

    private QueryDialogHelp(final Window window) {

        super(window);

        m_parent = window;

        JEditorPane htmlLabel = new JEditorPane();
        htmlLabel.setEditable(false);
        htmlLabel.setContentType("text/html");

        // set some css
        HTMLEditorKit kit = new HTMLEditorKit();
        kit.getStyleSheet().addRule("body {font-family: Tahoma, Arial, Helvetica; font-size: 12pt; padding: 0.1em 0.1em 1.5em 0.1em;}");
        kit.getStyleSheet().addRule("h2{font-size: 140%;border-bottom: thin dotted #ffd600;}");
        kit.getStyleSheet().addRule("div#origin-bundle {color: #cccccc; font-size: 90%; margin-top: 2em; padding-top: 0.5em; border-top: 1px solid #bbbbbb;}");
        kit.getStyleSheet().addRule("dd { margin-left: 20px; } dt { display: block; float: left; margin-left:10pxm; margin-bottom: 2px; margin-top: 2px;font-weight: bold; ");
        kit.getStyleSheet().addRule("div.group {    border: 1px solid #ffd600;    padding: 5px;    margin-bottom: 2px;} div.groupname {    text-align: center;    font-weight: bold;    background-color: #EEEEEE;    padding: 0.3em;}");

        htmlLabel.setEditorKit(kit);

        try {
            htmlLabel.setPage(getClass().getResource("help.html"));
        } catch (IOException e) {
            NodeLogger.getLogger(getClass()).coding("help.html is invalid: " + e.getMessage(), e);
        }

        JScrollPane p = new JScrollPane(htmlLabel);
        p.setBackground(Color.white);
        setPreferredSize(new Dimension(300, m_parent.getHeight()));
        add(p);
    }

    /**
     * @param window parent of this dialog
     * @return the new helper dialog
     */
    public static JDialog openUserDialog(final Window window) {
        QueryDialogHelp queryDlg = new QueryDialogHelp(window);
        queryDlg.showDialog();
        return queryDlg;
    }

    /**
     * Show the helper dialog.
     */
    private void showDialog() {
        setTitle("XPath Query Settings");
        pack();
        centerDialog();

        setVisible(true);
    }

    /**
     * Sets this dialog in the center of the screen observing the current screen size.
     */
    private void centerDialog() {
        setLocation((int)m_parent.getLocationOnScreen().getX() + m_parent.getWidth(), (int)m_parent.getLocationOnScreen().getY());
    }

}
