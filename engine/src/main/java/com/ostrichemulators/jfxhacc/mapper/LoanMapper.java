/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Loan;
import com.ostrichemulators.jfxhacc.model.Recurrence;

public interface LoanMapper extends DataMapper<Loan> {

	/**
	 * Creates a new loan
	 *
	 * @param l
	 * @param t A pre-existing transaction
	 * @return
	 * @throws MapperException
	 */
	public Loan create( Loan l ) throws MapperException;

	public Loan get( Recurrence r ) throws MapperException;
}
