/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class RecoCellFactory<T> implements Callback<TableColumn<T, ReconcileState>, TableCell<T, ReconcileState>> {

	public static final Logger log = Logger.getLogger( RecoCellFactory.class );
	private final boolean useCheckBox;

	public RecoCellFactory( boolean allowEditing ) {
		useCheckBox = allowEditing;
	}

	public RecoCellFactory() {
		this( false );
	}

	@Override
	public TableCell<T, ReconcileState> call( TableColumn<T, ReconcileState> p ) {
		if ( useCheckBox ) {
			return new CheckoCell();
		}

		return new TableCell<T, ReconcileState>() {
			@Override
			protected void updateItem( ReconcileState reco, boolean empty ) {
				super.updateItem( reco, empty );

				if ( empty || null == reco ) {
					setText( null );
				}
				else {
					switch ( reco ) {
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

	private class CheckoCell extends TableCell<T, ReconcileState> implements ChangeListener<Boolean> {

		private final CheckBox checkbox = new CheckBox();

		public CheckoCell() {
			checkbox.setAllowIndeterminate( true );
			checkbox.selectedProperty().addListener( this );
			checkbox.indeterminateProperty().addListener( this );
		}

		@Override
		protected void updateItem( ReconcileState reco, boolean empty ) {
			super.updateItem( reco, empty );

			if ( empty || null == reco ) {
				setText( null );
			}
			else {
				if ( ReconcileState.CLEARED == reco ) {
					checkbox.setIndeterminate( true );
				}
				else {
					checkbox.setSelected( ReconcileState.RECONCILED == reco );
				}
				setGraphic( checkbox );
			}
		}

		@Override
		public void changed( ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1 ) {
			Split s = Split.class.cast( getTableRow().getItem() );

			if ( checkbox.isIndeterminate() ) {
				s.setReconciled( ReconcileState.CLEARED );
			}
			else {
				s.setReconciled( checkbox.isSelected()
						? ReconcileState.RECONCILED
						: ReconcileState.NOT_RECONCILED );
			}
		}
	}
}
