/**
 * Copyright(c) 2018
 * Ulord core developers
 */
package one.ulord.upaas.uxtoken.wallet.contract;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.ulord.upaas.uxtoken.wallet.contract.generated.MulTransfer;
import one.ulord.upaas.uxtoken.wallet.contract.generated.RewardPoolTokens;
import one.ulord.upaas.uxtoken.wallet.contract.generated.TeamDevTokens;
import one.ulord.upaas.uxtoken.wallet.contract.generated.UshareToken;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author haibo
 * @since 7/10/18
 */
public class UXWallet {
    private static Logger logger = LogManager.getLogger(UXWallet.class);
    public static BigInteger BLOCK_GAS_LIMIT = BigInteger.valueOf(6500000);
    /**
     * Min gas price
     */
    public static BigInteger GAS_PRICE = BigInteger.valueOf(200000000L); //0.2GWei

    private String uxTokenAddress = "";
    private String teamDevTokens = "";
    private String rewardPoolAddress = "";
    private String uxCandyAddress = "";

    // test-net address

    private String keystoreFile;
    private String mainAddress;

    private UshareToken uxCoin;
    private TeamDevTokens teamTokensHolder;
    private RewardPoolTokens rewardPoolTokens;
    private MulTransfer mulTransfer;

    private Transfer transfer;
    FastRawTransactionManager transactionManager;


    private TransactionActionHandler handler;

    Credentials credentials;

    private Web3j web3j;

    public UXWallet(String ethereumProvider, String keystoreFile, String keystorePassword,
                    String uxTokenAddress, String teamDevTokens,
                    String rewardPoolAddress, String uxCandyAddress,
                    TransactionActionHandler handler) throws IOException, CipherException {
        File file = new File(this.keystoreFile);
        if (!file.exists()){
            // try to get file from classpath
            String resourcePath = UXWallet.class.getClassLoader().getResource("").toString();
            int typeSplitePos = resourcePath.indexOf(":");
            if (typeSplitePos > 0){
                resourcePath = resourcePath.substring(typeSplitePos+1);
            }

            file = new File(resourcePath + this.keystoreFile);
            if (!file.exists()){
                throw new IOException("Cannot found keystore file.");
            }
        }

        init(ethereumProvider, file, keystorePassword,
                uxTokenAddress, teamDevTokens, rewardPoolAddress, uxCandyAddress);
    }
    public UXWallet(String ethereumProvider, File keystoreFile, String keystorePassword,
                    String uxTokenAddress, String teamDevTokens,
                    String rewardPoolAddress, String uxCandyAddress,
                    TransactionActionHandler handler) throws IOException, CipherException {
        this.handler = handler;
        init(ethereumProvider, keystoreFile, keystorePassword,
                uxTokenAddress, teamDevTokens, rewardPoolAddress, uxCandyAddress);
    }

    private void init(String ethereumProvider, File keystoreFile, String keystorePassword,
                      String uxTokenAddress, String teamDevTokens, String rewardPoolAddress, String uxCandyAddress)
            throws IOException, CipherException {
        this.web3j = Web3j.build(new HttpService(ethereumProvider));
        Web3ClientVersion web3ClientVersion = null;
        web3ClientVersion = web3j.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        if (clientVersion == null){
            throw new IOException("Ulord provider cannot connect.");
        }

        this.credentials = WalletUtils.loadCredentials(keystorePassword, keystoreFile);
        this.mainAddress = credentials.getAddress();

        // we need using fast transaction manager
        transactionManager = new FastRawTransactionManager(web3j, credentials);
        transfer = new Transfer(web3j, transactionManager);


        this.uxTokenAddress = uxTokenAddress;
        this.teamDevTokens = teamDevTokens;
        this.rewardPoolAddress = rewardPoolAddress;
        this.uxCandyAddress = uxCandyAddress;

        // load contract object
        this.uxCoin = UshareToken.load(uxTokenAddress, web3j, transactionManager,
                DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT);
        this.teamTokensHolder = TeamDevTokens.load(teamDevTokens, web3j, transactionManager,
                DefaultGasProvider.GAS_PRICE, DefaultGasProvider.GAS_LIMIT);
        this.rewardPoolTokens = RewardPoolTokens.load(rewardPoolAddress, web3j, transactionManager,
                DefaultGasProvider.GAS_PRICE, BLOCK_GAS_LIMIT); // Using block max limit
        this.mulTransfer = MulTransfer.load(uxCandyAddress, web3j, transactionManager,
                DefaultGasProvider.GAS_PRICE, BLOCK_GAS_LIMIT);
    }

