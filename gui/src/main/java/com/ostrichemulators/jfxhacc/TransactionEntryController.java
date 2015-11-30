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
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
	private Button splitsBtn;

	private Transaction trans;
	private Account account;
	private Journal journal;
	private boolean newtrans;
	private AccountMapper amap;
	private PayeeMapper pmap;
	private TransactionMapper tmap;
	private final List<CloseListener> listenees = new ArrayList<>();
	private AutoCompletePopupHandler autocomplete;

	private final Map<String, Payee> payeemap = new HashMap<>();
	private final List<Account> allaccounts = new ArrayList<>();
	private final ObservableList<Account> accounts
			= FXCollections.observableArrayList();
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

	public void setAccount( Account a, Journal j ) {
		account = a;
		journal = j;

		try {
			accounts.addAll( amap.getAll() );
			accounts.remove( a );
			SortedList<Account> sorted = new SortedList<>( accounts );
			sorted.setComparator( new Comparator<Account>() {

				@Override
				public int compare( Account o1, Account o2 ) {
					return Collator.getInstance().compare( GuiUtils.getFullName( o1, amap ),
							GuiUtils.getFullName( o2, amap ) );
				}
			} );
			accountfield.setItems( sorted );

			accountfield.setOnKeyTyped( new EventHandler<KeyEvent>() {
				@Override
				public void handle( KeyEvent t ) {
					t.consume();
					String charo = t.getCharacter().toUpperCase();
					Account selected = null;
					for ( Account acct : accountfield.getItems() ) {
						String fullname = GuiUtils.getFullName( acct, amap ).toUpperCase();
						if ( fullname.startsWith( charo ) ) {
							selected = acct;
							break;
						}
					}

					if ( null == selected ) {
						// didn't find a match with full names, so check regular names
						for ( Account acct : accountfield.getItems() ) {
							String fullname = acct.getName().toUpperCase();
							if ( fullname.startsWith( charo ) ) {
								selected = acct;
								break;
							}
						}
					}

					if ( null != selected ) {
						accountfield.getSelectionModel().select( selected );
						accountfield.setValue( selected );
					}
				}
			} );
		}
		catch ( MapperException me ) {
			log.error( me, me );
			// FIXME: tell the user
		}
	}

	public void save() {
		try {
			Payee payee = pmap.createOrGet( payeefield.getText() );
			Date tdate = getDate();
			String num = numberfield.getText();
			String mymemo = memofield.getText();
			ReconcileState myreco = getReco();
			Money mymoney = getSplitAmount();

			if ( newtrans ) {
				Split mysplit = new SplitImpl( account, mymoney, mymemo, myreco );
				Split yoursplit = new SplitImpl( accountfield.getValue(),
						mymoney.opposite(), mymemo, ReconcileState.NOT_RECONCILED );

				Set<Split> splits = new HashSet<>();
				splits.add( mysplit );
				splits.add( yoursplit );

				trans = tmap.create( tdate, payee, num, splits, journal );

				for ( CloseListener c : listenees ) {
					c.added( trans );
				}
			}
			else {
//				for ( Split s : trans.getSplits() ) {
//					Account acct = s.getAccount();
//					if ( acct.equals( account ) ) {
//						s.setMemo( mymemo );
//						s.setReconciled( myreco );
//						s.setValue( mymoney );
//					}
//					else {
//						s.setValue( mymoney.opposite() );
//					}
//				}

				trans.setDate( tdate );
				trans.setNumber( num );
				trans.setPayee( payee );
				tmap.update( trans );

				for ( CloseListener c : listenees ) {
					c.updated( trans );
				}
				log.debug( "updating old transaction" );
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
		boolean posmoney = money.isPositive();

		if ( account.getAccountType().isDebitPlus() ) {
			if ( to ) {
				return ( posmoney ? money.opposite() : money );
			}
			else {
				return ( posmoney ? money : money.opposite() );
			}
		}
		else {
			if ( to ) {
				return ( posmoney ? money.opposite() : money );
			}
			else {
				return ( posmoney ? money : money.opposite() );
			}
		}
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
		autocomplete = new AutoCompletePopupHandler( payeefield );

		accountfield.setEditable( false );
		recofield.setAllowIndeterminate( true );

		try {
			pmap = engine.getPayeeMapper();
			amap = engine.getAccountMapper();
			allaccounts.clear();
			allaccounts.addAll( amap.getAll() );
			tmap = engine.getTransactionMapper();

			for ( Payee p : pmap.getAll() ) {
				payeemap.put( p.getName(), p );
			}
		}
		catch ( Exception x ) {
			log.warn( x, x );
		}

		autocomplete.getEntries().addAll( payeemap.keySet() );

		accountfield.setButtonCell( makeAccountCell() );
		accountfield.setCellFactory( new Callback<ListView<Account>, ListCell<Account>>() {

			@Override
			public ListCell<Account> call( ListView<Account> p ) {
				return makeAccountCell();
			}
		} );
	}

	public void setTransaction( Date d, ReconcileState rs, boolean to ) {
		clear();
		trans = null;
		newtrans = true;

		tofromBtn.setText( to ? "To" : "From" );

		setDate( d );
		setReco( rs );
		payeefield.requestFocus();
	}

	public void setTransaction( Date d, Split s ) {
		clear();
		trans = null;
		newtrans = true;
		memofield.setText( s.getMemo() );
		amountfield.setText( s.getValue().toPositiveString() );

		tofromBtn.setText( s.isCredit() != account.getAccountType().isDebitPlus()
				? "To" : "From" );

		setDate( d );
		setReco( s.getReconciled() );
		payeefield.requestFocus();
	}

	public void setTransaction( Transaction t ) {
		clear();
		trans = t;
		newtrans = false;

		Split mysplit = t.getSplit( account );
		memofield.setText( mysplit.getMemo() );
		amountfield.setText( mysplit.getValue().toString() );

		setReco( mysplit.getReconciled() );

		payeefield.setText( t.getPayee().getName() );

		updateSplitData( trans.getSplits() );

		numberfield.setText( t.getNumber() );

		setDate( t.getDate() );

		boolean debit = mysplit.isDebit();
		tofromBtn.setText( debit ? "To" : "From" );

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
	}

	private ListCell<Account> makeAccountCell() {
		return new ListCell<Account>() {
			@Override
			protected void updateItem( Account acct, boolean empty ) {
				super.updateItem( acct, empty );
				if ( null == acct || empty ) {
					setText( "Split" );
				}
				else {
					setText( GuiUtils.getFullName( acct, amap ) );
				}
			}
		};
	}

	@FXML
	public void openSplits() {
		setVisible( false );
	}

	public Account getSelectedAccount() {
		return accountfield.getValue();
	}

	public void updateSplitData( Set<Split> splits ) {
		log.debug( "update split data ("
				+ ( null == splits ? "" : splits.size() ) + " splits)" );
		if ( null == splits ) {
			if ( newtrans ) {
				accountfield.getSelectionModel().clearSelection();
				accountfield.setValue( null );
				accountfield.setDisable( false );
				amountfield.setEditable( true );
				amountfield.setDisable( false );
				tofromBtn.setDisable( false );
				return;
			}
			else {
				splits = trans.getSplits();
			}
		}

		if ( splits.size() <= 2 ) {
			tofromBtn.setDisable( false );
			accountfield.setDisable( false );
			amountfield.setEditable( true );
			amountfield.setDisable( false );

			for ( Split s : splits ) {
				Account a = s.getAccount();
				if ( !a.equals( account ) ) {
					accountfield.getSelectionModel().select( a );
					accountfield.setValue( a );
				}
			}
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

		for ( Split s : splits ) {
			Account a = s.getAccount();
			if ( a.equals( account ) ) {
				setReco( s.getReconciled() );
				memofield.setText( s.getMemo() );
				amountfield.setText( s.getValue().toString() );
			}
		}
	}

	public static interface CloseListener {

		public void closed();

		public void added( Transaction t );

		public void updated( Transaction t );
	}
}