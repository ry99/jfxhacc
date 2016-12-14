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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <T>
 */
public abstract class AbstractDataManager<T extends IDable> {

	protected final ObservableList<T> all;

	public AbstractDataManager( DataMapper<T> mapper ) {
		try {
			all = FXCollections.observableArrayList( mapper.getAll() );

			mapper.addMapperListener( new MapperListener<T>() {
				@Override
				public void added( T t ) {
					add( t, all );
				}

				@Override
				public void updated( T t ) {
					ListIterator<T> it = all.listIterator();
					while ( it.hasNext() ) {
						T a = it.next();
						if ( a.getId().equals( t.getId() ) ) {
							update( a, t );
						}
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
		return all;
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
		return getMap().get( id );
	}
}
