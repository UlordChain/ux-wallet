/**
 * Copyright(c) 2018
 * Ulord core developers
 */
package one.ulord.upaas.uxtoken.wallet;

import one.ulord.upaas.uxtoken.wallet.address.AddressTarget;
import one.ulord.upaas.uxtoken.wallet.address.ImportExcel;
import one.ulord.upaas.uxtoken.wallet.components.*;
import one.ulord.upaas.uxtoken.wallet.contract.TransactionActionHandler;
import one.ulord.upaas.uxtoken.wallet.contract.UXWallet;
import one.ulord.upaas.uxtoken.wallet.utils.GlobalConfig;
import javafx.application.Platform;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.web3j.crypto.CipherException;

import javafx.scene.control.Dialog;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * UX wallet
 * @author haibo
 * @since 7/10/18
 */
public class Application extends javafx.application.Application implements TransactionActionHandler {


    private final String actionEtherTransfer = "transferEther";
    private final String actionTokenTransfer = "transferToken";
    private final String actionDistributeToken = "distributeToken";
    private final String actionUnlockTeamContract = "unlockTeamContract";
    private final String actionUnlockOtherContract = "unlockOtherContract";

    private final String accountPrompt = "账户";

    public BigDecimal V10POW18 = new BigDecimal(10).pow(18);

    UXWallet uxWallet = null;
    private Stage primaryStage;

    Text accountAddress;
    Text ethValue;
    Text uxValue;
    Text candyBalance;

    private String web3jProvider;
    private long contractStartTime;
    private final static String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    TableView<AddressTarget> addressTable ;
    List<AddressTarget> addressTargets = new ArrayList<>();

    private String ushareTokenAddress;
    private String dBControlAddress;
    private String centerPublishAddress;
    private String candyUXAddress;
    private String teamDevTokenAddress;
    private String rewardPoolTokensAddress;
    private String paymentAddress;
    private String authModuleAddress;

    private String teamExAddress;
    private String fundationExAddress;
    private String operateExAddress;
    private String rewardPoolExAddress;
    private String clamiPoolExAddress;

    private long teamDuration = 6 * 30 * 24 * 3600; //six months

    private BigInteger totalSupply = new BigInteger("10000000000000000000000000000");
    private BigInteger teamSupply = totalSupply.multiply(BigInteger.valueOf(100)).divide(BigInteger.valueOf(5));
//    private BigInteger fundationSupply = totalSupply.multiply(BigInteger.valueOf(100)).divide(BigInteger.valueOf(15));
//    private BigInteger operationSupply = totalSupply.multiply(BigInteger.valueOf(100)).divide(BigInteger.valueOf(25));
    private BigInteger rewardPoolSupply = totalSupply.multiply(BigInteger.valueOf(100)).divide(BigInteger.valueOf(55));


    ProgressBar pbTeamExtract;
    ProgressBar pbOtherExtract;
    Text teamContractBalance;
    Text teamAddressBalance;
    Text otherContractBalance;
    Text funcationAddressBalance;
    Text rewardAddressBalance;
    Text operateAddressBalance;


