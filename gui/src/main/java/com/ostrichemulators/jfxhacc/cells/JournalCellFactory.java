/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.cells;

import com.ostrichemulators.jfxhacc.datamanager.JournalManager;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.SplitStub;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class JournalCellFactory implements Callback<TableColumn<SplitStub, URI>, TableCell<SplitStub, URI>> {

	public static final Logger log = Logger.getLogger( JournalCellFactory.class );
	private final Map<URI, String> names = new HashMap<>();

	public JournalCellFactory( DataEngine jm ) {
		JournalManager jman = new JournalManager( jm );
		names.putAll( jman.getNameMap() );
	}

	@Override
	public TableCell<SplitStub, URI> call( TableColumn<SplitStub, URI> p ) {
		return new TableCell<SplitStub, URI>() {
			@Override
			protected void updateItem( URI t, boolean empty ) {
				super.updateItem( t, empty );
				if ( ( empty || null == t ) ) {
					setText( null );
				}
				else {
					setText( names.get( t ) );
				}
			}
		};
	}
}
