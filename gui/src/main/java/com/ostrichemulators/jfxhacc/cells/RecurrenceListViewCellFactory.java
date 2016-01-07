/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Recurrence;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 *
 * @author ryan
 */
public class RecurrenceListViewCellFactory
		implements Callback<ListView<Recurrence>, ListCell<Recurrence>> {

	@Override
	public ListCell<Recurrence> call( ListView<Recurrence> p ) {
		return new ListCell<Recurrence>() {

			@Override
			protected void updateItem( Recurrence item, boolean empty ) {
				super.updateItem( item, empty );
				if ( null == item || empty ) {
					textProperty().unbind();
					setText( null );
				}
				else {
					textProperty().bind( item.getNameProperty() );
				}
			}
		};
	}
}