    public Application(){
        GlobalConfig.loadConfigure("boot.properties");
        if (GlobalConfig.get("profiles.active") != null){
            GlobalConfig.loadConfigure("config-" + GlobalConfig.get("profiles.active") + ".properties");
        }else{
            GlobalConfig.loadConfigure("config-test.properties");
        }
        web3jProvider = GlobalConfig.get("usc.rpc");

        ushareTokenAddress = GlobalConfig.get("UshareTokenAddress");
        centerPublishAddress = GlobalConfig.get("CenterPublishAddress");
        candyUXAddress = GlobalConfig.get("CandyUXAddress");
        teamDevTokenAddress = GlobalConfig.get("TeamDevTokenAddress");
        rewardPoolTokensAddress = GlobalConfig.get("RewardPoolTokensAddress");

        teamExAddress = GlobalConfig.get("teamExAddress");
        fundationExAddress = GlobalConfig.get("foundationExAddress");
        operateExAddress = GlobalConfig.get("operateExAddress");
        rewardPoolExAddress = GlobalConfig.get("rewardPoolExAddress");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane border = new BorderPane();
        border.setTop(topAnchorPane());
        border.setLeft(leftGridPane());
        border.setCenter(centerTable());
        border.setBottom(bottomAnchorPane());

        Scene scene = new Scene(border, 1024, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("UShare(UX) Wallet");
        primaryStage.show();

        primaryStage.getIcons().add(new Image(Application.class
                .getClassLoader().getResourceAsStream("graphics/ushare-logo.png")));

        this.primaryStage = primaryStage;
    }

    /**
     * 读Excel文件
     * @param
     */
    public void readExcelFile(File file) {
        ImportExcel importExcel = new ImportExcel();
        try {
            final List<AddressTarget> list = importExcel.getAllByExcel(file);
            addressTable.getItems().clear();
            addressTable.getItems().addAll(list);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * TOP
     * @return
     */
    private AnchorPane topAnchorPane() {

        AnchorPane anchor = new AnchorPane();
        GridPane pane = new GridPane();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开秘钥文件");
        Stage openFile = new Stage();

        pane.setPadding(new Insets(11.5, 12.5, 13.5, 14.5));
        pane.setHgap(5.5);
        pane.setVgap(5.5);
        accountAddress = new Text(accountPrompt + " : 0x0000000000000000000000000000000000000000");
        pane.add(accountAddress, 0, 0, 4, 1);
        ethValue = new Text("0.00");
        ethValue.setFill(Color.web("#2a61e6"));
        ethValue.setCursor(Cursor.HAND);
        Tooltip t = new Tooltip("点击转以太币");
        Tooltip.install(ethValue, t);
        ethValue.setOnMouseClicked(event -> {
            // 转账提示
            TransferDialog dialog = new TransferDialog("以太转账", "请输入转账目标地址和数量:");
            dialog.showAndWait().ifPresent(response->{
                if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE){
                    BigDecimal ether = dialog.getTargetValue();
                    BigInteger wei = ether.multiply(BigDecimal.valueOf(10).pow(18)).toBigInteger();
                    uxWallet.transferEther(actionEtherTransfer + ":"
                            + dialog.getTargetAddress() + ":" + dialog.getTargetValue(),
                            dialog.getTargetAddress(), wei);
                }
            });

        });
        pane.add(ethValue, 0, 1);
        Text ethUnit = new Text("SUT");
        pane.add(ethUnit, 1, 1);
        uxValue = new Text("0.00");
        uxValue.setFill(Color.web("#2a61e6"));
        uxValue.setCursor(Cursor.HAND);
        Tooltip uxTooltip = new Tooltip("点击转UX");
        Tooltip.install(uxValue, uxTooltip);
        uxValue.setOnMouseClicked(event -> {
            // 转账提示
            TransferDialog dialog = new TransferDialog("UX转账", "请输入转账目标地址和数量:");
            dialog.showAndWait().ifPresent(response->{
                if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE){
                    BigDecimal ether = dialog.getTargetValue();
                    BigInteger wei = ether.multiply(BigDecimal.valueOf(10).pow(18)).toBigInteger();
                    uxWallet.transferToken(actionTokenTransfer + ":"
                                    + dialog.getTargetAddress() + ":" + dialog.getTargetValue(),
                            dialog.getTargetAddress(), wei);
                }
            });

        });
        pane.add(uxValue, 2, 1);
        Text uxUnit = new Text("UX");
        pane.add(uxUnit, 3, 1);

        Text candyBox = new Text("糖果盒子");
        pane.add(candyBox, 1, 2);
        candyBalance = new Text();
        candyBalance.setFill(Color.web("#2a61e6"));
        pane.add(candyBalance, 2, 2);
        Text candyUnit = new Text("UX");
        pane.add(candyUnit, 3, 2);


        /**
         * 文件选择框按钮
         */
        ImageView lockImage = new ImageView(new Image(Application.class
                .getClassLoader().getResourceAsStream("graphics/lock.png")));
        lockImage.setFitHeight(15.0);
        lockImage.setFitWidth(15.0);
        Button openButton = new Button("", lockImage);
        Tooltip loadTooltip = new Tooltip("加载私钥文件");
        Tooltip.install(openButton, loadTooltip);

        pane.add(openButton, 4, 0);
        openButton.setOnAction(
                (final ActionEvent e) -> {
                    File file = fileChooser.showOpenDialog(openFile);
                    if (file != null) {
                        loadKeystoreFile(file);
                    }
                });
        // Refresh Button
        ImageView refreshImage = new ImageView(new Image(Application.class
                .getClassLoader().getResourceAsStream("graphics/refresh.png")));
        refreshImage.setFitHeight(15.0);
        refreshImage.setFitWidth(15.0);
        Button refreshButton = new Button("", refreshImage);
        Tooltip refreshTooltip = new Tooltip("点击刷新账号余额");
        Tooltip.install(refreshButton, refreshTooltip);
        pane.add(refreshButton, 4, 1);
        refreshButton.setOnAction(
                (final ActionEvent e) -> {
                    refreshBalance();
                });

        // Pull to candy Button
        ImageView candyImage = new ImageView(new Image(Application.class
                .getClassLoader().getResourceAsStream("graphics/candy.png")));
        candyImage.setFitHeight(15.0);
        candyImage.setFitWidth(15.0);
        Button candyButton = new Button("", candyImage);
        Tooltip candyTooltip = new Tooltip("点击提取到糖果盒子");
        Tooltip.install(candyButton, candyTooltip);
        pane.add(candyButton, 4, 2);
        candyButton.setOnAction(
                (final ActionEvent e) -> {
                    pushToCandyBox();
                });

        /**
         * LOGO
         */
        InputStream is = Application.class.getClassLoader().getResourceAsStream("graphics/ushare.png");
        ImageView logoView = new ImageView(new Image(is));
        logoView.setFitHeight(80);
        logoView.setFitWidth(234);

        anchor.getChildren().addAll(logoView, pane);
        AnchorPane.setLeftAnchor(logoView, 5.0);
        AnchorPane.setRightAnchor(pane, 5.0);
        return anchor;
    }

    private void pushToCandyBox() {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setContentText("请输入转移到糖果盒子的UX数量:");
        String amount = dialog.showAndWait().get();
        if (amount != null){
            BigDecimal value = new BigDecimal(amount);
            if (!BigDecimal.ZERO.equals(value)){
                uxWallet.transferToken(actionTokenTransfer + ":" + uxWallet.getCandyAddress() + ":" + value,
                        uxWallet.getCandyAddress(),
                        value.multiply(V10POW18).toBigInteger());
            }
        }
    }

    private void refreshBalance() {
        try {
            this.accountAddress.setText(accountPrompt + " : " + uxWallet.getMainAddress());
            this.ethValue.setText(uxWallet.getEthBalance().toString());
            this.uxValue.setText(uxWallet.getTokenBalance().toString());
            this.candyBalance.setText(uxWallet.getTokenBalance(uxWallet.getCandyAddress()).toString());

            pbTeamExtract.setProgress(uxWallet.teamCollectedTokens()
                    .multiply(BigInteger.valueOf(100))
                    .divide(teamSupply).doubleValue()/100.0);
            pbOtherExtract.setProgress(uxWallet.otherCollectedTokens()
                    .multiply(BigInteger.valueOf(100))
                    .divide(rewardPoolSupply).doubleValue()/100.0);

            teamContractBalance.setText(uxWallet.getTokenBalance(uxWallet.getTeamTokensHolderAddress()).toString());
            otherContractBalance.setText(uxWallet.getTokenBalance(uxWallet.getRewardPoolAddress()).toString());

            teamAddressBalance.setText(uxWallet.getTokenBalance(teamExAddress).toString());
            funcationAddressBalance.setText(uxWallet.getTokenBalance(fundationExAddress).toString());
            operateAddressBalance.setText(uxWallet.getTokenBalance(operateExAddress).toString());
            rewardAddressBalance.setText(uxWallet.getTokenBalance(rewardPoolExAddress).toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showBalance(String address) {
        try {
            Dialog dialog;
            if (uxWallet == null){
                dialog = new PromptDialog(PromptDialog.PromptType.INFO, "余额查询", "请先导入私钥再使用", null);
            }else{
                dialog = new AddressBalanceDialog(address ,
                                uxWallet.getEthBalance(address).toString() ,
                                uxWallet.getTokenBalance(address).toString());
            }

            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 加载私钥，并提示用户输入密码
     * @param file 用户打开的文件
     */
    private void loadKeystoreFile(File file) {
        PasswordDialog dialog = new PasswordDialog();
        String keystorePassword = dialog.showAndWait().get();
        if (keystorePassword == null){ return; }

        try {
            uxWallet = new UXWallet(web3jProvider, file, keystorePassword,
                    this.ushareTokenAddress, this.teamDevTokenAddress,
                    this.rewardPoolTokensAddress, this.candyUXAddress,
                    this);
            refreshBalance();
        } catch (IOException e) {
            showException("无法处理秘钥文件，请确认秘钥文件是否存在或可访问。");
        } catch (CipherException e) {
            showException("密码错误，请重新输入密码或者更换秘钥文件。");
        } catch (Exception e) {
            e.printStackTrace();
            showException("错误:" + e.getMessage());
        }

    }

    private void showException(String message) {
        PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.FAIL,"提示", message, null);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    /**
     * LEFT
     * @return
     */
    private GridPane leftGridPane() {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(11.5, 12.5, 13.5, 14.5));
        pane.setHgap(5.5);
        pane.setVgap(5.5);


        try {
            Label labelTeamPrompt = new Label("团队:(5%)");
            pane.add(labelTeamPrompt, 0, 0, 2, 1);
            pbTeamExtract = new ProgressBar();
            pane.add(pbTeamExtract, 0, 1);


            Label labelTeamContractPrompt = new Label("合约余额");
            pane.add(labelTeamContractPrompt, 0, 2);
            Button btnExtractTeam = new Button("提取");
            Tooltip ttExtractTeam = new Tooltip("点击提取团队合约中锁定的代币");
            Tooltip.install(btnExtractTeam, ttExtractTeam);
            btnExtractTeam.setOnAction(e->{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("提币");
                alert.setHeaderText("提取团队合约UX");
                alert.setContentText("提币需要等待合约解锁时间，且只有合约管理员才可以提币到团队地址，" +
                        "同时需要付出交易费用，请确认是否执行提币操作？");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    uxWallet.extractTeamContract(actionUnlockTeamContract);
                }
            });
            pane.add(btnExtractTeam, 1, 2);
            teamContractBalance = new Text("0.00");
            teamContractBalance.setFill(Color.web("#2a61e6"));
            pane.add(teamContractBalance, 0, 3);
            Label lblTeamContractUnit = new Label("UX");
            pane.add(lblTeamContractUnit, 1, 3);

            Label labelTeamAddressPrompt = new Label("管理地址余额");
            pane.add(labelTeamAddressPrompt, 0, 4, 2, 1);
            teamAddressBalance = new Text("0");
            teamAddressBalance.setFill(Color.web("#2a61e6"));
            pane.add(teamAddressBalance, 0, 5);
            Label lblTeamAddressUnit = new Label("UX");
            pane.add(lblTeamAddressUnit, 1, 5);





            Label labelRewardPrompt = new Label("奖励池:(55%)");
            labelRewardPrompt.setPadding(new Insets(25,0,0,0));
            pane.add(labelRewardPrompt, 0, 6, 2, 1);
            pbOtherExtract = new ProgressBar();
            pane.add(pbOtherExtract, 0, 7);


            Label labelOtherContractPrompt = new Label("合约余额");
            pane.add(labelOtherContractPrompt, 0, 8);
            Button btnExtractOther = new Button("提取");
            Tooltip ttExtractOther = new Tooltip("点击提取其他合约中锁定的代币");
            Tooltip.install(btnExtractOther, ttExtractOther);
            btnExtractOther.setOnAction(e->{
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("提币");
                alert.setHeaderText("提取其他合约UX到挖矿，推广和生态地址");
                alert.setContentText("提币需要等待合约解锁时间，且只有合约管理员才可以提币到团队地址，" +
                        "同时需要付出交易费用，请确认是否执行提币操作？");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    uxWallet.extractTeamContract(actionUnlockOtherContract);
                }
            });
            pane.add(btnExtractOther, 1, 8);
            otherContractBalance = new Text("0");
            otherContractBalance.setFill(Color.web("#2a61e6"));
            pane.add(otherContractBalance, 0, 9);
            Label lblOtherContractUnit = new Label("UX");
            pane.add(lblOtherContractUnit, 1, 9);

            Label labelMinerPrompt = new Label("奖励池地址余额(55%)");
            pane.add(labelMinerPrompt, 0, 10, 2, 1);
            rewardAddressBalance = new Text("0");
            rewardAddressBalance.setFill(Color.web("#2a61e6"));
            pane.add(rewardAddressBalance, 0, 11);
            Label lblMinerAddressUnit = new Label("UX");
            pane.add(lblMinerAddressUnit, 1, 11);



            Label labelOtherPrompt = new Label("其他:(40%)");
            labelOtherPrompt.setPadding(new Insets(25,0,0,0));
            pane.add(labelOtherPrompt, 0, 12, 2, 1);

            Label labelOperatePrompt = new Label("推广地址余额(25%)");
            pane.add(labelOperatePrompt, 0, 13, 2, 1);
            operateAddressBalance = new Text("0");
            operateAddressBalance.setFill(Color.web("#2a61e6"));
            pane.add(operateAddressBalance, 0, 14);
            Label operateAddressUnit = new Label("UX");
            pane.add(operateAddressUnit, 1, 14);

            Label labelCommunityPrompt = new Label("基金会余额(15%)");
            pane.add(labelCommunityPrompt, 0, 15, 2, 1);
            funcationAddressBalance = new Text("0");
            funcationAddressBalance.setFill(Color.web("#2a61e6"));
            pane.add(funcationAddressBalance, 0, 16);
            Label communityAddressUnit = new Label("UX");
            pane.add(communityAddressUnit, 1, 16);

        } catch (Exception e) {
            e.printStackTrace();
        }




        return pane;
    }

    /**
     * 在CENTER创建一个空表格
     */
    private VBox centerTable() {

        TableColumn<AddressTarget, String> addressCol = new TableColumn<>("地址");
        addressCol.setMinWidth(360);
        addressCol.setCellValueFactory(
                new PropertyValueFactory<AddressTarget, String>("address"));

        TableColumn<AddressTarget, String> amountCol = new TableColumn<AddressTarget, String>("数量");
        amountCol.setMinWidth(160);
        amountCol.setCellValueFactory(
                new PropertyValueFactory<>("amount"));

        TableColumn<AddressTarget, String> submitDateCol = new TableColumn<AddressTarget, String>("提交时间");
        submitDateCol.setMinWidth(200);
        submitDateCol.setCellValueFactory(
                new PropertyValueFactory<AddressTarget, String>("submitDate"));

        TableColumn<AddressTarget, String> tradeDateCol = new TableColumn<AddressTarget, String>("交易结果");
        tradeDateCol.setMinWidth(100);
        tradeDateCol.setCellValueFactory(
                new PropertyValueFactory<AddressTarget, String>("tradeResult"));

        addressTable = new TableView<>();
        addressTable.setPlaceholder(new Text("请导入或添加需要发币的地址"));
        addressTable.getColumns().addAll(addressCol, amountCol, submitDateCol, tradeDateCol);

        ModifiableObservableListBase<AddressTarget> observableList = new ModifiableObservableListBase<AddressTarget>() {
            @Override
            public int size() {
                return addressTargets.size();
            }

            @Override
            protected void doAdd(int index, AddressTarget element) {
                addressTargets.add(index, element);
            }

            @Override
            protected AddressTarget doSet(int index, AddressTarget element) {
                return addressTargets.set(index, element);
            }

            @Override
            protected AddressTarget doRemove(int index) {
                return addressTargets.remove(index);
            }

            @Override
            public AddressTarget get(int index) {
                return addressTargets.get(index);
            }
        };

        addressTable.setItems(observableList);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.getChildren().addAll(addressTable);
        VBox.setVgrow(addressTable, Priority.ALWAYS);

        addressTable.getSelectionModel().setCellSelectionEnabled(true);
        addressTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        return vbox;
    }

    /**
     * 在bottom添加“导入”、“发币”按钮
     * @return
     */
    private AnchorPane bottomAnchorPane() {

        AnchorPane anchorpane = new AnchorPane();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择Excel文件");
        Stage selectFile = new Stage();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("XLS", "*.xls"), new FileChooser.ExtensionFilter("XLSX", "*.xlsx"));

        Button buttonQuery = new Button("查询");
        buttonQuery.setOnAction(
                (final ActionEvent e) -> {
                    QueryAddressDialog dialog = new QueryAddressDialog("查询地址余额", "请输入地址查询以太和UX余额");
                    dialog.showAndWait();
                    if (dialog.getTargetAddress() != null && dialog.getTargetAddress().length() == 42){
                        showBalance(dialog.getTargetAddress());
                    }
                });

        Button buttonAdd = new Button("添加");
        buttonAdd.setOnAction(
                (final ActionEvent e) -> {
                    TransferDialog dialog = new TransferDialog("添加地址", "请输入地址和转移的UX数量");
                    dialog.showAndWait();
                    if (dialog.getTargetAddress() != null && dialog.getTargetAddress().length() == 42){
//                        addressTargets.a
                        addressTable.getItems().add(new AddressTarget(dialog.getTargetAddress(), dialog.getTargetValue().toString()));
                    }
                });

        Button buttonInput = new Button("导入");
        buttonInput.setOnAction(
                (final ActionEvent e) -> {
                    File file = fileChooser.showOpenDialog(selectFile);
                    if (file != null) {
                        readExcelFile(file);
                    }
                });

        Button buttonPay = new Button("发币");
        buttonPay.setOnAction((e)->{
            distributeToken(addressTable.getItems());
        });

        HBox hb = new HBox();
        hb.setPadding(new Insets(0, 10, 10, 10));
        hb.setSpacing(10);
        hb.getChildren().addAll(buttonQuery, buttonAdd, buttonInput, buttonPay);

        anchorpane.getChildren().add(hb);
        AnchorPane.setBottomAnchor(hb, 8.0);
        AnchorPane.setRightAnchor(hb, 5.0);
        return anchorpane;
    }

    /**
     * Distribute token to multi-address
     * @param items
     */
    private void distributeToken(ObservableList<AddressTarget> items) {
        // check amount information
        String amount = null;
        boolean allEqual = true;
        BigDecimal candyBoxBalance = null;
        try {
            BigDecimal balance = uxWallet.getTokenBalance(uxWallet.getCandyAddress());
            candyBoxBalance = balance.multiply(V10POW18);
        } catch (Exception e) {
            PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提示",
                    "无法获得账户信息,请确认网络链接是否正常.", null);
            dialog.showAndWait();
            return;
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for(AddressTarget item : items){
            // get target account
            if (amount != null){
                if (allEqual && !amount.equals(item.getAmount())){
                    allEqual = false;
                }
            }else{
                amount = item.getAmount();
            }
            //
            totalAmount.add(new BigDecimal(item.getAmount()));
        }

        if (candyBoxBalance.compareTo(totalAmount) < 0){
            PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提示",
                    "糖果盒子余额不够,同时请确认加载的私钥是糖果盒子管理员的私钥", null);
            dialog.showAndWait();
            return;
        }

        int pos = 0, len = items.size();
        while(pos < len){
            int batchSize = len - pos > 200 ? 200 : len - pos;
            if (allEqual) {
                List<String> addressList = new ArrayList<>(batchSize);

                for (int i = pos, n = pos + batchSize; i < n; i++) {
                    AddressTarget target = items.get(i);
                    addressList.add(target.getAddress());
                }
                BigDecimal value = new BigDecimal(amount);
                BigInteger amountValue = value.multiply(V10POW18).toBigInteger();
                uxWallet.transferTokens(actionDistributeToken + ":发糖果(每账号" + amount + ")" + ":[" + pos + " ~ "
                        + (pos + batchSize) + "]/" + items.size(), addressList, amountValue);
            } else {
                List<String> addressList = new ArrayList<>(batchSize);
                List<BigInteger> amountList = new ArrayList<>(batchSize);
                for (int i = pos, n = pos + batchSize; i < n; i++) {
                    AddressTarget target = items.get(i);
                    addressList.add(target.getAddress());
                    BigDecimal value = new BigDecimal(target.getAmount());
                    amountList.add(value.multiply(V10POW18).toBigInteger());
                }
                uxWallet.transferTokens(actionDistributeToken+ ":发糖果" + ":[" + pos + " ~ "
                        + (pos + batchSize) + "]/" + items.size(), addressList, amountList);
            }

            // update table
            String startTime = sdf.format(Calendar.getInstance().getTime());
            for (int i = pos, n = pos + batchSize; i < n; i++){
                addressTable.getItems().get(i).setSubmitDate(startTime);
            }

            pos += batchSize;
        }
    }

    @Override
    public void success(String id, String txhash) {
        if (id.startsWith(actionEtherTransfer)){
            // ether transfer
            String items[] = id.split(":");
            if (items.length == 3){
                String targetAddress = items[1];
                String etherValue = items[2];
                Platform.runLater(()->{
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.SUCCESS,"以太转账",
                            "以太转账成功", "目标地址:" + targetAddress + ", 数量" + etherValue);
                    dialog.showAndWait();
                });

            }else{
                // !!!
            }
        }else if(id.startsWith(actionTokenTransfer)) {
            String items[] = id.split(":");
            if (items.length == 3) {
                String targetAddress = items[1];
                String uxValue = items[2];
                Platform.runLater(() -> {
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.SUCCESS, "UX转账", "UX转账成功:",
                            "目标地址:" + targetAddress + ", 数量" + uxValue);
                    dialog.showAndWait();
                });

            } else {
                // !!!
            }
        }else if(id.startsWith(actionDistributeToken)){
            String items[] = id.split(":");
            if (items.length == 3) {
                String prompt1 = items[1];
                String prompt2 = items[2];

                // update table
                String tradeStatus[] = prompt2.split("/");
                if (tradeStatus.length == 2){
                    // [a ~ b]
                    String fromTo[] = tradeStatus[0].split("\\s~\\s");
                    if (fromTo.length == 2){
                        int from = Integer.parseInt(fromTo[0].substring(1));
                        int to = Integer.parseInt(fromTo[1].substring(0, fromTo[1].length() - 1));
                        for (int i = from; i < to; i++){
                            addressTable.getItems().get(i).setTradeResult("交易成功");
                        }
                    }
                }


                Platform.runLater(() -> {
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.SUCCESS, "UX糖果分发", prompt1,
                            "发送批次 " + prompt2 +  " 成功.");
                    dialog.show();
                });
            }else if(id.startsWith(actionUnlockTeamContract)) {
                Platform.runLater(() -> {
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提取UX", "提取团队合约UX成功", null);
                    dialog.show();
                });
            }else if(id.startsWith(actionUnlockOtherContract)){
                Platform.runLater(() -> {
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提取UX",
                            "提取锁定合约UX到挖矿，推广和生态地址成功",
                            null);
                    dialog.show();
                });

            } else {
                Platform.runLater(() -> {
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.SUCCESS,
                            "UX糖果", "UX糖果发送成功", null);
                    dialog.show();
                });
            }
        }else{
            Platform.runLater(()-> {
                PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提示", "交易返回成功信息:", txhash);
                dialog.show();
            });
        }
    }

    @Override
    public void fail(String id, String message) {
        if (id.startsWith(actionEtherTransfer)){
            // ether transfer
            String items[] = id.split(":");
            if (items.length == 3){
                String targetAddress = items[1];
                String etherValue = items[2];
                Platform.runLater(()->{
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.FAIL,"以太转账",
                            "以太转账失败", "目标地址:"
                        + targetAddress + ", 数量" + etherValue + ", 原因:" + message);
                    dialog.showAndWait();
                });
            }else{
                // !!!
            }
        }else if(id.startsWith(actionTokenTransfer)){
            String items[] = id.split(":");
            if (items.length == 3){
                String targetAddress = items[1];
                String uxValue = items[2];
                Platform.runLater(()->{
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.SUCCESS,"UX转账", "UX转账失败:",
                            "目标地址:" + targetAddress + ", 数量" + uxValue + ", 原因:" + message);
                    dialog.showAndWait();
                });

            }else{
                // !!!
            }
        }else if(id.startsWith(actionDistributeToken)) {
            String items[] = id.split(":");
            if (items.length == 3) {
                String prompt1 = items[1];
                String prompt2 = items[2];

                // update table
                String tradeStatus[] = prompt2.split("/");
                if (tradeStatus.length == 2) {
                    // [a ~ b]
                    String fromTo[] = tradeStatus[0].split("[ ]+~[ ]+");
                    if (fromTo.length == 2) {
                        int from = Integer.parseInt(fromTo[0].substring(1));
                        int to = Integer.parseInt(fromTo[1].substring(0, fromTo[1].length() - 1));
                        for (int i = from; i < to; i++) {
                            addressTable.getItems().get(i).setTradeResult("交易失败");
                        }
                    }
                }

                Platform.runLater(() -> {
                    PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "UX糖果分发失败", prompt1 + "," + prompt2,
                            message);
                    dialog.show();
                });

            } else {
                // !!!
            }
        }else if(id.startsWith(actionUnlockTeamContract)) {
            Platform.runLater(() -> {
                PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提取UX", "提取团队合约UX失败",
                        message);
                dialog.show();
            });
        }else if(id.startsWith(actionUnlockOtherContract)){
            Platform.runLater(() -> {
                PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提取UX",
                        "提取锁定合约UX到挖矿，推广和生态地址失败",
                        message);
                dialog.show();
            });
        }else{
            Platform.runLater(()-> {
                PromptDialog dialog = new PromptDialog(PromptDialog.PromptType.INFO, "提示", "交易失败, 信息:", message);
                dialog.showAndWait();
            });
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
