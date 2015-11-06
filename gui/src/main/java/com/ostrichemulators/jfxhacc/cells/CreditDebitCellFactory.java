/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Transaction;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class CreditDebitCellFactory implements Callback<TreeTableColumn<Transaction, Money>, TreeTableCell<Transaction, Money>> {

	public static final Logger log = Logger.getLogger( CreditDebitCellFactory.class );

	@Override
	public TreeTableCell<Transaction, Money> call( TreeTableColumn<Transaction, Money> p ) {
		return new TreeTableCell<Transaction, Money>() {
			@Override
			protected void updateItem( Money t, boolean empty ) {
				super.updateItem( t, empty );
			}
		};
	}
}