    public String getCandyAddress(){
        return this.uxCandyAddress;
    }
    public String getUShareTokenAddress(){return this.uxTokenAddress;}
    public String getRewardPoolAddress(){return this.rewardPoolAddress;}
    public String getTeamTokensHolderAddress(){return this.teamDevTokens;}

    public String getMainAddress(){
        return this.mainAddress;
    }

    public BigDecimal getEthBalance() throws IOException {
        return getEthBalance(this.mainAddress);
    }

    public BigDecimal getEthBalance(String address) throws IOException {
        BigInteger balance = web3j.ethGetBalance(address,
                DefaultBlockParameterName.LATEST).send().getBalance();
        return Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
    }

    /**
     * Get ulord side chain token balance
     * @return token balance (Unit UX)
     * @throws Exception
     */
    public BigDecimal getTokenBalance() throws Exception {
        return getTokenBalance(this.mainAddress);
    }

    public BigDecimal getTokenBalance(String address) throws Exception {
        BigInteger value = uxCoin.balanceOf(address).send();
        return new BigDecimal(value).divide(BigDecimal.valueOf(10).pow(18));
    }

    /**
     * Transfer amount of gas from main address to specified address
     * @param reqId request id
     * @param toAddress target address
     * @param value gas value
     * @throws IOException IOException while send a RPC call
     */
    public void transferEther(final String reqId, String toAddress, BigInteger value) {
        logger.info("Transfer Ether to:{}, {}", toAddress, value.toString());
        // transfer using fast transaction manager
        transfer.sendFunds(toAddress, new BigDecimal(value), Convert.Unit.WEI, GAS_PRICE, DefaultGasProvider.GAS_LIMIT)
                .sendAsync().whenCompleteAsync((receipt, e)->{
            if (e == null && receipt != null){
                processTransactionReceipt(reqId, receipt);
            }else{
                processTransactionException(reqId, e);
            }
        });
//        web3j.ethGetTransactionCount(this.mainAddress, DefaultBlockParameterName.LATEST)
//                .sendAsync().whenCompleteAsync((txCount, e1)->
//        {
//            if (e1 == null){
//                RawTransaction rawtx = RawTransaction.createEtherTransaction(txCount.getTransactionCount(),
//                        GAS_PRICE, DefaultGasProvider.GAS_LIMIT, toAddress, value);
//
//                byte[] rawTxData = TransactionEncoder.signMessage(rawtx, this.credentials);
//                web3j.ethSendRawTransaction(Numeric.toHexString(rawTxData))
//                        .sendAsync().whenCompleteAsync((txHash, e2)->{
//                    if (e2 == null){
//                        String hash = txHash.getTransactionHash();
//                        long startTime = System.currentTimeMillis();
//                        boolean success = false;
//                        while( System.currentTimeMillis() - startTime < 16000) {
//                            try {
//                                EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(hash).send();
//                                if (receipt.getError() != null) {
//                                    this.handler.fail(reqId, receipt.getError().getMessage());
//                                } else if (receipt.getResult() == null) {
//                                    // we need to continue wait
//                                    try {
//                                        Thread.sleep(1000);
//                                    } catch (InterruptedException e) {
//                                    }
//                                } else {
//                                    processTransactionReceipt(reqId, receipt.getResult());
//                                    success = true;
//                                    break;
//                                }
//                            } catch (IOException e) {
//                                this.handler.fail(reqId, e.getMessage());
//                            }
//                        }
//                        if (!success){
//                            this.handler.fail(reqId, "Transaction maybe cannot be confirmed in current, " +
//                                    "you can refresh you balance to check it.");
//                        }
//                    }else{
//                        this.handler.fail(reqId, e2.getMessage());
//                    }
//                });
//            }else{
//                this.handler.fail(reqId, "Get address nonce error:" + e1.getMessage());
//            }
//        });

    }

    private void processTransactionException(String reqId, Throwable e) {
        // we need reset nonce
        resetNonce();
        logger.warn("Transaction exception:" +  reqId + ", " + e.getMessage());
        this.handler.fail(reqId, e.getMessage());
    }

