/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Money;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class MoneyCellFactory<T> implements Callback<TableColumn<T, Money>, TableCell<T, Money>> {

	public static final Logger log = Logger.getLogger( MoneyCellFactory.class );

	@Override
	public TableCell<T, Money> call( TableColumn<T, Money> p ) {
		return new TextFieldTableCell<T, Money>( new MoneyStringConverter() ) {
			@Override
			public void updateItem( Money t, boolean empty ) {
				super.updateItem( t, empty );
				if ( ( null == t || empty ) ) {
					setText( null );
				}
				else {
					setText( t.toString() );
					this.setAlignment( Pos.TOP_RIGHT );
				}
			}
		};
	}
}
