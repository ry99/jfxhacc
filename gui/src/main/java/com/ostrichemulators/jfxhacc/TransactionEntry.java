/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.io.IOException;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
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
	private AccountMapper amap;
	private PayeeMapper pmap;

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
		payeefield.setEditable( true );
		accountfield.setEditable( false );

		try {
			DataEngine eng = MainApp.getEngine();
			pmap = eng.getPayeeMapper();
			amap = eng.getAccountMapper();
			for ( Payee p : pmap.getAll() ) {
				payeemap.put( p.getName(), p );
			}

			ObservableList<Account> accounts
					= FXCollections.observableArrayList( amap.getAll() );
			SortedList<Account> sorted = new SortedList<>( accounts );
			sorted.setComparator( new Comparator<Account>() {

				@Override
				public int compare( Account o1, Account o2 ) {
					return Collator.getInstance().compare( getFullName( o1 ), getFullName( o2 ) );
				}
			} );
			accountfield.setItems( sorted );
		}
		catch ( Exception x ) {
			log.warn( x, x );
		}

		payeefield.getEntries().addAll( payeemap.keySet() );

		accountfield.setCellFactory( new Callback<ListView<Account>, ListCell<Account>>() {

			@Override
			public ListCell<Account> call( ListView<Account> p ) {
				return new ListCell<Account>() {

					@Override
					protected void updateItem( Account t, boolean empty ) {
						super.updateItem( t, empty );
						if ( !( null == t || empty ) ) {
							setText( getFullName( t ) );
						}
					}
				};
			}
		} );

	}

	public void newTransaction( Date d, ReconcileState rs, boolean to ) {
		trans = null;
		newtrans = true;

		amountfield.clear();
		memofield.clear();
		numberfield.clear();

		tofromBtn.setText( to ? "To" : "From" );

		setDate( d );
		setReco( rs );
		payeefield.requestFocus();
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
					accountfield.getEditor().setText( a.getName() );
				}
			}
		}

		numberfield.setText( t.getNumber() );

		setDate( t.getDate() );

		tofromBtn.setText( mysplit.isDebit() ? "To" : "From" );
		payeefield.requestFocus();
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
		tofromBtn.setText( tofromBtn.getText().equals( "To" ) ? "From" : "To" );
	}

	@FXML
	protected void keytype( KeyEvent ke ) {
		log.debug( "key typed!" );
	}

	@FXML
	protected void keypress( KeyEvent ke ) {
		log.debug( "key typed!" );
	}

	private String getFullName( Account a ) {
		try {
			List<Account> parents = amap.getParents( a );
			StringBuilder sb = new StringBuilder();
			for ( Account parent : parents ) {
				sb.append( parent.getName() ).append( "::" );
			}

			return sb.toString();
		}
		catch ( MapperException me ) {
			log.warn( me, me );
		}

		return a.getName();
	}
}
