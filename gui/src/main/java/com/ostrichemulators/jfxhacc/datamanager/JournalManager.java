/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Journal;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public class JournalManager extends AbstractDataManager<Journal> {

	private static final Logger log = Logger.getLogger( JournalManager.class );

	public JournalManager( DataEngine de ) {
		super( de.getJournalMapper() );
	}

	public ObservableMap<URI, String> getNameMap() {
		ObservableMap<URI, Journal> journals = getMap();
		ObservableMap<URI, String> pa = FXCollections.observableHashMap();
		for ( Map.Entry<URI, Journal> e : journals.entrySet() ) {
			pa.put( e.getKey(), e.getValue().getName() );
		}

		journals.addListener( new MapChangeListener<URI, Journal>() {
			@Override
			public void onChanged( MapChangeListener.Change<? extends URI, ? extends Journal> change ) {
				if ( null == change.getValueAdded() ) {
					pa.remove( change.getKey() );
				}
				else {
					pa.put( change.getKey(), change.getValueAdded().getName() );
				}
			}
		} );

		return FXCollections.unmodifiableObservableMap( pa );
	}

	@Override
	protected void update( Journal inlist, Journal newvals ) {
		inlist.setName( newvals.getName() );
	}
}
