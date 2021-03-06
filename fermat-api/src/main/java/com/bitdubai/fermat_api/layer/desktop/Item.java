package com.bitdubai.fermat_api.layer.desktop;

import com.bitdubai.fermat_api.layer.dmp_module.wallet_manager.InstalledWallet;
import com.bitdubai.fermat_api.layer.interface_objects.FermatInterfaceObject;
import com.bitdubai.fermat_api.layer.interface_objects.InterfaceType;

import java.util.List;

/**
 * Created by Matas Furszyfer on 2015.11.01..
 */
public class Item<I extends FermatInterfaceObject> implements FermatInterfaceObject {

    I object;
    public boolean selected=false;


    public Item(I object) {
        this.object = object;
    }

    @Override
    public InterfaceType getType() {
        return object.getType();
    }

    @Override
    public String getName() {
        return object.getName();
    }

    @Override
    public String getIcon() {
        return object.getIcon();
    }

    @Override
    public void setIconResource(int bitcoin_wallet) {
        object.setIconResource(bitcoin_wallet);
    }

    @Override
    public int getIconResource() {
        return object.getIconResource();
    }

    @Override
    public int getPosition() {
        return object.getPosition();
    }

    @Override
    public void setPosition(int position) {
        object.setPosition(position);
    }

    public I getInterfaceObject() {
        return object;
    }
}
