/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;

/**
 *
 * @author ryan
 */
public interface QueryHandler<T> {

	public void handleTuple( BindingSet set, ValueFactory vf );

	public T getResult();
}
