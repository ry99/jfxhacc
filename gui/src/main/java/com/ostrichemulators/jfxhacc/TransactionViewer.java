/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.CreditDebitValueFactory;
import com.ostrichemulators.jfxhacc.cells.DateCellFactory;
import com.ostrichemulators.jfxhacc.cells.MoneyCellFactory;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoCellFactory;
import com.ostrichemulators.jfxhacc.cells.PayeeAccountMemoValueFactory;
import com.ostrichemulators.jfxhacc.cells.RecoCellFactory;
import com.ostrichemulators.jfxhacc.cells.RecoValueFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionViewer extends AnchorPane implements ShutdownListener {

	private static final Logger log = Logger.getLogger( TransactionViewer.class );
	private static final String PREF_SPLITTER = "dataentry-splitter-location";

	@FXML
	private SplitPane splitter;
	@FXML
	private TableView<Transaction> transtable;
	@FXML
	private TableColumn<Transaction, Date> date;
	@FXML
	private TableColumn<Transaction, String> number;
	@FXML
	private TableColumn<Transaction, PAMData> payee;
	@FXML
	private TableColumn<Transaction, Money> credit;
	@FXML
	private TableColumn<Transaction, Money> debit;
	@FXML
	private TableColumn<Transaction, ReconcileState> reco;

	private TransactionEntry dataentry = new TransactionEntry();

	private Account account;
	private final PayeeAccountMemoValueFactory payeefac = new PayeeAccountMemoValueFactory();
	private final CreditDebitValueFactory creditfac = new CreditDebitValueFactory( true );
	private final CreditDebitValueFactory debitfac = new CreditDebitValueFactory( false );
	private final RecoValueFactory recofac = new RecoValueFactory();
	private boolean firstload = true;
	private double splitterpos;

	public TransactionViewer() {
		FXMLLoader fxmlLoader
				= new FXMLLoader( getClass().getResource( "/fxml/TransactionViewer.fxml" ) );
		fxmlLoader.setRoot( this );
		fxmlLoader.setController( this );

		try {
			fxmlLoader.load();
		}
		catch ( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

	public void setAccount( Account acct ) {
		account = acct;
		payeefac.setAccount( acct );
		creditfac.setAccount( acct );
		debitfac.setAccount( acct );
		recofac.setAccount( acct );
		dataentry.setAccount( acct );
		refresh();
	}

	public void refresh() {
		transtable.getItems().clear();

		DataEngine engine = MainApp.getEngine();
		try {
			List<Journal> journals = new ArrayList<>( engine.getJournalMapper().getAll() );
			log.debug( "fetching transactions for " + account );
			List<Transaction> trans = engine.getTransactionMapper().getAll( account,
					journals.get( 0 ) );
			log.debug( "populating transaction viewer with " + trans.size() + " transactions" );
			transtable.getItems().addAll( trans );

			transtable.sort();
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}

		if ( firstload ) {
			firstload = false;
			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			ObservableList<TableColumn<Transaction, ?>> cols = transtable.getColumns();
			int i = 0;
			for ( TableColumn<Transaction, ?> tc : cols ) {
				double size = prefs.getDouble( "col" + ( i++ ), -1 );
				if ( size > 0 ) {
					tc.setPrefWidth( size );
				}
			}
		}
	}

	@FXML
	public void initialize() {
		MainApp.getShutdownNotifier().addShutdownListener( this );
		setOnKeyPressed( ( event ) -> keyPressed( event ) );

		transtable.setFixedCellSize( 48 ); // FIXME

		date.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, Date> p )
				-> new ReadOnlyObjectWrapper<>( p.getValue().getDate() ) );
		date.setCellFactory( new DateCellFactory() );

		payee.setCellValueFactory( payeefac );
		payee.setCellFactory( new PayeeAccountMemoCellFactory() );

		number.setCellValueFactory( ( TableColumn.CellDataFeatures<Transaction, String> p )
				-> new ReadOnlyStringWrapper( p.getValue().getNumber() ) );

		reco.setCellValueFactory( recofac );
		reco.setCellFactory( new RecoCellFactory() );

		credit.setCellValueFactory( creditfac );
		credit.setCellFactory( new MoneyCellFactory() );

		debit.setCellValueFactory( debitfac );
		debit.setCellFactory( new MoneyCellFactory() );

		splitter.getItems().add( dataentry );
		dataentry.addSaveListener( ( event ) -> {
			splitterpos = splitter.getDividerPositions()[0];
			splitter.setDividerPositions( 1.0 );
		} );

		Preferences prefs = Preferences.userNodeForPackage( TransactionViewer.class );
		splitterpos = prefs.getDouble( PREF_SPLITTER, 0.70 );
		splitter.setDividerPositions( 1.0 );

		transtable.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<Transaction>() {

			@Override
			public void changed( ObservableValue<? extends Transaction> ov,
					Transaction oldval, Transaction newval ) {
				if ( null != newval ) {
					editTrans( newval );
				}
			}
		} );
	}

	public void editTrans( Transaction t ) {
		splitter.setDividerPositions( splitterpos );
		dataentry.requestFocus();
		dataentry.setTransaction( t );
	}

	public void newTrans( Date d, ReconcileState rs ) {
		splitter.setDividerPositions( splitterpos );
		dataentry.requestFocus();

		boolean to = true;
		dataentry.newTransaction( d, rs, to );
	}

	@Override
	public void shutdown() {
		Preferences prefs = Preferences.userNodeForPackage( getClass() );
		ObservableList<TableColumn<Transaction, ?>> cols = transtable.getColumns();
		int i = 0;
		for ( TableColumn<Transaction, ?> tc : cols ) {
			prefs.putDouble( "col" + ( i++ ), tc.getWidth() );
		}

		prefs.putDouble( PREF_SPLITTER, splitterpos );
	}

	@FXML
	public void keyPressed( KeyEvent ke ) {
		KeyCode code = ke.getCode();
		if ( KeyCode.ESCAPE == code ) {
			splitter.setDividerPositions( 1.0 );
			ke.consume();
		}
		else if ( KeyCode.I == code ) {
			Transaction t = transtable.getSelectionModel().getSelectedItem();
			Date tdate = ( null == t ? new Date() : t.getDate() );
			ReconcileState rs = ReconcileState.NOT_RECONCILED;
			boolean to = true;
			newTrans( tdate, rs );
		}
		else if ( KeyCode.R == code ) {

		}
		else if ( KeyCode.E == code ) {
			editTrans( transtable.getSelectionModel().getSelectedItem() );
		}
	}

	public static final class PAMData implements Comparable<PAMData> {

		public final String payee;
		public final String account;
		public final String memo;

		public PAMData( String payee, String account, String memo ) {
			this.payee = payee;
			this.account = account;
			this.memo = memo;
		}

		@Override
		public int compareTo( PAMData o ) {
			return toString().compareTo( o.toString() );
		}

		@Override
		public String toString() {
			return payee + account + memo;
		}
	}
}
