/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Recurrence;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Date;
import java.util.List;
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

	/**
	 * Executes the given recurrence, which inserts a new transaction into the
	 * dataset. The transaction will have the date of
	 * {@link Recurrence#getNextRun()}.
	 *
	 * @param r
	 * @throws MapperException
	 */
	public void execute( Recurrence r ) throws MapperException;

	/**
	 * Gets a list of all recurrences that are currently due. If a recurrence
	 * should have run multiple times, it'll be in the returned list multiple
	 * times
	 *
	 * @param d
	 * @return
	 * @throws MapperException
	 */
	public List<Recurrence> getDue( Date d ) throws MapperException;
}
