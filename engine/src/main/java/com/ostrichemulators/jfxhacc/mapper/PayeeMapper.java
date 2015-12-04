/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Payee;
import java.util.Map;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 */
public interface PayeeMapper extends DataMapper<Payee> {

	public Payee create( String name ) throws MapperException;
	
	public Payee createOrGet( String name ) throws MapperException;

	public Map<URI, String> getPayees() throws MapperException;
}
