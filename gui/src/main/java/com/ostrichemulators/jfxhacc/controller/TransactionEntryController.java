/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.AutoCompletePopupHandler;
import com.ostrichemulators.jfxhacc.cells.AccountListCell;
import com.ostrichemulators.jfxhacc.converter.MoneyStringConverter;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.model.impl.TransactionImpl;
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import com.ostrichemulators.jfxhacc.utility.TransactionHelper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class TransactionEntryController extends AnchorPane {

	private static final Logger log = Logger.getLogger( TransactionEntryController.class );
	private static final String JNL_SELECTED = "journal.selected";
	@FXML
	private Button tofromBtn;
	@FXML
	private ComboBox<Account> accountfield;
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
	@FXML
	private TextField payeefield;
	@FXML
	private ChoiceBox<Journal> journalchsr;
	@FXML
	private Button splitsBtn;

	private TransactionImpl trans;
	private Account account;
	private Journal defaultjournal;
	private boolean newtrans;
	private AccountMapper amap;
	private PayeeMapper pmap;
	private TransactionMapper tmap;
	private final List<CloseListener> listenees = new ArrayList<>();

	private final Map<String, Payee> payeemap = new HashMap<>();
	private final List<Account> allaccounts = new ArrayList<>();
	//private final ObservableList<Account> accounts
	//		= FXCollections.observableArrayList();
	private final DataEngine engine;

	public TransactionEntryController( DataEngine en ) {
		engine = en;
	}

	public void addCloseListener( CloseListener cc ) {
		listenees.add( cc );
	}

	public void removeCloseListener( CloseListener cc ) {
		listenees.remove( cc );
	}

	public void setAccount( Account a ) {
		account = a;

		try {
			GuiUtils.makeAccountCombo( accountfield, amap.getAll(), amap );
		}
		catch ( MapperException me ) {
			log.error( me, me );
			// FIXME: tell the user
		}
	}

	@FXML
	public void save() {
		log.debug( "saving" );
		try {
			Payee payee = pmap.createOrGet( payeefield.getText() );
			Date tdate = getDate();
			ReconcileState myreco = getReco();
			Money mymoney = getSplitAmount();

			Split mysplit = trans.getSplit( account );
			mysplit.setReconciled( myreco );
			mysplit.setValue( mymoney );

			Split other = TransactionHelper.getOther( trans, account );
			if ( null != other ) {
				other.setValue( mymoney.opposite() );
				other.setAccount( getSelectedAccount() );
				if ( null == other.getMemo() || other.getMemo().isEmpty() ) {
					other.setMemo( memofield.getText() );
				}
			}

			defaultjournal = journalchsr.getValue();
			trans.setDate( tdate );
			trans.setPayee( payee );
			trans.setJournal( defaultjournal );

			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			prefs.put( JNL_SELECTED, defaultjournal.getId().stringValue() );

			if ( newtrans ) {
				tmap.create( trans );
				for ( CloseListener c : listenees ) {
					c.added( trans );
				}
				log.debug( "created new transaction" );
			}
			else {
				tmap.update( trans );
				for ( CloseListener c : listenees ) {
					c.updated( trans );
				}
				log.debug( "updated old transaction" );
			}
		}
		catch ( MapperException me ) {
			log.error( me, me );
			// FIXME: alert the user!
		}
	}

	public Money getSplitAmount() {
		String moneystr = amountfield.getText();
		if ( null == moneystr || moneystr.isEmpty() ) {
			return new Money();
		}

		boolean to = ( "To".equals( tofromBtn.getText() ) );
		Money money = Money.valueOf( moneystr );

		if ( to ) {
			money = money.opposite();
		}

		return money;
	}

	public String getMemo() {
		return memofield.getText();
	}

	/**
	 * Initializes the controller class.
	 */
	@FXML
	public void initialize() {
		payeefield.setEditable( true );
		payeefield.textProperty().addListener( new ChangeListener<String>() {

			@Override
			public void changed( ObservableValue<? extends String> ov, String t, String t1 ) {
				if ( newtrans && payeemap.containsKey( t1 ) ) {
					try {
						List<Account> populars
								= amap.getPopularAccounts( payeemap.get( t1 ), account );
						if ( !populars.isEmpty() ) {
							Account acct = populars.get( 0 );
							accountfield.getSelectionModel().select( acct );
							accountfield.setValue( acct );
							amountfield.requestFocus();
							amountfield.selectAll();
						}

					}
					catch ( MapperException me ) {
						log.error( me, me );
					}
				}
			}
		} );

		accountfield.setEditable( false );
		recofield.setAllowIndeterminate( true );

		try {
			pmap = engine.getPayeeMapper();
			amap = engine.getAccountMapper();
			allaccounts.clear();
			allaccounts.addAll( amap.getAll() );
			tmap = engine.getTransactionMapper();

			journalchsr.setItems( engine.getJournalMapper().getObservable() );

			Preferences prefs = Preferences.userNodeForPackage( getClass() );
			String jid = prefs.get( JNL_SELECTED, "" );
			if ( jid.isEmpty() ) {
				defaultjournal = journalchsr.getItems().get( 0 );
			}
			else {
				for ( Journal j : journalchsr.getItems() ) {
					if ( j.getId().stringValue().equals( jid ) ) {
						defaultjournal = j;
					}
				}
			}

			journalchsr.setValue( defaultjournal );

			for ( Payee p : pmap.getAll() ) {
				payeemap.put( p.getName(), p );
			}
		}
		catch ( Exception x ) {
			log.warn( x, x );
		}

		AutoCompletePopupHandler autocomplete = new AutoCompletePopupHandler( payeefield,
				payeemap.keySet() );
		accountfield.setButtonCell( makeAccountCell() );
		accountfield.setCellFactory( new Callback<ListView<Account>, ListCell<Account>>() {

			@Override
			public ListCell<Account> call( ListView<Account> p ) {
				return makeAccountCell();
			}
		} );
	}

	public void setTransaction( TransactionImpl t ) {
		clear();
		trans = t;
		newtrans = ( null == t.getId() );

		Split mysplit = t.getSplit( account );
		memofield.setText( mysplit.getMemo() );
		amountfield.textProperty().bindBidirectional( mysplit.getValueProperty(),
				new MoneyStringConverter() );

		memofield.textProperty().bindBidirectional( mysplit.getMemoProperty() );

		journalchsr.setItems( engine.getJournalMapper().getObservable() );
		journalchsr.setValue( null == t.getJournal() ? defaultjournal
				: t.getJournal() );

		if ( null != t.getPayee() ) {
			payeefield.setText( t.getPayee().getName() );
		}

		if ( 1 == trans.getSplits().size() ) {
			trans.addSplit( new SplitImpl( accountfield.getItems().get( 0 ),
					getSplitAmount().opposite(), mysplit.getMemo(),
					ReconcileState.NOT_RECONCILED ) );
		}

		updateSplitData();

		numberfield.textProperty().bindBidirectional( t.getNumberProperty() );

		setDate( t.getDate() );

		tofromBtn.setText( mysplit.isDebit() ? "From" : "To" );

		payeefield.requestFocus();
	}

	protected void clear() {
		memofield.clear();
		payeefield.clear();
		amountfield.clear();
		numberfield.clear();
		recofield.setSelected( false );
		recofield.setIndeterminate( false );
		datefield.setValue( LocalDate.now() );
		accountfield.setValue( accountfield.getItems().get( 0 ) );
		journalchsr.setItems( FXCollections.emptyObservableList() );

		memofield.textProperty().unbind();
		amountfield.textProperty().unbind();
		numberfield.textProperty().unbind();
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

	public void setSplitsButtonOnAction( EventHandler<ActionEvent> ae ) {
		splitsBtn.setOnAction( ae );
	}

	public final ReconcileState getReco() {
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
		for ( Split s : trans.getSplits() ) {
			s.setValue( s.getRawValueProperty().getValue().opposite() );
		}
	}

	public Set<Split> getSplits() {
		Set<Split> set = new HashSet<>();
		if ( accountfield.isDisabled() ) {
			set.addAll( trans.getSplits() );
		}
		else {
			Split mysplit = trans.getSplit( account );
			mysplit.setReconciled( getReco() );
			Split other = TransactionHelper.getOther( trans, account );

			mysplit.setValue( getSplitAmount() );
			other.setValue( mysplit.getRawValueProperty().getValue().opposite() );

			set.add( mysplit );
			set.add( other );
		}

		return set;
	}

	@FXML
	protected void keytype( KeyEvent ke ) {
	}

	@FXML
	protected void keyrelease( KeyEvent ke ) {
		KeyCode code = ke.getCode();
		if ( KeyCode.ENTER == code ) {
			ke.consume();
			save();
		}
	}

	@FXML
	protected void keypress( KeyEvent ke ) {
		KeyCode code = ke.getCode();
		if ( KeyCode.ESCAPE == code ) {
			log.debug( "esc pressed!" );
			ke.consume();
			for ( CloseListener cl : listenees ) {
				cl.closed();
			}
		}
		else if ( KeyCode.UP == code ) {
			LocalDate ld = datefield.getValue();
			ld = ld.plusDays( 1 );
			datefield.setValue( ld );
			ke.consume();
		}
		else if ( KeyCode.DOWN == code ) {
			LocalDate ld = datefield.getValue();
			ld = ld.minusDays( 1 );
			datefield.setValue( ld );
			ke.consume();
		}
	}

	private ListCell<Account> makeAccountCell() {
		return new AccountListCell( amap, true );
	}

	@FXML
	public void openSplits() {
		setVisible( false );
	}

	public Account getSelectedAccount() {
		return accountfield.getValue();
	}

	public void updateSplitData() {
		Set<Split> splits = trans.getSplits();
		log.debug( "update split data (" + splits.size() + " splits)" );
		Split mysplit = trans.getSplit( account );

		Split other = TransactionHelper.getOther( trans, account );
		setReco( mysplit.getReconciled() );

		if ( newtrans ) {
			accountfield.getSelectionModel().clearSelection();
			amountfield.setEditable( true );

			accountfield.setDisable( false );
			amountfield.setDisable( false );
			tofromBtn.setDisable( false );

			if ( null == other ) {
				// > 2 splits, so must use split editor
				accountfield.getSelectionModel().select( null );
				accountfield.setValue( null );
				accountfield.setDisable( true );
				amountfield.setEditable( false );
				amountfield.setDisable( true );
				tofromBtn.setDisable( true );
			}
			else {
				accountfield.getSelectionModel().select( other.getAccount() );
			}
		}
		else {
			if ( splits.size() <= 2 ) {
				amountfield.setEditable( true );

				amountfield.setDisable( false );
				tofromBtn.setDisable( false );
				accountfield.setDisable( false );

				accountfield.getSelectionModel().select( other.getAccount() );
				accountfield.setValue( other.getAccount() );
			}
			else {
				// > 2 splits, so must use split editor
				accountfield.getSelectionModel().select( null );
				accountfield.setValue( null );
				accountfield.setDisable( true );
				amountfield.setEditable( false );
				amountfield.setDisable( true );
				tofromBtn.setDisable( true );
			}
		}
	}

	public static interface CloseListener {

		public void closed();

		public void added( Transaction t );

		public void updated( Transaction t );
	}
}
