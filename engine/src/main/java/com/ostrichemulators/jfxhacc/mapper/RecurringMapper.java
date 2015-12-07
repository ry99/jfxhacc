/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Recurring;

/**
 *
 * @author ryan
 */
public interface RecurringMapper extends DataMapper<Recurring> {

	/**
	 * Creates the given recurring transaction
	 *
	 * @param r
	 * @return
	 * @throws MapperException
	 */
	public Recurring create( Recurring r ) throws MapperException;

}
