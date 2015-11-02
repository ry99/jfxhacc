/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import info.aduna.iteration.Iterations;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class AccountMapperImplTest {

	private RepositoryConnection rc;
	private AccountMapperImpl ami;
	private static final URI GETID = UriUtil.randomUri( JfxHacc.ACCOUNT_TYPE );

	public AccountMapperImplTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception {
		rc = DbUtil.createInMemRepository();
		ami = new AccountMapperImpl( rc );

		rc.begin();
		rc.add( new StatementImpl( GETID, RDF.TYPE, JfxHacc.ACCOUNT_TYPE ) );
		rc.add( new StatementImpl( GETID, RDFS.LABEL, new LiteralImpl( "tester" ) ) );
		rc.add( new StatementImpl( GETID, Accounts.TYPE_PRED,
				AccountType.EQUITY.getUri() ) );
		rc.add( new StatementImpl( GETID, Accounts.OBAL_PRED,
				rc.getValueFactory().createLiteral( 1507 ) ) );
		rc.commit();
	}

	@After
	public void tearDown() throws Exception {
		ami.release();
		rc.close();
		rc.getRepository().shutDown();
	}

	@Test
	public void testCreate() throws Exception {
		Account acct
				= ami.create( "Test Account", AccountType.ASSET, new Money( 4000 ) );
		List<Statement> stmts = Iterations.asList( rc.getStatements( acct.getId(),
				null, null, false ) );
		assertEquals( 4, stmts.size() );
		assertEquals( acct, ami.get( acct.getId() ) );
	}

	@Test
	public void testGet() throws Exception {
		Account acct = ami.get( GETID );
		assertEquals( AccountType.EQUITY, acct.getAccountType() );
		assertEquals( new Money( 1507 ), acct.getOpeningBalance() );
	}

	@Test
	public void testGetAll() throws Exception {
		Account acct = ami.get( GETID );
		Collection<Account> list = ami.getAll();
		assertEquals( acct, list.iterator().next() );
	}

	@Test
	public void testRemove() throws Exception {
		ami.remove( GETID );
		List<Statement> stmts = Iterations.asList( rc.getStatements( GETID,
				null, null, false ) );
		assertTrue( stmts.isEmpty() );
	}

	//@Test
	public void testUpdate() throws Exception {
	}
}
