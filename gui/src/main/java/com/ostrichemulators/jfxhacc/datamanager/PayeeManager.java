/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.model.Payee;
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
public class PayeeManager extends AbstractDataManager<Payee> {

	private static final Logger log = Logger.getLogger( PayeeManager.class );

	public PayeeManager( DataEngine de ) {
		super( de.getPayeeMapper() );
	}

	public ObservableMap<URI, String> getNameMap() {
		ObservableMap<URI, Payee> payees = getMap();
		ObservableMap<URI, String> pa = FXCollections.observableHashMap();
		for ( Map.Entry<URI, Payee> e : payees.entrySet() ) {
			pa.put( e.getKey(), e.getValue().getName() );
		}

		payees.addListener( new MapChangeListener<URI, Payee>() {
			@Override
			public void onChanged( MapChangeListener.Change<? extends URI, ? extends Payee> change ) {
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
	protected void update( Payee inlist, Payee newvals ) {
		inlist.setName( newvals.getName() );
	}
}
