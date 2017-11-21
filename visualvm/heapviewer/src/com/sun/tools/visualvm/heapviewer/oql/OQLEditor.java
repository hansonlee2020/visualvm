/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.tools.visualvm.heapviewer.oql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.NoCaret;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.modules.profiler.oql.engine.api.OQLException;
import org.netbeans.modules.profiler.oql.spi.OQLEditorImpl;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Bachorik
 * @author Jiri Sedlacek
 */
public class OQLEditor extends JPanel {

    final public static String VALIDITY_PROPERTY = OQLEditorImpl.VALIDITY_PROPERTY;
    volatile private boolean lexervalid = false;
    volatile private boolean parserValid = false;
    volatile private boolean oldValidity = false;
    private JEditorPane queryEditor = null;
    final private OQLEngine engine;

    final private Color disabledBgColor = UIUtils.isGTKLookAndFeel() ?
                  UIManager.getLookAndFeel().getDefaults().getColor("desktop") : // NOI18N
                  UIManager.getColor("TextField.disabledBackground"); // NOI18N

    final private transient Caret nullCaret = new NoCaret();

    private Color lastBgColor = null;
    private Caret lastCaret = null;
    
    private Font font;

    public OQLEditor(OQLEngine engine) {
        this.engine = engine;
    }

    private void init() {
        final DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { validateScript(); }
            public void removeUpdate(DocumentEvent e)  { validateScript(); }
            public void changedUpdate(DocumentEvent e) { validateScript(); }
        };

        OQLEditorImpl impl = Lookup.getDefault().lookup(OQLEditorImpl.class);
        if (impl != null) {
            queryEditor = impl.getEditorPane();
            queryEditor.getDocument().putProperty(OQLEngine.class, engine);
            queryEditor.getDocument().putProperty(OQLEditorImpl.ValidationCallback.class, new OQLEditorImpl.ValidationCallback() {

                public void callback(boolean lexingResult) {
                    lexervalid = lexingResult;
                    validateScript();
                }
            });
            
            queryEditor.getCaret().addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    try {
                        Rectangle edit = queryEditor == null ? null :
                                         queryEditor.getUI().modelToView(
                                         queryEditor, queryEditor.getCaretPosition());
                        if (edit != null) queryEditor.scrollRectToVisible(edit);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            
            Element root = queryEditor.getDocument().getDefaultRootElement();
            String family = StyleConstants.getFontFamily(root.getAttributes());
            int size = StyleConstants.getFontSize(root.getAttributes());
            font = new Font(family, Font.PLAIN, size);
        } else {
            queryEditor = new JEditorPane() {
                public void setText(String text) {
                    Document doc = getDocument();
                    if (doc != null) doc.removeDocumentListener(listener);
                    setDocument(getEditorKit().createDefaultDocument());
                    doc = getDocument();
                    if (doc != null) doc.addDocumentListener(listener);
                    super.setText(text);
                }
            };
            
            // #262619 (workaround for JDK9 bug)
            try {
                queryEditor.setContentType("text/x-oql"); // NOI18N
            } catch (NullPointerException e) {}
            
            font = Font.decode("Monospaced"); // NOI18N
            queryEditor.setFont(font);
            lexervalid = true; // no lexer info available; assume the lexing info is valid
        }

        queryEditor.setOpaque(isOpaque());
        queryEditor.setBackground(getBackground());
        
        // Do not display NB TopComponent switcher, let the focus subsystem transfer the focus out of the editor
        queryEditor.putClientProperty("nb.ctrltab.popupswitcher.disable", Boolean.TRUE); // NOI18N

        setLayout(new BorderLayout());
        add(queryEditor, BorderLayout.CENTER);
    }

    public void setScript(String script) {
        getEditor().setText(script);
    }

    public String getScript() {
        return getEditor().getText();
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (queryEditor != null) queryEditor.setBackground(bg);
    }

    @Override
    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        if (queryEditor != null) queryEditor.setOpaque(isOpaque);
    }

    @Override
    public void requestFocus() {
        getEditor().requestFocus();
    }

    final private void validateScript() {
        if (lexervalid || !parserValid) {
            // only parse the query if there are no errors from lexer
            try {
                engine.parseQuery(getScript());
                parserValid = true;
            } catch (OQLException e) {
                StatusDisplayer.getDefault().setStatusText(e.getLocalizedMessage());
                parserValid = false;
            }
        }

        firePropertyChange(VALIDITY_PROPERTY, oldValidity, lexervalid && parserValid);
        oldValidity = lexervalid && parserValid;
    }

    public void setEditable(boolean b) {
        JEditorPane editor = getEditor();
        if (editor.isEditable() == b) return;
        
        editor.setEditable(b);

        if (b) {
            if (lastBgColor != null) editor.setBackground(lastBgColor);
            if (lastCaret != null) editor.setCaret(lastCaret);
        } else {
            lastBgColor = editor.getBackground();
            lastCaret = editor.getCaret();
            editor.setBackground(disabledBgColor);
            editor.setCaret(nullCaret);
        }
    }

    public boolean isEditable() {
        return getEditor().isEditable();
    }
    
    public Font getFont() {
        if (queryEditor == null) init();
        return font;
    }
    
    public JEditorPane getEditor() {
        if (queryEditor == null) init();
        return queryEditor;
    }
    
}