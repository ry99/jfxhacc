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
import info.aduna.iteration.Iterations;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author ryan
 */
public class DbUtil {

	private static final Logger log = Logger.getLogger( DbUtil.class );

	private DbUtil() {
	}

	public static RepositoryConnection createRepository( String db ) throws RepositoryException {
		RepositoryConnection rc = null;
		boolean doinit = true;
		if ( db.endsWith( "memorystore.data" ) ) {
			// make an in-memory repository
			rc = createInMemRepository( new File( db ) );
		}
		else if ( db.endsWith( ".ttl" ) || db.endsWith( ".rdf" ) || db.endsWith( ".nt" ) ) {
			// use the given file to populate an in-memory datastore
			rc = createInMemRepository();
			doinit = false;
			RDFFormat fmt = RDFFormat.NTRIPLES;
			if ( db.endsWith( ".ttl" ) ) {
				fmt = RDFFormat.TURTLE;
			}
			else if ( db.endsWith( ".rdf" ) ) {
				fmt = RDFFormat.RDFXML;
			}

			try {
				log.info( "loading data from:" + db );
				rc.add( new File( db ), "", fmt );
			}
			catch ( IOException | RDFParseException e ) {
				log.error( e, e );
			}
		}
		else if ( db.startsWith( "http" ) ) {
			// make an http repository
			Repository repo = new HTTPRepository( db );
			repo.initialize();
			rc = repo.getConnection();
		}

		if ( null == rc ) {
			//nothing worked so far, so see if we just have a plain old directory
			File tryfile = new File( db );
			if ( tryfile.isDirectory() ) {
				rc = createInMemRepository( tryfile );
			}
			else {
				throw new RepositoryException( "Unable to open database: " + db );
			}
		}

		initNamespaces( rc );

		if ( doinit && initDb( rc ) ) {
			firstTime( rc );
		}

		log.info( "database initialized" );
		return rc;
	}

	public static RepositoryConnection createInMemRepository() throws RepositoryException {
		Repository repo = new SailRepository( new MemoryStore() );
		repo.initialize();
		RepositoryConnection rc = repo.getConnection();
		initDb( rc );
		initNamespaces( rc );
		firstTime( rc );
		return rc;
	}

	private static RepositoryConnection createInMemRepository( File datadir ) throws RepositoryException {
		boolean init = ( !datadir.exists() );

		if ( init ) {
			if ( datadir.getName().equals( "memorystore.data" ) ) {
				datadir.getParentFile().mkdirs();
			}
			else {
				datadir.mkdirs();
			}
		}

		if ( datadir.getName().equals( "memorystore.data" ) ) {
			// OpenRDF automatically adds the "memorystore.data" part
			datadir = datadir.getParentFile();
		}

		Repository repo = new SailRepository( new MemoryStore( datadir ) );
		repo.initialize();
		return repo.getConnection();
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

	private static boolean initDb( RepositoryConnection rc ) throws RepositoryException {
		List<Statement> stmts = Iterations.asList( rc.getStatements( null, RDF.TYPE,
				JfxHacc.DATASET_TYPE, false ) );
		URI dbid = null;
		boolean wasCreated = true;

		ValueFactory vf = rc.getValueFactory();
		rc.begin();
		if ( stmts.isEmpty() ) {
			dbid = UriUtil.randomUri( new URIImpl( JfxHacc.BASE ) );
			rc.add( dbid, RDF.TYPE, JfxHacc.DATASET_TYPE );
			rc.add( dbid, DCTERMS.CREATED, vf.createLiteral( new Date() ) );
			rc.add( dbid, DCTERMS.CREATOR, vf.createLiteral( System.getProperty( "user.name" ) ) );
		}
		else {
			dbid = URI.class.cast( stmts.get( 0 ).getSubject() );
			wasCreated = false;
		}

		rc.remove( dbid, DCTERMS.MODIFIED, null );
		rc.add( dbid, DCTERMS.MODIFIED,
				rc.getValueFactory().createLiteral( new Date() ) );

		rc.commit();
		return wasCreated;
	}

	private static void firstTime( RepositoryConnection rc ) throws RepositoryException {
		// this is a good place to add the standard ontology
	}

	public static Literal fromDate( Date date ) {
		return new ValueFactoryImpl().createLiteral( date );
	}

	public static Date toDate( Literal cal ) {
		return cal.calendarValue().toGregorianCalendar().getTime();
	}
}
