/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Transaction;
import org.openrdf.model.URI;

public interface RecurrenceMapper extends DataMapper<Recurrence> {

	/**
	 * Creates a new recurring transaction based on the given transaction
	 *
	 * @param r
	 * @param t A pre-existing transaction
	 * @return
	 * @throws MapperException
	 */
	public Recurrence create( Recurrence r, Transaction t ) throws MapperException;

	public URI getObjectType( Recurrence r ) throws MapperException;
}
