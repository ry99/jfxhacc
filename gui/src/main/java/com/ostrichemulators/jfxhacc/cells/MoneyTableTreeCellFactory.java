/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Money;
import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 * @param <T>
 */
public class MoneyTableTreeCellFactory<T> implements Callback<TreeTableColumn<T, Money>, TreeTableCell<T, Money>> {

	public static final Logger log = Logger.getLogger( MoneyTableTreeCellFactory.class );

	@Override
	public TreeTableCell<T, Money> call( TreeTableColumn<T, Money> p ) {
		return new TreeTableCell<T, Money>() {
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
