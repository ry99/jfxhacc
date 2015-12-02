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
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.utility.TransactionHelper;
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
	private TransactionImpl trans;
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
			trans.getSplit( acct ).setReconciled( tec.getReco() );
			Split other = TransactionHelper.getOther( trans, acct );
			if ( null != other ) {
				other.setValue( tec.getSplitAmount().opposite() );
				other.setAccount( tec.getSelectedAccount() );
			}

			gridpane.setVisible( false );
			splitspane.setVisible( true );
		} );

		swc.setOkButtonOnAction( event -> {
			tec.updateSplitData();
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

	public void setTransaction( Date d, Split s ) {
		Transaction t = new TransactionImpl( null, d, null, null );
		t.addSplit( s );
		setTransaction( t );
	}

	public void setTransaction( Transaction t ) {
		trans = new TransactionImpl( t.getId(), t.getDate(), t.getNumber(), t.getPayee() );

		for ( Split s : t.getSplits() ) {
			trans.addSplit( new SplitImpl( s ) );
		}

		tec.setTransaction( trans );
		swc.setSplits( trans.getSplitsProperty() );
		reset();
	}

	private void reset() {
		gridpane.setVisible( true );
		splitspane.setVisible( false );
	}

	public void close() {

	}
}
