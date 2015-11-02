/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.engine;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.SplitMapper;
import com.ostrichemulators.jfxhacc.mapper.TransactionMapper;

/**
 *
 * @author ryan
 */
public interface DataEngine {

	public AccountMapper getAccountMapper();

	public JournalMapper getJournalMapper();

	public PayeeMapper getPayeeMapper();

	public TransactionMapper getTransactionMapper();

	public SplitMapper getSplitMapper();

	public void release();
}
