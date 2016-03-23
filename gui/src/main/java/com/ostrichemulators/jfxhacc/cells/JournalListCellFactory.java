/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Journal;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class JournalListCellFactory implements Callback<ListView<Journal>, ListCell<Journal>> {

	public static final Logger log = Logger.getLogger( JournalListCellFactory.class );

	@Override
	public ListCell<Journal> call( ListView<Journal> p ) {
		ListCell<Journal> cell = new ListCell<Journal>() {
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

		return cell;
	}
}
