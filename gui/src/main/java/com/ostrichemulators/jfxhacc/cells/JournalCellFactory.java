/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Transaction;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class JournalCellFactory implements Callback<TableColumn<Transaction, Journal>, TableCell<Transaction, Journal>> {

	public static final Logger log = Logger.getLogger(JournalCellFactory.class );

	@Override
	public TableCell<Transaction, Journal> call( TableColumn<Transaction, Journal> p ) {
		return new TableCell<Transaction, Journal>() {
			@Override
			protected void updateItem( Journal t, boolean empty ) {
				super.updateItem( t, empty );
				if ( ( empty || null == t ) ) {
					setText( null );
				}
				else {
					setText( t.getName() );
				}
			}
		};
	}
}
