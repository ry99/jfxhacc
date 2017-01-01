package com.ostrichemulators.jfxhacc;

import com.ostrichemulators.jfxhacc.controller.MainWindowController;
import com.ostrichemulators.jfxhacc.engine.DataEngine;
import com.ostrichemulators.jfxhacc.engine.impl.RdfDataEngine;
import com.ostrichemulators.jfxhacc.mapper.MapperException;
import com.ostrichemulators.jfxhacc.model.Account;
import com.ostrichemulators.jfxhacc.utility.DbUtil;
import com.ostrichemulators.jfxhacc.utility.PredicateFactory;
import com.ostrichemulators.jfxhacc.utility.PredicateFactoryImpl;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import static javafx.application.Application.launch;

public class MainApp extends Application {

	private static final Logger log = Logger.getLogger( MainApp.class );
	private RepositoryConnection rc;

	private static RdfDataEngine engine;
	private static StageRememberer stager;
	private static MainWindowController controller;
	public static final PredicateFactory PF = new PredicateFactoryImpl();

	public static DataEngine getEngine() {
		return engine;
	}

	public static StageRememberer getShutdownNotifier() {
		return stager;
	}

	public static void select( Account acct ) {
		controller.select( acct );
	}

	@Override
	public void start( Stage stage ) throws Exception {
		MainApp.stager = new StageRememberer( stage, "main" );

		URL loc = getClass().getResource( "/fxml/Scene.fxml" );
		FXMLLoader fxmlloader = new FXMLLoader( loc );
		controller = new MainWindowController( engine );
		fxmlloader.setController( controller );

		Parent root = fxmlloader.load();

		Scene scene = new Scene( root );
		scene.getStylesheets().add( "/styles/Styles.css" );

		stage.setTitle( "Jfx Home Accountant" );
		stage.setScene( scene );

		MainApp.stager.restore( stage );
		stage.setOnCloseRequest( MainApp.stager );

		stage.show();
	}

	@Override
	public void init() {
		try {
			final Parameters params = getParameters();
			final List<String> parameters = params.getRaw();

			File defaultdb = new File( System.getProperty( "user.home" ), ".jfxhacc" );
			List<File> filesToLoad = new ArrayList<>();

			String database;
			if ( parameters.isEmpty() ) {
				database = defaultdb.getPath();
			}
			else {
				database = parameters.get( 0 );

				if ( parameters.size() > 1 ) {
					ListIterator<String> it = parameters.listIterator( 1 );
					while ( it.hasNext() ) {
						String name = it.next();
						String uname = name.toUpperCase();
						File file = new File( name );

						if ( file.exists() ) {
							if ( uname.endsWith( ".RDF" )
									|| uname.endsWith( ".TTL" )
									|| uname.endsWith( ".NT" ) ) {
								filesToLoad.add( file );
							}
							else {
								log.error( "cannot handle file: " + file
										+ " (nt, rdf, and ttl formats only)" );
							}
						}
						else {
							log.error( "cannot find file: " + file );
						}
					}
				}
			}

			rc = DbUtil.createRepository( database );

			for ( File file : filesToLoad ) {
				String ufile = file.getName().toUpperCase();
				RDFFormat fmt = RDFFormat.NTRIPLES;
				if ( ufile.endsWith( "RDF" ) ) {
					fmt = RDFFormat.RDFXML;
				}
				else if ( ufile.endsWith( "TTL" ) ) {
					fmt = RDFFormat.TURTLE;
				}
				log.info( "loading " + fmt + " data from " + file );
				rc.begin();
				rc.add( file, "", fmt );
				rc.commit();
			}

			DbUtil.upgradeIfNecessary( rc );
			engine = new RdfDataEngine( rc );
		}
		catch ( RepositoryException | IOException | RDFParseException e ) {
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

	public static class StageRememberer implements EventHandler<WindowEvent> {

		Preferences userPrefs = Preferences.userNodeForPackage( MainApp.class );
		private final Stage mystage;
		private final String prefix;
		private final List<ShutdownListener> listeners = new ArrayList<>();

		public StageRememberer( Stage primaryStage, String prefix ) {
			this.mystage = primaryStage;
			this.prefix = prefix;
		}

		public void addShutdownListener( ShutdownListener r ) {
			listeners.add( r );
		}

		public void removeShutdownListener( ShutdownListener r ) {
			listeners.remove( r );
		}

		public Stage getStage() {
			return mystage;
		}

		public void restore( Stage stage ) {
			double x = userPrefs.getDouble( prefix + ".stage.x", 100 );
			double y = userPrefs.getDouble( prefix + ".stage.y", 100 );
			double w = userPrefs.getDouble( prefix + ".stage.width", 800 );
			double h = userPrefs.getDouble( prefix + ".stage.height", 600 );
			mystage.setX( x );
			mystage.setY( y );
			mystage.setWidth( w );
			mystage.setHeight( h );
		}

		@Override
		public void handle( WindowEvent t ) {
			userPrefs.putDouble( prefix + ".stage.x", mystage.getX() );
			userPrefs.putDouble( prefix + ".stage.y", mystage.getY() );
			userPrefs.putDouble( prefix + ".stage.width", mystage.getWidth() );
			userPrefs.putDouble( prefix + ".stage.height", mystage.getHeight() );

			for ( ShutdownListener p : listeners ) {
				p.shutdown();
			}
		}
	}
}
