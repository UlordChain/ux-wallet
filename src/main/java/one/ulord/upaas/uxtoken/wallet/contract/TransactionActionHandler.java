/**
 * Copyright(c) 2018
 * Ulord core developers
 */
package one.ulord.upaas.uxtoken.wallet.contract;

/**
 * @author haibo
 * @since 7/10/18
 */
public interface TransactionActionHandler {
    void success(String id, String txhash);
    void fail(String id, String message);
}
