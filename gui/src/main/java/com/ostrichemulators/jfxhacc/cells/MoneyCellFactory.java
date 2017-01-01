/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.converter.MoneyStringConverter;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 * @param <T>
 */
public class MoneyCellFactory<T> implements Callback<TableColumn<T, Money>, TableCell<T, Money>> {

	public static final Logger log = Logger.getLogger( MoneyCellFactory.class );
	private final ChangeListener<Money> handler;

	public MoneyCellFactory() {
		this( null );
	}

	public MoneyCellFactory( ChangeListener<Money> changer ) {
		handler = changer;
	}

	@Override
	public TableCell<T, Money> call( TableColumn<T, Money> p ) {
		return new TextFieldTableCell<T, Money>( new MoneyStringConverter() ) {
			@Override
			public void updateItem( Money t, boolean empty ) {
				super.updateItem( t, empty );
				if ( null == t || empty || t.isZero() ){
					setText( null );
				}
				else {
					setText( t.toString() );
					this.setAlignment( Pos.TOP_RIGHT );
				}
			}

			@Override
			public void commitEdit( Money t ) {
				Split split = Split.class.cast( getTableRow().getItem() );
				Money old = split.getValue();

				if( split.isCredit() ){
					split.setCredit( t );
				}
				else{
					split.setDebit( t );
				}

				if ( null != handler ) {
					handler.changed( split.getValueProperty(), old, t );
				}

				super.cancelEdit();
			}
		};
	}
}
