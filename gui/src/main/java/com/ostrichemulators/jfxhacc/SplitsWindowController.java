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
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Split.ReconcileState;
import java.util.Collection;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author ryan
 */
public class SplitsWindowController {

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

	private Stage stage;
	private final DataEngine engine;
	private final ObservableSet<Split> splits = FXCollections.observableSet();
	private boolean canceled = false;

	public SplitsWindowController( DataEngine eng ) {
		engine = eng;
	}

	@FXML
	void cancel( ActionEvent event ) {
		canceled = true;
		stage.close();
	}

	public boolean wasCanceled() {
		return canceled;
	}

	@FXML
	void save( ActionEvent event ) {
		stage.close();
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
	}

	public void setSplits( Collection<Split> set ) {
		splits.addAll( set );
		splittable.getItems().setAll( splits );
	}

	public ObservableSet<Split> getSplits() {
		return splits;
	}

	public void setStage( Stage s ) {
		stage = s;
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
