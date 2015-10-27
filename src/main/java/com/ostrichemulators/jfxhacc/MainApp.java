package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.engine.impl.RdfDataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.mapper.impl.AccountMapperImpl;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.model.AccountType;
import com.ostrichemulators.jfxhacc.model.Money;
import com.ostrichemulators.jfxhacc.model.impl.AccountImpl;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class MainApp extends Application {

	private static final Logger log = Logger.getLogger( MainApp.class );
	private RepositoryConnection rc;
	private File datadir;

	private static RdfDataEngine engine;

	public static DataEngine getEngine() {
		return engine;
	}

	@Override
	public void start( Stage stage ) throws Exception {
		Parent root = FXMLLoader.load( getClass().getResource( "/fxml/Scene.fxml" ) );

		Scene scene = new Scene( root );
		scene.getStylesheets().add( "/styles/Styles.css" );

		stage.setTitle( "Jfx Home Accountant" );
		stage.setScene( scene );
		stage.show();
	}

	@Override
	public void init() {
		try {
			final Parameters params = getParameters();
			final List<String> parameters = params.getRaw();
			datadir = ( parameters.isEmpty()
					? new File( System.getProperty( "user.home" ), ".jfxhacc" )
					: new File( parameters.get( 0 ) ) );

			boolean initdataset = ( !datadir.exists() );
			rc = DbUtil.createRepository( datadir );
			if ( initdataset ) {
				initKb( rc );
			}

			engine = new RdfDataEngine( rc );
		}
		catch ( Exception e ) {
			System.err.println( e.getLocalizedMessage() );
			log.fatal( e, e );
			Platform.exit();
		}
	}

	@Override
	public void stop() {
		if ( log.isTraceEnabled() ) {
			try {
				engine.dump( new File( "/tmp/dump.ttl" ) );
			}
			catch ( RepositoryException | IOException e ) {
				log.warn( e, e );
			}
		}

		try {
			rc.close();
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}

		try {
			rc.getRepository().shutDown();
		}
		catch ( Exception e ) {
			log.warn( e, e );
		}
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application.
	 * main() serves only as fallback in case the application can not be launched
	 * through deployment artifacts, e.g., in IDEs with limited FX support.
	 * NetBeans ignores main().
	 *
	 * @param args the command line arguments
	 */
	public static void main( String[] args ) {
		launch( args );
	}

	private static void initKb( RepositoryConnection rc ) throws RepositoryException {
		// this is a good place to add the standard ontology

		Random r = new Random();
		AccountMapperImpl ami = new AccountMapperImpl( rc );
		for ( String name : new String[]{ "Eh", "BEE", "si" } ) {
			Account a = new AccountImpl( AccountType.ASSET, name );
			a.setOpeningBalance( new Money( r.nextInt( 50000 ) ) );
			try {
				ami.create( a );
			}
			catch ( MapperException ne ) {
				log.error( ne, ne );
			}
		}
		ami.release();
	}
}
