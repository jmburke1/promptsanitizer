/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class ScrollPaneMouseAdapterTest {
    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = Mockito.mockitoSession()
                .strictness(Strictness.STRICT_STUBS)
                .startMocking();
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    void shouldStopCellEditingWhenTableIsEditingAndMousePressed() {
        JTable table = Mockito.mock(JTable.class);
        TableCellEditor cellEditor = Mockito.mock(TableCellEditor.class);
        JButton button1 = Mockito.mock(JButton.class);
        JButton button2 = Mockito.mock(JButton.class);
        List<JButton> buttons = new ArrayList<>();
        buttons.add(button1);
        buttons.add(button2);
        Mockito.when(table.isEditing()).thenReturn(true);
        Mockito.when(table.getCellEditor()).thenReturn(cellEditor);
        ScrollPaneMouseAdapter sut = new ScrollPaneMouseAdapter(table, buttons);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);

        sut.mousePressed(mouseEvent);

        Mockito.verify(table).getCellEditor();
        Mockito.verify(cellEditor).stopCellEditing();
        Mockito.verify(button1).setEnabled(true);
        Mockito.verify(button2).setEnabled(true);
    }

    @Test
    void shouldNotStopCellEditingWhenTableNotEditing() {
        JTable table = Mockito.mock(JTable.class);
        JButton button1 = Mockito.mock(JButton.class);
        JButton button2 = Mockito.mock(JButton.class);
        List<JButton> buttons = new ArrayList<>();
        buttons.add(button1);
        buttons.add(button2);
        Mockito.when(table.isEditing()).thenReturn(false);
        ScrollPaneMouseAdapter sut = new ScrollPaneMouseAdapter(table, buttons);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);

        sut.mousePressed(mouseEvent);

        Mockito.verify(table, Mockito.never()).getCellEditor();
        Mockito.verify(button1).setEnabled(true);
        Mockito.verify(button2).setEnabled(true);
    }
}
