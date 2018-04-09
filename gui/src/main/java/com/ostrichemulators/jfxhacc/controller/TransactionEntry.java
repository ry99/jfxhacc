/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.datamanager.AccountManager;
import com.ostrichemulators.jfxhacc.datamanager.PayeeManager;
import com.ostrichemulators.jfxhacc.datamanager.SplitStubManager;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
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

	public TransactionEntry( DataEngine eng, AccountManager mgr, PayeeManager pmgr,
      SplitStubManager stubs ) {
		setMinHeight( 0d );
		setPrefHeight( 150d );

		tec = new TransactionEntryController( eng, mgr, pmgr, stubs );
		swc = new SplitsWindowController( mgr );

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
			swc.setSplits( tec.getSplits() );

			gridpane.setVisible( false );
			splitspane.setVisible( true );
		} );

		swc.setOkButtonOnAction( event -> {
			tec.updateSplitData();
			gridpane.setVisible( true );
			splitspane.setVisible( false );
		} );
	}

	public void setAccount( Account acct ) {
		tec.setAccount( acct );
		swc.setAccount( acct );
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

	public void setTransaction( Transaction trans ) {
		log.debug( "settrans: " );
		log.debug( "  " + trans );
		for ( Split s : trans.getSplits() ) {
			log.debug( "    " + s );
		}

		tec.setTransaction( trans );
		swc.setSplits( trans.getSplits() );
		reset();
	}

	private void reset() {
		gridpane.setVisible( true );
		splitspane.setVisible( false );
	}

	public void close() {
	}
}
