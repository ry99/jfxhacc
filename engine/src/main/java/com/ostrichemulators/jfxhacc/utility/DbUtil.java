/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Payees;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import com.ostrichemulators.jfxhacc.model.vocabulary.Transactions;
import java.io.File;
import java.util.Date;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class DbUtil {

	private DbUtil() {
	}

	public static RepositoryConnection createRepository( File datadir ) throws RepositoryException {
		boolean init = ( !datadir.exists() );

		if ( !datadir.exists() ) {
			if ( datadir.getName().equals( "memorystore.data" ) ) {
				datadir.getParentFile().mkdirs();
			}
			else {
				datadir.mkdirs();
			}
		}

		ForwardChainingRDFSInferencer fci
				= new ForwardChainingRDFSInferencer( new MemoryStore( datadir ) );
		Repository repo = new SailRepository( fci );
		repo.initialize();

		if ( init ) {
			init( repo );
		}

		RepositoryConnection rc = repo.getConnection();
		initNamespaces( rc );

		return rc;
	}

	public static RepositoryConnection createRepository( String http ) throws RepositoryException {
		Repository repo = new HTTPRepository( http );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		initNamespaces( rc );
		return rc;
	}


	public static RepositoryConnection createInMemRepository() throws RepositoryException {
		ForwardChainingRDFSInferencer fci
				= new ForwardChainingRDFSInferencer( new MemoryStore() );
		Repository repo = new SailRepository( fci );
		repo.initialize();
		init( repo );
		RepositoryConnection rc = repo.getConnection();
		initNamespaces( rc );
		return rc;
	}

	private static void initNamespaces( RepositoryConnection rc ) throws RepositoryException {
		rc.begin();
		rc.setNamespace( JfxHacc.PREFIX, JfxHacc.NAMESPACE );
		rc.setNamespace( Accounts.PREFIX, Accounts.NAMESPACE );
		rc.setNamespace( Splits.PREFIX, Splits.NAMESPACE );
		rc.setNamespace( Transactions.PREFIX, Transactions.NAMESPACE );
		rc.setNamespace( Payees.PREFIX, Payees.NAMESPACE );
		rc.setNamespace( DCTERMS.PREFIX, DCTERMS.NAMESPACE );
		rc.setNamespace( "t", "http://com.ostrich-emulators/jfxhacc/transaction#" );
		rc.setNamespace( "p", "http://com.ostrich-emulators/jfxhacc/payee#" );
		rc.setNamespace( "a", "http://com.ostrich-emulators/jfxhacc/account#" );
		rc.setNamespace( "s", "http://com.ostrich-emulators/jfxhacc/split#" );
		rc.setNamespace( "j", "http://com.ostrich-emulators/jfxhacc/journal#" );

		rc.setNamespace( RDFS.PREFIX, RDFS.NAMESPACE );
		rc.setNamespace( RDF.PREFIX, RDF.NAMESPACE );
		rc.setNamespace( XMLSchema.PREFIX, XMLSchema.NAMESPACE );
		rc.commit();
	}

	private static void init( Repository repo ) throws RepositoryException {
		RepositoryConnection rc = repo.getConnection();
		rc.begin();
		URI dbid = UriUtil.randomUri( new URIImpl( JfxHacc.BASE ) );
		rc.add( new StatementImpl( dbid, RDF.TYPE, JfxHacc.DATASET_TYPE ) );
		rc.commit();
		rc.close();
	}

	public static Literal fromDate( Date date ) {
		return new ValueFactoryImpl().createLiteral( date );
	}

	public static Date toDate( Literal cal ) {
		return cal.calendarValue().toGregorianCalendar().getTime();
	}

}
