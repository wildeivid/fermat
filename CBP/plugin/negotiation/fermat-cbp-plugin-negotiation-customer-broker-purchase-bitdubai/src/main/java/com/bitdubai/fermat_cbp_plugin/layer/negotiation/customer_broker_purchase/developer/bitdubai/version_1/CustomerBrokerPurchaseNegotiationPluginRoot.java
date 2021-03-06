package com.bitdubai.fermat_cbp_plugin.layer.negotiation.customer_broker_purchase.developer.bitdubai.version_1;

import com.bitdubai.fermat_api.CantStartPluginException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.abstract_classes.AbstractPlugin;
import com.bitdubai.fermat_api.layer.all_definition.common.system.annotations.NeededAddonReference;
import com.bitdubai.fermat_api.layer.all_definition.common.system.utils.PluginVersionReference;
import com.bitdubai.fermat_api.layer.all_definition.developer.DatabaseManagerForDevelopers;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabase;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTable;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperDatabaseTableRecord;
import com.bitdubai.fermat_api.layer.all_definition.developer.DeveloperObjectFactory;
import com.bitdubai.fermat_api.layer.all_definition.enums.Addons;
import com.bitdubai.fermat_api.layer.all_definition.enums.Layers;
import com.bitdubai.fermat_api.layer.all_definition.enums.Platforms;
import com.bitdubai.fermat_api.layer.all_definition.enums.ServiceStatus;
import com.bitdubai.fermat_api.layer.all_definition.util.Version;
import com.bitdubai.fermat_api.layer.osa_android.database_system.PluginDatabaseSystem;
import com.bitdubai.fermat_cbp_api.all_definition.enums.ClauseStatus;
import com.bitdubai.fermat_cbp_api.all_definition.enums.ClauseType;
import com.bitdubai.fermat_cbp_api.all_definition.enums.CurrencyType;
import com.bitdubai.fermat_cbp_api.all_definition.enums.NegotiationStatus;
import com.bitdubai.fermat_cbp_api.all_definition.negotiation.Clause;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.exceptions.CantCreateCustomerBrokerPurchaseNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.exceptions.CantGetListPurchaseNegotiationsException;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.exceptions.CantUpdateCustomerBrokerPurchaseNegotiationException;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.interfaces.CustomerBrokerPurchaseNegotiation;
import com.bitdubai.fermat_cbp_api.layer.negotiation.customer_broker_purchase.interfaces.CustomerBrokerPurchaseNegotiationManager;
import com.bitdubai.fermat_cbp_api.layer.negotiation.exceptions.CantGetListClauseException;
import com.bitdubai.fermat_cbp_api.layer.negotiation.exceptions.CantGetNextClauseTypeException;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation.customer_broker_purchase.developer.bitdubai.version_1.database.CustomerBrokerPurchaseNegotiationDao;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation.customer_broker_purchase.developer.bitdubai.version_1.database.CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory;
import com.bitdubai.fermat_cbp_plugin.layer.negotiation.customer_broker_purchase.developer.bitdubai.version_1.exceptions.CantInitializeCustomerBrokerPurchaseNegotiationDatabaseException;
import com.bitdubai.fermat_pip_api.layer.user.device_user.interfaces.DeviceUserManager;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.interfaces.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.enums.UnexpectedPluginExceptionSeverity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jorge on 12-10-2015.
 * Update by Angel on 30/11/2015
 */

public class CustomerBrokerPurchaseNegotiationPluginRoot extends AbstractPlugin implements CustomerBrokerPurchaseNegotiationManager, DatabaseManagerForDevelopers {

    @NeededAddonReference(platform = Platforms.PLUG_INS_PLATFORM, layer = Layers.PLATFORM_SERVICE, addon = Addons.ERROR_MANAGER)
    private ErrorManager errorManager;

    @NeededAddonReference(platform = Platforms.PLUG_INS_PLATFORM, layer = Layers.USER, addon = Addons.DEVICE_USER)
    private DeviceUserManager deviceUserManager;

    @NeededAddonReference(platform = Platforms.OPERATIVE_SYSTEM_API, layer = Layers.SYSTEM, addon = Addons.PLUGIN_DATABASE_SYSTEM)
    private PluginDatabaseSystem pluginDatabaseSystem;

    private CustomerBrokerPurchaseNegotiationDao customerBrokerPurchaseNegotiationDao;

    /*
       Builder
    */

        public CustomerBrokerPurchaseNegotiationPluginRoot() {
            super(new PluginVersionReference(new Version()));
        }

