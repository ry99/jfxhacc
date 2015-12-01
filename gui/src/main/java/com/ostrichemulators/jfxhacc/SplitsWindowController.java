/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.cells.AccountCellFactory;
import com.ostrichemulators.jfxhacc.cells.MoneyCellFactory;
import com.ostrichemulators.jfxhacc.cells.RecoCellFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class SplitsWindowController {

	public static final Logger log = Logger.getLogger( SplitsWindowController.class );
	@FXML
	private TableView<Split> splittable;
	@FXML
	private TableColumn<Split, Account> account;
	@FXML
	private TableColumn<Split, Money> credit;
	@FXML
	private TableColumn<Split, Money> debit;
	@FXML
	private TableColumn<Split, ReconcileState> reco;
	@FXML
	private TableColumn<Split, String> memo;
	@FXML
	private ButtonBar buttons;
	@FXML
	private Button okBtn;

	private final DataEngine engine;
	private final ObservableList<Split> splits = FXCollections.observableArrayList();
	private Account myacct;
	private EventHandler<ActionEvent> okhandler = null;

	public SplitsWindowController( DataEngine eng ) {
		engine = eng;
	}

	public void setOkButtonOnAction( EventHandler<ActionEvent> ae ) {
		okhandler = ae;
	}

	@FXML
	public void buttonPressed( ActionEvent event ) {
		if ( "OK".equals( okBtn.getText() ) ) {
			okhandler.handle( event );
		}
		else {
			balance();
		}
	}

	public void setAccount( Account acct ) {
		myacct = acct;
	}

	public void updateSplitData( Account acct, Money val, String memo,
			ReconcileState rs ) {
		boolean issplit = ( null == acct );

		if ( splits.isEmpty() ) {
			// this is a new transaction, so make some splits based on our data
			splits.add( new SplitImpl( myacct, val, memo, rs ) );
			if ( !issplit ) {
				splits.add( new SplitImpl( acct, val.opposite(), memo, rs ) );
			}
		}
		else {
			for ( Split s : splits ) {
				if ( s.getAccount().equals( myacct ) ) {
					s.setReconciled( rs );
					s.setMemo( memo );
					s.setValue( val );
				}
				else {
					if ( !issplit ) {
						// only have two accounts, so we can update the table data
						s.setAccount( acct );
						s.setValue( val.opposite() );
					}
				}
			}
		}
	}

	@FXML
	public void initialize() {
		reco.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, ReconcileState> p )
				-> p.getValue().getReconciledProperty() );
		reco.setCellFactory( new RecoCellFactory<>( true ) );

		credit.setCellValueFactory( new CDValueFactory( true ) );
		credit.setCellFactory( new MoneyCellFactory<>() );
		debit.setCellValueFactory( new CDValueFactory( false ) );
		debit.setCellFactory( new MoneyCellFactory<>() );

		account.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, Account> p )
				-> p.getValue().getAccountProperty() );
		account.setCellFactory( new AccountCellFactory<>( engine.getAccountMapper(), true ) );

		memo.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, String> p )
				-> p.getValue().getMemoProperty() );
		memo.setCellFactory( TextFieldTableCell.<Split>forTableColumn() );

		splittable.setItems( splits );
	}

	public void setSplits( SetProperty<Split> set ) {
		okBtn.disableProperty().unbind();
		splits.setAll( set );

		Observable amts[] = new Observable[set.size()];
		int i = 0;
		for ( Split s : set ) {
			amts[i++] = s.getValueProperty();
		}

		okBtn.textProperty().bind( Bindings.createStringBinding( new Callable<String>() {

			@Override
			public String call() throws Exception {
				Money bal = TransactionMapper.balancingValue( splits, myacct );
				return ( bal.isZero() ? "OK" : "Unbalanced: " + bal.toString() );
			}

		}, amts ) );
	}

	public Set<Split> getSplits() {
		Set<Split> set = new HashSet<>( splits );
		return set;
	}

	public void clear() {
		splits.clear();
	}

	@FXML
	public void balance() {
		Money bal = TransactionMapper.balancingValue( splits, myacct );
		for ( Split s : splits ) {
			if ( s.getAccount().equals( myacct ) ) {
				s.add( bal );
			}
		}
	}

	private static class CDValueFactory implements Callback<TableColumn.CellDataFeatures<Split, Money>, ObservableValue<Money>> {

		public static final Logger log = Logger.getLogger( CDValueFactory.class );
		private final boolean credit;

		public CDValueFactory( boolean iscredit ) {
			credit = iscredit;
		}

		@Override
		public ObservableValue<Money> call( TableColumn.CellDataFeatures<Split, Money> p ) {
			Split s = p.getValue();
			return ( ( credit && s.isCredit() ) || ( !credit && s.isDebit() )
					? s.getValueProperty() : null );
		}
	}
}
