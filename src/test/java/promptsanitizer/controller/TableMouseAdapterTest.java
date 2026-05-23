/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package promptsanitizer.controller;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

class TableMouseAdapterTest {
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
    void shouldEditCellAtRowAndColumnWhenClickCount1AndSelectedRowNotLessThanZero() {
        JTable jTable = Mockito.mock(JTable.class);
        JButton jButton = Mockito.mock(JButton.class);
        java.util.List<JButton> list = new ArrayList<>();
        list.add(jButton);
        Point point = Mockito.mock(Point.class);
        TableMouseAdapter sut = new TableMouseAdapter(jTable, list);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);
        Mockito.when(mouseEvent.getPoint()).thenReturn(point);
        Mockito.when(mouseEvent.getClickCount()).thenReturn(1);
        Mockito.when(jTable.getSelectedRow()).thenReturn(3);
        Mockito.when(jTable.columnAtPoint(point)).thenReturn(5);
        Mockito.when(jTable.rowAtPoint(point)).thenReturn(3);

        sut.mouseClicked(mouseEvent);

        Mockito.verify(jTable).editCellAt(3, 5);
        Mockito.verify(jButton).setEnabled(false);
    }

    @Test
    void shouldNotEditCellAtRowAndColumnWhenRowNegative() {
        JTable jTable = Mockito.mock(JTable.class);
        Point point = Mockito.mock(Point.class);
        TableMouseAdapter sut = new TableMouseAdapter(jTable, null);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);
        Mockito.when(mouseEvent.getPoint()).thenReturn(point);
        Mockito.when(mouseEvent.getClickCount()).thenReturn(1);
        Mockito.when(jTable.getSelectedRow()).thenReturn(3);
        Mockito.when(jTable.columnAtPoint(point)).thenReturn(5);
        Mockito.when(jTable.rowAtPoint(point)).thenReturn(-3);

        sut.mouseClicked(mouseEvent);

        Mockito.verify(jTable, Mockito.never()).editCellAt(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void shouldNotEditCellAtRowAndColumnWhenColumnNegative() {
        JTable jTable = Mockito.mock(JTable.class);
        Point point = Mockito.mock(Point.class);
        TableMouseAdapter sut = new TableMouseAdapter(jTable, null);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);
        Mockito.when(mouseEvent.getPoint()).thenReturn(point);
        Mockito.when(mouseEvent.getClickCount()).thenReturn(1);
        Mockito.when(jTable.getSelectedRow()).thenReturn(3);
        Mockito.when(jTable.columnAtPoint(point)).thenReturn(-5);
        Mockito.when(jTable.rowAtPoint(point)).thenReturn(3);

        sut.mouseClicked(mouseEvent);

        Mockito.verify(jTable, Mockito.never()).editCellAt(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void shouldNotEditCellAtRowAndColumnWhenClickCountWrong() {
        JTable jTable = Mockito.mock(JTable.class);
        TableMouseAdapter sut = new TableMouseAdapter(jTable, null);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);
        Mockito.when(mouseEvent.getClickCount()).thenReturn(5);

        sut.mouseClicked(mouseEvent);

        Mockito.verify(jTable, Mockito.never()).editCellAt(Mockito.anyInt(), Mockito.anyInt());
    }
    @Test
    void shouldNotEditCellAtRowAndColumnWhenClickCount1ButSelectedRowLessThanZero() {
        JTable jTable = Mockito.mock(JTable.class);
        TableMouseAdapter sut = new TableMouseAdapter(jTable, null);
        MouseEvent mouseEvent = Mockito.mock(MouseEvent.class);
        Mockito.when(mouseEvent.getClickCount()).thenReturn(1);
        Mockito.when(jTable.getSelectedRow()).thenReturn(-3);

        sut.mouseClicked(mouseEvent);

        Mockito.verify(jTable, Mockito.never()).editCellAt(Mockito.anyInt(), Mockito.anyInt());
    }
}