/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.TransactionViewController.PAMData;
import com.ostrichemulators.jfxhacc.model.Transaction;
import javafx.geometry.VPos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class PayeeAccountMemoCellFactory implements Callback<TableColumn<Transaction, PAMData>, TableCell<Transaction, PAMData>> {

	public static final Logger log = Logger.getLogger( PayeeAccountMemoCellFactory.class );
	private final boolean reconciler;

	public PayeeAccountMemoCellFactory( boolean forReconcileWindow ) {
		reconciler = forReconcileWindow;
	}

	@Override
	public TableCell<Transaction, PAMData> call( TableColumn<Transaction, PAMData> p ) {
		return new TableCell<Transaction, PAMData>() {
			@Override
			protected void updateItem( PAMData t, boolean empty ) {
				super.updateItem( t, empty );

				Pane pane = null;
				if ( empty || null == t ) {
					setText( null );
				}
				else {
					pane = new Pane();

					final double halfwidth = p.getWidth() / 2;
					final Font font = this.getFont();

					final Text payee = new Text( t.payee );
					payee.setFont( font );
					payee.setTextOrigin( VPos.TOP );
					payee.relocate( 0, 0 );

					final Text memo = new Text( t.memo );
					memo.setFont( font );
					memo.setTextOrigin( VPos.TOP );
					memo.setWrappingWidth( halfwidth );

					if ( reconciler ) {
						memo.relocate( halfwidth, 0 );
						pane.getChildren().addAll( payee, memo );
					}
					else {
						final double height = p.getTableView().getFixedCellSize() / 2;

						Font italics
								= Font.font( font.getName(), FontPosture.ITALIC, font.getSize() );
						final Text account = new Text( t.account );
						account.setFont( italics );
						account.setTextOrigin( VPos.TOP );
						account.setWrappingWidth( halfwidth );
						account.relocate( 0, height );

						memo.relocate( halfwidth, height );
						pane.getChildren().addAll( payee, account, memo );
					}
					setText( null );
					setGraphic( pane );
				}
			}
		};
	}
}
