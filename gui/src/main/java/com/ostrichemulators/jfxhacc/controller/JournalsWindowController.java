/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.controller;

import com.ostrichemulators.jfxhacc.cells.JournalListCellFactory;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Journal;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 *
 * @author ryan
 */
public class JournalsWindowController {

	private static final Logger log = Logger.getLogger( JournalsWindowController.class );
	@FXML
	private ListView<Journal> jlist;
	@FXML
	private TextField name;

	private final DataEngine engine;
	private final JournalMapper jmap;
	private Stage stage;

	public JournalsWindowController( DataEngine eng ) {
		engine = eng;
		jmap = engine.getJournalMapper();
	}

	public void setStage( Stage s ) {
		stage = s;
	}

	@FXML
	public void initialize() {
		jlist.setItems( jmap.getObservable() );
		jlist.setCellFactory( new JournalListCellFactory() );

		jlist.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<Journal>() {
					@Override
					public void changed( ObservableValue<? extends Journal> ov,
							Journal old_val, Journal new_val ) {
						name.setText( null == new_val ? "" : new_val.getName() );
					}
				} );
	}

	@FXML
	public void save() {
		try {
			Journal j = jlist.getSelectionModel().getSelectedItem();
			j.setName( name.getText() );
			jmap.update( j );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}

	@FXML
	public void close() {
		stage.close();
	}

	@FXML
	public void deleteCurrent() {
		try {
			jmap.remove( jlist.getSelectionModel().getSelectedItem() );
		}
		catch ( MapperException me ) {
			log.error( me, me );
		}
	}
}
