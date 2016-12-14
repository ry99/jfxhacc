/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.MapperListener;
import com.ostrichemulators.jfxhacc.model.IDable;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <T>
 */
public class MapperListenerAdapter<T extends IDable> implements MapperListener<T> {

	public static final Logger log = Logger.getLogger( MapperListenerAdapter.class );

	@Override
	public void added( T t ) {
	}

	@Override
	public void updated( T t ) {
	}

	@Override
	public void removed( URI uri ) {
	}
}
