/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class MoneyTableTreeCellFactory implements Callback<TreeTableColumn<Account, Money>, TreeTableCell<Account, Money>> {

	public static final Logger log = Logger.getLogger( MoneyTableTreeCellFactory.class );

	@Override
	public TreeTableCell<Account, Money> call( TreeTableColumn<Account, Money> p ) {
		return new TreeTableCell<Account, Money>() {
			@Override
			protected void updateItem( Money t, boolean empty ) {
				super.updateItem( t, empty );
				if ( ( null == t || empty ) ) {
					setText( null );
				}
				else {
					setText( t.toString() );
				}

				setAlignment( Pos.TOP_RIGHT );
			}
		};
	}
}
