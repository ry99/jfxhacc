/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import com.ostrichemulators.jfxhacc.model.vocabulary.Accounts;
import com.ostrichemulators.jfxhacc.model.vocabulary.JfxHacc;
import com.ostrichemulators.jfxhacc.model.vocabulary.Splits;
import java.io.File;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
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
}
