/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Transaction;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class MoneyCellFactory implements Callback<TableColumn<Transaction, Money>, TableCell<Transaction, Money>> {

	public static final Logger log = Logger.getLogger( MoneyCellFactory.class );

	@Override
	public TableCell<Transaction, Money> call( TableColumn<Transaction, Money> p ) {
		return new TableCell<Transaction, Money>() {
			@Override
			protected void updateItem( Money t, boolean empty ) {
				super.updateItem( t, empty );
				if( !( null == t || empty ) ){
					setText( t.toString() );
				}

				setAlignment( Pos.TOP_RIGHT );
			}
		};
	}
}
