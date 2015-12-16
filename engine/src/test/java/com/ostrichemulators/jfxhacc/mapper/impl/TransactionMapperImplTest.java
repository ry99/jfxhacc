/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper.impl;

import com.ostrichemulators.jfxhacc.mapper.TransactionListener;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Journal;
import com.ostrichemulators.jfxhacc.model.Payee;
import com.ostrichemulators.jfxhacc.model.Split;
import com.ostrichemulators.jfxhacc.model.Transaction;
import com.ostrichemulators.jfxhacc.model.impl.JournalImpl;
import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import com.ostrichemulators.jfxhacc.utility.UriUtil;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.RepositoryConnection;

/**
 *
 * @author ryan
 */
public class TransactionMapperImplTest {

	private RepositoryConnection rc;
	private PayeeMapperImpl pmi;
	private AccountMapperImpl ami;
	private JournalMapperImpl jmi;
	private TransactionMapperImpl tmap;
	private static final URI ACCTID = UriUtil.randomUri( Accounts.TYPE );

	public TransactionMapperImplTest() {
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
		pmi = new PayeeMapperImpl( rc );
		jmi = new JournalMapperImpl( rc );
		tmap = new TransactionMapperImpl( rc, ami, pmi, jmi );

		rc.begin();
		rc.add( ACCTID, RDF.TYPE, Accounts.TYPE );
		rc.add( ACCTID, RDFS.LABEL, new LiteralImpl( "tester" ) );
		rc.add( ACCTID, Accounts.TYPE_PRED, AccountType.EQUITY.getUri() );
		rc.add( ACCTID, Accounts.OBAL_PRED, rc.getValueFactory().createLiteral( 1507 ) );

		rc.commit();
	}

	@After
	public void tearDown() {
	}

	//@Test
	public void testAddMapperListener() {
		System.out.println( "addMapperListener" );
		TransactionListener tl = null;
		TransactionMapperImpl instance = null;
		instance.addMapperListener( tl );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testRemoveMapperListener() {
		System.out.println( "removeMapperListener" );
		TransactionListener tl = null;
		TransactionMapperImpl instance = null;
		instance.removeMapperListener( tl );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testCreate_5args() throws Exception {
		System.out.println( "create" );
		Date d = null;
		Payee p = null;
		String number = "";
		Collection<Split> splits = null;
		Journal journal = null;
		TransactionMapperImpl instance = null;
		Transaction expResult = null;
		Transaction result = instance.create( d, p, number, splits, journal );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testCreate_Transaction() throws Exception {
		System.out.println( "create" );
		Transaction t = null;
		TransactionMapperImpl instance = null;
		Transaction expResult = null;
		Transaction result = instance.create( t );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testRemove() throws Exception {
		System.out.println( "remove" );
		URI id = null;
		TransactionMapperImpl instance = null;
		instance.remove( id );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testGet() throws Exception {
		System.out.println( "get" );
		URI id = null;
		TransactionMapperImpl instance = null;
		Transaction expResult = null;
		Transaction result = instance.get( id );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testUpdate() throws Exception {
		System.out.println( "update" );
		Transaction t = null;
		TransactionMapperImpl instance = null;
		instance.update( t );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testGetTransaction() throws Exception {
		System.out.println( "getTransaction" );
		Split s = null;
		TransactionMapperImpl instance = null;
		Transaction expResult = null;
		Transaction result = instance.getTransaction( s );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testGetSplitMap() throws Exception {
		System.out.println( "getSplitMap" );
		URI transid = null;
		TransactionMapperImpl instance = null;
		Set<Split> expResult = null;
		Set<Split> result = instance.getSplitMap( transid );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testReconcile() throws Exception {
		System.out.println( "reconcile" );
		Split.ReconcileState rs = null;
		Account acct = null;
		Split[] splits = null;
		TransactionMapperImpl instance = null;
		instance.reconcile( rs, acct, splits );
		fail( "The test case is a prototype." );
	}

	// @Test
	public void testGetAll() throws Exception {
		Account acct = ami.get( ACCTID );
		Journal journal = new JournalImpl( "test" );

		List<Transaction> expResult = null;
		List<Transaction> result = tmap.getAll( acct, journal );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

	//@Test
	public void testGetUnreconciled() throws Exception {
		System.out.println( "getUnreconciled" );
		Account acct = null;
		Journal journal = null;
		Date asof = null;
		TransactionMapperImpl instance = null;
		List<Transaction> expResult = null;
		List<Transaction> result = instance.getUnreconciled( acct, journal, asof );
		assertEquals( expResult, result );
		fail( "The test case is a prototype." );
	}

}