    /*
        Plugin Interface implementation.
    */

        @Override
        public void start() throws CantStartPluginException {
            this.serviceStatus = ServiceStatus.STARTED;
            try {
                this.customerBrokerPurchaseNegotiationDao = new CustomerBrokerPurchaseNegotiationDao(pluginDatabaseSystem, pluginId);
                this.customerBrokerPurchaseNegotiationDao.initializeDatabase();
            } catch (CantInitializeCustomerBrokerPurchaseNegotiationDatabaseException e) {
                errorManager.reportUnexpectedPluginException(this.getPluginVersionReference(), UnexpectedPluginExceptionSeverity.DISABLES_THIS_PLUGIN, e);
                throw new CantStartPluginException();
            }
        }

    /*
        DatabaseManagerForDevelopers Interface implementation.
    */

        @Override
        public List<DeveloperDatabase> getDatabaseList(DeveloperObjectFactory developerObjectFactory) {
            CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory dbFactory = new CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory(this.pluginDatabaseSystem, this.pluginId);
            return dbFactory.getDatabaseList(developerObjectFactory);
        }

        @Override
        public List<DeveloperDatabaseTable> getDatabaseTableList(DeveloperObjectFactory developerObjectFactory, DeveloperDatabase developerDatabase) {
            CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory dbFactory = new CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory(this.pluginDatabaseSystem, this.pluginId);
            return dbFactory.getDatabaseTableList(developerObjectFactory);
        }

        @Override
        public List<DeveloperDatabaseTableRecord> getDatabaseTableContent(DeveloperObjectFactory developerObjectFactory, DeveloperDatabase developerDatabase, DeveloperDatabaseTable developerDatabaseTable) {
            try {
                CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory dbFactory = new CustomerBrokerPurchaseNegotiationDeveloperDatabaseFactory(this.pluginDatabaseSystem, this.pluginId);
                dbFactory.initializeDatabase();
                return dbFactory.getDatabaseTableContent(developerObjectFactory, developerDatabaseTable);
            } catch (CantInitializeCustomerBrokerPurchaseNegotiationDatabaseException e) {
                this.errorManager.reportUnexpectedPluginException(this.getPluginVersionReference(), UnexpectedPluginExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_PLUGIN, e);
            }
            return new ArrayList<>();
        }

    /*
        CustomerBrokerPurchaseManager Interface implementation.
    */

        @Override
        public void createCustomerBrokerPurchaseNegotiation(CustomerBrokerPurchaseNegotiation negotiation) throws CantCreateCustomerBrokerPurchaseNegotiationException {
            this.customerBrokerPurchaseNegotiationDao.createCustomerBrokerPurchaseNegotiation(negotiation);
        }

        @Override
        public void updateCustomerBrokerPurchaseNegotiation(CustomerBrokerPurchaseNegotiation negotiation) throws CantUpdateCustomerBrokerPurchaseNegotiationException {
            this.customerBrokerPurchaseNegotiationDao.updateCustomerBrokerPurchaseNegotiation(negotiation);
        }

        @Override
        public void cancelNegotiation(CustomerBrokerPurchaseNegotiation negotiation) throws CantUpdateCustomerBrokerPurchaseNegotiationException {
            this.customerBrokerPurchaseNegotiationDao.cancelNegotiation(negotiation);
        }

        @Override
        public boolean closeNegotiation(CustomerBrokerPurchaseNegotiation negotiation) throws CantUpdateCustomerBrokerPurchaseNegotiationException {
            try {
                if(verifyStatusClause(negotiation.getClauses())) {
                    this.customerBrokerPurchaseNegotiationDao.closeNegotiation(negotiation);
                    return true;
                }
                return false;
            } catch (CantGetListClauseException e) {
                throw new CantUpdateCustomerBrokerPurchaseNegotiationException(CantUpdateCustomerBrokerPurchaseNegotiationException.DEFAULT_MESSAGE, e, "", "");
            }
        }

        @Override
        public void sendToBroker(CustomerBrokerPurchaseNegotiation negotiation) throws CantUpdateCustomerBrokerPurchaseNegotiationException {
            this.customerBrokerPurchaseNegotiationDao.sendToBroker(negotiation);
        }

        @Override
        public void waitForBroker(CustomerBrokerPurchaseNegotiation negotiation) throws CantUpdateCustomerBrokerPurchaseNegotiationException {
            this.customerBrokerPurchaseNegotiationDao.waitForBroker(negotiation);
        }

