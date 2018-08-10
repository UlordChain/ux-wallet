/**
 * Copyright(c) 2018
 * Ulord core developers
 */
package one.ulord.upaas.uxtoken.wallet.components;

import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.math.BigDecimal;

/**
 * @author haibo
 * @since 7/21/18
 */
public class TransferDialog extends Dialog<ButtonType> {
    TextField targetAddr;
    TextField valueAddr;

    public TransferDialog(String title, String message){
        setTitle(title);
        setHeaderText(message);

        ButtonType passwordButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(passwordButtonType, cancelType);

        GridPane gridPane = new GridPane();
        Text targetAddrPrompt = new Text("目标地址：");
        gridPane.add(targetAddrPrompt, 0, 0);
        targetAddr = new TextField("0x");
        gridPane.add(targetAddr, 1, 0);
        Text valueAddrPrompt = new Text("数量：");
        gridPane.add(valueAddrPrompt, 0, 1);
        valueAddr = new TextField("0.0");
        gridPane.add(valueAddr, 1, 1);

        getDialogPane().setContent(gridPane);

        Platform.runLater(() -> targetAddr.requestFocus());
    }

    public String getTargetAddress(){
        return targetAddr.getText();
    }

    public BigDecimal getTargetValue(){
        return new BigDecimal(valueAddr.getText());
    }
}
