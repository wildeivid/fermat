package com.bitdubai.fermat_android_api.layer.definition.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.bitdubai.fermat_android_api.engine.PaintActivityFeatures;
import com.bitdubai.fermat_android_api.layer.definition.wallet.interfaces.SubAppsSession;
import com.bitdubai.fermat_android_api.layer.definition.wallet.interfaces.WizardConfiguration;
import com.bitdubai.fermat_android_api.ui.adapters.FermatAdapter;
import com.bitdubai.fermat_android_api.ui.inflater.ViewInflater;
import com.bitdubai.fermat_android_api.ui.interfaces.FermatFragments;
import com.bitdubai.fermat_api.layer.all_definition.enums.Engine;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.interfaces.FermatScreenSwapper;
import com.bitdubai.fermat_api.layer.dmp_module.wallet_manager.InstalledSubApp;
import com.bitdubai.fermat_api.layer.dmp_module.wallet_manager.InstalledWallet;
import com.bitdubai.fermat_wpd_api.layer.wpd_middleware.wallet_settings.interfaces.SubAppSettings;
import com.bitdubai.fermat_pip_api.layer.network_service.subapp_resources.SubAppResourcesProviderManager;

/**
 * Common Android Fragment Base
 *
 * @author Matias Furszy
 * @author Francisco Vasquez
 * @version 1.1
 */
public abstract class FermatFragment extends AbstractFermatFragment implements FermatFragments {

    /**
     * FLAGS
     */
    protected boolean isAttached;

    /**
     * Platform
     */
    protected SubAppsSession subAppsSession;
    protected SubAppSettings subAppSettings;
    protected SubAppResourcesProviderManager subAppResourcesProviderManager;

    /**
     * ViewInflater
     */
    protected ViewInflater viewInflater;

    /**
     * REFERENCES
     */
    protected WizardConfiguration context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = (WizardConfiguration) getActivity();
            viewInflater = new ViewInflater(getActivity(), subAppResourcesProviderManager);
        } catch (Exception ex) {
            throw new ClassCastException("cannot convert the current context to WizardConfiguration");
        }
    }

    /**
     * Start a configuration Wizard
     *
     * @param key  Enum Wizard registered type
     * @param args Object[] where you're be able to passing arguments like session, settings, resources, module, etc...
     */
    protected void startWizard(String key, Object... args) {
        if (context != null && isAttached) {
            context.showWizard(key, args);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        isAttached = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isAttached = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    /**
     * Setting up SubApp Session
     *
     * @param subAppsSession SubAppSession object
     */
    public void setSubAppsSession(SubAppsSession subAppsSession) {
        this.subAppsSession = subAppsSession;
    }

    /**
     * Setting up SubAppSettings
     *
     * @param subAppSettings SubAppSettings object
     */
    public void setSubAppSettings(SubAppSettings subAppSettings) {
        this.subAppSettings = subAppSettings;
    }

    /**
     * Setting up SubAppResourcesProviderManager
     *
     * @param subAppResourcesProviderManager SubAppResourcesProviderManager object
     */
    public void setSubAppResourcesProviderManager(SubAppResourcesProviderManager subAppResourcesProviderManager) {
        this.subAppResourcesProviderManager = subAppResourcesProviderManager;
    }

    /**
     * Change activity
     */
    protected final void changeActivity(String activityCode,String appPublicKey, Object... objectses) {
        ((FermatScreenSwapper) getActivity()).changeActivity(activityCode, appPublicKey,objectses);
    }
    /**
     * Change activity
     */
    @Deprecated
    protected final void changeActivity(String activityCode, Object... objectses) {
        ((FermatScreenSwapper) getActivity()).changeActivity(activityCode, null);
    }

    /**
     * Change activity
     */
    protected final void changeFragment(String fragment,int idContainer) {
        ((FermatScreenSwapper) getActivity()).changeScreen(fragment, idContainer, null);
    }

    protected final RelativeLayout getToolbarHeader() {
        return getPaintActivtyFeactures().getToolbarHeader();
    }

    protected PaintActivityFeatures getPaintActivtyFeactures(){
        return ((PaintActivityFeatures)getActivity());
    }

    protected void setNavigationDrawer(FermatAdapter adapter){
        getPaintActivtyFeactures().changeNavigationDrawerAdapter(adapter);
    }


    protected void addNavigationHeader(View view){
        getPaintActivtyFeactures().addNavigationViewHeader(view);
    }

    protected void changeApp(Engine emgine,Object[] objects){
        //getFermatScreenSwapper().connectWithOtherApp(emgine, objects);
    }

    protected void selectSubApp(InstalledSubApp installedSubApp){
        getFermatScreenSwapper().selectSubApp(installedSubApp);
    }

    protected void selectWallet(InstalledWallet installedWallet){
        getFermatScreenSwapper().selectWallet(installedWallet);
    }

    protected FermatScreenSwapper getFermatScreenSwapper(){
        return (FermatScreenSwapper) getActivity();
    }



}

