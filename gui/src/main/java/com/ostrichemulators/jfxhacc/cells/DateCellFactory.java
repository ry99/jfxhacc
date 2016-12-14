/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.SplitStub;
import java.text.DateFormat;
import java.util.Date;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class DateCellFactory implements Callback<TableColumn<SplitStub, Date>, TableCell<SplitStub, Date>> {

	public static final Logger log = Logger.getLogger( DateCellFactory.class );

	private final DateFormat SDF = DateFormat.getDateInstance( DateFormat.MEDIUM );

	@Override
	public TableCell<SplitStub, Date> call( TableColumn<SplitStub, Date> p ) {
		return new TableCell<SplitStub, Date>() {
			@Override
			protected void updateItem( Date t, boolean empty ) {
				super.updateItem( t, empty );
				if ( ( empty || null == t ) ) {
					setText( null );
				}
				else {
					setText( SDF.format( t ) );
				}
			}
		};
	}
}