        @Override
        public Collection<CustomerBrokerPurchaseNegotiation> getNegotiations() throws CantGetListPurchaseNegotiationsException {
            return this.customerBrokerPurchaseNegotiationDao.getNegotiations();
        }

        @Override
        public Collection<CustomerBrokerPurchaseNegotiation> getNegotiationsByContractId(UUID negotiationId) throws CantGetListPurchaseNegotiationsException {
            return this.customerBrokerPurchaseNegotiationDao.getNegotiationsByContractId(negotiationId);
        }

        @Override
        public Collection<CustomerBrokerPurchaseNegotiation> getNegotiationsByStatus(NegotiationStatus status) throws CantGetListPurchaseNegotiationsException {
            return this.customerBrokerPurchaseNegotiationDao.getNegotiations(status);
        }

        @Override
        public ClauseType getNextClauseType(ClauseType type) throws CantGetNextClauseTypeException {
            switch (type) {
                case CUSTOMER_CURRENCY:
                    return ClauseType.EXCHANGE_RATE;

                case EXCHANGE_RATE:
                    return ClauseType.CUSTOMER_CURRENCY_QUANTITY;

                case CUSTOMER_CURRENCY_QUANTITY:
                    return ClauseType.CUSTOMER_PAYMENT_METHOD;

                case CUSTOMER_BANK:
                    return ClauseType.CUSTOMER_BANK_ACCOUNT;

                case PLACE_TO_MEET:
                    return ClauseType.DATE_TIME_TO_MEET;

                case CUSTOMER_PLACE_TO_DELIVER:
                    return ClauseType.CUSTOMER_DATE_TIME_TO_DELIVER;

                default:
                    throw new CantGetNextClauseTypeException(CantGetNextClauseTypeException.DEFAULT_MESSAGE);
            }
        }

        @Override
        public ClauseType getNextClauseTypeByCurrencyType(CurrencyType paymentMethod) throws CantGetNextClauseTypeException {
            switch (paymentMethod) {
                case CRYPTO_MONEY:
                    return ClauseType.CUSTOMER_CRYPTO_ADDRESS;

                case BANK_MONEY:
                    return ClauseType.CUSTOMER_BANK;

                case CASH_ON_HAND_MONEY:
                    return ClauseType.PLACE_TO_MEET;

                case CASH_DELIVERY_MONEY:
                    return ClauseType.CUSTOMER_PLACE_TO_DELIVER;

                default:
                    throw new CantGetNextClauseTypeException(CantGetNextClauseTypeException.DEFAULT_MESSAGE);
            }
        }

    /*
    *   Private Methods
    * */

        private boolean verifyStatusClause(Collection<Clause> clausules) throws CantUpdateCustomerBrokerPurchaseNegotiationException {
            Map<ClauseType, String> clausesAgreed = new HashMap<ClauseType, String>();

            for(Clause clause : clausules) {
                if(clause.getStatus() == ClauseStatus.AGREED){
                    clausesAgreed.put(clause.getType(), clause.getValue());
                }
            }

            if(
                    ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_CURRENCY) ) &&
                            ( !clausesAgreed.containsKey(ClauseType.EXCHANGE_RATE) ) &&
                            ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_CURRENCY_QUANTITY) ) &&
                            ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_PAYMENT_METHOD) )
                    ){
                return false;
            }

            if( clausesAgreed.containsValue(CurrencyType.CRYPTO_MONEY.getCode()) ){
                if( !clausesAgreed.containsKey(ClauseType.CUSTOMER_CRYPTO_ADDRESS) ){
                    return false;
                }
            }

            if( clausesAgreed.containsValue(CurrencyType.BANK_MONEY.getCode()) ){
                if(
                        ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_BANK) ) &&
                                ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_BANK_ACCOUNT) )
                        ){
                    return false;
                }
            }

            if( clausesAgreed.containsValue(CurrencyType.CASH_ON_HAND_MONEY.getCode()) ){
                if(
                        ( !clausesAgreed.containsKey(ClauseType.PLACE_TO_MEET) ) &&
                                ( !clausesAgreed.containsKey(ClauseType.DATE_TIME_TO_MEET) )
                        ){
                    return false;
                }
            }

            if( clausesAgreed.containsValue(CurrencyType.CASH_DELIVERY_MONEY.getCode()) ){
                if(
                        ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_PLACE_TO_DELIVER) ) &&
                                ( !clausesAgreed.containsKey(ClauseType.CUSTOMER_DATE_TIME_TO_DELIVER) )
                        ){
                    return false;
                }
            }

            return true;
        }
}
