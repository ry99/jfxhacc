/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import java.util.Collection;

/**
 *
 * @author ryan
 */
public interface TransactionListener extends MapperListener<Transaction> {

	public void reconciled( Account acct, Collection<Split> splits );

}
