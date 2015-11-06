/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Transaction;
import java.text.DateFormat;
import java.util.Date;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class DateCellFactory implements Callback<TreeTableColumn<Transaction, Date>, TreeTableCell<Transaction, Date>> {

	public static final Logger log = Logger.getLogger( DateCellFactory.class );

	private final DateFormat SDF = DateFormat.getDateInstance( DateFormat.SHORT );

	@Override
	public TreeTableCell<Transaction, Date> call( TreeTableColumn<Transaction, Date> p ) {
		return new TreeTableCell<Transaction, Date>() {
			@Override
			protected void updateItem( Date t, boolean empty ) {
				super.updateItem( t, empty );
				if ( !( empty || null == t ) ) {
					setText( SDF.format( t ) );
				}
			}
		};
	}
}