    private void resetNonce() {
        transactionManager.setNonce(BigInteger.valueOf(-1));
        logger.info("RESET NONCE VALUE:" + transactionManager.getCurrentNonce());
    }

    /**
     * Transfer amount of token to a specified address
     * @param reqId request id
     * @param toAddress a target address
     * @param quantity quantity
     */
    public void transferToken(final String reqId, String toAddress, BigInteger quantity){
        logger.info("Transfer UX to:{}, {}", toAddress, quantity.toString());
        uxCoin.transfer(toAddress, quantity).sendAsync().whenCompleteAsync((receipt, e) -> {
            processReceipt(reqId, receipt, e);
        });
    }

    private void processReceipt(String reqId, TransactionReceipt receipt, Throwable e) {
        if (e == null){
            logger.info(reqId + ":" + receipt);
            processTransactionReceipt(reqId, receipt);
        }else{
            logger.info(reqId + ":" + e.getMessage());
            this.handler.fail(reqId, e.getMessage());
        }
    }

    /**
     * Transfer to multiple address using different quality from current address
     * @param reqId request id
     * @param address a set of target address
     * @param quality a set of quality need to transfer
     */
    public void transferTokens(final String reqId, List<String> address, List<BigInteger> quality){
        if (address == null || address.size() == 0 || quality == null || quality.size() == 0
                || address.size() != quality.size()) {
            throw new RuntimeException("Invalid parameter: Need a valid address and quality parameter, and the length of list must be equal.");
        }
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, len = address.size(); i < len; i++) {
                sb.append("{").append(address.get(i)).append(", ").append(quality.get(i)).append("},");
            }

            logger.info("Transfer UX to:{}", sb.toString());
        }
        mulTransfer.mulPayDiff(address, quality).sendAsync().whenCompleteAsync((receipt, e)-> {
            processReceipt(reqId, receipt, e);
        });
    }

    /**
     * Transfer to multiple address using different quality from current address
     * @param reqId request id
     * @param address a set of target address
     * @param quality a set of quality need to transfer
     */
    public void transferTokens(final String reqId, List<String> address, BigInteger quality){
        if (address == null || address.size() == 0 || quality.equals(BigInteger.ZERO)) {
            throw new RuntimeException("Invalid parameter: Need a valid address and quality parameter");
        }
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0, len = address.size(); i < len; i++) {
                sb.append(address.get(i)).append(", ");
            }
            sb.append("]").append(quality.toString());

            logger.info("Transfer UX to:{}", sb.toString());
        }
        mulTransfer.mulPaySame(quality, address).sendAsync().whenCompleteAsync((receipt, e)-> {
            if (e == null){
                processTransactionReceipt(reqId, receipt);
            }else{
                this.handler.fail(reqId, e.getMessage());
            }
        });
    }

    /**
     * query other lock contract can extract amount
     * @return
     */
    public BigInteger otherCollectedTokens() throws Exception {
        return rewardPoolTokens.collectedTokens().send();
    }

    /**
     * query team can extract amount
     * @return
     * @throws Exception
     */
    public BigInteger teamCollectedTokens() throws Exception {
        return teamTokensHolder.collectedTokens().send();
    }

    private void processTransactionReceipt(String reqId, TransactionReceipt transactionReceipt) {
        logger.info("receive a receipt:" + reqId + ", " + transactionReceipt.toString());
        if (transactionReceipt.isStatusOK()) {
            if (this.handler != null){
                this.handler.success(reqId, transactionReceipt.getTransactionHash());
            }
        }else{
            if (this.handler != null){
                this.handler.fail(reqId,
                        "Unknown exception, the receipt has received:"
                                + transactionReceipt.getTransactionHash());
            }
        }
    }

    public void extractTeamContract(String reqId) {
        teamTokensHolder.unLock().sendAsync().whenCompleteAsync((receipt, e)-> {
            if (e == null){
                processTransactionReceipt(reqId, receipt);
            }else{
                this.handler.fail(reqId, e.getMessage());
            }
        });
    }

    public void extractOtherContract(String reqId) {
        rewardPoolTokens.unLock().sendAsync().whenCompleteAsync((receipt, e)-> {
            if (e == null){
                processTransactionReceipt(reqId, receipt);
            }else{
                this.handler.fail(reqId, e.getMessage());
            }
        });
    }
}
