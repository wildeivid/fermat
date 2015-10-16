package com.bitdubai.fermat_cry_plugin.layer.crypto_vault.developer.bitdubai.version_1.structure;

import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.events.EventSource;
import com.bitdubai.fermat_api.layer.all_definition.events.interfaces.FermatEvent;
import com.bitdubai.fermat_api.layer.all_definition.exceptions.InvalidParameterException;
import com.bitdubai.fermat_api.layer.all_definition.transaction_transference_protocol.ProtocolStatus;
import com.bitdubai.fermat_api.layer.all_definition.transaction_transference_protocol.crypto_transactions.CryptoStatus;
import com.bitdubai.fermat_api.layer.all_definition.transaction_transference_protocol.crypto_transactions.CryptoTransactionType;
import com.bitdubai.fermat_api.layer.osa_android.database_system.Database;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseFilterType;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTable;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTableRecord;
import com.bitdubai.fermat_api.layer.osa_android.database_system.DatabaseTransaction;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantInsertRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantLoadTableToMemoryException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.CantUpdateRecordException;
import com.bitdubai.fermat_api.layer.osa_android.database_system.exceptions.DatabaseTransactionFailedException;
import com.bitdubai.fermat_cry_api.layer.definition.enums.EventType;
import com.bitdubai.fermat_pip_api.layer.pip_platform_service.event_manager.interfaces.EventManager;
import com.bitdubai.fermat_cry_api.layer.crypto_vault.events.IncomingCryptoOnCryptoNetworkEvent;
import com.bitdubai.fermat_cry_plugin.layer.crypto_vault.developer.bitdubai.version_1.exceptions.CantExecuteQueryException;
import com.bitdubai.fermat_cry_plugin.layer.crypto_vault.developer.bitdubai.version_1.exceptions.UnexpectedResultReturnedFromDatabaseException;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by rodrigo on 2015.06.17..
 * Modified by lnacosta (laion.cj91@gmail.com) on 15/10/2015.
 */
public class CryptoVaultDatabaseActions {

    private final Database     database    ;
    private final EventManager eventManager;

    /**
     * Constructor with final params.
     */
    public CryptoVaultDatabaseActions(final Database     database    ,
                                      final EventManager eventManager){

        this.database     = database    ;
        this.eventManager = eventManager;
    }

