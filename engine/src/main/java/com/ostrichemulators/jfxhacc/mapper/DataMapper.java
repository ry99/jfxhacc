/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.IDable;
import java.util.Collection;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <T>
 */
public interface DataMapper<T extends IDable> {

	public Collection<T> getAll() throws MapperException;

	public T get( URI id ) throws MapperException;

	public void remove( T t ) throws MapperException;

	public void remove( URI id ) throws MapperException;

	public void update( T t ) throws MapperException;

	public void release();

	public void addMapperListener( MapperListener<T> l );

	public void removeMapperListener( MapperListener<T> l );
}
