/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Payee;

/**
 *
 * @author ryan
 */
public interface PayeeMapper extends DataMapper<Payee> {

	public Payee create( String name ) throws MapperException;

	public Payee createOrGet( String name ) throws MapperException;
}
