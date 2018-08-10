/**
 * Copyright(c) 2018
 * Ulord core developers
 */
package one.ulord.upaas.uxtoken.wallet.components;

import one.ulord.upaas.uxtoken.wallet.Application;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author haibo
 * @since 7/21/18
 */
public class AddressBalanceDialog extends Dialog<ButtonBar.ButtonData> {
    public AddressBalanceDialog(String address, String ethBalance, String uxBalance){
        setTitle("余额");
        setHeaderText("余额查询结果:");

        ImageView logo = new ImageView(new Image(Application.class.getClassLoader()
                .getResourceAsStream("graphics/info.png")));
        logo.setFitHeight(30.0);
        logo.setFitWidth(30.0);
        getDialogPane().setGraphic(logo);

        ButtonType passwordButtonType = new ButtonType("确认", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(passwordButtonType);

        GridPane gridPane = new GridPane();
        Text targetAddrPrompt = new Text("地址：");
        gridPane.add(targetAddrPrompt, 0, 0);
        Text addressText = new Text(address);
        gridPane.add(addressText, 1, 0, 2, 1);

        Text ethBalancePrompt = new Text("以太余额:");
        gridPane.add(ethBalancePrompt, 0, 1);
        Text ethBalanceText = new Text(ethBalance);
        ethBalanceText.setFill(Color.web("#2a61e6"));
        gridPane.add(ethBalanceText, 1, 1);
        Text ethBalanceUnit = new Text("SUT");
        gridPane.add(ethBalanceUnit, 2, 1);

        Text uxBalancePrompt = new Text("UX余额:");
        gridPane.add(uxBalancePrompt, 0, 2);
        Text uxBalanceText = new Text(uxBalance);
        uxBalanceText.setFill(Color.web("#2a61e6"));
        gridPane.add(uxBalanceText, 1, 2);
        Text uxBalanceUnit = new Text("UX");
        gridPane.add(uxBalanceUnit, 2, 2);


        getDialogPane().setContent(gridPane);
    }
}
