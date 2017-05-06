package org.pentaho.di.core.hadoop.database;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.namedcluster.NamedClusterManager;
import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.namedcluster.NamedClusterDialog;
import org.pentaho.di.ui.core.namedcluster.NamedClusterUIHelper;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.ui.database.Messages;
import org.pentaho.ui.database.event.FragmentHandler;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

import java.io.InputStream;
import java.util.stream.Collectors;

/**
 * Created by dstepanov on 06/05/17.
 */
public class HadoopFragmentHandler extends FragmentHandler {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!

  private NamedClusterDialog namedClusterDialog;

  private String hadoopPackagePath = "org/pentaho/di/ui/database/";

  public HadoopFragmentHandler() {
  }

  @Override public String getName() {
    return "fragmentHandler";
  }

  @Override
  protected String getFragment( DatabaseInterface database, String dbName, String extension, String defaultFragment ) {
    String fragment;
    String ext = ( extension == null ? "" : extension );
    String databaseName = ( dbName == null ? "" : dbName );
    if ( database.getXulOverlayFile() != null ) {
      fragment = hadoopPackagePath.concat( database.getXulOverlayFile() ).concat( ext );
    } else {
      fragment = hadoopPackagePath.concat( databaseName ).concat( ext );
    }
    InputStream in = getClass().getClassLoader().getResourceAsStream( fragment.toLowerCase() );
    if ( in == null ) {
      return super.getFragment( database, dbName, extension, defaultFragment );
    }
    return fragment;
  }

  @Override protected void loadDatabaseOptionsFragment( String fragmentUri ) throws XulException {
    if ( fragmentUri.contains( hadoopPackagePath ) ) {
      XulComponent groupElement = document.getElementById( "database-options-box" );
      XulComponent parentElement = groupElement.getParent();

      XulDomContainer fragmentContainer;

      try {

        // Get new group box fragment ...
        // This will effectively set up the SWT parent child relationship...
        fragmentContainer = this.xulDomContainer.loadFragment( fragmentUri, Messages.getBundle() );
        XulMenuList namedClustersList =
          (XulMenuList) fragmentContainer.getDocumentRoot().getElementById( "namedcluster-menu-list" );
        fulfillHadoopCluster( namedClustersList );
        XulComponent newGroup = fragmentContainer.getDocumentRoot().getFirstChild();
        parentElement.replaceChild( groupElement, newGroup );

      } catch ( XulException e ) {
        e.printStackTrace();
        throw e;
      }
    } else {
      super.loadDatabaseOptionsFragment( fragmentUri );
    }

  }

  private void fulfillHadoopCluster( XulMenuList namedClustersList ) {
    if ( namedClustersList != null ) {
      namedClustersList.setElements( NamedClusterUIHelper.getNamedClusters().stream()
        .map( NamedCluster::getName )
        .collect( Collectors.toList() ) );
    }
  }

  private NamedClusterDialog getNamedClusterDialog() {
    if ( namedClusterDialog == null ) {
      namedClusterDialog = NamedClusterUIHelper.getNamedClusterUIFactory()
        .createNamedClusterDialog( ( (SwtDialog) document.getElementById( "general-datasource-window" ) ).getShell() );
    }
    return namedClusterDialog;
  }

  public void editNamedCluster() {
    try {
      XulMenuList namedClustersList = (XulMenuList) document.getElementById( "namedcluster-menu-list" );
      String namedClusterName = namedClustersList.getSelectedItem();

      if ( namedClusterName != null && !namedClusterName.isEmpty() ) {
        // Grab the first item in the list & send it to the database dialog
        NamedCluster original =
          NamedClusterManager.getInstance().read( namedClusterName, Spoon.getInstance().getMetaStore() );
        NamedCluster namedCluster = original.clone();

        // Make sure this NamedCluster already exists and store its id for updating
        if ( NamedClusterManager.getInstance().read( namedClusterName, Spoon.getInstance().getMetaStore() )
          == null ) {
          MessageBox mb =
            new MessageBox( ( (SwtDialog) document.getElementById( "general-datasource-window" ) ).getShell(),
              SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedCluster.Edit.DoesNotExists.Message" ) );
          mb
            .setText( BaseMessages.getString(
              PKG, "RepositoryExplorerDialog.NamedCluster.Edit.DoesNotExists.Title" ) );
          mb.open();
        } else {
          getNamedClusterDialog().setNamedCluster( namedCluster );
          getNamedClusterDialog().setNewClusterCheck( false );
          namedClusterName = getNamedClusterDialog().open();
          if ( namedClusterName != null && !namedClusterName.equals( "" ) ) {
            // delete original
            NamedClusterManager.getInstance().delete( original.getName(), Spoon.getInstance().getMetaStore() );
            NamedClusterManager.getInstance().create( namedCluster, Spoon.getInstance().getMetaStore() );
          }
        }
      } else {
        MessageBox mb =
          new MessageBox( ( (SwtDialog) document.getElementById( "general-datasource-window" ) ).getShell(),
            SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.NamedCluster.Edit.NoItemSelected.Message" ) );
        mb
          .setText( BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.NamedCluster.Edit.NoItemSelected.Title" ) );
        mb.open();
      }
    } catch ( Exception e ) {
      new ErrorDialog( ( (SwtDialog) document.getElementById( "general-datasource-window" ) ).getShell(),
        BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Edit.UnexpectedError.Title" ),
        BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Edit.UnexpectedError.Message" ), e );

    } finally {
      XulMenuList namedClustersList =
        (XulMenuList) document.getElementById( "namedcluster-menu-list" );
      fulfillHadoopCluster( namedClustersList );
    }
  }

  public void createNamedCluster() {
    try {
      // user will have to select from list of templates
      // for now hard code to hadoop-cluster
      NamedCluster namedCluterTemplate = NamedClusterManager.getInstance().getClusterTemplate();
      namedCluterTemplate.initializeVariablesFrom( null );
      getNamedClusterDialog().setNamedCluster( namedCluterTemplate );
      getNamedClusterDialog().setNewClusterCheck( true );

      String namedClusterName = getNamedClusterDialog().open();
      if ( namedClusterName != null && !namedClusterName.equals( "" ) ) {
        // See if this named cluster exists...
        NamedCluster namedCluster =
          NamedClusterManager.getInstance().read( namedClusterName, Spoon.getInstance().getMetaStore() );
        if ( namedCluster == null ) {
          NamedClusterManager.getInstance()
            .create( getNamedClusterDialog().getNamedCluster(), Spoon.getInstance().getMetaStore() );
        } else {
          MessageBox mb =
            new MessageBox( ( (SwtDialog) document.getElementById( "general-datasource-window" ) ).getShell(),
              SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedCluster.Create.AlreadyExists.Message" ) );
          mb.setText( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedCluster.Create.AlreadyExists.Title" ) );
          mb.open();
        }
      }
    } catch ( MetaStoreException e ) {
      new ErrorDialog( ( (SwtDialog) document.getElementById( "general-datasource-window" ) ).getShell(),
        BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Create.UnexpectedError.Title" ),
        BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Create.UnexpectedError.Message" ), e );
    } finally {
      XulMenuList namedClustersList =
        (XulMenuList) document.getElementById( "namedcluster-menu-list" );
      fulfillHadoopCluster( namedClustersList );
    }
  }
}
