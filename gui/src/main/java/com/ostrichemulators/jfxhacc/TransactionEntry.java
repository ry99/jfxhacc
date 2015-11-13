/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionEntry extends AnchorPane {

	private static final Logger log = Logger.getLogger( TransactionEntry.class );
	@FXML
	private Label accountLabel;
	@FXML
	private Button splitBtn;
	@FXML
	private Button saveBtn;
	@FXML
	private Button tofromBtn;
	@FXML
	private ComboBox<Account> accountfield;
	@FXML
	private AutoCompleteTextField payeefield;
	@FXML
	private CheckBox recofield;
	@FXML
	private DatePicker datefield;
	@FXML
	private TextField amountfield;
	@FXML
	private TextField memofield;
	@FXML
	private TextField numberfield;

	private Transaction trans;
	private Account account;
	private boolean newtrans;

	private Map<String, Payee> payeemap = new HashMap<>();

	public TransactionEntry() {
		FXMLLoader fxmlLoader
				= new FXMLLoader( getClass().getResource( "/fxml/TransactionEntry.fxml" ) );
		fxmlLoader.setRoot( this );
		fxmlLoader.setController( this );

		try {
			fxmlLoader.load();
		}
		catch ( IOException exception ) {
			throw new RuntimeException( exception );
		}
	}

	public void setAccount( Account a ) {
		account = a;
	}

	public void addSaveListener( EventHandler<ActionEvent> cl ) {
		saveBtn.addEventHandler( ActionEvent.ACTION, cl );
	}

	public void save() {

	}

	/**
	 * Initializes the controller class.
	 */
	@FXML
	public void initialize() {
		log.debug( "dataentry init" );
		payeefield.setEditable( true );
		accountfield.setEditable( false );

		try {
			for ( Payee p : MainApp.getEngine().getPayeeMapper().getAll() ) {
				payeemap.put( p.getName(), p );
			}

			accountfield.getItems().addAll( MainApp.getEngine().getAccountMapper().getAll() );
		}
		catch ( Exception x ) {
			log.warn( x, x );
		}

		payeefield.getEntries().addAll( payeemap.keySet() );
	}

	public void newTransaction( Date d, ReconcileState rs, boolean to ) {
		trans = null;
		newtrans = true;

		amountfield.clear();
		memofield.clear();
		numberfield.clear();

		tofromBtn.setText( to ? "To Account" : "From Account" );

		setDate( d );
		setReco( rs );
	}

	public void setTransaction( Transaction t ) {
		trans = t;
		newtrans = false;

		Map<Account, Split> splits = t.getSplits();
		Split mysplit = splits.get( account );
		memofield.setText( mysplit.getMemo() );
		amountfield.setText( mysplit.getValue().toString() );

		setReco( mysplit.getReconciled() );

		payeefield.setText( t.getPayee().getName() );

		if ( splits.size() > 2 ) {
			// do something here, eh?
		}
		else {
			for ( Account a : splits.keySet() ) {
				if ( !a.equals( account ) ) {
					accountfield.getSelectionModel().select( a );
				}
			}
		}

		numberfield.setText( t.getNumber() );

		setDate( t.getDate() );

		tofromBtn.setText( mysplit.isDebit() ? "To Account" : "From Account" );
	}

	protected final void setDate( Date t ) {
		Instant instant = t.toInstant();
		LocalDate ld = instant.atZone( ZoneId.systemDefault() ).toLocalDate();
		datefield.setValue( ld );
	}

	protected final Date getDate() {
		LocalDate localDate = datefield.getValue();
		Instant instant = Instant.from( localDate.atStartOfDay( ZoneId.systemDefault() ) );
		return Date.from( instant );
	}

	protected final void setReco( ReconcileState rs ) {
		if ( ReconcileState.CLEARED == rs ) {
			recofield.setIndeterminate( true );
		}
		else {
			recofield.setSelected( ReconcileState.RECONCILED == rs );
		}
	}

	protected final ReconcileState getReco() {
		if ( recofield.isIndeterminate() ) {
			return ReconcileState.CLEARED;
		}

		return ( recofield.isSelected()
				? ReconcileState.RECONCILED
				: ReconcileState.NOT_RECONCILED );
	}

	@FXML
	protected void switchToFrom() {
		tofromBtn.setText( tofromBtn.getText().equals( "To Account" )
				? "From Account"
				: "To Account" );

	}
}
