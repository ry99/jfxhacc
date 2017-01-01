/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.mapper.DataMapper;
import com.ostrichemulators.jfxhacc.mapper.NamedIDable;
import java.util.Map;
import java.util.function.Predicate;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <T>
 */
public abstract class NamedIDableDataManager<T extends NamedIDable> extends AbstractDataManager<T> {

	private static final Logger log = Logger.getLogger( NamedIDableDataManager.class );

	public NamedIDableDataManager( DataMapper<T> mapper ) {
		this( mapper, null );
	}

	public NamedIDableDataManager( DataMapper<T> mapper, Callback<T, Observable[]> cb ) {
		super( mapper, cb );
	}

	@Override
	protected void update( T inlist, T newvals ) {
		inlist.setName( newvals.getName() );
	}

	public T get( String name ) {
		ObservableList<T> list = getAll().filtered( new Predicate<T>() {
			@Override
			public boolean test( T t ) {
				return t.getName().equals( name );
			}
		} );

		return ( list.isEmpty() ? null : list.get( 0 ) );
	}

	public ObservableMap<URI, String> getNameMap() {
		ObservableMap<URI, T> journals = getMap();
		ObservableMap<URI, String> pa = FXCollections.observableHashMap();
		for ( Map.Entry<URI, T> e : journals.entrySet() ) {
			pa.put( e.getKey(), e.getValue().getName() );
		}

		journals.addListener( new MapChangeListener<URI, T>() {
			@Override
			public void onChanged( MapChangeListener.Change<? extends URI, ? extends T> change ) {
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

}
