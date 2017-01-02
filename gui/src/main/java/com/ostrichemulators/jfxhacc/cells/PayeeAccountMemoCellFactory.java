/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.controller.TransactionViewController.PAMData;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PayeeAccountMemoCellFactory implements Callback<TableColumn<SplitStub, PAMData>, TableCell<SplitStub, PAMData>> {

	public static final Logger log = Logger.getLogger( PayeeAccountMemoCellFactory.class );
	private final boolean reconciler;

	public PayeeAccountMemoCellFactory( boolean forReconcileWindow ) {
		reconciler = forReconcileWindow;
	}

	@Override
	public TableCell<SplitStub, PAMData> call( TableColumn<SplitStub, PAMData> p ) {
		return new TableCell<SplitStub, PAMData>() {
			@Override
			protected void updateItem( PAMData t, boolean empty ) {
				super.updateItem( t, empty );

				if ( empty || null == t ) {
					setText( null );
					setGraphic( null );
				}
				else {
					GridPane pane = new GridPane();
					pane.setVgap( 5d );
					pane.setPrefWidth( p.getWidth() );
					pane.setMaxWidth( Double.MAX_VALUE );

					String memsafe = t.memo.getValueSafe();
					Label payee = new Label();// t.payee.getValueSafe() );
					Label memo = new Label();// memsafe );
					payee.textProperty().bind( t.payee );
					memo.textProperty().bind( t.memo );

					payee.setWrapText( false );
					memo.setWrapText( false );
					memo.prefWidthProperty().bind( p.widthProperty().divide( 2 ) );

					if ( reconciler ) {
						pane.add( payee, 0, 0 );
						pane.add( memo, 1, 0 );
						payee.prefWidthProperty().bind( p.widthProperty().divide( 2 ) );
					}
					else {
						Label acct = new Label();// t.account.getValueSafe() );
						acct.textProperty().bind( t.account );
						acct.setWrapText( false );

						pane.add( payee, 0, 0, 2, 1 );
						if ( memsafe.isEmpty() ) {
							pane.add( acct, 0, 1, 2, 1 );
						}
						else {
							pane.add( acct, 0, 1 );
							pane.add( memo, 1, 1 );
							acct.prefWidthProperty().bind( p.widthProperty().divide( 2 ) );
						}

						Font italics
								= Font.font( getFont().getName(), FontPosture.ITALIC, getFont().getSize() );
						acct.setFont( italics );
					}

					setText( null );
					setGraphic( pane );
				}
			}
		};
	}
}
