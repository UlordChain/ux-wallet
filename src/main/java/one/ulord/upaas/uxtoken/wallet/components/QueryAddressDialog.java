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

/**
 * @author haibo
 * @since 7/21/18
 */
public class QueryAddressDialog extends Dialog<ButtonType> {
    TextField targetAddr;

    public QueryAddressDialog(String title, String message){
        setTitle(title);
        setHeaderText(message);

        GridPane gridPane = new GridPane();
        Text targetAddrPrompt = new Text("地址：");
        gridPane.add(targetAddrPrompt, 0, 0);
        targetAddr = new TextField("0x");
        gridPane.add(targetAddr, 1, 0);


        getDialogPane().setContent(gridPane);

        Platform.runLater(() -> targetAddr.requestFocus());

        ButtonType passwordButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(passwordButtonType);
    }

    public String getTargetAddress(){
        return targetAddr.getText();
    }
}