    public void saveIncomingTransaction(final UUID txId,
                                        final String txHash) throws CantExecuteQueryException      ,
                                                                    CantLoadTableToMemoryException {
        /**
         * I need to validate that this is not a transaction I already saved because it might be from a transaction
         * generated by our wallet.
         */
        try {

            DatabaseTable cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            cryptoTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME, txHash, DatabaseFilterType.EQUAL);
            cryptoTxTable.loadToMemory();

            if (cryptoTxTable.getRecords().isEmpty()){
                /**
                 * If this is not a transaction that we previously generated, then I will identify it as a new transaction.
                 */
                DatabaseTableRecord incomingTxRecord =  cryptoTxTable.getEmptyRecord();

                incomingTxRecord.setUUIDValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME            , txId                                    );
                incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME        , txHash                                  );
                incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_PROTOCOL_STS_COLUMN_NAME    , ProtocolStatus.TO_BE_NOTIFIED.getCode() );
                incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_STS_COLUMN_NAME , CryptoStatus.ON_CRYPTO_NETWORK.getCode());
                incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_TYPE_COLUMN_NAME, CryptoTransactionType.INCOMING.getCode());

                cryptoTxTable.insertRecord(incomingTxRecord);


                // after I save the transaction in the database and the vault, I'll raise the incoming transaction.
                FermatEvent event = new IncomingCryptoOnCryptoNetworkEvent(EventType.INCOMING_CRYPTO_ON_CRYPTO_NETWORK);
                event.setSource(EventSource.CRYPTO_VAULT);
                eventManager.raiseEvent(event);
            }

        } catch (CantInsertRecordException e) {

            throw new CantExecuteQueryException("Error trying to persist in saveIncomingTransaction method.", e, "Transaction Hash:" + txHash, "Error in database plugin.");
        } catch (CantLoadTableToMemoryException e) {

            throw new CantExecuteQueryException("Error trying to bring data in saveIncomingTransaction method.", e, "Transaction Hash:" + txHash, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * Validates if the transaction ID passed is new or not. This helps to decide If I need to apply the transactions or not
     * @param txId the ID of the transaction
     * @return
     */
    public boolean isNewFermatTransaction(final UUID txId) throws CantExecuteQueryException {

        try {
            DatabaseTable fermatTxTable = database.getTable(CryptoVaultDatabaseConstants.FERMAT_TRANSACTIONS_TABLE_NAME);

            fermatTxTable.setUUIDFilter(CryptoVaultDatabaseConstants.FERMAT_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME, txId, DatabaseFilterType.EQUAL);

                fermatTxTable.loadToMemory();

            /**
             * If I couldnt find any record with this transaction id, then it is a new transactions.
             */
            if (fermatTxTable.getRecords().isEmpty())
                return true;
            else
                return false;

        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {

            throw new CantExecuteQueryException("Error validating transaction in DB.", cantLoadTableToMemory, "Transaction Id:" + txId, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    // returns type of transaction having i count if already exists, if not by default returns incoming
    public CryptoTransactionType calculateTransactionType(final String txHash) throws CantExecuteQueryException {

        try {
            DatabaseTable fermatTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            fermatTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME, txHash, DatabaseFilterType.EQUAL);

            fermatTxTable.loadToMemory();

            /**
             * If I couldnt find any record with this transaction id, then it is a new transactions.
             */
            if (fermatTxTable.getRecords().isEmpty())
                return CryptoTransactionType.INCOMING;
            else
                return CryptoTransactionType.getByCode(fermatTxTable.getRecords().get(0).getStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_TYPE_COLUMN_NAME));

        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {

            throw new CantExecuteQueryException("Error validating transaction in DB.", cantLoadTableToMemory, "Transaction HASH:" + txHash, "Error in database plugin.");
        } catch (InvalidParameterException invalidParameterException) {

            throw new CantExecuteQueryException("Error with transaction type code.", invalidParameterException, "Transaction HASH:" + txHash, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * I will persist a new crypto transaction generated by our wallet.
     */
    public  void persistNewTransaction(final String txId  ,
                                       final String txHash) throws CantExecuteQueryException {

        try {

            DatabaseTable cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            DatabaseTableRecord incomingTxRecord =  cryptoTxTable.getEmptyRecord();

            incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME  , txId  );
            incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME, txHash);

            /**
             * since the wallet generated this transaction, we dont need to inform it.
             */
            incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_PROTOCOL_STS_COLUMN_NAME, ProtocolStatus.NO_ACTION_REQUIRED.getCode());


            /**
             * The transaction was just generated by us, si it will be saved in PENDING_SUBMIT just in case we are not connected to the network.
             * Then the confidence level will be updated if we were able to send it to the network
             */
            incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_STS_COLUMN_NAME, CryptoStatus.ON_CRYPTO_NETWORK.getCode());

            // set the transaction type indicating that is our transaction.
            incomingTxRecord.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_TYPE_COLUMN_NAME, CryptoTransactionType.OUTGOING.getCode());

            cryptoTxTable.insertRecord(incomingTxRecord);

        } catch (CantInsertRecordException e) {

            throw new CantExecuteQueryException("Error persisting in DB.", e, "Transaction Hash:" + txHash, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * Will retrieve all the transactions that are in status pending ProtocolStatus = TO_BE_NOTIFIED
     * @return
     */
    public HashMap<String, String> getPendingTransactionsHeadersByTransactionType(CryptoTransactionType type) throws CantExecuteQueryException {
        /**
         * I need to obtain all the transactions ids with protocol status SENDING_NOTIFIED y TO_BE_NOTIFIED
         */
        try {
            DatabaseTable cryptoTxTable;
            HashMap<String, String> transactionsIds = new HashMap<>();

            cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            /**
             * I get the transaction IDs and Hashes for the TO_BE_NOTIFIED
             */
            cryptoTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_PROTOCOL_STS_COLUMN_NAME    , ProtocolStatus.TO_BE_NOTIFIED.getCode(), DatabaseFilterType.EQUAL);
            cryptoTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_TYPE_COLUMN_NAME, type                         .getCode(), DatabaseFilterType.EQUAL);

            cryptoTxTable.loadToMemory();
             for (DatabaseTableRecord record : cryptoTxTable.getRecords()){
                transactionsIds.put(record.getStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME), record.getStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME));
             }

            return transactionsIds;

        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {

            throw new CantExecuteQueryException("Error executing query in DB.", cantLoadTableToMemory, null, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * Will update the protocol status of the passed transaction.
     * @param txId
     * @param newStatus
     */
    public void updateTransactionProtocolStatus(final UUID           txId     ,
                                                final ProtocolStatus newStatus) throws CantExecuteQueryException                     ,
                                                                                       UnexpectedResultReturnedFromDatabaseException {

        try {
            DatabaseTable cryptoTxTable;
            cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            cryptoTxTable.setUUIDFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME, txId, DatabaseFilterType.EQUAL);

            cryptoTxTable.loadToMemory();

            DatabaseTableRecord toUpdate;

            if (cryptoTxTable.getRecords().size() > 1)
                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", null, "Txid:" + txId+ " Protocol Status:" + newStatus.toString(), "duplicated Transaction Id.");
            else {
                toUpdate = cryptoTxTable.getRecords().get(0);
            }

            /**
             * I set the Protocol status to the new value
             */
            toUpdate.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_PROTOCOL_STS_COLUMN_NAME, newStatus.getCode());
            cryptoTxTable.updateRecord(toUpdate);

        } catch (CantUpdateRecordException cantUpdateRecord) {

            throw new CantExecuteQueryException("Error trying yo update a record in DB.", cantUpdateRecord, "TxId " + txId, "Error in database plugin.");
        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {

            throw new CantExecuteQueryException("Error executing query in DB.", cantLoadTableToMemory, "TxId " + txId, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * Gets from database the current CryptoStatus of a transaction.
     * @param txId
     * @return
     * @throws CantExecuteQueryException
     * @throws UnexpectedResultReturnedFromDatabaseException
     */
    public CryptoStatus getCryptoStatus (String txId) throws CantExecuteQueryException, UnexpectedResultReturnedFromDatabaseException {
        try {
            DatabaseTable cryptoTxTable;
            cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            cryptoTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME, txId, DatabaseFilterType.EQUAL);

            cryptoTxTable.loadToMemory();

            DatabaseTableRecord currentRecord;
            /**
             * I will make sure I only get one result.
             */
            if (cryptoTxTable.getRecords().size() > 1)
                throw new UnexpectedResultReturnedFromDatabaseException("Unexpected result. More than value returned.", null, "TxId:" + txId, "duplicated Transaction Id.");
            else if (cryptoTxTable.getRecords().size() == 0)
                throw new UnexpectedResultReturnedFromDatabaseException("No values returned when trying to get CryptoStatus from transaction in database.", null, "TxId:" + txId, "transaction not yet persisted in database.");
            else
                currentRecord = cryptoTxTable.getRecords().get(0);

            return CryptoStatus.getByCode(currentRecord.getStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_STS_COLUMN_NAME));

        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {

            throw new CantExecuteQueryException("Error executing query in DB.", cantLoadTableToMemory, "TxId " + txId, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * Will search for pending transactions to be notified with the passed crypto:_Status
     * @param cryptoStatus
     * @return
     * @throws CantExecuteQueryException
     */
    public boolean isPendingTransactions(CryptoStatus cryptoStatus) throws CantExecuteQueryException {
        try {
            DatabaseTable cryptoTxTable;
            cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

            cryptoTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_PROTOCOL_STS_COLUMN_NAME   , ProtocolStatus.TO_BE_NOTIFIED.getCode() , DatabaseFilterType.EQUAL);
            cryptoTxTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_STS_COLUMN_NAME, cryptoStatus.getCode()                  , DatabaseFilterType.EQUAL);

            cryptoTxTable.loadToMemory();

            return !cryptoTxTable.getRecords().isEmpty();

        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {
            throw new CantExecuteQueryException("Error executing query in DB.", cantLoadTableToMemory, null, "Error in database plugin.");
        }catch(Exception exception){
            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }


    /**
     * increase by one or resets to zero the counter of transactions found ready to be consumed
     * @param newOcurrence
     * @return the amount of iterations
     * @throws CantExecuteQueryException
     */
    public int updateTransactionProtocolStatus(boolean newOcurrence) throws CantExecuteQueryException {
        try {
            DatabaseTable transactionProtocolStatusTable = database.getTable(CryptoVaultDatabaseConstants.TRANSITION_PROTOCOL_STATUS_TABLE_NAME);

            transactionProtocolStatusTable.loadToMemory();

            List<DatabaseTableRecord> records = transactionProtocolStatusTable.getRecords();

            if (records.isEmpty()) {
                /**
                 * there are no records, I will insert the first one that will be always updated
                 */
                long timestamp = System.currentTimeMillis() / 1000L;
                DatabaseTableRecord emptyRecord = transactionProtocolStatusTable.getEmptyRecord();

                emptyRecord.setLongValue(CryptoVaultDatabaseConstants.TRANSITION_PROTOCOL_STATUS_TABLE_TIMESTAMP_COLUMN_NAME, timestamp);
                emptyRecord.setIntegerValue(CryptoVaultDatabaseConstants.TRANSITION_PROTOCOL_STATUS_TABLE_OCURRENCES_COLUMN_NAME, 0);

                transactionProtocolStatusTable.insertRecord(emptyRecord);

                /**
                 * returns 1
                 */
                return 0;
            }

            DatabaseTableRecord record = records.get(0);

            if (newOcurrence) {
                /**
                 * I need to increase the ocurrences counter by one
                 */
                int ocurrence = record.getIntegerValue(CryptoVaultDatabaseConstants.TRANSITION_PROTOCOL_STATUS_TABLE_OCURRENCES_COLUMN_NAME);
                ocurrence++;
                record.setIntegerValue(CryptoVaultDatabaseConstants.TRANSITION_PROTOCOL_STATUS_TABLE_OCURRENCES_COLUMN_NAME, ocurrence);

                transactionProtocolStatusTable.updateRecord(record);

                return ocurrence;

            } else {
                /**
                 * I need to reset the counter to 0
                 */
                record.setIntegerValue(CryptoVaultDatabaseConstants.TRANSITION_PROTOCOL_STATUS_TABLE_OCURRENCES_COLUMN_NAME, 0);
                transactionProtocolStatusTable.updateRecord(record);

                return 0;

            }
            //
        } catch (CantInsertRecordException e) {

            throw new CantExecuteQueryException("Error trying to insert in DB.", e, null, "Error in database plugin.");
        } catch (CantUpdateRecordException e) {

            throw new CantExecuteQueryException("Error trying to update in DB.", e, null, "Error in database plugin.");
        } catch (CantLoadTableToMemoryException cantLoadTableToMemory) {

            throw new CantExecuteQueryException("Error executing query in DB.", cantLoadTableToMemory, null, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    /**
     * Insert a new Fermat transaction in the database
     * @param txId
     * @throws CantExecuteQueryException
     */
    public void persistnewFermatTransaction(final String txId) throws CantExecuteQueryException {

        try {
            DatabaseTable fermatTable;
            fermatTable = database.getTable(CryptoVaultDatabaseConstants.FERMAT_TRANSACTIONS_TABLE_NAME);

            DatabaseTableRecord insert = fermatTable.getEmptyRecord();

            insert.setStringValue(CryptoVaultDatabaseConstants.FERMAT_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME, txId);

            fermatTable.insertRecord(insert);

        } catch (CantInsertRecordException e) {

            throw new CantExecuteQueryException("Error executing query in DB.", e, "TxId: " + txId, "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }

    public CryptoStatus getLastCryptoStatus(final String txHash) throws CantLoadTableToMemoryException, InvalidParameterException {

        DatabaseTable cryptoTransactionsTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);

        cryptoTransactionsTable.setStringFilter(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME, txHash, DatabaseFilterType.EQUAL);
        cryptoTransactionsTable.loadToMemory();

        List<DatabaseTableRecord> databaseTableRecordList = cryptoTransactionsTable.getRecords();
        if (databaseTableRecordList.isEmpty()) {
            return null;
        } else {
            CryptoStatus lastCryptoStatus = null;
            for (DatabaseTableRecord record : databaseTableRecordList) {
                CryptoStatus cryptoStatus = CryptoStatus.getByCode(record.getStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_STS_COLUMN_NAME));
                if (lastCryptoStatus == null)
                    lastCryptoStatus = cryptoStatus;
                else if (lastCryptoStatus.getOrder() < cryptoStatus.getOrder())
                    lastCryptoStatus = cryptoStatus;
            }
            return lastCryptoStatus;
        }
    }

    /**
     * Insert a new transaction with the confidence level or update the cryptoStatus of an existing transaction.
     */
    public void insertNewTransactionWithNewConfidence(final String                hashAsString,
                                                      final CryptoStatus          cryptoStatus,
                                                      final CryptoTransactionType type        ) throws CantExecuteQueryException {


        try {

            DatabaseTable cryptoTxTable = database.getTable(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_NAME);
            DatabaseTableRecord record = cryptoTxTable.getEmptyRecord();

            // new values to assign
            UUID           newFermatId    = UUID.randomUUID()            ;
            ProtocolStatus protocolStatus = ProtocolStatus.TO_BE_NOTIFIED;

            record.setUUIDValue  (CryptoVaultDatabaseConstants.FERMAT_TRANSACTIONS_TABLE_TRX_ID_COLUMN_NAME          , newFermatId             );
            record.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRX_HASH_COLUMN_NAME        , hashAsString            );
            record.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_PROTOCOL_STS_COLUMN_NAME    , protocolStatus.getCode());
            record.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_STS_COLUMN_NAME , cryptoStatus  .getCode());
            record.setStringValue(CryptoVaultDatabaseConstants.CRYPTO_TRANSACTIONS_TABLE_TRANSACTION_TYPE_COLUMN_NAME, type          .getCode());

            DatabaseTransaction dbTran = database.newTransaction();
            dbTran.addRecordToInsert(cryptoTxTable, record);

            database.executeTransaction(dbTran);

        } catch (DatabaseTransactionFailedException e) {

            throw new CantExecuteQueryException("Error inserting new transaction because of transaction confidence changed.", e, "Transaction Hash:" + hashAsString + " CryptoStatus:" + cryptoStatus.toString(), "Error in database plugin.");
        } catch(Exception exception){

            throw new CantExecuteQueryException(CantExecuteQueryException.DEFAULT_MESSAGE, FermatException.wrapException(exception), null, null);
        }
    }
}
