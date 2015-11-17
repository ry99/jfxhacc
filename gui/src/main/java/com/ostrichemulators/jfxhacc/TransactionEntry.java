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
import com.ostrichemulators.jfxhacc.utility.GuiUtils;
import java.io.IOException;
import java.text.Collator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
public class TransactionEntry extends AnchorPane {

	private static final Logger log = Logger.getLogger( TransactionEntry.class );
	@FXML
	private Button splitBtn;
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

	private Transaction trans;
	private Account account;
	private Journal journal;
	private boolean newtrans;
	private AccountMapper amap;
	private PayeeMapper pmap;
	private TransactionMapper tmap;
	private List<CloseListener> listenees = new ArrayList<>();
	private AutoCompletePopupHandler autocomplete;

	private Map<String, Payee> payeemap = new HashMap<>();
	private final List<Account> allaccounts = new ArrayList<>();
	private final ObservableList<Account> accounts
			= FXCollections.observableArrayList();

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

	public void addCloseListener( CloseListener cc ) {
		listenees.add( cc );
	}

	public void removeCloseListener( CloseListener cc ) {
		listenees.remove( cc );
	}

	public void setAccount( Account a ) {
		account = a;

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

	public void setJournal( Journal j ) {
		journal = j;
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
				Split mysplit = tmap.create( mymoney, mymemo, myreco );
				Split yoursplit = tmap.create( mymoney.opposite(), mymemo, ReconcileState.NOT_RECONCILED );

				Map<Account, Split> splits = new HashMap<>();
				splits.put( account, mysplit );
				splits.put( accountfield.getValue(), yoursplit );

				trans = tmap.create( tdate, payee, num, splits, journal );

				for ( CloseListener c : listenees ) {
					c.added( trans );
				}
			}
			else {
				for ( Map.Entry<Account, Split> en : trans.getSplits().entrySet() ) {
					Account acct = en.getKey();
					Split s = en.getValue();
					if ( acct.equals( account ) ) {
						s.setMemo( mymemo );
						s.setReconciled( myreco );
						s.setValue( mymoney );
					}
					else {
						en.getValue().setValue( mymoney.opposite() );
					}
				}

				trans.setDate( tdate );
				trans.setNumber( num );
				trans.setPayee( payee );
				// trans.setSplits( splits );
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

	private Money getSplitAmount() {
		boolean to = ( "To".equals( tofromBtn.getText() ) );
		Money money = Money.valueOf( amountfield.getText() );
		boolean posmoney = money.isPositive();

		if ( account.getAccountType().isRightPlus() ) {
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
			DataEngine eng = MainApp.getEngine();
			pmap = eng.getPayeeMapper();
			amap = eng.getAccountMapper();
			allaccounts.clear();
			allaccounts.addAll( amap.getAll() );
			tmap = eng.getTransactionMapper();

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

		amountfield.clear();
		memofield.clear();
		numberfield.clear();

		tofromBtn.setText( to ? "To" : "From" );

		setDate( d );
		setReco( rs );
		payeefield.requestFocus();
	}

	public void setTransaction( Transaction t ) {
		clear();
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
					accountfield.setValue( a );
				}
			}
		}

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
		datefield.setValue( LocalDate.now() );
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
				if ( !( null == acct || empty ) ) {
					setText( GuiUtils.getFullName( acct, amap ) );
				}
			}
		};
	}

	public static interface CloseListener {

		public void closed();

		public void added( Transaction t );

		public void updated( Transaction t );
	}
}
