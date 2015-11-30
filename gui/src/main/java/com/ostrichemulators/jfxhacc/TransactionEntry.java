/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.util.Date;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class TransactionEntry extends StackPane {

	public static final Logger log = Logger.getLogger( TransactionEntry.class );
	private final Pane gridpane;
	private final Pane splitspane;
	private final TransactionEntryController tec;
	private final SplitsWindowController swc;
	private Account acct;

	public TransactionEntry( DataEngine eng ) {
		setMinHeight( 0d );
		setPrefHeight( 150d );

		tec = new TransactionEntryController( eng );
		swc = new SplitsWindowController( eng );

		FXMLLoader deloader
				= new FXMLLoader( getClass().getResource( "/fxml/TransactionEntry.fxml" ) );
		deloader.setController( tec );

		FXMLLoader swloader
				= new FXMLLoader( getClass().getResource( "/fxml/SplitsWindow.fxml" ) );
		swloader.setController( swc );

		try {
			gridpane = deloader.load();
			splitspane = swloader.load();
		}
		catch ( IOException exception ) {
			throw new RuntimeException( exception );
		}

		getChildren().addAll( gridpane, splitspane );
		splitspane.setVisible( false );

		tec.setSplitsButtonOnAction( event -> {
			swc.updateSplitData( tec.getSelectedAccount(), tec.getSplitAmount(),
					tec.getMemo(), tec.getReco() );
			gridpane.setVisible( false );
			splitspane.setVisible( true );
		} );

		swc.setOkButtonOnAction( event -> {
			tec.updateSplitData( swc.getSplits() );
			gridpane.setVisible( true );
			splitspane.setVisible( false );
		} );
	}

	public void setAccount( Account acct, Journal jnl ) {
		tec.setAccount( acct, jnl );
		swc.setAccount( acct );
		this.acct = acct;
	}

	public void addCloseListener( TransactionEntryController.CloseListener cc ) {
		tec.addCloseListener( cc );
	}

	public void removeCloseListener( TransactionEntryController.CloseListener cc ) {
		tec.removeCloseListener( cc );
	}

	public void setTransaction( Date d, Split.ReconcileState rs, boolean to ) {
		tec.setTransaction( d, rs, to );
		swc.clear();
		reset();
	}

	public void setTransaction( Date d, Split s ) {
		tec.setTransaction( d, s );
		swc.clear();
		reset();
	}

	public void setTransaction( Transaction t ) {
		tec.setTransaction( t );
		swc.setSplits( t.getSplitsProperty() );
		reset();
	}

	private void reset() {
		gridpane.setVisible( true );
		splitspane.setVisible( false );
	}

	public void close() {

	}
}
