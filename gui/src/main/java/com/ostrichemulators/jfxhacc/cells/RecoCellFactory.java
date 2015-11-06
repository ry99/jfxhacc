/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class RecoCellFactory implements Callback<TreeTableColumn<Transaction, ReconcileState>, TreeTableCell<Transaction, ReconcileState>> {

	public static final Logger log = Logger.getLogger( RecoCellFactory.class );

	@Override
	public TreeTableCell<Transaction, ReconcileState> call( TreeTableColumn<Transaction, ReconcileState> p ) {
		return new TreeTableCell<Transaction, ReconcileState>() {
			@Override
			protected void updateItem( ReconcileState t, boolean empty ) {
				super.updateItem( t, empty );
				
				if ( !( empty || null == t ) ) {
					switch ( t ) {
						case RECONCILED:
							setText( "R" );
							break;
						case CLEARED:
							setText( "C" );
							break;
						default:
							setText( null );
					}
				}
			}
		};
	}
}
