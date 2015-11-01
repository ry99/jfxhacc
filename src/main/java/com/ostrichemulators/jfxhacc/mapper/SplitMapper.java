/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ryan
 */
public interface SplitMapper extends DataMapper<Split> {

	public Map<Transaction, Map<Split, Account>> getSplits( List<Transaction> trans ) throws MapperException;

	public Split create( Split s, Account a ) throws MapperException;

}
