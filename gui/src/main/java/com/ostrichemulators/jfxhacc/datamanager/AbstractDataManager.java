/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.datamanager;

import com.ostrichemulators.jfxhacc.mapper.DataMapper;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.model.IDable;
import java.util.ListIterator;
import java.util.function.Predicate;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
public abstract class AbstractDataManager<T extends IDable> {

	private final static Logger log = Logger.getLogger( AbstractDataManager.class );

	private final ObservableList<T> all;

	public AbstractDataManager( DataMapper<T> mapper ) {
		this( mapper, null );
	}

	public AbstractDataManager( DataMapper<T> mapper, Callback<T, Observable[]> cb ) {
		try {
			all = ( null == cb
					? FXCollections.observableArrayList()
					: FXCollections.observableArrayList( cb ) );
			all.addAll( mapper.getAll() );

			mapper.addMapperListener( new MapperListener<T>() {
				@Override
				public void added( T t ) {
					add( t, all );
				}

				@Override
				public void updated( T t ) {
					T old = get( t.getId() );
					if ( null == old ) {
						added( t );
					}
					else {
						update( old, t );
					}
				}

				@Override
				public void removed( URI uri ) {
					remove( uri, all );
				}
			} );
		}
		catch ( MapperException me ) {
			throw new RuntimeException( "could not initialize manager", me );
		}
	}

	protected abstract void update( T inlist, T newvals );

	protected void add( T item, ObservableList<T> list ) {
		list.add( item );
	}

	/**
	 * Remove the given URI
	 *
	 * @param id the id to remove
	 * @param list all the elements we know about
	 */
	protected void remove( URI id, ObservableList<T> list ) {
		ListIterator<T> li = list.listIterator();
		while ( li.hasNext() ) {
			T t = li.next();
			if ( t.getId().equals( id ) ) {
				li.remove();
			}
		}
	}

	public ObservableList<T> getAll() {
		return FXCollections.unmodifiableObservableList( all );
	}

	public IntegerProperty sizeProperty() {
		IntegerProperty ip = new SimpleIntegerProperty();
		ip.bind( Bindings.size( getAll() ) );
		return ip;
	}

	public ObservableMap<URI, T> getMap() {
		ObservableList<T> list = getAll();
		ObservableMap<URI, T> pmap = FXCollections.observableHashMap();
		for ( T p : list ) {
			pmap.put( p.getId(), p );
		}

		list.addListener( new ListChangeListener<T>() {
			@Override
			public void onChanged( ListChangeListener.Change<? extends T> c ) {
				while ( c.next() ) {
					if ( c.wasAdded() ) {
						for ( T p : c.getAddedSubList() ) {
							pmap.put( p.getId(), p );
						}
					}
					else if ( c.wasRemoved() ) {
						for ( T p : c.getRemoved() ) {
							pmap.remove( p.getId() );
						}
					}
				}
			}
		} );

		return FXCollections.unmodifiableObservableMap( pmap );
	}

	/**
	 * Gets a single element from the cache
	 *
	 * @param id
	 * @return
	 */
	public T get( URI id ) {
		ObservableList<T> l = getAll().filtered( new Predicate<T>() {
			@Override
			public boolean test( T t ) {
				return t.getId().equals( id );
			}
		} );

		return ( l.isEmpty() ? null : l.get( 0 ) );
	}
}
