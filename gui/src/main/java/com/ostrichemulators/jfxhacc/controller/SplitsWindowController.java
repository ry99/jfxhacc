/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.cells.AccountCellFactory;
import com.ostrichemulators.jfxhacc.cells.MoneyCellFactory;
import com.ostrichemulators.jfxhacc.cells.RecoCellFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import com.ostrichemulators.jfxhacc.model.impl.SplitImpl;
import com.ostrichemulators.jfxhacc.utility.TransactionHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SetProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
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
	//private final ObservableList<Split> splits = FXCollections.observableArrayList();
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
		if ( "OK".equals( okBtn.getText() ) && null != okhandler ) {
			okhandler.handle( event );
		}
		else {
			balance();
		}
	}

	public void hideOkWhenBalanced( boolean hide ) {

		if ( hide ) {
			okBtn.visibleProperty().bind(
					okBtn.textProperty().isEqualTo( "OK" ).not() );
		}
		else {
			okBtn.visibleProperty().unbind();
			okBtn.setVisible( true );
		}
	}

	public void setAccount( Account acct ) {
		myacct = acct;
	}

	@FXML
	public void initialize() {
		reco.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, ReconcileState> p )
				-> p.getValue().getReconciledProperty() );
		reco.setCellFactory( new RecoCellFactory<>( true ) );

		credit.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, Money> p )
				-> p.getValue().getRawValueProperty() );
		credit.setCellFactory( new MoneyCellFactory<>( true ) );
		debit.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, Money> p )
				-> p.getValue().getRawValueProperty() );
		debit.setCellFactory( new MoneyCellFactory<>( false ) );

		account.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, Account> p )
				-> p.getValue().getAccountProperty() );
		account.setCellFactory( new AccountCellFactory<>( engine.getAccountMapper(), true ) );

		memo.setCellValueFactory( ( TableColumn.CellDataFeatures<Split, String> p )
				-> p.getValue().getMemoProperty() );
		memo.setCellFactory( TextFieldTableCell.<Split>forTableColumn() );

		makeBalanceBinding();
	}

	@FXML
	public void addsplit() {
		SplitImpl si = new SplitImpl();
		if ( null != myacct ) {
			si.setAccount( myacct );
		}
		splittable.getItems().add( si );
		makeBalanceBinding();
	}

	public void setSplits( SetProperty<Split> set ) {
		okBtn.disableProperty().unbind();

		for ( Split s : set.get() ) {
			log.debug( "setspls: " + s.getId().getLocalName() + " "
					+ s.getAccount().getId().getLocalName() + " " + s + " "
					+ s.getRawValueProperty().getValue().value() );
		}

		splittable.getItems().setAll( set );
		makeBalanceBinding();
	}

	private void makeBalanceBinding() {
		Observable amts[] = new Observable[splittable.getItems().size()];
		int i = 0;
		for ( Split s : splittable.getItems() ) {
			amts[i++] = s.getRawValueProperty();
		}

		okBtn.textProperty().unbind();

		okBtn.textProperty().bind( Bindings.createStringBinding( new Callable<String>() {

			@Override
			public String call() throws Exception {
				Money bal = TransactionHelper.balancingValue( splittable.getItems(), myacct );
				return ( bal.isZero() ? "OK" : "Unbalanced: " + bal.toString() );
			}

		}, amts ) );
	}

	public Set<Split> getSplits() {
		Set<Split> set = new HashSet<>( splittable.getItems() );
		return set;
	}

	public void clear() {
		splittable.getItems().clear();
		makeBalanceBinding();
	}

	@FXML
	public void balance() {
		Money bal = TransactionHelper.balancingValue( splittable.getItems(), myacct );
		if ( null == myacct ) {
			SplitImpl s = new SplitImpl();
			splittable.getItems().add( s );
			makeBalanceBinding();
			s.setValue( bal );
		}
		else {
			for ( Split s : splittable.getItems() ) {
				if ( s.getAccount().equals( myacct ) ) {
					s.add( bal );
					break;
				}
			}
		}
	}
}
