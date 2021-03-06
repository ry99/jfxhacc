/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.engine;

import com.ostrichemulators.jfxhacc.mapper.AccountMapper;
import com.ostrichemulators.jfxhacc.mapper.JournalMapper;
import com.ostrichemulators.jfxhacc.mapper.LoanMapper;
import com.ostrichemulators.jfxhacc.mapper.PayeeMapper;
import com.ostrichemulators.jfxhacc.mapper.RecurrenceMapper;
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

	public RecurrenceMapper getRecurrenceMapper();

	public LoanMapper getLoanMapper();

	public void release();
}
